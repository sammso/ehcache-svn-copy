package net.sf.ehcache.server.soap.jaxws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.util.List;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.1-02/02/2007 03:56 AM(vivekp)-FCS
 * Generated source version: 2.1
 */
@WebService(name = "EhcacheWebServiceEndpoint", targetNamespace = "http://soap.server.ehcache.sf.net/")
@XmlSeeAlso({
        ObjectFactory.class
})
public interface EhcacheWebServiceEndpoint {


    /**
     * @param arg1
     * @param arg0
     * @throws CacheException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "loadAll", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.LoadAll")
    @ResponseWrapper(localName = "loadAllResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.LoadAllResponse")
    public void loadAll(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0,
            @WebParam(name = "arg1", targetNamespace = "")
            List<String> arg1)
            throws CacheException_Exception
            ;

    /**
     * @return returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "ping", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.Ping")
    @ResponseWrapper(localName = "pingResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.PingResponse")
    public String ping();

    /**
     * @param arg0
     * @return returns net.sf.ehcache.server.soap.jaxws.Cache
     * @throws CacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getCache", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetCache")
    @ResponseWrapper(localName = "getCacheResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetCacheResponse")
    public Cache getCache(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0)
            throws CacheException_Exception
            ;

    /**
     * @param arg0
     * @throws ObjectExistsException_Exception
     *
     * @throws IllegalStateException_Exception
     *
     * @throws CacheException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "addCache", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.AddCache")
    @ResponseWrapper(localName = "addCacheResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.AddCacheResponse")
    public void addCache(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0)
            throws CacheException_Exception, IllegalStateException_Exception, ObjectExistsException_Exception
            ;

    /**
     * @param arg0
     * @throws IllegalStateException_Exception
     *
     */
    @WebMethod
    @RequestWrapper(localName = "removeCache", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.RemoveCache")
    @ResponseWrapper(localName = "removeCacheResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.RemoveCacheResponse")
    public void removeCache(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0)
            throws IllegalStateException_Exception
            ;

    /**
     * @return returns java.util.List<java.lang.String>
     * @throws IllegalStateException_Exception
     *
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "cacheNames", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.CacheNames")
    @ResponseWrapper(localName = "cacheNamesResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.CacheNamesResponse")
    public List<String> cacheNames()
            throws IllegalStateException_Exception
            ;

    /**
     * @param arg0
     * @return returns java.util.List<java.lang.Object>
     * @throws IllegalStateException_Exception
     *
     * @throws CacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getKeysWithExpiryCheck", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetKeysWithExpiryCheck")
    @ResponseWrapper(localName = "getKeysWithExpiryCheckResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetKeysWithExpiryCheckResponse")
    public List<Object> getKeysWithExpiryCheck(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0)
            throws CacheException_Exception, IllegalStateException_Exception
            ;

    /**
     * @param arg0
     * @return returns java.util.List<java.lang.Object>
     * @throws IllegalStateException_Exception
     *
     * @throws CacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getKeysNoDuplicateCheck", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetKeysNoDuplicateCheck")
    @ResponseWrapper(localName = "getKeysNoDuplicateCheckResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetKeysNoDuplicateCheckResponse")
    public List<Object> getKeysNoDuplicateCheck(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0)
            throws CacheException_Exception, IllegalStateException_Exception
            ;

    /**
     * @param arg1
     * @param arg0
     * @return returns net.sf.ehcache.server.soap.jaxws.Element
     * @throws IllegalStateException_Exception
     *
     * @throws CacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getQuiet", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetQuiet")
    @ResponseWrapper(localName = "getQuietResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetQuietResponse")
    public Element getQuiet(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0,
            @WebParam(name = "arg1", targetNamespace = "")
            Object arg1)
            throws CacheException_Exception, IllegalStateException_Exception
            ;

    /**
     * @param arg1
     * @param arg0
     * @throws IllegalArgumentException_Exception
     *
     * @throws IllegalStateException_Exception
     *
     * @throws CacheException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "putQuiet", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.PutQuiet")
    @ResponseWrapper(localName = "putQuietResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.PutQuietResponse")
    public void putQuiet(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0,
            @WebParam(name = "arg1", targetNamespace = "")
            Element arg1)
            throws CacheException_Exception, IllegalArgumentException_Exception, IllegalStateException_Exception
            ;

    /**
     * @param arg1
     * @param arg0
     * @return returns boolean
     * @throws IllegalStateException_Exception
     *
     * @throws CacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "removeQuiet", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.RemoveQuiet")
    @ResponseWrapper(localName = "removeQuietResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.RemoveQuietResponse")
    public boolean removeQuiet(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0,
            @WebParam(name = "arg1", targetNamespace = "")
            String arg1)
            throws CacheException_Exception, IllegalStateException_Exception
            ;

    /**
     * @param arg0
     * @return returns net.sf.ehcache.server.soap.jaxws.Statistics
     * @throws IllegalStateException_Exception
     *
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getStatistics", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetStatistics")
    @ResponseWrapper(localName = "getStatisticsResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetStatisticsResponse")
    public Statistics getStatistics(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0)
            throws IllegalStateException_Exception
            ;

    /**
     * @param arg1
     * @param arg0
     * @return returns net.sf.ehcache.server.soap.jaxws.HashMap
     * @throws CacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getAllWithLoader", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetAllWithLoader")
    @ResponseWrapper(localName = "getAllWithLoaderResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetAllWithLoaderResponse")
    public HashMap getAllWithLoader(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0,
            @WebParam(name = "arg1", targetNamespace = "")
            List<Object> arg1)
            throws CacheException_Exception
            ;

    /**
     * @param arg1
     * @param arg0
     * @return returns net.sf.ehcache.server.soap.jaxws.Element
     * @throws NoSuchCacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getWithLoader", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetWithLoader")
    @ResponseWrapper(localName = "getWithLoaderResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetWithLoaderResponse")
    public Element getWithLoader(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0,
            @WebParam(name = "arg1", targetNamespace = "")
            String arg1)
            throws NoSuchCacheException_Exception
            ;

    /**
     * @param arg1
     * @param arg0
     * @return returns net.sf.ehcache.server.soap.jaxws.Element
     * @throws NoSuchCacheException_Exception
     * @throws IllegalStateException_Exception
     *
     * @throws CacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "get", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.Get")
    @ResponseWrapper(localName = "getResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetResponse")
    public Element get(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0,
            @WebParam(name = "arg1", targetNamespace = "")
            String arg1)
            throws CacheException_Exception, IllegalStateException_Exception, NoSuchCacheException_Exception
            ;

    /**
     * @param arg1
     * @param arg0
     * @throws NoSuchCacheException_Exception
     * @throws CacheException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "put", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.Put")
    @ResponseWrapper(localName = "putResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.PutResponse")
    public void put(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0,
            @WebParam(name = "arg1", targetNamespace = "")
            Element arg1)
            throws CacheException_Exception, NoSuchCacheException_Exception
            ;

    /**
     * @param arg1
     * @param arg0
     * @return returns boolean
     * @throws CacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "remove", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.Remove")
    @ResponseWrapper(localName = "removeResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.RemoveResponse")
    public boolean remove(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0,
            @WebParam(name = "arg1", targetNamespace = "")
            String arg1)
            throws CacheException_Exception
            ;

    /**
     * @param arg1
     * @param arg0
     * @throws CacheException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "load", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.Load")
    @ResponseWrapper(localName = "loadResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.LoadResponse")
    public void load(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0,
            @WebParam(name = "arg1", targetNamespace = "")
            String arg1)
            throws CacheException_Exception
            ;

    /**
     * @param arg0
     * @return returns int
     * @throws IllegalStateException_Exception
     *
     * @throws CacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getSize", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetSize")
    @ResponseWrapper(localName = "getSizeResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetSizeResponse")
    public int getSize(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0)
            throws CacheException_Exception, IllegalStateException_Exception
            ;

    /**
     * @param arg0
     * @throws IllegalStateException_Exception
     *
     * @throws CacheException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "removeAll", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.RemoveAll")
    @ResponseWrapper(localName = "removeAllResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.RemoveAllResponse")
    public void removeAll(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0)
            throws CacheException_Exception, IllegalStateException_Exception
            ;

    /**
     * @param arg0
     * @return returns java.util.List<java.lang.Object>
     * @throws IllegalStateException_Exception
     *
     * @throws CacheException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getKeys", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetKeys")
    @ResponseWrapper(localName = "getKeysResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetKeysResponse")
    public List<Object> getKeys(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0)
            throws CacheException_Exception, IllegalStateException_Exception
            ;

    /**
     * @param arg0
     * @return returns net.sf.ehcache.server.soap.jaxws.Status
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getStatus", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetStatus")
    @ResponseWrapper(localName = "getStatusResponse", targetNamespace = "http://soap.server.ehcache.sf.net/", className = "net.sf.ehcache.server.soap.jaxws.GetStatusResponse")
    public Status getStatus(
            @WebParam(name = "arg0", targetNamespace = "")
            String arg0);

}
