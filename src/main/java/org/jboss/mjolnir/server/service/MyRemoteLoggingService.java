package org.jboss.mjolnir.server.service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.google.gwt.logging.server.RemoteLoggingServiceImpl;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class MyRemoteLoggingService extends RemoteLoggingServiceImpl {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        setSymbolMapsDirectory("/WEB-INF/deploy/mjolnir/symbolMaps");
    }
}
