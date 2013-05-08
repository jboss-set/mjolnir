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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.mjolnir.authentication.KerberosUser;

/**
 * @author: navssurtani
 * @since: 0.1
 */

public class LoginPage implements EntryPoint {

    @Override
    public void onModuleLoad() {

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
            private final LoginServiceAsync loginService = GWT.create(LoginService.class);

            @Override
            public void onClick(ClickEvent clickEvent) {
                loginService.login(nameField.getText(), githubName.getText()
                        , passwordField.getText(), getCallback());
            }

            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                if (keyUpEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER)
                    loginService.login(nameField.getText(), githubName.getText()
                            , passwordField.getText(), getCallback());
            }

            private AsyncCallback<KerberosUser> getCallback() {

                AsyncCallback toReturn = new AsyncCallback<KerberosUser>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        dialogBox.setText("Failed remote call.");
                        responseLabel.addStyleName("serverResponseLabelError");
                        responseLabel.setText("Your login for has failed. Try again. Your credentials might be wrong.");
                        dialogBox.center();
                    }
                    @Override
                    public void onSuccess(KerberosUser kerberosUser) {
                        dialogBox.setText("Remote call worked.");
                        responseLabel.setHTML("Login succeeded.");
                        responseLabel.setText("Login for " + kerberosUser.getGithubName() + " succeeded.");
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
}
