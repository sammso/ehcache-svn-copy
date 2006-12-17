/**
 *  Copyright 2003-2006 Greg Luck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.sf.ehcache.management;

import net.sf.ehcache.AbstractCacheTest;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * These tests use the JDK1.5 platform mbean server
 * @author Greg Luck
 * @version $Id$
 */
public class MBeanServerRegistrationTest extends AbstractCacheTest {

    private static final Log LOG = LogFactory.getLog(MBeanServerRegistrationTest.class.getName());
    private MBeanServer mBeanServer;


    /**
     * setup test
     */
    protected void setUp() throws Exception {
        super.setUp();
        createMBeanServer();
    }

    private void createMBeanServer() {
        //todo deal with 1.4
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    /**
     * teardown
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        //Ensure the CacheManager shutdown clears all ObjectNames from the MBeanServer
        assertEquals(0, mBeanServer.queryNames(new ObjectName("net.sf.ehcache:*"), null).size());
    }


    /**
     * Integration test for the registration service
     */
    public void testRegistrationServiceFourTrue() throws Exception {
        RegistrationService.registerMBeans(manager, mBeanServer, true, true, true, true);
        assertEquals(37, mBeanServer.queryNames(new ObjectName("net.sf.ehcache:*"), null).size());
    }

    /**
     * Integration test for the registration service
     */
    public void testRegistrationServiceThreeTrue() throws Exception {
        RegistrationService.registerMBeans(manager, mBeanServer, true, true, true, false);
        assertEquals(25, mBeanServer.queryNames(new ObjectName("net.sf.ehcache:*"), null).size());

    }

    /**
     * Integration test for the registration service
     */
    public void testRegistrationServiceTwoTrue() throws Exception {
        RegistrationService.registerMBeans(manager, mBeanServer, true, true, false, false);
        assertEquals(13, mBeanServer.queryNames(new ObjectName("net.sf.ehcache:*"), null).size());

    }

    /**
     * Integration test for the registration service
     */
    public void testRegistrationServiceOneTrue() throws Exception {
        RegistrationService.registerMBeans(manager, mBeanServer, true, false, false, false);
        assertEquals(1, mBeanServer.queryNames(new ObjectName("net.sf.ehcache:*"), null).size());

    }

    /**
     * Integration test for the registration service
     */
    public void testRegistrationServiceNoneTrue() throws Exception {
        RegistrationService.registerMBeans(manager, mBeanServer, false, false, false, false);
        assertEquals(0, mBeanServer.queryNames(new ObjectName("net.sf.ehcache:*"), null).size());

    }

    /**
     * Can we register the CacheManager MBean?
     */
    public void testRegisterCacheManager() throws Exception {
        //Set size so the second element overflows to disk.
        Ehcache ehcache = new net.sf.ehcache.Cache("testNoOverflowToDisk", 1, false, true, 500, 200);
        manager.addCache(ehcache);

        ehcache.put(new Element("key1", "value1"));                                    
        ehcache.put(new Element("key2", "value1"));
        assertNull(ehcache.get("key1"));
        assertNotNull(ehcache.get("key2"));


        ObjectName name = new ObjectName("net.sf.ehcache:type=CacheManager,name=1");
        CacheManager cacheManager = new CacheManager(manager);
        mBeanServer.registerMBean(cacheManager, name);
        mBeanServer.unregisterMBean(name);

        name = new ObjectName("net.sf.ehcache:type=CacheManager.Cache,CacheManager=1,name=testOverflowToDisk");
        mBeanServer.registerMBean(new Cache(ehcache), name);
        mBeanServer.unregisterMBean(name);

        name = new ObjectName("net.sf.ehcache:type=CacheManager.Cache,CacheManager=1,name=sampleCache1");
        mBeanServer.registerMBean(new Cache(manager.getCache("sampleCache1")), name);
        mBeanServer.unregisterMBean(name);

    }


    /**
     * Can we register the CacheManager MBean?
     */
    public void testListCachesFromManager() throws Exception {
        //Set size so the second element overflows to disk.
        Ehcache ehcache = new net.sf.ehcache.Cache("testNoOverflowToDisk", 1, false, true, 500, 200);
        manager.addCache(ehcache);

        ehcache.put(new Element("key1", "value1"));
        ehcache.put(new Element("key2", "value1"));
        assertNull(ehcache.get("key1"));
        assertNotNull(ehcache.get("key2"));

        ObjectName name = new ObjectName("net.sf.ehcache:type=CacheManager,name=1");
        CacheManager cacheManager = new CacheManager(manager);
        mBeanServer.registerMBean(cacheManager, name);

        Object object = mBeanServer.getAttribute(name, "Status");
        LOG.info(object);

        List caches = (List) mBeanServer.getAttribute(name, "Caches");
        assertEquals(13, caches.size());

        for (int i = 0; i < caches.size(); i++) {
            Cache cache = (Cache) caches.get(i);
            String cacheName = cache.getName();
            LOG.info(cacheName);
        }
        mBeanServer.unregisterMBean(name);
    }

}
