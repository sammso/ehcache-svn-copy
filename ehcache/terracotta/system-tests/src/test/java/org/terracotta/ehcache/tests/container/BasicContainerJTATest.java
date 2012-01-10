/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.ehcache.tests.container;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.tc.test.AppServerInfo;
import com.tc.test.server.appserver.deployment.AbstractStandaloneTwoServerDeploymentTest;
import com.tc.test.server.appserver.deployment.DeploymentBuilder;
import com.tc.test.server.appserver.deployment.WebApplicationServer;

import junit.framework.Test;

public class BasicContainerJTATest extends AbstractStandaloneTwoServerDeploymentTest {

  private static final String CONTEXT = "BasicContainerJTATest";

  public BasicContainerJTATest() {
    if(appServerInfo().getId() == AppServerInfo.JETTY ||
            appServerInfo().getId() == AppServerInfo.TOMCAT ||
            appServerInfo().getId() == AppServerInfo.WEBSPHERE) {
      // Jetty and Tomcat have no TM and we know the Websphere one is not compatible 
      disableTest();
    }
  }
  
  public static Test suite() {
    return new BasicContainerJTATestSetup();
  }

  public void testBasics() throws Exception {
    System.out.println("Running test");
    WebConversation conversation = new WebConversation();

    // do insert on server0
    WebResponse response1 = request(server0, "cmd=insert", conversation);
    assertEquals("OK", response1.getText().trim());

    // do query on server1
    response1 = request(server1, "cmd=query", conversation);
    assertEquals("OK", response1.getText().trim());
    System.out.println("Test finished");
  }

  private WebResponse request(WebApplicationServer server, String params, WebConversation con) throws Exception {
    return server.ping("/" + CONTEXT + "/BasicJTATestServlet?" + params, con);
  }

  private static class BasicContainerJTATestSetup extends AbstractStandaloneContainerJTATestSetup {

    public BasicContainerJTATestSetup() {
      super(BasicContainerJTATest.class, "basic-xa-appserver-test.xml", CONTEXT);
    }

    @Override
    protected void configureWar(DeploymentBuilder builder) {
      super.configureWar(builder);
      builder.addServlet("BasicTestJTAServlet", "/BasicJTATestServlet/*", BasicJTATestServlet.class, null, false);
    }

  }

}
