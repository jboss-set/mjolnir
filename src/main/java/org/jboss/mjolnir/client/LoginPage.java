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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.mjolnir.authentication.KerberosUser;
import org.jboss.mjolnir.authentication.LoginFailedException;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author: navssurtani
 * @since: 0.1
 */

public class LoginPage implements EntryPoint {


    @Override
    public void onModuleLoad() {

        final SessionServiceAsync sessionServiceAsync = (SessionServiceAsync) GWT.create(SessionService.class);
        generateSession(sessionServiceAsync);

        final Button loginButton = new Button("Login");
        loginButton.setEnabled(true);
        final TextBox nameField = new TextBox();
        final TextBox githubName = new TextBox();
        final PasswordTextBox passwordField = new PasswordTextBox();

        nameField.setTitle("Kerberos ID");
        githubName.setTitle("Github ID");
        passwordField.setTitle("Password");

        RootPanel.get("nameFieldContainer").add(nameField);
        RootPanel.get("githubFieldContainer").add(githubName);
        RootPanel.get("passwordFieldContainer").add(passwordField);
        RootPanel.get("loginButtonContainer").add(loginButton);

        // Dialog box stuff post authentication.
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText("Checking login credentials.");

        // Response labels.
        final HTML responseLabel = new HTML();


        final Button closeButton = new Button("Close");
        closeButton.getElement().setId("close");

        // Sort out the paneling for the Dialog Box.
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setStyleName("dialogVPanel");
        verticalPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        verticalPanel.add(responseLabel);
        verticalPanel.add(closeButton);

        // Set the widget
        dialogBox.setWidget(verticalPanel);

        // Handler for the close button.
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                dialogBox.hide();
                loginButton.setEnabled(true);
                loginButton.setFocus(true);
            }
        });


        // Handler for the login phase.
        class LoginHandler implements ClickHandler, KeyUpHandler {
            final LoginServiceAsync loginService = (LoginServiceAsync) GWT.create(LoginService.class);
            @Override
            public void onClick(ClickEvent clickEvent) {
                makeSecureLogin(nameField.getText(), githubName.getText(), passwordField.getText());
            }

            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                if (keyUpEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    makeSecureLogin(nameField.getText(), githubName.getText(), passwordField.getText());
                }
            }

            private void makeSecureLogin(final String krb5Name, final String githubName, final String pwd) {
                XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
                ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
                xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        dialogBox.setText("Remote call failed");

                        try {
                            throw throwable;
                        } catch (RpcTokenException rpcException) {
                            responseLabel.setHTML("RPC Token could not be generated.");
                        } catch (Throwable other) {
                            responseLabel.setHTML(other.getMessage());
                        }
                        dialogBox.center();
                    }

                    @Override
                    public void onSuccess(XsrfToken xsrfToken) {
                        ((HasRpcToken) loginService).setRpcToken(xsrfToken);
                        loginService.login(krb5Name, githubName, pwd, getLoginCallback());
                    }
                });
            }

            private AsyncCallback<KerberosUser> getLoginCallback() {

                AsyncCallback toReturn = new AsyncCallback<KerberosUser>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        dialogBox.setText("Remote call failed");
                        try {
                            throw throwable;
                        } catch (LoginFailedException lfe) {
                            responseLabel.setText(lfe.getSymbol());
                        } catch (Throwable other) {
                            responseLabel.setText(other.getMessage());
                        }
                        dialogBox.center();
                    }

                    @Override
                    public void onSuccess(KerberosUser kerberosUser) {
                        dialogBox.setText("Remote call successful");
                        responseLabel.setText("Login for " + kerberosUser.getGithubName() + " succeeded.");
                        dialogBox.center();
                    }
                };
                return toReturn;
            }
        }

        // Create the handler and make sure that it can deal with the fields.
        LoginHandler handler = new LoginHandler();
        loginButton.addClickHandler(handler);
        passwordField.addKeyUpHandler(handler);
    }

    private void generateSession(SessionServiceAsync sessionServiceAsync) {
        sessionServiceAsync.createSession(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                // No-op.
            }

            @Override
            public void onSuccess(Void result) {
                // No-op.
            }
        });

    }
}
