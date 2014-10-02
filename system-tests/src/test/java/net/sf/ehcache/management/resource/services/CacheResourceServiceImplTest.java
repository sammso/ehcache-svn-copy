package net.sf.ehcache.management.resource.services;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ManagementRESTServiceConfiguration;
import net.sf.ehcache.config.TerracottaClientConfiguration;
import net.sf.ehcache.config.TerracottaConfiguration;
import net.sf.ehcache.management.resource.CacheEntity;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Anthony Dahanne
 * The aim of this test is to check via HTTP that the ehcache standalone agent /tc-management-api/agents/cacheManagers/caches endpoint
 * works fine
 */
public class CacheResourceServiceImplTest extends ResourceServiceImplITHelper {

  protected static final String EXPECTED_RESOURCE_LOCATION = "{baseUrl}/tc-management-api/agents{agentIds}/cacheManagers{cmIds}/caches{cacheIds}";

  @BeforeClass
  public static void setUpCluster() throws Exception {
    setUpCluster(CacheResourceServiceImplTest.class);
  }

  @Test
  /**
   * - GET the list of caches
   *
   * @throws Exception
   */
  public void getCachesTest() throws Exception {
    /*
 [
     {
        "version": null,
        "agentId": "embedded",
        "name": "testCache2",
        "cacheManagerName": "testCacheManagerProgrammatic",
        "attributes": {
            "Searchable": false,
            "LocalHeapCountBased": false,
            "LoggingEnabled": false,
            "MaxBytesLocalHeap": 0,
            "NodeBulkLoadEnabled": false,
            "MaxBytesLocalOffHeapAsString": "0",
            "WriterMaxQueueSize": 0,
            "TerracottaConsistency": "na",
            "WriterConcurrency": 1,
            "OverflowToDisk": true,
            "DiskPersistent": false,
            "MemoryStoreEvictionPolicy": "LRU",
            "TimeToIdleSeconds": 0,
            "DiskExpiryThreadIntervalSeconds": 120,
            "MaxBytesLocalOffHeap": 0,
            "MaxBytesLocalHeapAsString": "0",
            "HasWriteBehindWriter": false,
            "MaxBytesLocalDiskAsString": "0",
            "OverflowToOffHeap": false,
            "MaxEntriesLocalHeap": 0,
            "Transactional": false,
            "PinnedToStore": "na",
            "TerracottaClustered": false,
            "MaxBytesLocalDisk": 0,
            "Enabled": true,
            "MaxElementsOnDisk": 0,
            "Pinned": false,
            "Eternal": false,
            "MaxEntriesInCache": 0,
            "Status": "STATUS_ALIVE",
            "PersistenceStrategy": ""
        }
    },
]
     */


    // I need a cacheManager not clustered
    CacheManager standaloneCacheManager = createStandaloneCacheManagerARC();
    Cache cacheStandalone = standaloneCacheManager.getCache("testCacheStandaloneARC");


    for (int i=0; i<1000 ; i++) {
      cacheStandalone.put(new Element("key" + i, "value" + i));
    }

    String agentsFilter = "";
    String cmsFilter = "";
    String cachesFilter = "";


    expect().contentType(ContentType.JSON)
            .body("get(1).agentId", equalTo("embedded"))
            .body("get(1).name", equalTo("testCacheStandaloneARC"))
            .body("get(1).cacheManagerName", equalTo("testCacheManagerStandaloneARC"))
            .body("get(1).attributes.LocalHeapSizeInBytes", greaterThan(0))
            .body("get(1).attributes.InMemorySize", equalTo(1000))
            .body("get(1).attributes.LocalDiskSize", greaterThan(0))
            .body("get(1).attributes.LocalHeapSize", equalTo(1000))
            .body("get(1).attributes.DiskExpiryThreadIntervalSeconds", equalTo(120))
            .body("get(1).attributes.LocalDiskSizeInBytes", greaterThan(0))
            .body("get(1).attributes.Size", equalTo(1000))
            .body("get(1).attributes.Status", equalTo( "STATUS_ALIVE"))
            .body("get(0).name", equalTo("testCache"))
            .body("size()", is(2))
            .statusCode(200)
            .given()
              .queryParam("show", "LocalHeapSizeInBytes")
              .queryParam("show", "InMemorySize")
              .queryParam("show", "LocalDiskSize")
              .queryParam("show", "LocalHeapSize")
              .queryParam("show", "DiskExpiryThreadIntervalSeconds")
              .queryParam("show", "LocalDiskSizeInBytes")
              .queryParam("show", "Size")
              .queryParam("show", "Status")
            .when().get(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);


    cachesFilter = ";names=testCacheStandaloneARC";
    // we filter to return only the attribute CacheNames, and working only on the testCache2 Cache
    expect().contentType(ContentType.JSON)
            .body("get(0).agentId", equalTo("embedded"))
            .body("get(0).name", equalTo("testCacheStandaloneARC"))
            .body("get(0).cacheManagerName", equalTo("testCacheManagerStandaloneARC"))
            .body("get(0).attributes.Size", equalTo(1000))
            .body("size()",is(1))
            .statusCode(200)
            .given()
              .queryParam("show", "Size")
              .queryParam("show", "PutCount")
            .when().get(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);

    standaloneCacheManager.removeAllCaches();
    standaloneCacheManager.shutdown();

  }

  @Test
  /**
   * - PUT an updated CacheEntity
   *
   * @throws Exception
   */
  public void updateCachesTest__FailWhenNotSpecifyingACache() throws Exception {
    // you have to specify a cache when doing mutation
    CacheEntity cacheManagerEntity = new CacheEntity();
    Map<String,Object> attributes = new HashMap<String, Object>();
    attributes.put("MaxEntriesLocalHeap",20000);
    attributes.put("Enabled", Boolean.FALSE);
    cacheManagerEntity.getAttributes().putAll(attributes);
    String agentsFilter = "";
    String cmsFilter = "";
    String cachesFilter = "";

    expect().statusCode(400)
            .body("details", equalTo(""))
            .body("error", equalTo("No cache specified. Unsafe requests must specify a single cache name."))
            .given()
            .contentType(ContentType.JSON)
            .body(cacheManagerEntity)
            .when().put(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);


    cachesFilter = ";names=testCache";
    expect().statusCode(400)
            .body("details", equalTo(""))
            .body("error", equalTo("No cache manager specified. Unsafe requests must specify a single cache manager name."))
            .given()
            .contentType(ContentType.JSON)
            .body(cacheManagerEntity)
            .when().put(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);

    cmsFilter = ";names=testCacheManager";
    cachesFilter = ";names=boups";
    expect().statusCode(400)
            .body("details", equalTo("Cache not found !"))
            .body("error", equalTo("Failed to create or update cache"))
            .given()
            .contentType(ContentType.JSON)
            .body(cacheManagerEntity)
            .when().put(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);

    cmsFilter = ";names=pif";
    cachesFilter = ";names=testCache";
    expect().statusCode(400)
            .body("details", equalTo("CacheManager not found !"))
            .body("error", equalTo("Failed to create or update cache"))
            .given()
            .contentType(ContentType.JSON)
            .body(cacheManagerEntity)
            .when().put(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);

    cmsFilter = "";
    cachesFilter = "";
    // we check nothing has changed
    expect().contentType(ContentType.JSON)
            .body("get(0).agentId", equalTo("embedded"))
            .body("get(0).name", equalTo("testCache"))
            .body("get(0).attributes.MaxEntriesLocalHeap",equalTo(10000) )
            .body("get(0).attributes.Enabled", equalTo(Boolean.TRUE))
            .statusCode(200)
            .when().get(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);
  }

  @Test
  /**
   * - PUT an updated CacheEntity
   *
   * Those are the mutable attributes from the rest agent, followed by Gary's comments
   * ENABLED_ATTR: the user can change directly from the management panel
   * LOGGING_ENABLED: just never had this in the DevConsole and nobody's ever asked for it
   * BULK_LOAD_ENABLED: will probably be adding this with the management panel overhaul
   * MAX_ENTRIES_LOCAL_HEAP: we do support this, but not when you've already specified MAX_BYTES_LOCAL_HEAP
   * MAX_ELEMENTS_ON_DISK: same as above, except you've already specified MAX_BYTES_LOCAL_HEAP
   * MAX_ENTRIES_IN_CACHE: if it's a Terracotta-clustered cache, we support this
   * MAX_BYTES_LOCAL_DISK_STRING
   * MAX_BYTES_LOCAL_HEAP_STRING
   * TIME_TO_IDLE_SEC
   * TIME_TO_LIVE_SEC
   *
   * @throws Exception
   */
  public void updateCachesTest() throws Exception {

    // I need a cacheManager not clustered
    CacheManager standaloneCacheManager = createStandaloneCacheManager();

    // you have to specify a cache when doing mutation
    CacheEntity cacheEntity = new CacheEntity();
    Map<String,Object> attributes = new HashMap<String, Object>();
    attributes.put("MaxEntriesInCache", 30000);
    attributes.put("MaxEntriesLocalHeap",20000);
    attributes.put("LoggingEnabled", Boolean.TRUE);
    attributes.put("MaxElementsOnDisk",40000);
    attributes.put("TimeToIdleSeconds", 20);
    attributes.put("TimeToLiveSeconds", 43);
    attributes.put("Enabled", Boolean.FALSE);


    String agentsFilter = "";
    String cmsFilter = ";names=testCacheManagerStandalone";
    String cachesFilter = ";names=testCacheStandalone";
    cacheEntity.getAttributes().putAll(attributes);
    expect().statusCode(204).log().ifStatusCodeIsEqualTo(400)
            .given()
            .contentType(ContentType.JSON)
            .body(cacheEntity)
            .when().put(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);


    cmsFilter = "";
    // we check the properties were changed
    expect().contentType(ContentType.JSON)
            .body("get(0).agentId", equalTo("embedded"))
            .body("get(0).name", equalTo("testCacheStandalone"))
            .body("get(0).attributes.MaxEntriesInCache", equalTo(30000))
            .body("get(0).attributes.MaxEntriesLocalHeap", equalTo(20000))
            .body("get(0).attributes.LoggingEnabled", equalTo(Boolean.TRUE))
            .body("get(0).attributes.MaxElementsOnDisk", equalTo(40000))
            .body("get(0).attributes.TimeToIdleSeconds", equalTo(20))
            .body("get(0).attributes.TimeToLiveSeconds", equalTo(43))
            .body("get(0).attributes.Enabled", equalTo(Boolean.FALSE))
            .statusCode(200)
            .when().get(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);


    standaloneCacheManager.removeAllCaches();
    standaloneCacheManager.shutdown();
    // I need another cache that does not have set MaxBytesLocalHeap nor MaxBytesLocalDisk
    CacheManager cacheManagerNew = getCacheManagerNew();

    cacheEntity = new CacheEntity();
    attributes = new HashMap<String, Object>();
    attributes.put("MaxBytesLocalDiskAsString", "30M");
    attributes.put("MaxBytesLocalHeapAsString","20M");
    cacheEntity.getAttributes().putAll(attributes);

    cmsFilter = ";names=cacheManagerNew";
    cachesFilter = ";names=CacheNew";

    expect().log().ifStatusCodeIsEqualTo(400)
            .statusCode(204)
            .given()
            .contentType(ContentType.JSON)
            .body(cacheEntity)
            .when().put(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);

    cmsFilter = "";
    cachesFilter = ";names=CacheNew";
    // we check the properties were changed
    expect().contentType(ContentType.JSON)
            .body("get(0).agentId", equalTo("embedded"))
            .body("get(0).name", equalTo("CacheNew"))
            .body("get(0).attributes.MaxBytesLocalDiskAsString", equalTo("30M"))
            .body("get(0).attributes.MaxBytesLocalHeapAsString", equalTo("20M"))
            .statusCode(200)
            .when().get(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);
    cacheManagerNew.removeAllCaches();
    cacheManagerNew.shutdown();


  }

  @Test
  /**
   * - PUT an updated CacheEntity
   *
   * Those are the mutable attributes from the rest agent, followed by Gary's comments
   * ENABLED_ATTR: the user can change directly from the management panel
   * LOGGING_ENABLED: just never had this in the DevConsole and nobody's ever asked for it
   * BULK_LOAD_ENABLED: will probably be adding this with the management panel overhaul
   * MAX_ENTRIES_LOCAL_HEAP: we do support this, but not when you've already specified MAX_BYTES_LOCAL_HEAP
   * MAX_ELEMENTS_ON_DISK: same as above, except you've already specified MAX_BYTES_LOCAL_HEAP
   * MAX_ENTRIES_IN_CACHE: if it's a Terracotta-clustered cache, we support this
   * MAX_BYTES_LOCAL_DISK_STRING
   * MAX_BYTES_LOCAL_HEAP_STRING
   * TIME_TO_IDLE_SEC
   * TIME_TO_LIVE_SEC
   *
   * @throws Exception
   */
  public void updateCachesTest__clustered() throws Exception {


    CacheManager clusteredCacheManager = createClusteredCacheManager();

    try {
      // you have to specify a cache when doing mutation
      CacheEntity cacheEntity = new CacheEntity();
      Map<String, Object> attributes = new HashMap<String, Object>();
      attributes.put("MaxEntriesInCache", 30000);
      attributes.put("MaxEntriesLocalHeap", 20000);
      attributes.put("LoggingEnabled", Boolean.TRUE);
      attributes.put("TimeToIdleSeconds", 20);
      attributes.put("TimeToLiveSeconds", 43);
      attributes.put("NodeBulkLoadEnabled", Boolean.TRUE); //ONLY FOR CLUSTERED !!!
      attributes.put("Enabled", Boolean.FALSE);

      String agentId = getEhCacheAgentId();
      final String agentsFilter = ";ids=" + agentId;
      String cmsFilter = ";names=testCacheManagerClustered";
      String cachesFilter = ";names=testCacheClustered";
      cacheEntity.getAttributes().putAll(attributes);
      expect().statusCode(204).log().ifStatusCodeIsEqualTo(400)
              .given()
              .contentType(ContentType.JSON)
              .body(cacheEntity)
              .when().put(EXPECTED_RESOURCE_LOCATION, CLUSTERED_BASE_URL, agentsFilter, cmsFilter, cachesFilter);


      cmsFilter = "";
      // we check the properties were changed
      expect().contentType(ContentType.JSON)
              .body("get(0).agentId", equalTo(agentId))
              .body("get(0).name", equalTo("testCacheClustered"))
              .body("get(0).attributes.MaxEntriesInCache", equalTo(30000))
              .body("get(0).attributes.MaxEntriesLocalHeap", equalTo(20000))
              .body("get(0).attributes.LoggingEnabled", equalTo(Boolean.TRUE))
              .body("get(0).attributes.NodeBulkLoadEnabled", equalTo(Boolean.TRUE)) //ONLY FOR CLUSTERED !!!
              .body("get(0).attributes.ClusterBulkLoadEnabled", equalTo(Boolean.TRUE)) //ONLY FOR CLUSTERED !!!
              .body("get(0).attributes.TimeToIdleSeconds", equalTo(20))
              .body("get(0).attributes.TimeToLiveSeconds", equalTo(43))
              .body("get(0).attributes.Enabled", equalTo(Boolean.FALSE))
              .statusCode(200)
              .given()
                .queryParam("show", "MaxEntriesInCache")
                .queryParam("show", "MaxEntriesLocalHeap")
                .queryParam("show", "LoggingEnabled")
                .queryParam("show", "NodeBulkLoadEnabled")
                .queryParam("show", "ClusterBulkLoadEnabled")
                .queryParam("show", "TimeToIdleSeconds")
                .queryParam("show", "TimeToLiveSeconds")
                .queryParam("show", "Enabled")
              .when().get(EXPECTED_RESOURCE_LOCATION, CLUSTERED_BASE_URL, agentsFilter, cmsFilter, cachesFilter);
    } finally {
      clusteredCacheManager.shutdown();
    }

  }


  @Test
  /**
   * - PUT an updated CacheEntity, with attributes not allowed
   * only 6 attributes are supported (cf previosu test), the others are forbidden because we do not allow them to be updated
   * @throws Exception
   */
  public void updateCachesTest__FailWhenMutatingForbiddenAttributes() throws Exception {

    CacheEntity cacheManagerEntity = new CacheEntity();
    cacheManagerEntity.setName("superName");
    Map<String,Object> attributes = new HashMap<String, Object>();
    attributes.put("LocalOffHeapSizeInBytes","20000");
    attributes.put("Pinned", Boolean.TRUE);
    cacheManagerEntity.getAttributes().putAll(attributes);

    String agentsFilter = "";
    String cmsFilter = ";names=testCacheManager";
    String cachesFilter = ";names=testCache";

    expect().statusCode(400)
            .body("details", equalTo("You are not allowed to update those attributes : name LocalOffHeapSizeInBytes Pinned . " +
                    "Only TimeToIdleSeconds Enabled MaxBytesLocalDiskAsString MaxBytesLocalHeapAsString MaxElementsOnDisk" +
                    " TimeToLiveSeconds MaxEntriesLocalHeap LoggingEnabled NodeBulkLoadEnabled MaxEntriesInCache can be updated for a Cache."))
            .body("error", equalTo("Failed to create or update cache"))
            .given()
            .contentType(ContentType.JSON)
            .body(cacheManagerEntity)
            .when().put(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);

    cmsFilter ="";
    // we check nothing has changed
    expect().contentType(ContentType.JSON)
            .body("get(0).agentId", equalTo("embedded"))
            .body("get(0).name", equalTo("testCache"))
            .body("get(0).attributes.LocalOffHeapSizeInBytes", equalTo(0))
            .body("get(0).attributes.Pinned", equalTo(Boolean.FALSE))
            .body("size()",is(1))
            .statusCode(200)
            .given()
              .queryParam("show", "LocalOffHeapSizeInBytes")
              .queryParam("show", "Pinned")
            .when().get(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);
  }



  @Test
  /**
   * - PUT an updated CacheEntity
   * @throws Exception
   */
  public void updateCachesTest__CacheManagerDoesNotExist() throws Exception {


    String agentsFilter = "";
    String cmsFilter = ";names=cachemanagerDoesNotExist";
    String cachesFilter = ";names=testCache";

    CacheEntity cacheEntity = new CacheEntity();
    expect().statusCode(400)
            .body("details", equalTo("CacheManager not found !"))
            .body("error", equalTo("Failed to create or update cache"))
            .given()
            .contentType(ContentType.JSON)
            .body(cacheEntity)
            .when().put(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);
  }


  @Test
  /**
   * - PUT a CacheEntity, not matching any known caches : creating is not allowed
   *
   * @throws Exception
   */
  public void updateCachesTest__CantCreateCache() throws Exception {


    String agentsFilter = "";
    String cmsFilter = ";names=testCacheManager";
    String cachesFilter = ";names=cacheThatDoesNotExist";
    CacheEntity cacheEntity = new CacheEntity();
    expect().statusCode(400)
            .body("details", equalTo("Cache not found !"))
            .body("error", equalTo("Failed to create or update cache"))
            .given()
            .contentType(ContentType.JSON)
            .body(cacheEntity)
            .when().put(EXPECTED_RESOURCE_LOCATION, STANDALONE_BASE_URL, agentsFilter,cmsFilter, cachesFilter);
  }

  private CacheManager getCacheManagerNew() {
    Configuration configuration = new Configuration();
    configuration.setName("cacheManagerNew");

    CacheConfiguration myCache = new CacheConfiguration()
            .eternal(false).name("CacheNew");
    myCache.setMaxBytesLocalHeap("5M");
    myCache.setMaxBytesLocalDisk("3M");
    configuration.addCache(myCache);
    ManagementRESTServiceConfiguration managementRESTServiceConfiguration = new ManagementRESTServiceConfiguration();
    managementRESTServiceConfiguration.setBind("0.0.0.0:"+STANDALONE_REST_AGENT_PORT);
    managementRESTServiceConfiguration.setEnabled(true);
    configuration.addManagementRESTService(managementRESTServiceConfiguration);
    CacheManager cacheManager = new CacheManager(configuration);
    Cache exampleCache = cacheManager.getCache("CacheNew");
    assert (exampleCache != null);
    return cacheManager;
  }

  private CacheManager createStandaloneCacheManagerARC() {
    Configuration configuration = new Configuration();
    configuration.setName("testCacheManagerStandaloneARC");
    configuration.setMaxBytesLocalDisk("10M");
    configuration.setMaxBytesLocalHeap("5M");
    CacheConfiguration myCache = new CacheConfiguration().eternal(false).name("testCacheStandaloneARC");
    configuration.addCache(myCache);
    ManagementRESTServiceConfiguration managementRESTServiceConfiguration = new ManagementRESTServiceConfiguration();
    managementRESTServiceConfiguration.setBind("0.0.0.0:"+STANDALONE_REST_AGENT_PORT);
    managementRESTServiceConfiguration.setEnabled(true);
    configuration.addManagementRESTService(managementRESTServiceConfiguration);

    CacheManager mgr = new CacheManager(configuration);
    Cache exampleCache = mgr.getCache("testCacheStandaloneARC");
    assert (exampleCache != null);
    return mgr;
  }

  private CacheManager createStandaloneCacheManager() {
    CacheConfiguration myCache = new CacheConfiguration().eternal(false).name("testCacheStandalone").maxEntriesLocalHeap(10000);
    Configuration configuration = new Configuration().name("testCacheManagerStandalone").cache(myCache);

    ManagementRESTServiceConfiguration managementRESTServiceConfiguration = new ManagementRESTServiceConfiguration();
    managementRESTServiceConfiguration.setBind("0.0.0.0:"+STANDALONE_REST_AGENT_PORT);
    managementRESTServiceConfiguration.setEnabled(true);
    configuration.addManagementRESTService(managementRESTServiceConfiguration);

    CacheManager mgr = new CacheManager(configuration);
    Cache exampleCache = mgr.getCache("testCacheStandalone");
    assert (exampleCache != null);
    return mgr;
  }

  private CacheManager createClusteredCacheManager() {
    Configuration configuration = new Configuration();
    configuration.setName("testCacheManagerClustered");
    TerracottaClientConfiguration terracottaConfiguration = new TerracottaClientConfiguration().url(CLUSTER_URL);
    configuration.addTerracottaConfig(terracottaConfiguration);
    CacheConfiguration myCache = new CacheConfiguration().eternal(false).name("testCacheClustered").terracotta(new TerracottaConfiguration()).maxEntriesLocalHeap(10000).timeToIdleSeconds(1);
    configuration.addCache(myCache);
    ManagementRESTServiceConfiguration managementRESTServiceConfiguration = new ManagementRESTServiceConfiguration();
    managementRESTServiceConfiguration.setBind("0.0.0.0:"+STANDALONE_REST_AGENT_PORT);
    managementRESTServiceConfiguration.setEnabled(true);
    configuration.addManagementRESTService(managementRESTServiceConfiguration);

    CacheManager mgr = new CacheManager(configuration);
    Cache exampleCache = mgr.getCache("testCacheClustered");
    assert (exampleCache != null);
    return mgr;
  }

}
