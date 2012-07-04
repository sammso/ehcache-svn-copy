/**
 *  Copyright Terracotta, Inc.
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
package net.sf.ehcache.transaction.manager.selector;

import net.sf.ehcache.transaction.xa.EhcacheXAResource;
import net.sf.ehcache.util.ClassLoaderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import javax.transaction.xa.XAResource;

/**
 * A Selector for the Weblogic Server's JTA transaction manager
 *
 * @author Ludovic Orban
 */
public class WeblogicSelector extends FactorySelector {
    private static final Logger LOG = LoggerFactory.getLogger(WeblogicSelector.class);

    /**
     * Constructor
     */
    public WeblogicSelector() {
        super("Weblogic", "weblogic.transaction.TxHelper", "getTransactionManager");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerResource(EhcacheXAResource ehcacheXAResource, boolean forRecovery) {
        System.out.println("*** registering " + ehcacheXAResource + " - for recovery? " + forRecovery);
        if (!forRecovery) {
            return;
        }

        String uniqueName = ehcacheXAResource.getCacheName();
        try {
            Class tmImplClass = ClassLoaderUtil.loadClass("weblogic.transaction.TransactionManager");

            Class[] signature = new Class[] {String.class, XAResource.class};
            Object[] args = new Object[] {uniqueName, ehcacheXAResource};
            Method method = tmImplClass.getMethod("registerResource", signature);
            method.invoke(getTransactionManager(), args);
        } catch (Exception e) {
            LOG.error("unable to register resource of cache " + uniqueName + " with Weblogic", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterResource(final EhcacheXAResource ehcacheXAResource, final boolean forRecovery) {
        System.out.println("*** unregistering " + ehcacheXAResource + " - for recovery? " + forRecovery);
        if (!forRecovery) {
            return;
        }

        String uniqueName = ehcacheXAResource.getCacheName();
        try {
            Class tmImplClass = ClassLoaderUtil.loadClass("weblogic.transaction.TransactionManager");

            Class[] signature = new Class[] {String.class, Boolean.TYPE};
            Object[] args = new Object[] {uniqueName, Boolean.TRUE};
            Method method = tmImplClass.getMethod("unregisterResource", signature);
            method.invoke(getTransactionManager(), args);
        } catch (Exception e) {
            LOG.error("unable to unregister resource of cache " + uniqueName + " with Weblogic", e);
        }
    }

}
