/*
 * [ * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.ehcache.tests;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.terracotta.api.ClusteringToolkit;
import org.terracotta.api.TerracottaClient;
import org.terracotta.coordination.Barrier;
import org.terracotta.tests.base.AbstractClientBase;

import java.util.concurrent.BrokenBarrierException;

public abstract class ClientBase extends AbstractClientBase {
  private static final String MANAGER_UTIL_CLASS_NAME                                    = "com.tc.object.bytecode.ManagerUtil";
  private static final String MANAGER_UTIL_WAITFORALLCURRENTTRANSACTIONTOCOMPLETE_METHOD = "waitForAllCurrentTransactionsToComplete";

  private final String        name;

  private CacheManager        cacheManager;
  private TerracottaClient    terracottaClient;
  private Barrier             barrier;

  public ClientBase(String cacheName, String args[]) {
    super(args);
    this.name = cacheName;
  }

  public void run() {
    try {
      test(setupCache(), getClusteringToolkit());
      pass();
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  protected synchronized final Barrier getBarrierForAllClients() {
    if (barrier == null) {
      barrier = getClusteringToolkit().getBarrier("barrier with all clients", getParticipantCount());
    }
    return barrier;
  }

  protected final void waitForAllClients() throws InterruptedException, BrokenBarrierException {
    getBarrierForAllClients().await();
  }

  protected Cache setupCache() {
    cacheManager = new CacheManager(Client1.class.getResourceAsStream("/ehcache-config.xml"));
    return cacheManager.getCache(name);
  }

  public CacheManager getCacheManager() {
    return cacheManager;
  }

  protected ClusteringToolkit getClusteringToolkit() {
    return getTerracottaClient().getToolkit();
  }

  public synchronized TerracottaClient getTerracottaClient() {
    if (terracottaClient == null) {
      terracottaClient = new TerracottaClient(getTerracottaUrl());
    }
    return terracottaClient;
  }

  public synchronized void clearTerracottaClient() {
    terracottaClient = null;
    cacheManager = null;
  }

  protected abstract void test(Cache cache, ClusteringToolkit toolkit) throws Throwable;

  // work around for ManagerUtil.waitForAllCurrentTransactionsToComplete()
  public void waitForAllCurrentTransactionsToComplete() {
    try {
      ClassLoader cl = getClusteringToolkit().getList("testList").getClass().getClassLoader();
      Class managerUtil = cl.loadClass(MANAGER_UTIL_CLASS_NAME);
      managerUtil.getMethod(MANAGER_UTIL_WAITFORALLCURRENTTRANSACTIONTOCOMPLETE_METHOD).invoke(null);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

}
