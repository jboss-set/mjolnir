package org.jboss.set.mjolnir.server.bean;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.jboss.logging.Logger;
import org.jboss.set.mjolnir.server.util.JndiUtils;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class EntityManagerFactoryProducer {

    private static final Logger logger = Logger.getLogger(JndiUtils.class);

    @Produces @ApplicationScoped
    EntityManagerFactory createEntityManager() {
        String appName = (String) JndiUtils.lookup("java:app/AppName");
        logger.debugf("Detected application name: %s", appName);

        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.connection.datasource", "java:jboss/datasources/" + appName + "/MjolnirDS");

        return new HibernatePersistenceProvider().createEntityManagerFactory("MjolnirPU", properties);
    }

}
