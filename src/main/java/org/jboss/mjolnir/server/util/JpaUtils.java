package org.jboss.mjolnir.server.util;

import org.hibernate.ejb.HibernatePersistence;
import org.jboss.logging.Logger;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
public class JpaUtils {

    private final static Logger logger = Logger.getLogger(JndiUtils.class);

    private static EntityManagerFactory entityManagerFactory;

    private JpaUtils() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if(entityManagerFactory == null) {

            String appName = (String) JndiUtils.lookup("java:app/AppName");
            logger.debugf("Detected application name: %s", appName);

            Map properties = new HashMap();
            properties.put("hibernate.connection.datasource", "java:jboss/datasources/" + appName + "/MjolnirDS");

            entityManagerFactory = new HibernatePersistence().createEntityManagerFactory("MjolnirPU", properties);
        }

        return entityManagerFactory;
    }
}
