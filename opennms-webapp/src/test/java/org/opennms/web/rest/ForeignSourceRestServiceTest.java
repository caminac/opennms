package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class ForeignSourceRestServiceTest extends AbstractSpringJerseyRestTestCase {
    
    @Test
    public void testForeignSources() throws Exception {
        createForeignSource();
        String url = "/foreignSources";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("DHCP"));
        
        url = "/foreignSources/test";
        sendPut(url, "scanInterval=1h");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<scan-interval>1h</scan-interval>"));
        
        url = "/foreignSources/test";
        sendPut(url, "scanInterval=1h");
        sendRequest(DELETE, url, 200);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<scan-interval>1d</scan-interval>"));
    }
    
    @Test
    public void testDetectors() throws Exception {
        createForeignSource();

        String url = "/foreignSources/test/detectors";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<detectors>"));
        assertTrue(xml.contains("<detector "));
        assertTrue(xml.contains("name=\"DHCP\""));
        
        url = "/foreignSources/test/detectors/HTTP";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("org.opennms.netmgt.provision.detector.simple.HttpDetector"));
    }

    @Test
    public void testPolicies() throws Exception {
        createForeignSource();

        String url = "/foreignSources/test/policies";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<policies>"));
        assertTrue(xml.contains("<policy "));
        assertTrue(xml.contains("name=\"lower-case-node\""));
        
        url = "/foreignSources/test/policies/all-ipinterfaces";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
    }

    private void createForeignSource() throws Exception {
        String fs =
            "<foreign-source name=\"test\">" +
                "<scan-interval>1d</scan-interval>" +
                "<detectors>" + 
                    "<detector class=\"org.opennms.netmgt.provision.detector.dhcp.DhcpDetector\" name=\"DHCP\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.datagram.DnsDetector\" name=\"DNS\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.simple.HttpDetector\" name=\"HTTP\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.simple.HttpsDetector\" name=\"HTTPS\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.icmp.IcmpDetector\" name=\"ICMP\"/>" +
                "</detectors>" +
                "<policies>" +
                    "<policy name=\"lower-case-node\" class=\"org.opennms.netmgt.provision.persist.policies.NodeCategoryPolicy\">" +
                        "<parameter key=\"label\" value=\"~^[a-z]$\" />" +
                        "<parameter key=\"category\" value=\"Lower-Case-Nodes\" />" +
                    "</policy>" +
                    "<policy name=\"all-ipinterfaces\" class=\"org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy\" />" +
                "</policies>" +
            "</foreign-source>";
        sendPost("/foreignSources", fs);
    }
    
}
