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

import com.chemaxon.chemts.knime.dto.SystemInfoRespose;

public class ChemTSSystemInfoInvoker {

    private static final String URL_PATH = "/cc-api/system-info/";

    private final ChemTSRestInvoker chemtsRestInvoker;

    public ChemTSSystemInfoInvoker(RestConnectionDetails connectionDetails) {
        chemtsRestInvoker = new ChemTSRestInvoker(connectionDetails, URL_PATH);
    }

    public SystemInfoRespose getSystemInfo() {
        return chemtsRestInvoker.get(SystemInfoRespose.class);
    }
}
