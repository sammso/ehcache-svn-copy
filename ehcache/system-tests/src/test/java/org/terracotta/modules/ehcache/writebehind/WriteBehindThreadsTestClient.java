/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.modules.ehcache.writebehind;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.writer.writebehind.WriteBehindManager;

import org.terracotta.ehcache.tests.AbstractWriteBehindClient;
import org.terracotta.ehcache.tests.WriteBehindCacheWriter;
import org.terracotta.toolkit.Toolkit;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

public class WriteBehindThreadsTestClient extends AbstractWriteBehindClient {

  public WriteBehindThreadsTestClient(String[] args) {
    super(args);

  }

  @Override
  public void doTest() throws Throwable {
    ThreadMXBean tbean;
    tbean = ManagementFactory.getThreadMXBean();

    int nonDaemonThreadCountA = tbean.getThreadCount() - tbean.getDaemonThreadCount();
    int daemonThreadCountA = tbean.getDaemonThreadCount();
    long[] listA = tbean.getAllThreadIds();
    for (int loopNumber = 0; loopNumber < 4; loopNumber++) {
      cacheManager = new CacheManager(WriteBehindThreadsTestClient.class.getResourceAsStream("/ehcache-config.xml"));
      int daemonThreadCountB = tbean.getDaemonThreadCount();
      Assert.assertTrue(daemonThreadCountA < daemonThreadCountB);
      Cache cache = cacheManager.getCache("test");
      cache.registerCacheWriter(new WriteBehindCacheWriter(this));
      Assert.assertNotNull(cache.getWriterManager());
      Assert.assertTrue(cache.getWriterManager() instanceof WriteBehindManager);
      for (int i = 0; i < 10; i++) {
        cache.putWithWriter(new Element(i, i));
      }
      while (getWriteCount() < 10) {
        Thread.sleep(200);
      }
      resetWriteCount();
      cacheManager.shutdown();
      System.out.println("done with iteration "+loopNumber);
    }
    TimeUnit.MINUTES.sleep(1L);
    long[] listC = tbean.getAllThreadIds();
    int daemonThreadCountC = tbean.getDaemonThreadCount();
    int nonDaemonThreadCountC = tbean.getThreadCount() - tbean.getDaemonThreadCount();
    List<Long> listIntA = new ArrayList<Long>();
    for (long listAItrator : listA) {
      listIntA.add(new Long(listAItrator));
    }
    List<Long> listIntC = new ArrayList<Long>();
    for (long listAItrator : listC) {
      listIntC.add(new Long(listAItrator));
    }
    listIntC.removeAll(listIntA);
    Set<String> knownThreads = getKnownThreads();
    int skipThreadCount = 0;
    StringBuffer threadsInfo = new StringBuffer();
    System.out.println("\n\n" + listIntC.size() + " Start Printing Stack Trace--------------------");
    for (int i = 0; i < listIntC.size(); i++) {
      ThreadInfo tinfo = tbean.getThreadInfo(listIntC.get(i));
      if (knownThreads.contains(tinfo.getThreadName().trim())) {
        ++skipThreadCount;
        continue;
      }
      String info = "Thread name: " + tinfo.getThreadName() + " | " + tinfo.getThreadId();
      System.out.println(info);
      threadsInfo.append(info);
      for (StackTraceElement e : tinfo.getStackTrace()) {
        threadsInfo.append(e + "\n\n");
      }
    }
    System.out.println(threadsInfo + "\n\nEnd-----------------------\n\n");
    Assert.assertEquals(threadsInfo.toString(), daemonThreadCountA, daemonThreadCountC - skipThreadCount);
    Assert.assertEquals(nonDaemonThreadCountA, nonDaemonThreadCountC);
  }

  private Set<String> getKnownThreads() {
    Set<String> skipThreads = new HashSet<String>();
    skipThreads.add("Attach Listener");

    return skipThreads;
  }

  @Override
  protected void runTest(Cache cache, Toolkit toolkit) throws Throwable {
    // NO OP

  }

  @Override
  public long getSleepBetweenWrites() {
    return 0;
  }

  @Override
  public long getSleepBetweenDeletes() {
    return 0;
  }

}
