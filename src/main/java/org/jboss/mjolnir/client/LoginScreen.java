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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.mjolnir.authentication.LoginFailedException;

/**
 * @author: navssurtani
 * @since: 0.2
 */

public class LoginScreen extends Composite {

    private TextBox krb5NameField;
    private PasswordTextBox pwdField;
    private Button loginButton;

    private Grid loginGrid;

    private RootPanel loginPanel;

    // The service to make RPC calls.
    private LoginServiceAsync loginService = null;

    public LoginScreen() {
        krb5NameField = new TextBox();
        pwdField = new PasswordTextBox();
        loginButton = new Button("Login");

        krb5NameField.setTitle("Kerberos username");
        pwdField.setTitle("Kerberos password");
        loginButton.setEnabled(true);

        loginGrid = new Grid(3, 2);
        loginGrid.setWidget(0, 0, new Label("Kerberos Username"));
        loginGrid.setWidget(0, 1, krb5NameField);
        loginGrid.setWidget(1, 0, new Label("Kerberos Password"));
        loginGrid.setWidget(1, 1, pwdField);
        loginGrid.setWidget(2, 1, loginButton);

        loginPanel = RootPanel.get("loginPanelContainer");
        loginPanel.add(loginGrid);

        LoginHandler handler = new LoginHandler();
        loginButton.addClickHandler(handler);
        krb5NameField.addKeyUpHandler(handler);
        pwdField.addKeyUpHandler(handler);
    }

    private void checkIsRegistered(final String krb5Name) {
        loginService.isRegistered(krb5Name, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                displayErrorBox("Error checking if user exists on server.", caught.getMessage());
            }

            @Override
            public void onSuccess(Boolean isRegistered) {
                if (isRegistered) {
                    // Move to subscription screen.
                    EntryPage.getInstance().moveToSelectionScreen(krb5Name);

                } else {
                    // Move to add github name screen.
                    EntryPage.getInstance().moveToGithubRegistrationScreen(krb5Name);
                }
            }
        });
    }

    private void displayErrorBox(String errorHeader, String message) {
        final DialogBox errorBox = new DialogBox();
        errorBox.setText(errorHeader);
        final HTML errorLabel = new HTML();
        errorLabel.setHTML(message);
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        final Button closeButton = new Button("Close");
        closeButton.setEnabled(true);
        closeButton.getElement().setId("close");
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                errorBox.hide();
                loginButton.setFocus(true);
                loginButton.setEnabled(true);
            }
        });
        verticalPanel.add(errorLabel);
        verticalPanel.add(closeButton);
        errorBox.setWidget(verticalPanel);
        errorBox.center();
    }

    class LoginHandler implements ClickHandler, KeyUpHandler {

        @Override
        public void onClick(ClickEvent event) {
            makeSecureLogin(krb5NameField.getText(), pwdField.getText());
        }

        @Override
        public void onKeyUp(KeyUpEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
                makeSecureLogin(krb5NameField.getText(), pwdField.getText());
        }

        private void makeSecureLogin(final String krb5Name, final String password) {
            // First make sure that we can get our token sorted out.
            XsrfTokenServiceAsync xsrf = (XsrfTokenServiceAsync) GWT.create(XsrfTokenService.class);
            ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");


            xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
                @Override
                public void onFailure(Throwable caught) {
                    displayErrorBox("Could not generate token", caught.getMessage());
                }

                @Override
                public void onSuccess(XsrfToken result) {
                    // Now we can get the login service.
                    loginService = LoginService.Util.getInstance();
					((HasRpcToken) loginService).setRpcToken(result);
                    performLoginCall(krb5Name, password);
                }
            });

        }

        private void performLoginCall(final String krb5Name, String password) {
            loginService.login(krb5Name, password, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    // We want to check whether or not we have a LoginFailedException first.
                    try {
                        throw caught;
                    } catch (LoginFailedException lfe) {
                        displayErrorBox("Error with login", lfe.getSymbol());
                    } catch (Throwable other) {
                        displayErrorBox("Error with login", "There has been an unexpected error with your login." +
                                " Please email jboss-set@redhat.com");
                    }
                }

                @Override
                public void onSuccess(Boolean result) {
                    // Based off of the result, we either forward off to a new screen or we just display a
                    // pop-up stating that the password is incorrect and allow for a retry.
                    if (result) {
                        loginPanel.remove(loginGrid);
                        // Now we should make a check to see if the user exists or not. If that is true,
                        // we will either move to the RegisterGithubNameScreen or the SubscriptionScreen.
                        checkIsRegistered(krb5Name);
                    } else {
                        // Just display an error box with a close button that should allow us to come back to the
                        // same screen. Hopefully.
                        displayErrorBox("Incorrect password", "Your username-password combination is wrong. Please " +
                                "check and try again.");
                    }
                }
            });

        }
    }

}

