/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.modules.ehcache.store;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.terracotta.ehcache.tests.AbstractCacheTestBase;
import org.terracotta.ehcache.tests.ClientBase;
import org.terracotta.test.util.WaitUtil;
import org.terracotta.toolkit.Toolkit;

import com.tc.test.config.model.TestConfig;

import java.util.concurrent.Callable;

import junit.framework.Assert;

public class CachePinningInvalidatedEntriesRaceTest extends AbstractCacheTestBase {

  private static final int ELEMENT_COUNT = 1000;

  public CachePinningInvalidatedEntriesRaceTest(TestConfig testConfig) {
    super("cache-pinning-test.xml", testConfig, App.class, App.class);
  }

  public static class App extends ClientBase {
    public App(String[] args) {
      super("pinned", args);
    }

    @Override
    protected void runTest(Cache cache, Toolkit clusteringToolkit) throws Throwable {
      System.out.println("Testing with Strong tier pinned cache");
      basicTestInvalidatedEntries(cacheManager.getCache("pinned"), true);
      System.out.println("Testing with Eventual tier pinned cache");
      basicTestInvalidatedEntries(cacheManager.getCache("pinnedEventual"), true);

      System.out.println("Testing with Eventual element pinned cache");
      basicTestInvalidatedEntries(cacheManager.getCache("pinnedElementEventual"), false);
      System.out.println("Testing with Strong element pinned cache");
      basicTestInvalidatedEntries(cacheManager.getCache("pinnedElementStrong"), false);

    }

    private void basicTestInvalidatedEntries(final Cache cache, boolean tierPinned) throws Exception {
      int index = getBarrierForAllClients().await();
      debug("Client Index = " + index);
      if (index == 0) {
        for (int i = 0; i < ELEMENT_COUNT; i++) {
          if (!tierPinned) {
            cache.setPinned(getKey(i), true);
          }
          cache.put(new Element(getKey(i), getValue(i)));
          if (!tierPinned) {
            Assert.assertTrue(cache.isPinned(getKey(i)));
          }
        }
        Assert.assertEquals(ELEMENT_COUNT, cache.getSize());
        for (int i = 0; i < ELEMENT_COUNT; i++) {
          assertEquals(cache.get(getKey(i)).getObjectValue(), getValue(i));
        }

        Assert.assertEquals(ELEMENT_COUNT, cache.getMemoryStoreSize());
        Assert.assertEquals(ELEMENT_COUNT, cache.getStatistics().getInMemoryHits());
        Assert.assertEquals(0, cache.getStatistics().getInMemoryMisses());
        Assert.assertEquals(0, cache.getStatistics().getOnDiskHits());
        Assert.assertEquals(0, cache.getStatistics().getOnDiskMisses());
        Assert.assertEquals(0, cache.getStatistics().getEvictionCount());
        cache.getStatistics().clearStatistics();
        debug("done testing pining with client " + index + " size " + cache.getSize());
      }
      getBarrierForAllClients().await();
      if (index == 1) {
        debug("pinnging and updating elemnts on client " + index);
        // Update elements, invalidate entries on other client
        for (int i = 0; i < ELEMENT_COUNT; i++) {
          if (!tierPinned) {
            cache.setPinned(getKey(i), true);
          }
          cache.put(new Element(getKey(i), getValue(i + 1)));
        }
        waitForAllCurrentTransactionsToComplete(cache);
        Assert.assertEquals(ELEMENT_COUNT, cache.getSize());
      }

      getBarrierForAllClients().await();
      if (index == 0) {
        WaitUtil.waitUntilCallableReturnsTrue(new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            System.out.println("memory store count " + cache.getMemoryStoreSize());
            return cache.getMemoryStoreSize() >= ELEMENT_COUNT;
          }
        });
      }

      getBarrierForAllClients().await();
      for (int i = 0; i < ELEMENT_COUNT; i++) {
        assertEquals(cache.get(getKey(i)).getObjectValue(), getValue(i + 1));
      }
      // All the gets on both the clients should be local as the pinned entries would have been faulted.
      Assert.assertEquals(ELEMENT_COUNT, cache.getStatistics().getInMemoryHits());
      Assert.assertEquals(0, cache.getStatistics().getInMemoryMisses());
      Assert.assertEquals(0, cache.getStatistics().getOnDiskHits());
    }

    private Object getKey(int i) {
      return String.valueOf(i);
    }

    private Object getValue(int i) {
      return i;
    }

  }
}
