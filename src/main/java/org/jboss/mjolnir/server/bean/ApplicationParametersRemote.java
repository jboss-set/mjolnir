package org.jboss.mjolnir.server.bean;

import java.sql.SQLException;

/**
 * Remote view for ApplicationParametersBean.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface ApplicationParametersRemote {

    void reloadParameters() throws SQLException;

}
