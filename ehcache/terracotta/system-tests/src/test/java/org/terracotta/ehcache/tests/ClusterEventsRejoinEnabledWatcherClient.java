package org.terracotta.ehcache.tests;

import net.sf.ehcache.Cache;
import net.sf.ehcache.cluster.CacheCluster;
import net.sf.ehcache.cluster.ClusterScheme;

import org.terracotta.api.ClusteringToolkit;
import org.terracotta.coordination.Barrier;

public class ClusterEventsRejoinEnabledWatcherClient extends ClientBase {

  public ClusterEventsRejoinEnabledWatcherClient(String[] args) {
    super("test", args);
  }

  public static void main(String[] args) {
    new ClusterEventsRejoinEnabledWatcherClient(args).run();
  }

  @Override
  protected void test(Cache cache, ClusteringToolkit toolkit) throws Throwable {
    final Barrier barrier = toolkit.getBarrier("ClusterEventsWatcherClient", 2);
    barrier.await();

    CacheCluster cluster = cache.getCacheManager().getCluster(ClusterScheme.TERRACOTTA);
    assertTrue(cluster != null);
    assertTrue(cluster.getScheme().equals(ClusterScheme.TERRACOTTA));

    try {
      final long end = System.currentTimeMillis() + 20000L;
      while (System.currentTimeMillis() < end) {
        /*
         * Expect 5 clients, the original custom spawning client and then two for each ClusterEventsWatchClient: one for
         * the toolkit barrier and one for the Ehcache cache manager.
         */
        int count = cluster.getNodes().size();
        if (count == 5) return;
        if (count > 5) throw new AssertionError(count + " nodes observed!");
        System.err.println("nodes.size() = " + count);
        Thread.sleep(1000L);
      }

      throw new AssertionError("expected node count never reached");
    } finally {
      barrier.await();
    }
  }
}
