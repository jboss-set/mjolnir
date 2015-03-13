package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.client.exception.ApplicationException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class JndiUtils {

    private static Context context;
    private static DataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            String appName = (String) lookup("java:app/AppName");
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
            throw new ApplicationException("Couldn't perform JNDI lookup", e);
        }
    }

}
