package org.jboss.mjolnir.server;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Listener that creates and shuts down a cache manager instance.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class CacheContextListener implements ServletContextListener {

    private EmbeddedCacheManager cacheManager;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            final Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            final String cacheStoreLocation = (String) ctx.lookup("INFINISPAN_STORE");

            final GlobalConfigurationBuilder global = new GlobalConfigurationBuilder();
            global.globalJmxStatistics().jmxDomain("org.jboss.mjolnir");

            final ConfigurationBuilder builder = new ConfigurationBuilder();
            final Configuration config = builder.loaders().preload(true)
                    .addFileCacheStore().location(cacheStoreLocation)
                    .eviction().maxEntries(50)
                    .build();

            cacheManager = new DefaultCacheManager(config);
            final Cache cache = cacheManager.getCache();
            servletContextEvent.getServletContext().setAttribute("cache", cache);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (cacheManager != null) {
            cacheManager.stop();
        }
    }
}
