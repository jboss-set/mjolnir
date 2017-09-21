package org.jboss.set.mjolnir.server.util;

import org.jboss.logging.Logger;
import org.jboss.set.mjolnir.client.exception.ApplicationException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class JndiUtils {

    private static final Logger logger = Logger.getLogger(JndiUtils.class);

    private static Context context;
    private static DataSource dataSource;

    private JndiUtils() {
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            String appName = (String) lookup("java:app/AppName");
            logger.debugf("Detected application name: %s", appName);

            dataSource = (DataSource) lookup("java:jboss/datasources/" + appName + "/MjolnirDS");
        }
        return dataSource;
    }

    public static Object lookup(String name) {
        try {
            if (context == null) {
                context = new InitialContext();
            }
            return context.lookup(name);
        } catch (NamingException e) {
            throw new ApplicationException("Couldn't perform JNDI lookup for name: " + name, e);
        }
    }

}
