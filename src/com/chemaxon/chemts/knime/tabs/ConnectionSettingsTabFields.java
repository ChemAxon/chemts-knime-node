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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelAuthentication;
import org.knime.core.node.defaultnodesettings.SettingsModelAuthentication.AuthenticationType;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.chemaxon.chemts.knime.rest.RestConnectionDetails;

public class ConnectionSettingsTabFields implements RestConnectionDetails {

    public static final String CFGKEY_AUTH_TYPE = "Authentication type";

    public static final String AUTH_BASIC = "Basic";

    public static final String AUTH_OAUTH2 = "OAuth2";

    public static final String AUTH_ANONYMOUS = "None";

    public static final String CFGKEY_HOST = "Host";

    public static final String CFGKEY_TOKEN_URL = "Token endpoint";

    public static final String CFGKEY_TIMEOUT = "Timeout";

    public static final String CFGKEY_AUTH_OAUTH2 = "OAuth2";

    public static final String CFGKEY_AUTH_BASIC = "Basic";

    public static final int DEFAULT_TIMEOUT = 15000;

    private final SettingsModelString m_authType = new SettingsModelString(CFGKEY_AUTH_TYPE, AUTH_OAUTH2);

    private final SettingsModelString m_host = new SettingsModelString(CFGKEY_HOST, "");

    private final SettingsModelString m_token_url = new SettingsModelString(CFGKEY_TOKEN_URL, "");

    private final SettingsModelIntegerBounded m_timeout = new SettingsModelIntegerBounded(CFGKEY_TIMEOUT, DEFAULT_TIMEOUT, 0, Integer.MAX_VALUE);

    private final SettingsModelAuthentication m_oauth2Auth = new SettingsModelAuthentication(CFGKEY_AUTH_OAUTH2, AuthenticationType.USER_PWD);

    private final SettingsModelAuthentication m_basicAuth = new SettingsModelAuthentication(CFGKEY_AUTH_BASIC, AuthenticationType.USER_PWD);

    public ConnectionSettingsTabFields() {
        m_basicAuth.setEnabled(false);
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

    public void saveSettingsTo(NodeSettingsWO settings) {
        m_authType.saveSettingsTo(settings);
        m_timeout.saveSettingsTo(settings);
        m_host.saveSettingsTo(settings);
        m_token_url.saveSettingsTo(settings);
        m_oauth2Auth.saveSettingsTo(settings);
        m_basicAuth.saveSettingsTo(settings);
    }

    public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
        m_authType.loadSettingsFrom(settings);
        m_timeout.loadSettingsFrom(settings);
        m_host.loadSettingsFrom(settings);
        m_token_url.loadSettingsFrom(settings);
        m_oauth2Auth.loadSettingsFrom(settings);
        m_basicAuth.loadSettingsFrom(settings);
    }

    public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
        m_authType.validateSettings(settings);
        m_timeout.validateSettings(settings);
        m_host.validateSettings(settings);
        m_token_url.validateSettings(settings);
        m_oauth2Auth.validateSettings(settings);
        m_basicAuth.validateSettings(settings);
    }
}
