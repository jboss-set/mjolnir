package org.jboss.mjolnir.server.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
public class HibernateUtils {

    private final static Logger logger = Logger.getLogger(HibernateUtils.class);

    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;

    private HibernateUtils() {
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            String appName = (String) JndiUtils.lookup("java:app/AppName");
            logger.debugf("Detected application name: %s", appName);

            Configuration configuration = new Configuration()
                    .setProperty("hibernate.connection.datasource", "java:jboss/datasources/" + appName + "/MjolnirDS");
            configuration.configure();
            serviceRegistry = new ServiceRegistryBuilder().applySettings(
                    configuration.getProperties()). buildServiceRegistry();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        }

        return sessionFactory;
    }

}
