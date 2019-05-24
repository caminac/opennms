/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.telemetry.common.utils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.opennms.core.utils.InetAddressUtils;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

public class DnsUtilsTest {

    @Before
    public void before() {
        DnsUtils.bundleContext = null;
    }

    private List<String> getServers(final ExtendedResolver extendedResolver) throws Exception {
        final List<String> list = new ArrayList<>();

        for(final Resolver resolver : extendedResolver.getResolvers()) {
            final SimpleResolver simpleResolver = (SimpleResolver) resolver;

            final Field privateAddressField = SimpleResolver.class.getDeclaredField("address");
            privateAddressField.setAccessible(true);
            list.add(((InetSocketAddress)privateAddressField.get(simpleResolver)).getAddress().getHostAddress());
        }
        return list;
    }

    @Test
    public void setDnsServersTest() throws Exception{
        final List<String> addresses1 = getServers(DnsUtils.getResolver());
        Assert.assertEquals(true, addresses1.size() > 0);

        DnsUtils.setDnsServers("9.8.7.6", "8.7.6.5");
        final List<String> addresses2 = getServers(DnsUtils.getResolver());

        Assert.assertEquals(2, addresses2.size());
        Assert.assertEquals(true, addresses2.contains("9.8.7.6"));
        Assert.assertEquals(true, addresses2.contains("8.7.6.5"));

        DnsUtils.setDnsServers("4.3.2.1");
        final List<String> addresses3 = getServers(DnsUtils.getResolver());

        Assert.assertEquals(1, addresses3.size());
        Assert.assertEquals(true, addresses3.contains("4.3.2.1"));

        DnsUtils.setDnsServers();

        final List<String> addresses4 = getServers(DnsUtils.getResolver());
        Assert.assertEquals(true, addresses4.size() > 0);
    }

    @Test
    public void resolveTest() throws UnknownHostException {
        final Optional<String> hostname1 = DnsUtils.reverseLookup(InetAddress.getByAddress(new byte[]{1, 1, 1, 1}));
        Assert.assertEquals("one.one.one.one", hostname1.get());

        final Optional<String> hostname2 = DnsUtils.reverseLookup(InetAddressUtils.addr("1.1.1.1"));
        Assert.assertEquals("one.one.one.one", hostname2.get());

        final Optional<String> hostname3 = DnsUtils.reverseLookup(InetAddressUtils.addr("2606:4700:4700::1111"));
        Assert.assertEquals("one.one.one.one", hostname3.get());
    }

    @Test
    public void resolveFailTest() {
        // 198.51.100.0/24 should be TEST-NET-2 (see RFC #5737). Should fail...
        final Optional<String> hostname1 = DnsUtils.reverseLookup(InetAddressUtils.addr("198.51.100.1"));
        Assert.assertEquals(Optional.empty(), hostname1);

        final Optional<String> hostname2 = DnsUtils.reverseLookup(InetAddressUtils.addr("fe80::"));
        Assert.assertEquals(Optional.empty(), hostname2);
    }

    @Test
    public void setSystemProperties() throws Exception {
        final Properties p = new Properties();

        DnsUtils.bundleContext = new BundleContext() {
            @Override
            public String getProperty(String key) {
                return (String) p.get(key);
            }

            @Override
            public Bundle getBundle() {
                return null;
            }

            @Override
            public Bundle installBundle(String location, InputStream input) throws BundleException {
                return null;
            }

            @Override
            public Bundle installBundle(String location) throws BundleException {
                return null;
            }

            @Override
            public Bundle getBundle(long id) {
                return null;
            }

            @Override
            public Bundle[] getBundles() {
                return new Bundle[0];
            }

            @Override
            public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {

            }

            @Override
            public void addServiceListener(ServiceListener listener) {

            }

            @Override
            public void removeServiceListener(ServiceListener listener) {

            }

            @Override
            public void addBundleListener(BundleListener listener) {

            }

            @Override
            public void removeBundleListener(BundleListener listener) {

            }

            @Override
            public void addFrameworkListener(FrameworkListener listener) {

            }

            @Override
            public void removeFrameworkListener(FrameworkListener listener) {

            }

            @Override
            public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
                return null;
            }

            @Override
            public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
                return null;
            }

            @Override
            public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
                return null;
            }

            @Override
            public <S> ServiceRegistration<S> registerService(Class<S> clazz, ServiceFactory<S> factory, Dictionary<String, ?> properties) {
                return null;
            }

            @Override
            public ServiceReference<?>[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
                return new ServiceReference[0];
            }

            @Override
            public ServiceReference<?>[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
                return new ServiceReference[0];
            }

            @Override
            public ServiceReference<?> getServiceReference(String clazz) {
                return null;
            }

            @Override
            public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
                return null;
            }

            @Override
            public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) throws InvalidSyntaxException {
                return null;
            }

            @Override
            public <S> S getService(ServiceReference<S> reference) {
                return null;
            }

            @Override
            public boolean ungetService(ServiceReference<?> reference) {
                return false;
            }

            @Override
            public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> reference) {
                return null;
            }

            @Override
            public File getDataFile(String filename) {
                return null;
            }

            @Override
            public Filter createFilter(String filter) throws InvalidSyntaxException {
                return null;
            }

            @Override
            public Bundle getBundle(String location) {
                return null;
            }
        };

        final List<String> addresses1 = getServers(DnsUtils.getResolver());
        Assert.assertEquals(true, addresses1.size() > 0);

        p.setProperty(DnsUtils.DNS_ENABLED, "true");
        p.setProperty(DnsUtils.DNS_PRIMARY_SERVER, "1.1.1.1");
        p.setProperty(DnsUtils.DNS_SECONDARY_SERVER, "8.8.8.8");
        DnsUtils.reverseLookup("1.1.1.1");

        final List<String> addresses2 = getServers(DnsUtils.getResolver());
        Assert.assertEquals(2, addresses2.size());
        Assert.assertEquals(true, addresses2.contains("1.1.1.1"));
        Assert.assertEquals(true, addresses2.contains("8.8.8.8"));

        p.clear();
        p.setProperty(DnsUtils.DNS_ENABLED, "true");
        p.setProperty(DnsUtils.DNS_PRIMARY_SERVER, "8.8.4.4");
        DnsUtils.reverseLookup("1.1.1.1");

        final List<String> addresses3 = getServers(DnsUtils.getResolver());
        Assert.assertEquals(1, addresses3.size());
        Assert.assertEquals(true, addresses3.contains("8.8.4.4"));

        p.clear();
        p.setProperty(DnsUtils.DNS_ENABLED, "false");
        DnsUtils.reverseLookup("1.1.1.1");

        final List<String> addresses4 = getServers(DnsUtils.getResolver());
        Assert.assertEquals(true, addresses4.size() > 0);
    }
}
