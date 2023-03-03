/*
 * Licensed to the Chemaxon Ltd. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Chemaxon licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.chemaxon.chemts.knime.tabs;

import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.AUTH_ANONYMOUS;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.AUTH_BASIC;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.AUTH_OAUTH2;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.CFGKEY_AUTH_BASIC;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.CFGKEY_AUTH_OAUTH2;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.CFGKEY_AUTH_TYPE;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.CFGKEY_HOST;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.CFGKEY_TIMEOUT;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.CFGKEY_TOKEN_URL;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.DEFAULT_TIMEOUT;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentAuthentication;
import org.knime.core.node.defaultnodesettings.DialogComponentButton;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelAuthentication;
import org.knime.core.node.defaultnodesettings.SettingsModelAuthentication.AuthenticationType;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.chemaxon.chemts.knime.rest.ChemTSSystemInfoInvoker;
import com.chemaxon.chemts.knime.rest.RestConnectionDetails;

public class ConnectionSettingsTabComponent implements RestConnectionDetails {

    private final DefaultNodeSettingsPane pane;

    private final SettingsModelString m_authType = new SettingsModelString(CFGKEY_AUTH_TYPE, AUTH_OAUTH2);

    private final SettingsModelString m_host = new SettingsModelString(CFGKEY_HOST, "");

    private final SettingsModelString m_token_url = new SettingsModelString(CFGKEY_TOKEN_URL, "");

    private final SettingsModelIntegerBounded m_timeout = new SettingsModelIntegerBounded(CFGKEY_TIMEOUT, DEFAULT_TIMEOUT, 0, Integer.MAX_VALUE);

    private final SettingsModelAuthentication m_basicAuth = new SettingsModelAuthentication(CFGKEY_AUTH_BASIC, AuthenticationType.USER_PWD);

    private final SettingsModelAuthentication m_oauth2Auth = new SettingsModelAuthentication(CFGKEY_AUTH_OAUTH2, AuthenticationType.USER_PWD);

    private final DialogComponentLabel connectionStatusLabel = new DialogComponentLabel("");

    private DialogComponentAuthentication basicAuth = new DialogComponentAuthentication(m_basicAuth, null, AuthenticationType.USER_PWD);

    private DialogComponentString tokenUrl = new DialogComponentString(m_token_url, "Token endpoint:", false, 30);
    private DialogComponentAuthentication oauth2Auth = new DialogComponentAuthentication(m_oauth2Auth, null, AuthenticationType.USER_PWD);

    public ConnectionSettingsTabComponent(DefaultNodeSettingsPane pane) {
        this.pane = pane;
        this.m_authType.addChangeListener(new AuthTypeChangeListener());
    }

    public void addDialogComponents() {
        pane.createNewTab("Connection settings");

        pane.addDialogComponent(new DialogComponentButtonGroup(m_authType, false, "Authentication type:", AUTH_OAUTH2, AUTH_BASIC, AUTH_ANONYMOUS));

        pane.createNewGroup("Settings");
        pane.addDialogComponent(new DialogComponentString(m_host, "cHemTS host:", true, 30));
        pane.addDialogComponent(new DialogComponentNumber(m_timeout, "Timeout:", 1000, 5));

        pane.createNewGroup("OAuth2 authentication settings");
        pane.addDialogComponent(tokenUrl);

        oauth2Auth.setUsernameLabel("Client id:");
        oauth2Auth.setPasswordLabel("Client secret:");
        pane.addDialogComponent(oauth2Auth);

        pane.createNewGroup("Basic authentication settings");
        pane.addDialogComponent(basicAuth);

        pane.closeCurrentGroup();

        pane.addDialogComponent(connectionStatusLabel);
        DialogComponentButton testConnectionButton = new DialogComponentButton("Test connection");
        testConnectionButton.addActionListener(new ConnectionCheckActionListener());
        pane.addDialogComponent(testConnectionButton);
    }

    @Override
    public String getHost() {
        return m_host.getStringValue();
    }

    @Override
    public int getTimeout() {
        return m_timeout.getIntValue();
    }

    @Override
    public String getUsername() {
        return m_basicAuth.getUsername();
    }

    @Override
    public String getPassword() {
        return m_basicAuth.getPassword();
    }

    @Override
    public String getAuthType() {
        return m_authType.getStringValue();
    }

    @Override
    public String getClientId() {
        return m_oauth2Auth.getUsername();
    }

    @Override
    public String getClientSecret() {
        return m_oauth2Auth.getPassword();
    }

    @Override
    public String getTokenUrl() {
        return m_token_url.getStringValue();
    }

    public void clearConnectionStatusLabel() {
        connectionStatusLabel.setText("");
    }

    private class ConnectionCheckActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                new ChemTSSystemInfoInvoker(ConnectionSettingsTabComponent.this).getSystemInfo();
                connectionStatusLabel.setText("Success.");
            } catch (Exception e) {
                connectionStatusLabel.setText("Failed to connect.");
            }
        }
    }

    private class AuthTypeChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent event) {
            if (event.getSource() instanceof SettingsModelString) {
                SettingsModelString settings = (SettingsModelString) event.getSource();
                if (AUTH_OAUTH2.equals(settings.getStringValue())) {
                    tokenUrl.getModel().setEnabled(true);
                    oauth2Auth.getModel().setEnabled(true);
                    basicAuth.getModel().setEnabled(false);
                } else if (AUTH_BASIC.equals(settings.getStringValue())) {
                    tokenUrl.getModel().setEnabled(false);
                    oauth2Auth.getModel().setEnabled(false);
                    basicAuth.getModel().setEnabled(true);
                } else if (AUTH_ANONYMOUS.equals(settings.getStringValue())) {
                    tokenUrl.getModel().setEnabled(false);
                    oauth2Auth.getModel().setEnabled(false);
                    basicAuth.getModel().setEnabled(false);
                }
            }
        }
    }
}
