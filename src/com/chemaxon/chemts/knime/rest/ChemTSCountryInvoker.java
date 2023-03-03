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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

public class ChemTSCountryInvoker {

    private static final String URL_PATH = "/cc-api/hts/countries/";

    private final ChemTSRestInvoker chemtsRestInvoker;

    public ChemTSCountryInvoker(RestConnectionDetails connectionDetails) {
        chemtsRestInvoker = new ChemTSRestInvoker(connectionDetails, URL_PATH);
    }

    public List<String> getCountryNames() {
        return getCountries().values().stream().collect(toList());
    }

    public List<String> getCountryCodesByNames(List<String> countryNames) {
        return getCountries().entrySet().stream()
                .filter(entry -> countryNames.contains(entry.getValue()))
                .map(entry -> entry.getKey())
                .collect(toList());
    }

    public Map<String, String> getCountries() {
        return chemtsRestInvoker.get(new TypeReference<Map<String, String>>() {
        });
    }
}
