
package net.sf.ehcache.server.soap.jaxws;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.1-02/02/2007 03:56 AM(vivekp)-FCS
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "EhcacheWebServiceEndpointService", targetNamespace = "http://soap.server.ehcache.sf.net/", wsdlLocation = "http://localhost:9090/ehcache/soap/EhcacheWebServiceEndpoint?wsdl")
public class EhcacheWebServiceEndpointService
    extends Service
{

    private final static URL EHCACHEWEBSERVICEENDPOINTSERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("http://localhost:9090/ehcache/soap/EhcacheWebServiceEndpoint?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        EHCACHEWEBSERVICEENDPOINTSERVICE_WSDL_LOCATION = url;
    }

    public EhcacheWebServiceEndpointService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public EhcacheWebServiceEndpointService() {
        super(EHCACHEWEBSERVICEENDPOINTSERVICE_WSDL_LOCATION, new QName("http://soap.server.ehcache.sf.net/", "EhcacheWebServiceEndpointService"));
    }

    /**
     * 
     * @return
     *     returns EhcacheWebServiceEndpoint
     */
    @WebEndpoint(name = "EhcacheWebServiceEndpointPort")
    public EhcacheWebServiceEndpoint getEhcacheWebServiceEndpointPort() {
        return (EhcacheWebServiceEndpoint)super.getPort(new QName("http://soap.server.ehcache.sf.net/", "EhcacheWebServiceEndpointPort"), EhcacheWebServiceEndpoint.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns EhcacheWebServiceEndpoint
     */
    @WebEndpoint(name = "EhcacheWebServiceEndpointPort")
    public EhcacheWebServiceEndpoint getEhcacheWebServiceEndpointPort(WebServiceFeature... features) {
        return (EhcacheWebServiceEndpoint)super.getPort(new QName("http://soap.server.ehcache.sf.net/", "EhcacheWebServiceEndpointPort"), EhcacheWebServiceEndpoint.class, features);
    }

}
