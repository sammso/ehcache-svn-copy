/**
 *  Copyright 2003-2008 Luck Consulting Pty Ltd
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

package net.sf.ehcache.distribution.jms;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

import net.sf.ehcache.Status;
import net.sf.ehcache.distribution.CacheManagerPeerListener;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.ConnectionFactory;
import java.util.List;

/**
 * todo finish or remove
 * @author Greg Luck
 */
public class JMSCacheManagerPeerListener implements CacheManagerPeerListener {

    private CacheManager cacheManager;
    private ConnectionFactory connectionFactory;
    private Connection connection;

    /**
     * 
     * @param cacheManager
     * @param connectionFactory
     */
    public JMSCacheManagerPeerListener(CacheManager cacheManager, ConnectionFactory connectionFactory) {
        this.cacheManager = cacheManager;
        this.connectionFactory = connectionFactory;
    }

    /**
     * todo
     * All of the caches which are listening for remote changes.
     *
     * @return a list of <code>CachePeer</code> objects
     */
    public List getBoundCachePeers() {
        return null;
    }

    /**
     * A listener will normally have a resource that only one instance can use at the same time,
     * such as a port. This identifier is used to tell if it is unique and will not conflict with an
     * existing instance using the resource.
     *
     * @return a String identifier for the resource
     */
    public String getUniqueResourceIdentifier() {
        return null;
    }

    /**
     * If a conflict is detected in unique resource use, this method signals the listener to attempt
     * automatic resolution of the resource conflict.
     *
     * @throws IllegalStateException if the statis of the listener is not {@link net.sf.ehcache.Status#STATUS_UNINITIALISED}
     */
    public void attemptResolutionOfUniqueResourceConflict() throws IllegalStateException, CacheException {

    }

    /**
     * Call to start the listeners and do any other required initialisation.
     * init should also handle any work to do with the caches that are part of the initial configuration.
     *
     * @throws net.sf.ehcache.CacheException - all exceptions are wrapped in CacheException
     */
    public void init() throws CacheException {
        try {
            connection = connectionFactory.createConnection();
            Session mySession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        } catch (JMSException e) {
            throw new CacheException(e);
        }
    }

    /**
     * Returns the listener status.
     *
     * @return the status at the point in time the method is called
     */
    public Status getStatus() {
        return null;
    }

    /**
     * Stop the listener and free any resources.
     *
     * @throws net.sf.ehcache.CacheException - all exceptions are wrapped in CacheException
     */
    public void dispose() throws CacheException {

    }

    /**
     * Called immediately after a cache has been added and activated.
     * <p/>
     * Note that the CacheManager calls this method from a synchronized method. Any attempt to
     * call a synchronized method on CacheManager from this method will cause a deadlock.
     * <p/>
     * Note that activation will also cause a CacheEventListener status change notification
     * from {@link net.sf.ehcache.Status#STATUS_UNINITIALISED} to
     * {@link net.sf.ehcache.Status#STATUS_ALIVE}. Care should be taken on processing that
     * notification because:
     * <ul>
     * <li>the cache will not yet be accessible from the CacheManager.
     * <li>the addCaches methods which cause this notification are synchronized on the
     * CacheManager. An attempt to call {@link net.sf.ehcache.CacheManager#getEhcache(String)}
     * will cause a deadlock.
     * </ul>
     * The calling method will block until this method returns.
     * <p/>
     *
     * @param cacheName the name of the <code>Cache</code> the operation relates to
     * @see net.sf.ehcache.event.CacheEventListener
     */
    public void notifyCacheAdded(String cacheName) {

    }

    /**
     * Called immediately after a cache has been disposed and removed. The calling method will
     * block until this method returns.
     * <p/>
     * Note that the CacheManager calls this method from a synchronized method. Any attempt to
     * call a synchronized method on CacheManager from this method will cause a deadlock.
     * <p/>
     * Note that a {@link net.sf.ehcache.event.CacheEventListener} status changed will also be triggered. Any
     * attempt from that notification to access CacheManager will also result in a deadlock.
     *
     * @param cacheName the name of the <code>Cache</code> the operation relates to
     */
    public void notifyCacheRemoved(String cacheName) {

    }
}
