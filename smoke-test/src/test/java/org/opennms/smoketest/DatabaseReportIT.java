/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies that the database reports can be generated without any exceptions.
 *
 * The browser is configured to automatically download .pdf files and places these in
 * a downloads directory which the test verifies.
 */
@RunWith(Parameterized.class)
public class DatabaseReportIT extends OpenNMSSeleniumIT {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseReportIT.class);

    // Reports to verify
    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[][] {
                {"EarlyMorningReport", "PDF", "Early morning report", 1},
                {"ResponseTimeSummaryForNode", "PDF", "Response Time Summary for node", 1},
                {"AvailabilityByNode", "PDF", "Availability by node", 1},
                {"AvailabilitySummaryPast7Days", "PDF", "Availability Summary -Default configuration for past 7 Days", 1},
                {"ResponseTimeByNode", "PDF", "Response time by node", 1},
                {"SerialInterfaceUtilizationSummary", "PDF", "Serial Interface Utilization Summary", 1},
                {"TotalBytesTransferredByInterface", "PDF", "Total Bytes Transferred by Interface ", 1},
                {"AverageAndPeakTrafficRatesForNodesByInterface", "PDF", "Average and Peak Traffic rates for Nodes by Interface", 1},
                {"InterfaceAvailabilityReport", "PDF", "Interface Availability Report", 2},
                {"SnmpInterfaceAvailabilityReport", "PDF", "Snmp Interface Availability Report", 2},
                {"MaintenanceContractsExpired", "PDF", "Maintenance contracts expired", 2},
                {"MaintenanceContractsStrategy", "PDF", "Maintenance contracts strategy", 2},
                {"EventAnalysisReport", "PDF", "Event Analysis report", 2},
        };
    }

    @Parameterized.Parameter(0)
    public String reportId;

    @Parameterized.Parameter(1)
    public String reportFormat;

    @Parameterized.Parameter(2)
    public String reportName;

    @Parameterized.Parameter(3)
    public int page;

    /**
     * Fixed filename that PDF reports will have once downloaded.
     */
    private final File reportPdfFile = new File(getDownloadsFolder(), "report.pdf");

    @Before
    public void before() {
        cleanDownloadsFolder();

        // we do not want to wait 2 minutes, we only want to wait n seconds
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        LOG.info("Validating report generation '{}' ({})", reportName, reportFormat);

        Assert.assertNotNull(reportId);
        Assert.assertNotNull(reportFormat);
        Assert.assertNotNull(reportName);
        Assert.assertNotNull(page);

        LOG.info("Navigation to database reports page {}.", page);
        reportsPage();
        findElementByLink("Database Reports").click();
        findElementByLink("List reports").click();
        if (page > 1) {
            findElementByLink(Integer.toString(page)).click();
        }
    }

    @After
    public void after() {
        cleanDownloadsFolder();
    }

    @Test
    public void verifyReportExecution() {
        LOG.info("Verifying report '{}'", reportName);

        // the report should not exist in the downloads folder yet
        assertThat(reportPdfFile.exists(), equalTo(false));

        // execute report (no custom parameter setup)
        getInstantExecutionLink(reportName).click();
        findElementById("run").click(); // run report

        verify();
    }

    private void verify() {
        // verify current page and look out for errors
        // we do not use findElementByXpath(...) on purpose, we explicitly want to use this
        // otherwise we have to wait 2 minutes each time an error already occurred
        List<WebElement> errorElements = driver.findElements(By.xpath("//div[@class=\"alert alert-danger\"]"));
        if (!errorElements.isEmpty()) {
            Assert.fail("An error occurred while generating the report: " + errorElements.get(0).getText());
        }

        // let's wait until this file appears in the download folder
        await().atMost(2, MINUTES).pollInterval(5, TimeUnit.SECONDS).until(reportPdfFile::exists);

        // ensure it really has been downloaded and has a file size > 0
        Assert.assertTrue("No report was generated for report '" + reportName + "'", reportPdfFile.exists());
        Assert.assertTrue("The report is empty", reportPdfFile.length() > 0);

        // rename the report so it remains available as an artifact
        final File targetFile = new File(getDownloadsFolder(), reportId + ".pdf");
        reportPdfFile.renameTo(targetFile);
    }

    // find the run report link
    private WebElement getInstantExecutionLink(String reportName) {
        return findElementByXpath(String.format("//table/tbody/tr/td[text()='%s']/../td[3]/a", reportName));
    }

}
