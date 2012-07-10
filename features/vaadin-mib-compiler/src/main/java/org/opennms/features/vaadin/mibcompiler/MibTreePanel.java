/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.mibcompiler;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.vaadin.mibcompiler.api.Logger;
import org.opennms.features.vaadin.mibcompiler.api.MibParser;
import org.opennms.features.vaadin.mibcompiler.services.MibbleMibParser;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.TextFileProperty;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

/**
 * The Class MIB Tree Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibTreePanel extends Panel {

    /** The Constant PENDING. */
    private static final String PENDING = "pending";

    /** The Constant COMPILED. */
    private static final String COMPILED = "compiled";

    /** The Constant MIBS_ROOT_DIR. */
    private static final File MIBS_ROOT_DIR = new File(ConfigFileConstants.getHome(),  "/share/mibs"); // FIXME Must be configurable

    /** The Constant MIBS_COMPILED_DIR. */
    private static final File MIBS_COMPILED_DIR = new File(MIBS_ROOT_DIR, COMPILED);

    /** The Constant MIBS_PENDING_DIR. */
    private static final File MIBS_PENDING_DIR = new File(MIBS_ROOT_DIR, PENDING);

    /** The Constant ACTION_EDIT. */
    private static final Action ACTION_EDIT = new Action("Edit MIB");

    /** The Constant ACTION_COMPILE. */
    private static final Action ACTION_COMPILE = new Action("Compile MIB");

    /** The Constant ACTION_EVENTS. */
    private static final Action ACTION_EVENTS = new Action("Generate Events");

    /** The Constant ACTION_COLLECT. */
    private static final Action ACTION_COLLECT = new Action("Generate Data Collection");

    /** The MIBs tree. */
    private final Tree mibsTree;

    /** The MIBs container. */
    private final HierarchicalContainer mibsContainer;

    /** The MIB parser. */
    private final MibParser mibParser;

    /**
     * Instantiates a new compile panel.
     *
     * @param logger the logger
     */
    public MibTreePanel(final Logger logger) {
        super("MIB Compiler");

        // Parser Configuration

        mibParser = new MibbleMibParser();
        mibParser.addMibDirectory(MIBS_COMPILED_DIR);

        // Initialize Toolbar

        MibUploadButton upload = new MibUploadButton(MIBS_PENDING_DIR, MIBS_COMPILED_DIR, logger) {
            public void uploadHandler(String filename) {
                addTreeItem(filename, PENDING);
            }
        };
        addComponent(upload);

        // Initialize MIB Tree

        mibsContainer = new HierarchicalContainer();
        mibsTree = new Tree("MIB Tree");
        initMibTree(logger);
        addComponent(mibsTree);

        // Panel Setup

        setSizeFull();
        addStyleName(Runo.PANEL_LIGHT);
        ((VerticalLayout) getContent()).setComponentAlignment(upload, Alignment.TOP_RIGHT);
        ((VerticalLayout) getContent()).setExpandRatio(mibsTree, 1);
    }

    /**
     * Initialize the MIB tree.
     *
     * @param logger the logger
     */
    private void initMibTree(final Logger logger) {
        if (! MIBS_COMPILED_DIR.exists()) {
            MIBS_COMPILED_DIR.mkdirs();
        }
        if (! MIBS_PENDING_DIR.exists()) {
            MIBS_PENDING_DIR.mkdirs();
        }

        File[] folders = new File[] { MIBS_COMPILED_DIR, MIBS_PENDING_DIR };
        for (File folder : folders) {
            addTreeItem(folder.getName(), null);
        }
        for (File folder : folders) {
            String[] files = folder.list();
            for (String file : files) {
                addTreeItem(file, folder.getName());
            }
        }

        mibsTree.setContainerDataSource(mibsContainer);
        mibsTree.expandItemsRecursively(COMPILED);
        mibsTree.expandItemsRecursively(PENDING);

        mibsTree.addActionHandler(new Action.Handler() {

            public Action[] getActions(Object target, Object sender) {
                if (target == null) {
                    return new Action[] {};
                }
                Object parent = mibsContainer.getParent(target);
                if (parent == null) {
                    return new Action[] {};
                }
                if (parent.equals(COMPILED)) {
                    LogUtils.debugf(this, "Adding actions for COMPILED MIB %s", target);
                    return new Action[] { ACTION_EVENTS, ACTION_COLLECT };
                } else {
                    LogUtils.debugf(this, "Adding actions for PENDING MIB %s", target);
                    return new Action[] { ACTION_EDIT, ACTION_COMPILE };
                }
            }

            public void handleAction(Action action, Object sender, Object target) {
                String fileName = (String) target;
                if (action == ACTION_EDIT) {
                    Window w = new MibEditWindow(new TextFileProperty(new File(MIBS_PENDING_DIR, fileName)));
                    getApplication().getMainWindow().addWindow(w);
                }
                if (action == ACTION_COMPILE) {
                    if (parseMib(logger, new File(MIBS_PENDING_DIR, fileName))) {
                        mibsTree.removeItem(target);
                        addTreeItem(fileName, COMPILED);
                        File file = new File(MIBS_PENDING_DIR, fileName);
                        file.renameTo(new File(MIBS_COMPILED_DIR, file.getName()));
                    }
                }
                if (action == ACTION_EVENTS) {
                    generateEvents(logger, fileName);
                }
                if (action == ACTION_COLLECT) {
                    generateDataCollection(logger, fileName);
                }
            }
        });
    }

    /**
     * Adds the tree item.
     *
     * @param label the label
     * @param parent the parent
     */
    private void addTreeItem(final String label, final String parent) {
        mibsContainer.addItem(label);
        if (parent == null) {
            LogUtils.debugf(this, "Adding root directory %s", label);
            mibsContainer.setChildrenAllowed(parent, true);
        } else {
            LogUtils.debugf(this, "Adding item %s to %s folder", label, parent);
            mibsContainer.setParent(label, parent);
            mibsContainer.setChildrenAllowed(label, false);
        }
    }

    /**
     * Parses the MIB.
     *
     * @param logger the logger
     * @param mibFile the MIB file
     * @return true, if successful
     */
    private boolean parseMib(final Logger logger, final File mibFile) {
        logger.info("Parsing MIB file " + mibFile);
        if (mibParser.parseMib(mibFile)) {
            logger.info("MIB parsed successfuly.");
            return true;
        } else {
            List<String> dependencies = mibParser.getMissingDependencies();
            if (dependencies.isEmpty()) {
                logger.error("Found problem when compiling the MIB: <pre>" + mibParser.getFormattedErrors() + "</pre>");
            } else {
                logger.error("Dependencies required: <b>" + dependencies + "</b>");
            }
        }
        return false;
    }
    
    /**
     * Gets the events.
     *
     * @return the events
     */
    private Events getEvents() {
        return mibParser.getEvents("uei.example/traps"); // FIXME must show a modal event to request the base UEI
    }

    /**
     * Generate events.
     *
     * @param logger the logger
     * @param fileName the file name
     */
    private void generateEvents(final Logger logger, final String fileName) {
        if (parseMib(logger, new File(MIBS_COMPILED_DIR, fileName))) {
            final Events events = getEvents();
            logger.info("Found " + events.getEventCount() + " events.");
            final Window w = new Window(fileName);
            w.setScrollable(true);
            w.setClosable(false);
            w.setDraggable(false);
            w.addStyleName(Runo.WINDOW_DIALOG);
            w.setSizeFull();
            w.setContent(new EventPanel(events) {
                void cancelProcessing() {
                    getApplication().getMainWindow().removeWindow(w);
                }
                void generateEventFile() { // FIXME This is not elegant.
                    StringWriter writer = new StringWriter();
                    try {
                        events.marshal(writer);
                        logger.info("<pre>" + writer.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</pre>");
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                    getApplication().getMainWindow().removeWindow(w);
                }
            });
            getApplication().getMainWindow().addWindow(w);
        }
    }

    /**
     * Generate data collection.
     *
     * @param logger the logger
     * @param fileName the file name
     */
    private void generateDataCollection(final Logger logger, final String fileName) {
        getApplication().getMainWindow().showNotification("Not yet but comming soon!");
    }

}
