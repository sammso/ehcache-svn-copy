jsvc -stop -pidfile ../pid/ehcache_server.pid -cp ../lib/commons-daemon-1.0.1.jar:../lib/ehcache-standalone-server-0.5.jar:../lib/schemas-9.0.2-resources.jar:../lib/dtds-9.0.2-resources.jar:../lib/gf-embedded-api-1.0-alpha-4.jar:../lib/web-all-10.0-build-20080430.jar  net.sf.ehcache.server.standalone.Server 8080 ../war
