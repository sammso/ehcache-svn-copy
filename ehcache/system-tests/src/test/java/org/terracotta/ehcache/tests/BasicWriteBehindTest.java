/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.ehcache.tests;

import com.tc.test.config.model.TestConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicWriteBehindTest extends AbstractCacheTestBase {
  private int totalWriteCount  = 0;
  private int totalDeleteCount = 0;

  public BasicWriteBehindTest(TestConfig testConfig) {
    super("basic-writebehind-test.xml", testConfig, WriteBehindClient1.class, WriteBehindClient2.class);
    testConfig.getClientConfig().setParallelClients(false);
  }

  @Override
  protected void postClientVerification() {
    System.out.println("[Clients processed a total of " + totalWriteCount + " writes]");
    if (totalWriteCount < 1001 || totalWriteCount > 1900) { throw new AssertionError(totalWriteCount); }

    System.out.println("[Clients processed a total of " + totalDeleteCount + " deletes]");
    if (totalDeleteCount < 101 || totalDeleteCount > 190) { throw new AssertionError(totalDeleteCount); }
  }

  @Override
  protected void evaluateClientOutput(String clientName, int exitCode, File output) throws Throwable {
    super.evaluateClientOutput(clientName, exitCode, output);

    FileReader fr = null;
    BufferedReader reader = null;
    StringBuilder strBuilder = new StringBuilder();
    try {
      fr = new FileReader(output);
      reader = new BufferedReader(fr);
      String st = "";
      while ((st = reader.readLine()) != null) {
        strBuilder.append(st);
      }
    } catch (Exception e) {
      throw new AssertionError(e);
    } finally {
      try {
        fr.close();
        reader.close();
      } catch (Exception e) {
        //
      }
    }

    // Detect the number of writes that have happened
    int writeCount = detectLargestCount(strBuilder.toString(),
                                        Pattern.compile("\\[WriteBehindCacheWriter written (\\d+) for " + clientName
                                                        + "\\]"));
    totalWriteCount += writeCount;
    System.out.println("[" + clientName + " processed " + writeCount + " writes]");

    // Detect the number of deletes that have happened
    int deleteCount = detectLargestCount(strBuilder.toString(),
                                         Pattern.compile("\\[WriteBehindCacheWriter deleted (\\d+) for " + clientName
                                                         + "\\]"));
    totalDeleteCount += deleteCount;
    System.out.println("[" + clientName + " processed " + deleteCount + " deletes]");
  }

  private int detectLargestCount(String clientOutput, Pattern pattern) {
    Matcher matcher = pattern.matcher(clientOutput);
    int count = 0;
    while (matcher.find()) {
      int parsedCount = Integer.parseInt(matcher.group(1));
      if (parsedCount > count) {
        count = parsedCount;
      }
    }
    return count;
  }
}