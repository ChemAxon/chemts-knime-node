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

package com.chemaxon.chemts.knime.rest;

import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.AUTH_ANONYMOUS;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.AUTH_BASIC;
import static com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields.AUTH_OAUTH2;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.CharStreams;

public class ChemTSRestInvoker {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final RestConnectionDetails connectionDetails;
    private final String urlStr;
    private String accessToken;

    public ChemTSRestInvoker(RestConnectionDetails connectionDetails, String urlPath) {
        this.connectionDetails = connectionDetails;
        this.urlStr = connectionDetails.getHost() + urlPath;
        if (isOAuth2()) {
            initAccessToken();
        }
    }

    public <T> T get(Class<T> responseClass) {
        return get(new TypeReference<T>() {
            @Override
            public Type getType() {
                return responseClass;
            }
        });
    }

    public <T> T get(TypeReference<T> type) {
        return get(type, false);
    }

    private <T> T get(TypeReference<T> type, boolean refreshAccessToken) {
        HttpURLConnection connection = null;
        try {
            connection = getConnection();
            String responseJson = getJsonResponse(connection);
            return mapResponse(responseJson, type);
        } catch (IOException e) {
            throw new ChemTSInvocationException("Request failed. URL: " + urlStr, e);
        } catch (InvalidAccessTokenException e) {
            if (refreshAccessToken) {
                throw e;
            }
            return get(type, true);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public <T> T post(Object request, Class<T> responseClass) {
        return post(request, responseClass, false);
    }

    private <T> T post(Object request, Class<T> responseClass, boolean refreshAccessToken) {

        if (refreshAccessToken) {
            initAccessToken();
        }

        String requestJson = null;
        try {
            requestJson = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write object to json: " + request, e);
        }

        HttpURLConnection connection = null;
        try {
            connection = getConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestJson.getBytes());
            }
            String responseJson = getJsonResponse(connection);
            return mapResponse(responseJson, responseClass);
        } catch (IOException e) {
            throw new ChemTSInvocationException("Request failed. URL: " + urlStr, e);
        } catch (InvalidAccessTokenException e) {
            if (refreshAccessToken) {
                throw e;
            }
            return post(request, responseClass, true);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection getConnection() {
        switch (connectionDetails.getAuthType()) {
            case AUTH_BASIC:
                return getBasicAuthConnection(connectionDetails.getUsername(), connectionDetails.getPassword());
            case AUTH_ANONYMOUS:
                return getConnectionBase();
            case AUTH_OAUTH2:
                return getOauth2AuthConnection();
            default:
                // this should never be reached
                throw new IllegalStateException("Invaid connection settings");
        }
    }

    private HttpURLConnection getBasicAuthConnection(String username, String password) {
        HttpURLConnection connection = getConnectionBase();
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
        String authHeaderValue = "Basic " + new String(encodedAuth);
        connection.setRequestProperty("Authorization", authHeaderValue);
        return connection;
    }

    private HttpURLConnection getOauth2AuthConnection() {
        HttpURLConnection connection = getConnectionBase();
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        return connection;
    }

    private HttpURLConnection getConnectionBase() {
        HttpURLConnection connection;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json,text/plain");
            int timeout = connectionDetails.getTimeout();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
        } catch (IOException e) {
            throw new ChemTSInvocationException("Failed to set up connection. URL: " + urlStr, e);
        }
        return connection;
    }

    private void initAccessToken() {
        HttpURLConnection connection;
        try {
            URL url = new URL(connectionDetails.getTokenUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            int timeout = connectionDetails.getTimeout();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            String clientId = connectionDetails.getClientId();
            String clientSecret = connectionDetails.getClientSecret();
            String auth = clientId + ":" + clientSecret;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
            String authHeaderValue = "Basic " + new String(encodedAuth);
            connection.setRequestProperty("Authorization", authHeaderValue);
            String formParam = "grant_type=client_credentials";
            byte[] out = formParam.getBytes(UTF_8);
            int length = out.length;
            connection.setFixedLengthStreamingMode(length);
            connection.connect();

            try (OutputStream os = connection.getOutputStream()) {
                os.write(out);
            }
            String responseJson = getJsonResponse(connection);
            accessToken = mapper.readValue(responseJson, ObjectNode.class).get("access_token").textValue();
        } catch (IOException e) {
            throw new ChemTSInvocationException("Failed to set up connection. URL: " + connectionDetails.getTokenUrl(),
                    e);
        }
    }

    private String getJsonResponse(HttpURLConnection connection) throws IOException, InvalidAccessTokenException {
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED && isOAuth2()
                && !connection.getURL().toString().equals(connectionDetails.getTokenUrl())) {
            throw new InvalidAccessTokenException();
        }
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new ChemTSInvocationException("Request failed. HTTP error code : " + responseCode + " URL: " + urlStr);
        }
        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), UTF_8)) {
            return CharStreams.toString(reader);
        }
    }

    private <T> T mapResponse(String responseJson, Class<T> responseClass) {
        return mapResponse(responseJson, new TypeReference<T>() {
            @Override
            public Type getType() {
                return responseClass;
            }
        });
    }

    private <T> T mapResponse(String responseJson, TypeReference<T> type) {
        T response = null;
        try {
            response = mapper.readValue(responseJson, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse json: " + responseJson, e);
        }
        return response;
    }

    private boolean isOAuth2() {
        return AUTH_OAUTH2.equals(connectionDetails.getAuthType());
    }
}
