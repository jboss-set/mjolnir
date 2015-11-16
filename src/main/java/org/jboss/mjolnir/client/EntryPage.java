/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.mjolnir.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import org.jboss.mjolnir.shared.domain.KerberosUser;
import org.jboss.mjolnir.client.component.ErrorDialog;
import org.jboss.mjolnir.client.component.LayoutPanel;
import org.jboss.mjolnir.client.component.LoadingPanel;
import org.jboss.mjolnir.client.component.LoginScreen;
import org.jboss.mjolnir.client.service.LoginService;
import org.jboss.mjolnir.client.service.LoginServiceAsync;

/**
 * Application entry point
 *
 * @author navssurtani
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Deprecated
public class EntryPage implements EntryPoint {

    /**
     * Singleton EntryPage *
     */
    private static EntryPage instance = new EntryPage();

    private LoginServiceAsync loginService = LoginService.Util.getInstance();
    private Logger logger = Logger.getLogger("");

    // Constructor made private.
    private EntryPage() {
    }

    public static EntryPage getInstance() {
        return instance;
    }

    @Override
    public void onModuleLoad() {
        RootLayoutPanel.get().getElement().getStyle().setBackgroundColor("#ECECEC");
        RootLayoutPanel.get().add(new LoadingPanel());

        // setting uncaught exception handler
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                logger.log(Level.SEVERE, "Uncaught exception: ", e);
                final ErrorDialog errorDialog = new ErrorDialog(e);
                errorDialog.center();
            }
        });

        // verify that user is logged in and display appropriate page according to the result
        final XsrfTokenServiceAsync xsrfService = GWT.create(XsrfTokenService.class);
        ((ServiceDefTarget) xsrfService).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
        xsrfService.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
            @Override
            public void onFailure(Throwable caught) {
                throw new RuntimeException(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(XsrfToken result) {
                XsrfUtil.setToken(result);
                XsrfUtil.putToken((HasRpcToken) loginService);

                loginService.getLoggedUser(new AsyncCallback<KerberosUser>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, caught.getMessage());
                        throw new RuntimeException(caught.getMessage(), caught);
                    }

                    @Override
                    public void onSuccess(final KerberosUser user) {
                        if (user == null) { // not logged in, show login screen
                            goToLoginScreen();
                        } else { // logged in, show content
                            CurrentUser.set(user);
                            goToMainPage();
                        }
                    }
                });
            }
        });
    }

    public void goToLoginScreen() {
        final FlowPanel flowPanel = new FlowPanel();
        flowPanel.add(new LoginScreen() {
            @Override
            protected void onSuccessfulLogin(KerberosUser user) {
                CurrentUser.set(user);
                goToMainPage();
            }
        });

        RootLayoutPanel.get().clear();
        RootLayoutPanel.get().add(flowPanel);
    }

    public void goToMainPage() {
        RootLayoutPanel.get().clear();
        RootLayoutPanel.get().add(new LayoutPanel());
    }

}
