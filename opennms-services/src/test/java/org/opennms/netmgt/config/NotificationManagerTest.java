//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jul 02: Get rid of DataSource stuff since it is now in our superclass.
//              Use DaoTestConfigBean.  Add some comments. - dj@opennms.org
// 2007 Jul 03: Fix test resource calls. - dj@opennms.org
// 2007 Jul 03: Enable testIplikeAllStars. - dj@opennms.org
// 2007 Jul 03: Move notifd configuration to a resource. - dj@opennms.org
// 2007 Jun 29: Add additional tests for nodes without interfaces and interfaces
//              without services.  Reset FilterDaoFactory on setup. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.IOException;

import javax.sql.DataSource;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.notifd.mock.MockNotifdConfigManager;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    JUnitSnmpAgentExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitTemporaryDatabase()
public class NotificationManagerTest {
	@Autowired
	private DataSource m_dataSource;

    private NotificationManagerImpl m_notificationManager;
    private NotifdConfigManager m_configManager;

    @Before
    public void setUp() throws Exception {
    	// Make sure we get a new FilterDaoFactory every time because our
        // DataSource changes every test.
    	FilterDaoFactory.setInstance(null);
    	FilterDaoFactory.getInstance();
    	
        m_configManager = new MockNotifdConfigManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifd-configuration.xml"));
        m_notificationManager = new NotificationManagerImpl(m_configManager, m_dataSource);
    }
    /*
    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        super.onSetUpInTransactionIfEnabled();

         // Make sure we get a new FilterDaoFactory every time because our
         // DataSource changes every test.
        FilterDaoFactory.setInstance(null);
        FilterDaoFactory.getInstance();
        
        m_configManager = new MockNotifdConfigManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifd-configuration.xml"));
        m_notificationManager = new NotificationManagerImpl(m_configManager, getDataSource());
        
        JdbcTemplate template = getJdbcTemplate();
        assertNotNull("getJdbcTemplate() should not return null", template);
        assertNotNull("getJdbcTemplate().getJdbcOperations() should not return null", getSimpleJdbcTemplate().getJdbcOperations());

        template.update("INSERT INTO service ( serviceId, serviceName ) VALUES ( 1, 'HTTP' )");

        template.update("INSERT INTO node ( nodeId, nodeCreateTime, nodeLabel ) VALUES ( 1, now(), 'node 1' )");
        template.update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 1, '192.168.1.1' )");
        template.update("INSERT INTO ifServices ( nodeId, ipAddr, serviceId ) VALUES ( 1, '192.168.1.1', 1 )");

        template.update("INSERT INTO node ( nodeId, nodeCreateTime, nodeLabel ) VALUES ( 2, now(), 'node 2' )");
        template.update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 2, '192.168.1.1' )");
        template.update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 2, '0.0.0.0' )");
        template.update("INSERT INTO ifServices ( nodeId, ipAddr, serviceId ) VALUES ( 2, '192.168.1.1', 1 )");

        template.update("INSERT INTO node ( nodeId, nodeCreateTime, nodeLabel ) VALUES ( 3, now(), 'node 3' )");
        template.update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 3, '192.168.1.2' )");
        template.update("INSERT INTO ifServices ( nodeId, ipAddr, serviceId ) VALUES ( 3, '192.168.1.2', 1 )");

        // Node 4 has an interface, but no services
        template.update("INSERT INTO node ( nodeId, nodeCreateTime, nodeLabel ) VALUES ( 4, now(), 'node 4' )");
        template.update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 4, '192.168.1.3' )");

        // Node 5 has no interfaces
        template.update("INSERT INTO node ( nodeId, nodeCreateTime, nodeLabel ) VALUES ( 5, now(), 'node 5' )");

        template.update("INSERT INTO categories ( categoryId, categoryName ) VALUES ( 1, 'CategoryOne' )");
        template.update("INSERT INTO categories ( categoryId, categoryName ) VALUES ( 2, 'CategoryTwo' )");
        template.update("INSERT INTO categories ( categoryId, categoryName ) VALUES ( 3, 'CategoryThree' )");
        template.update("INSERT INTO categories ( categoryId, categoryName ) VALUES ( 4, 'CategoryFour' )");

        template.update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 1, 1 )");
        template.update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 2, 1 )");
        template.update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 3, 1 )");

        template.update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 1, 2 )");
        template.update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 2, 2 )");
        // Not a member of the third category, but is a member of the fourth
        template.update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 4, 2 )");

        setComplete();
        endTransaction();
        startNewTransaction();
    }
*/
    
    @Test
    public void testNoElement() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           0, null, null,
                                           "(ipaddr IPLIKE *.*.*.*)",
                                           true);
    }
    
    /**
     * This should match because even though the node is not set in the event,
     * the IP address is in the database on *some* node.
     */
    public void testNoNodeIdWithIpAddr() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           0, "192.168.1.1", null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    /**
     * Trapd sends events like this (with no nodeId set but an interface set)
     * when it gets a trap from a device with an IP that isn't in the
     * database.  This shouldn't send an event.
     */
    public void testNoNodeIdWithIpAddrNotInDb() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           0, "192.168.1.2", null,
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }

    /**
     * This should match because even though the node is not set in the event,
     * the IP address and service is in the database on *some* node.
     */
    public void testNoNodeIdWithService() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           0, null, "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }

    // FIXME... do we really want to return true if the rule is wrong?????
    public void testRuleBogus() {
        try {
            doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                               1, "192.168.1.1", "HTTP",
                                               "(aklsdfjweklj89jaikj)",
                                               false);
            Assert.fail("Expected exception to be thrown!");
        } catch (FilterParseException e) {
            // I expected this
        }
    }
    
    public void testIplikeAllStars() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr IPLIKE *.*.*.*)",
                                           true);
    }

    public void testNodeOnlyMatch() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, null, null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testNodeOnlyMatchZeroesIpAddr() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "0.0.0.0", null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testNodeOnlyNoMatch() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           3, null, null,
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }
    
    public void testWrongNodeId() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           2, "192.168.1.1", "HTTP",
                                           "(nodeid == 1)",
                                           false);
    }
    
    public void testIpAddrSpecificPass() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testIpAddrSpecificFail() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", null,
                                           "(ipaddr == '192.168.1.2')",
                                           false);
    }
    

    public void testIpAddrServiceSpecificPass() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testIpAddrServiceSpecificFail() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr == '192.168.1.2')",
                                           false);
    }
    
    public void testIpAddrServiceSpecificWrongService() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "ICMP",
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }

    public void testIpAddrServiceSpecificWrongIP() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.2", "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }
    
    public void testMultipleCategories() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(catincCategoryOne) & (catincCategoryTwo) & (catincCategoryThree)",
                                           true);
    }
    
    public void testMultipleCategoriesNotMember() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           2, "192.168.1.1", "HTTP",
                                           "(catincCategoryOne) & (catincCategoryTwo) & (catincCategoryThree)",
                                           false);
    }

    public void testIpAddrMatchWithNoServiceOnInterface() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           4, null, null,
                                           "(ipaddr == '192.168.1.3')",
                                           true);
    }

    /**
     * This test returns false because the ipInterface table is the
     * "primary" table in database-schema.xml, so it is joined with
     * every query, even if we don't ask for it to be joined and if
     * it isn't referenced in the filter query.  Sucky, huh?
     */
    public void testNodeMatchWithNoInterfacesOnNode() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           5, null, null,
                                           "(nodeId == 5)",
                                           false);
    }
    
    /**
     * This tests bugzilla bug #1807.  The problem happened when we add our
     * own constraints to the filter but fail to wrap the user's filter in
     * parens.  This isn't a problem when the outermost logic expression in
     * the user's filter (if any) is an AND, but it is if it's an OR.
     */
    public void testRuleWithOrNoMatch() {
        /*
         * Note: the nodeLabel for nodeId=3/ipAddr=192.168.1.2 is 'node 3'
         * which shouldn't match the filter.
         */
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                3, "192.168.1.2", "HTTP",
                "(nodelabel=='node 1') | (nodelabel=='node 2')",
                false);
    }
    
    private void doTestNodeInterfaceServiceWithRule(String description, int nodeId, String intf, String svc, String rule, boolean matches) {
        Notification notif = new Notification();
        notif.setName("a notification");
        notif.setRule(rule);
        
        EventBuilder builder = new EventBuilder("uei.opennms.org/doNotCareAboutTheUei", "Test.Event");
        builder.setNodeid(nodeId);
        builder.setInterface(addr(intf));
        builder.setService(svc);

        assertEquals(description, matches, m_notificationManager.nodeInterfaceServiceValid(notif, builder.getEvent()));
    }
    
    public class NotificationManagerImpl extends NotificationManager {
        protected NotificationManagerImpl(NotifdConfigManager configManager, DataSource dcf) {
            super(configManager, dcf);
        }

        @Override
        protected void saveXML(String xmlString) throws IOException {
            return;
            
        }

        @Override
        public void update() throws IOException, MarshalException, ValidationException {
            return;
        }
    }
}
