/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.modules.ehcache.l1bm;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.TerracottaConfiguration;
import net.sf.ehcache.config.TerracottaConfiguration.Consistency;

import org.terracotta.ehcache.tests.ClientBase;
import org.terracotta.toolkit.Toolkit;
import org.terracotta.toolkit.concurrent.ToolkitBarrier;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;

import junit.framework.Assert;

public class L1BMOnHeapBasicSanityTestApp extends ClientBase {
  private final ToolkitBarrier barrier;

  public L1BMOnHeapBasicSanityTestApp(String[] args) {
    super(args);
    this.barrier = getClusteringToolkit().getBarrier("L1BMOnHeapSanityTest", L1BMOnHeapSanityTest.NODE_COUNT);
  }

  public static void main(String[] args) {
    new L1BMOnHeapBasicSanityTestApp(args).run();
  }

  @Override
  protected void runTest(Cache cache, Toolkit clusteringToolkit) throws Throwable {

    Cache dcv2EventualSerializationWithStats = createCache("dcv2EventualSerializationWithStats", cacheManager, Consistency.EVENTUAL);
    testL1BigMemorySanity(dcv2EventualSerializationWithStats, true);

    Cache dcv2EventualSerializationWithoutStats = createCache("dcv2EventualSerializationWithoutStats", cacheManager, Consistency.EVENTUAL);
    testL1BigMemorySanity(dcv2EventualSerializationWithoutStats, true);

    Cache dcv2StrongSerializationWithStats = createCache("dcv2StrongSerializationWithStats", cacheManager, Consistency.STRONG);
    testL1BigMemorySanity(dcv2StrongSerializationWithStats, false);

    Cache dcv2StrongWithoutStats = createCache("dcv2StrongWithoutStats", cacheManager, Consistency.STRONG);
    testL1BigMemorySanity(dcv2StrongWithoutStats, false);

  }

  private void testL1BigMemorySanity(Cache cache, boolean shouldWait) throws InterruptedException,
      BrokenBarrierException {
    int index = barrier.await();
    int numOfElements = 2000;
    if (index == 0) {
      for (int i = 0; i < numOfElements; i++) {
        cache.put(new Element("key" + i, "val" + i));
      }
      System.out.println("XXXXX done with putting " + cache.getSize() + " entries");
    }
    barrier.await();
    if (shouldWait) {
      while (cache.getSize() != numOfElements) {
        Thread.sleep(1000);
      }
    }
    Assert.assertEquals(numOfElements, cache.getSize());
    System.out.println("XXXXXX client " + index + " cache size: " + cache.getSize() + " local: "
                       + cache.getStatistics().getLocalHeapSize());
    if (index == 0) {
      Assert.assertTrue(cache.getStatistics().getLocalHeapSize() > 0);
    } else {
      Assert.assertEquals(0, cache.getStatistics().getLocalHeapSize());
    }

    barrier.await();

    System.out.println("XXXXXX testing get");
    for (int i = 0; i < numOfElements; i++) {
      Assert.assertNotNull("value for key" + i + " is null", cache.get("key" + i));
    }
    Assert.assertTrue(cache.getStatistics().getLocalHeapSize() > 0);

    barrier.await();
    System.out.println("XXXX done with basic get, now removing random entries...");
    Set removedKeySet = new HashSet<String>();
    for (int i = 0; i < numOfElements; i++) {
      if (i % 10 == 0) {
        removedKeySet.add("key" + i);
      }
    }

    if (index == 0) {
      cache.removeAll(removedKeySet);
    }

    barrier.await();
    if (shouldWait) {
      while (cache.getSize() != numOfElements - removedKeySet.size()) {
        Thread.sleep(1000);
      }
    }
    System.out.println("XXXXX removed " + removedKeySet.size() + " elemets. Cache size: " + cache.getSize());

    System.out.println("XXXX testing get after remove.");
    for (int i = 0; i < numOfElements; i++) {
      String key = "key" + i;
      if (removedKeySet.contains(key)) {
        Assert.assertNull("value for " + key + " is not null", cache.get(key));
      } else {
        Assert.assertNotNull("value for " + key + " is null", cache.get(key));
      }
    }

    System.out.println("XXXXXX done with " + cache.getName());
  }

  private Cache createCache(String cacheName, CacheManager cm,
                             Consistency consistency) {
    CacheConfiguration cacheConfiguration = new CacheConfiguration();
    cacheConfiguration.setName(cacheName);
    cacheConfiguration.setMaxBytesLocalHeap(409600L);

    TerracottaConfiguration tcConfiguration = new TerracottaConfiguration();
    tcConfiguration.setConsistency(consistency);
    cacheConfiguration.addTerracotta(tcConfiguration);

    Cache cache = new Cache(cacheConfiguration);
    cm.addCache(cache);
    return cache;
  }
}
