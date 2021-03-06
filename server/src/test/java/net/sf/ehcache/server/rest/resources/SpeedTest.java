package net.sf.ehcache.server.rest.resources;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import net.sf.ehcache.Status;
import net.sf.ehcache.server.util.WebTestUtil;
import net.sf.ehcache.server.util.HttpUtil;
import net.sf.ehcache.server.util.StopWatch;
import net.sf.ehcache.util.MemoryEfficientByteArrayOutputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;


/**
 * @author Greg Luck
 * @version $Id: SpeedTest.java 796 2008-10-09 02:39:03Z gregluck $
 */
public class SpeedTest extends AbstractRestTest {

    private static final Logger LOG = LoggerFactory.getLogger(SpeedTest.class);

    /**
     * Make sure there is something in there
     */
    @BeforeClass
    public static void setUp() throws IOException, ParserConfigurationException, SAXException {
        Status somethingThatIsSerializable = Status.STATUS_ALIVE;
        byte[] serializedForm = MemoryEfficientByteArrayOutputStream.serialize(somethingThatIsSerializable).getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedForm);
        int status = HttpUtil.put("http://localhost:9090/ehcache/rest/sampleCache2/1", "application/x-java-serialized-object",
                byteArrayInputStream);
        assertEquals(201, status);

        HttpURLConnection urlConnection = HttpUtil.get("http://localhost:9090/ehcache/rest/sampleCache2/1");
        assertEquals(200, urlConnection.getResponseCode());
    }


    /**
     * Time to get 200 Cached Pages
     * StopWatch time: 947ms
     */
//    @Test
//    public void testSpeedHttpClientNotCached() throws IOException {
//        StopWatch stopWatch = new StopWatch();
//        String url = "http://localhost:9090/Login.jsp";
//        HttpClient httpClient = new HttpClient();
//        HttpMethod httpMethod = new GetMethod(url);
//        stopWatch.getElapsedTime();
//        for (int i = 0; i < 200; i++) {
//            httpClient.executeMethod(httpMethod);
//            httpMethod.getResponseBodyAsStream();
//        }
//        long time = stopWatch.getElapsedTime();
//        LOG.info("Time for 200 uncached page requests: " + time);
//    }

    /**
     * Latency 35 - 42ms
     */
    @Test
    public void testSpeedHttpClient() throws IOException, SAXException, ParserConfigurationException {
        StopWatch stopWatch = new StopWatch();
        String url = "http://localhost:9090/ehcache/rest/sampleCache2/1";
        HttpClient httpClient = new HttpClient();
        HttpMethod httpMethod = new GetMethod(url);
        stopWatch.getElapsedTime();
        for (int i = 0; i < 1000; i++) {
            httpClient.executeMethod(httpMethod);
            httpMethod.getResponseBodyAsStream();
        }
        long time = stopWatch.getElapsedTime();
        LOG.info("Time for 1000 cache requests: " + time + ". Latency " + 1000f / time + "ms");

    }

    /**
     * Latency .97ms
     */
    @Test
    public void testSpeedUrlConnection() throws IOException, SAXException, ParserConfigurationException {
        StopWatch stopWatch = new StopWatch();
        for (int i = 0; i < 1000; i++) {
            HttpURLConnection urlConnection = HttpUtil.get("http://localhost:9090/ehcache/rest/sampleCache2/1");
            assertEquals(200, urlConnection.getResponseCode());
        }
        long time = stopWatch.getElapsedTime();
        LOG.info("Time for 1000 cache requests: " + time + ". Latency " + 1000f / time + "ms");

    }

    /**
     * 1ms latency
     */
    @Test
    public void testSpeedNoDom() throws Exception {

        StopWatch stopWatch = new StopWatch();
        final WebConversation conversation = WebTestUtil.createWebConversation(true);

        String requestUrl = "http://localhost:9090/ehcache/rest/sampleCache2/1";
        stopWatch.getElapsedTime();
        for (int i = 0; i < 1000; i++) {
            WebResponse response = conversation.getResponse(requestUrl);
            response.getText().indexOf("timestamp");
        }
        long time = stopWatch.getElapsedTime();
        LOG.debug("Time for 1000 cache requests: " + time + ". Latency " + 1000f / time + "ms");

    }


    @Test
    public void testConcurrentRequests() throws Exception {

        final List<Callable<?>> executables = new ArrayList<Callable<?>>();
        for (int i = 0; i < 40; i++) {
          executables.add(new Callable<Void>() {
                public Void call() throws Exception {
                    testSpeedNoDom();
                    return null;
                }
            });
        }
        WebTestUtil.runThreads(executables);
    }



    /**
     * Memcached 1.2.1 with memcache Java lib 1.5.1
     * 10000 sets: 3396ms
     * 10000 gets: 3551ms
     * 10000 getMulti: 2132ms
     * 10000 deletes: 2065ms
     * <p/>
     * Ehcache 0.9 with Ehcache 2.0.0 and Jetty
     * INFO: 10000 puts: 2961ms
     * INFO: 10000 gets: 3841ms
     * INFO: 10000 deletes: 2685ms
     * <p/>
     * Ehcache 0.9 with Ehcache 2.0.0 and GFV3 (ehcache-standalone)
     * INFO: 10000 puts: 3784ms
     * INFO: 10000 gets: 3866ms
     * INFO: 10000 deletes: 2752ms
     */
    @Test
    public void testMemCacheBench() throws Exception {

        //warm up Java Web Container, which Memcache does not need, as it is C based
        testConcurrentRequests();
        Thread.sleep(2000);


        int cacheOperations = 10000;
        String cacheUrl = "http://localhost:9090/ehcache/rest/sampleCache1";
        String mediaType = "text/plain";
        String keyBase = "testKey";
        String object = "This is a test of an object blah blah es, serialization does not seem to slow things down so much.  The gzip compression is horrible horrible performance, so we only use it for very large objects.  I have not done any heavy benchmarking recently";
        byte[] objectAsBytes = object.getBytes();

        StopWatch stopWatch = new StopWatch();
        for (int i = 0; i < cacheOperations; i++) {
            String keyUrl = new StringBuffer(cacheUrl).append('/').append(keyBase).append(i).toString();
            assertEquals(201, HttpUtil.put(keyUrl, mediaType, new ByteArrayInputStream(objectAsBytes)));
        }
        LOG.info(cacheOperations + " puts: " + stopWatch.getElapsedTime() + "ms");


        stopWatch = new StopWatch();


        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.connection.stalecheck", false);
        for (int i = 0; i < cacheOperations; i++) {
            String url = new StringBuffer(cacheUrl).append('/').append(keyBase).append(i).toString();
            HttpMethod httpMethod = new GetMethod(url);
            httpMethod.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            httpClient.executeMethod(httpMethod);
        }
        LOG.info(cacheOperations + " gets: " + stopWatch.getElapsedTime() + "ms");

        stopWatch = new StopWatch();

        httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.connection.stalecheck", false);
        for (int i = 0; i < cacheOperations; i++) {
            String url = new StringBuffer(cacheUrl).append('/').append(keyBase).append(i).toString();
            HttpMethod httpMethod = new DeleteMethod(url);
            httpMethod.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            httpClient.executeMethod(httpMethod);
        }
        LOG.info(cacheOperations + " deletes: " + stopWatch.getElapsedTime() + "ms");
    }

    /**
     * Manual stability test
     */
//    @Test
    public void testStability() throws Exception {
        while (true) {
            testMemCacheBench();
            testConcurrentRequests();
            testSpeedNoDom();
            testSpeedHttpClient();
            testSpeedUrlConnection();

        }

    }
}
