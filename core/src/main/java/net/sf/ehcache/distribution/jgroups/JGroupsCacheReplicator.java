/**
 *  Copyright 2003-2007 Luck Consulting Pty Ltd
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

package net.sf.ehcache.distribution.jgroups;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CachePeer;
import net.sf.ehcache.distribution.CacheReplicator;

/**
 * @author Pierre Monestie (pmonestie[at]@gmail.com)
 * @author <a href="mailto:gluck@gregluck.com">Greg Luck</a>
 * @version $Id$
 *          <p/>
 *          This implements CacheReplicator using JGroups as underlying replication mechanism
 *          The peer provider should be of type JGroupsCacheManagerPeerProvider
 *          It is assumed that the cachepeer is a JGroupManager
 */
public class JGroupsCacheReplicator implements CacheReplicator {
    private static final Log log = LogFactory.getLog(JGroupsCacheReplicator.class);

    private JGroupManager jmanager;

    /**
     * Whether or not to replicate puts
     */
    boolean replicatePuts;

    /**
     * Whether or not to replicate updates
     */
    boolean replicateUpdates;

    /**
     * Replicate update via copying, if false via deleting
     */
    boolean replicateUpdatesViaCopy;

    /**
     * Whether or not to replicate remove events
     */
    boolean replicateRemovals;

    /**
     * Weather or not to replicate asynchronously. If true a background thread is ran and fire update at a set intervale
     */
    boolean replicateAsync;

    /**
     * Asynchronous replication interval
     */
    private long asynchronousReplicationInterval = 1000;

    protected final List replicationQueue = new LinkedList();

    protected Status status;


    private ReplicationThread replicationThread = null;


    /**
     * Constructor called by factory
     *
     * @param replicatePuts
     * @param replicateUpdates
     * @param replicateUpdatesViaCopy
     * @param replicateRemovals
     * @param replicateAsync
     */
    public JGroupsCacheReplicator(boolean replicatePuts, boolean replicateUpdates, boolean replicateUpdatesViaCopy,
                                  boolean replicateRemovals, boolean replicateAsync) {
        super();

        this.replicatePuts = replicatePuts;
        this.replicateUpdates = replicateUpdates;
        this.replicateUpdatesViaCopy = replicateUpdatesViaCopy;
        this.replicateRemovals = replicateRemovals;
        this.replicateAsync = replicateAsync;

        if (replicateAsync) {
            replicationThread = new ReplicationThread();
            replicationThread.start();
        }
        status = Status.STATUS_ALIVE;
    }

    public JGroupManager getJmanager() {
        return jmanager;
    }

    public boolean isReplicateAsync() {
        return replicateAsync;
    }

    public void setReplicateAsync(boolean replicateAsync) {
        this.replicateAsync = replicateAsync;
    }

    public boolean isReplicatePuts() {
        return replicatePuts;
    }

    public void setReplicatePuts(boolean replicatePuts) {
        this.replicatePuts = replicatePuts;
    }

    public boolean getReplicateRemovals() {
        return replicateRemovals;
    }

    public void setReplicateRemovals(boolean replicateRemovals) {
        this.replicateRemovals = replicateRemovals;
    }

    public boolean getReplicateUpdates() {
        return replicateUpdates;
    }

    public void setReplicateUpdates(boolean replicateUpdates) {
        this.replicateUpdates = replicateUpdates;
    }

    public void setReplicateUpdatesViaCopy(boolean replicateUpdatesViaCopy) {
        this.replicateUpdatesViaCopy = replicateUpdatesViaCopy;
    }

    public boolean alive() {
        // TODO Auto-generated method stub
        return true;
    }

    public boolean isReplicateUpdatesViaCopy() {
        // TODO Auto-generated method stub
        return replicateUpdatesViaCopy;
    }

    public boolean notAlive() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setJmanager(JGroupManager jmanager) {
        this.jmanager = jmanager;
    }

    public void dispose() {
        status = Status.STATUS_SHUTDOWN;
        flushReplicationQueue();
    }

    public void notifyElementExpired(Ehcache cache, Element element) {
        // TODO Auto-generated method stub
        // log.trace("Sending out exp el:"+element);

    }

    /**
     * Used to send notification to the peer. If Async this method simply add
     * the element to the replication queue. If not async, searches for the
     * cachePeer and send the Message. That way the class handles both async and sync replication
     * Sending is delegated to the peer (of type JGroupManager)
     *
     * @param cache
     * @param e
     */
    protected void sendNotification(Ehcache cache, JGroupEventMessage e) {

        if (replicateAsync) {
            addMessageToQueue(e);
            return;
        }
        CacheManagerPeerProvider provider = cache.getCacheManager().getCachePeerProvider();
        List l = provider.listRemoteCachePeers(cache);
        ArrayList a = new ArrayList();

        a.add(e);


        for (int i = 0; i < l.size(); i++) {
            CachePeer peer = (CachePeer) l.get(i);
            try {
                peer.send(a);
            } catch (RemoteException e1) {
                // TODO Auto-generated catch block
                // e1.printStackTrace();
            }
            // peer.
        }

    }

    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        if (notAlive())
            return;

        if (getReplicatePuts()) {
            // if (log.isTraceEnabled())
            // log.trace("Sending out add/upd el:" + element);
            replicatePutNotification(cache, element);
        }

    }

    private void replicatePutNotification(Ehcache cache, Element element) {
        if (!element.isKeySerializable()) {
            log.warn("Key " + element.getObjectKey() + " is not Serializable and cannot be replicated.");
            return;
        }
        if (!element.isSerializable()) {
            log.warn("Object with key " + element.getObjectKey() + " is not Serializable and cannot be updated via copy");
            return;
        }
        JGroupEventMessage e = new JGroupEventMessage(JGroupEventMessage.PUT, (Serializable) element.getObjectKey(), element,
                cache, cache.getName());

        sendNotification(cache, e);
    }

    private void replicateRemoveNotification(Ehcache cache, Element element) {
        if (!element.isKeySerializable()) {
            log.warn("Key " + element.getObjectKey() + " is not Serializable and cannot be replicated.");
            return;
        }
        JGroupEventMessage e = new JGroupEventMessage(JGroupEventMessage.REMOVE, (Serializable) element.getObjectKey(), null,
                cache, cache.getName());

        sendNotification(cache, e);
    }

    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        if (notAlive())
            return;
        if (getReplicateRemovals()) {
            replicateRemoveNotification(cache, element);

        }

    }

    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
        if (notAlive())
            return;
        if (!replicateUpdates)
            return;

        if (isReplicateUpdatesViaCopy())
            replicatePutNotification(cache, element);
        else
            replicateRemoveNotification(cache, element);

    }

    public void notifyElementEvicted(Ehcache cache, Element element) {

        // TODO Auto-generated method stub

    }

    public boolean getReplicatePuts() {
        return replicatePuts;
    }

    public void setReplicatePut(boolean replicatePut) {
        this.replicatePuts = replicatePut;
    }

    public void notifyRemoveAll(Ehcache cache) {
        if (getReplicateRemovals()) {
            log.trace("Remove all elements called");
            JGroupEventMessage e = new JGroupEventMessage(JGroupEventMessage.REMOVE_ALL, null, null, cache, cache.getName());
            sendNotification(cache, e);
        }

    }

    /**
     * Package protected List of cache peers
     *
     * @param cache
     * @return a list of {@link CachePeer} peers for the given cache, excluding
     *         the local peer.
     */
    static List listRemoteCachePeers(Ehcache cache) {
        CacheManagerPeerProvider provider = cache.getCacheManager().getCachePeerProvider();
        return provider.listRemoteCachePeers(cache);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private final class ReplicationThread extends Thread {
        public ReplicationThread() {
            super("Replication Thread");
            setDaemon(true);
            setPriority(Thread.NORM_PRIORITY);
        }

        /**
         * RemoteDebugger thread method.
         */
        public final void run() {
            replicationThreadMain();
        }
    }

    private void replicationThreadMain() {
        while (true) {
            // Wait for elements in the replicationQueue
            while (alive() && replicationQueue != null && replicationQueue.size() == 0) {
                try {

                    Thread.sleep(asynchronousReplicationInterval);
                } catch (InterruptedException e) {
                    log.debug("Spool Thread interrupted.");
                    return;
                }
            }
            if (notAlive()) {
                return;
            }
            try {
                if (replicationQueue.size() != 0) {
                    flushReplicationQueue();
                }
            } catch (Throwable e) {
                log.warn("Exception on flushing of replication queue: " + e.getMessage() + ". Continuing...", e);
            }
        }
    }

    private void addMessageToQueue(JGroupEventMessage msg) {
        synchronized (replicationQueue) {
            replicationQueue.add(msg);
        }
    }

    /**
     * Gets called once per {@link #asynchronousReplicationInterval}. <p/>
     * Sends accumulated messages in bulk to each peer. i.e. if ther are 100
     * messages and 1 peer, 1 RMI invocation results, not 100. Also, if a peer
     * is unavailable this is discovered in only 1 try. <p/> Makes a copy of the
     * queue so as not to hold up the enqueue operations. <p/> Any exceptions
     * are caught so that the replication thread does not die, and because
     * errors are expected, due to peers becoming unavailable. <p/> This method
     * issues warnings for problems that can be fixed with configuration
     * changes.
     */
    private void flushReplicationQueue() {
        List resolvedEventMessages;
        Ehcache cache;
        synchronized (replicationQueue) {
            if (replicationQueue.size() == 0) {
                return;
            }
            resolvedEventMessages = extractAndResolveEventMessages(replicationQueue);
            cache = ((JGroupEventMessage) replicationQueue.get(0)).getCache();
            replicationQueue.clear();
        }

        List cachePeers = listRemoteCachePeers(cache);

        for (int j = 0; j < cachePeers.size(); j++) {
            CachePeer cachePeer = (CachePeer) cachePeers.get(j);
            try {
                cachePeer.send(resolvedEventMessages);
            } catch (UnmarshalException e) {
                String message = e.getMessage();
                if (message.indexOf("Read time out") != 0) {
                    log.warn("Unable to send message to remote peer due to socket read timeout. Consider increasing"
                            + " the socketTimeoutMillis setting in the cacheManagerPeerListenerFactory. " + "Message was: "
                            + e.getMessage());
                } else {
                    log.debug("Unable to send message to remote peer.  Message was: " + e.getMessage());
                }
            } catch (Throwable t) {
                log.warn("Unable to send message to remote peer.  Message was: " + t.getMessage(), t);
            }
        }
        if (log.isWarnEnabled()) {
            // int eventMessagesNotResolved = replicationQueueCopy.size() -
            // resolvedEventMessages.size();
            // if (eventMessagesNotResolved > 0) {
            // log.warn(eventMessagesNotResolved + " messages were discarded on
            // replicate due to reclamation of "
            // + "SoftReferences by the VM. Consider increasing the maximum heap
            // size
            // and/or setting the "
            // + "starting heap size to a higher value.");
            // }

        }
    }

    /**
     * Extracts CacheEventMessages and attempts to get a hard reference to the
     * underlying EventMessage <p/> If an EventMessage has been invalidated due
     * to SoftReference collection of the Element, it is not propagated. This
     * only affects puts and updates via copy.
     *
     * @param replicationQueueCopy
     * @return a list of EventMessages which were able to be resolved
     */
    private static List extractAndResolveEventMessages(List replicationQueueCopy) {
        List list = new ArrayList();
        for (int i = 0; i < replicationQueueCopy.size(); i++) {
            JGroupEventMessage eventMessage = (JGroupEventMessage) replicationQueueCopy.get(i);
            if (eventMessage != null && eventMessage.isValid()) {
                list.add(eventMessage);
            } else {
                log.error("Collected soft ref");
            }
        }
        return list;
    }

    public long getAsynchronousReplicationInterval() {
        return asynchronousReplicationInterval;
    }

    public void setAsynchronousReplicationInterval(long asynchronousReplicationInterval) {
        this.asynchronousReplicationInterval = asynchronousReplicationInterval;
    }

}
