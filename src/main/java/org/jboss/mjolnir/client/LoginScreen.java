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
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.mjolnir.authentication.KerberosUser;

/**
 * Login form.
 *
 * @author navsurtani
 * @author Tomas Hofman (thofman@redhat.com)
 */

public abstract class LoginScreen extends Composite {

    private LoginServiceAsync loginService = LoginService.Util.getInstance();
    private Label feedback;

    public LoginScreen() {
        final HTMLPanel panel = new HTMLPanel("");
        panel.setStyleName("login-panel");
        panel.setWidth("400px");
        panel.getElement().getStyle().setProperty("margin", "0 auto");
        panel.getElement().getStyle().setPaddingTop(5, Style.Unit.EM);

        final FormPanel form = new FormPanel();
        panel.add(form);

        final TextBox nameField = new TextBox();
        final PasswordTextBox passwordField = new PasswordTextBox();
        final SubmitButton loginButton = new SubmitButton("Login");

        nameField.setTitle("Kerberos usernaame");
        passwordField.setTitle("Kerberos password");
        loginButton.setEnabled(true);

        final Grid loginGrid = new Grid(3, 2);
        loginGrid.setWidget(0, 0, new Label("Kerberos Username"));
        loginGrid.setWidget(0, 1, nameField);
        loginGrid.setWidget(1, 0, new Label("Kerberos Password"));
        loginGrid.setWidget(1, 1, passwordField);
        loginGrid.setWidget(2, 1, loginButton);
        form.setWidget(loginGrid);

        feedback = new Label();
        panel.add(feedback);

        initWidget(panel);

        form.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent submitEvent) {
                final XsrfTokenServiceAsync xsrf = GWT.create(XsrfTokenService.class);
                ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");

                xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        showMessage("Could not generate token: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(XsrfToken result) {
                        // Now we can get the login service.
                        loginService = LoginService.Util.getInstance();
                        ((HasRpcToken) loginService).setRpcToken(result);
                        performLoginCall(nameField.getText(), passwordField.getText());
                    }
                });
                submitEvent.cancel(); // prevent form from submitting, since it was handled by Ajax
            }
        });
    }

    private void showMessage(String message) {
        feedback.setText(message);
    }

    private void performLoginCall(final String krb5Name, String password) {
        loginService.login(krb5Name, password, new AsyncCallback<KerberosUser>() {
            @Override
            public void onFailure(Throwable caught) {
                showMessage("Login failed: " + caught.getMessage());
            }

            @Override
            public void onSuccess(KerberosUser user) {
                if (user != null) {
                    onSuccessfulLogin(user);
                } else {
                    showMessage("Login failure: wrong credentials.");
                }
            }
        });
    }

    /**
     * Method which is called on successful login.
     *
     * To be implemented by caller.
     *
     * @param user logged user
     */
    protected abstract void onSuccessfulLogin(KerberosUser user);

}

