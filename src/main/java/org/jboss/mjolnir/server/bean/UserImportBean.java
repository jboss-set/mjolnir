package org.jboss.mjolnir.server.bean;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.mjolnir.authentication.KerberosUser;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Imports users from Infinispan cache to database.
 * <p/>
 * Import is performed automatically after bean construction and is only performed once. The fact that import
 * has already been performed is indicated by application parameter {@link UserImportBean#USER_IMPORT_COMPLETED_KEY}.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
@Startup
public class UserImportBean {

    private final static String USER_IMPORT_COMPLETED_KEY = "application.user_import_completed";
    private final static String INFINISPAN_PATH_KEY = "infinispan.path";

    private static final Logger logger = Logger.getLogger("");

    private EmbeddedCacheManager cacheManager;

    @EJB
    private ApplicationParameters applicationParameters;

    @EJB
    private UserRepository userRepository;

    @PostConstruct
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    public void initBean() {
        try {
            final String wasImportCompleted = applicationParameters.getParameter(USER_IMPORT_COMPLETED_KEY);

            if (!Boolean.parseBoolean(wasImportCompleted)) {
                logger.log(Level.INFO, "Starting data import from Infinispan cache.");

                startCacheManager();
                importUsersFromCache();
                stopCacheManager();

                applicationParameters.setParameter(USER_IMPORT_COMPLETED_KEY, Boolean.TRUE.toString());
            }
        } catch (NamingException e) {
            logger.log(Level.SEVERE, "Couldn't start cache manager.", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Couldn't import data from Infinispan", e);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Couldn't import data from Infinispan", e);
        }
    }

    private void startCacheManager() throws NamingException {
        final String cacheStoreLocation = applicationParameters.getMandatoryParameter(INFINISPAN_PATH_KEY);

        final GlobalConfigurationBuilder global = new GlobalConfigurationBuilder();
        global.globalJmxStatistics().jmxDomain("org.jboss.mjolnir");

        final ConfigurationBuilder builder = new ConfigurationBuilder();
        final Configuration config = builder.loaders().preload(true)
                .addFileCacheStore().location(cacheStoreLocation)
                .eviction().maxEntries(50)
                .build();

        cacheManager = new DefaultCacheManager(config);
    }

    private void stopCacheManager() {
        if (cacheManager != null) {
            cacheManager.stop();
        }
    }

    private void importUsersFromCache() throws SQLException {
        final Cache<String, KerberosUser> cache = cacheManager.getCache();
        for (KerberosUser user : cache.values()) {
            if (userRepository.getUser(user.getName()) == null) {
                userRepository.saveOrUpdateUser(user);
            }
        }
    }
}
