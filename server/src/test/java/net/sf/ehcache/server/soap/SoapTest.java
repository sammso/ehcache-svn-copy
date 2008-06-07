package net.sf.ehcache.server.soap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import javax.xml.ws.Endpoint;


/**
 * Tests the Soap server
 * @author Greg Luck
 * @version $Id$
 */
public class SoapTest {
    private Object implementor;
    private String address;
    private WebServiceThread webServiceThread;


    @Test
    public void testEhcache() {


    }


    @BeforeClass
    public void startService() {
        implementor = new Ehcache();
        address = "http://localhost:9000/temp";

        webServiceThread = new WebServiceThread();
        webServiceThread.start();
        assertTrue(webServiceThread.isAlive());


    }

    @AfterClass
    public void stopService() {
        webServiceThread.interrupt();
    }


    /**
     * Used to initialise the debugger and run its monitoring in another thread so we can keep doing stuff
     */
    class WebServiceThread extends Thread {

        /**
         * If this thread was constructed using a separate
         * <code>Runnable</code> run object, then that
         * <code>Runnable</code> object's <code>run</code> method is called;
         * otherwise, this method does nothing and returns.
         * <p/>
         * Subclasses of <code>Thread</code> should override this method.
         *
         * @see Thread#start()
         * @see Thread#stop()
         * @see Thread#Thread(ThreadGroup,
         *      Runnable, String)
         * @see Runnable#run()
         */
        public void run() {
            try {
                Endpoint.publish(address, implementor);
            } catch (Throwable e) {
                fail(e.getMessage());
            }
        }
    }

}
