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

package com.chemaxon.chemts.knime.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckListResults {

    private List<HtsData> htsData;
    private Map<String, String> pharmaAgreement;
    private Map<String, String> drugInfo;
    private String errorMessage;

    public List<HtsData> getHtsData() {
        if (htsData == null) {
            htsData = new ArrayList<>();
        }
        return htsData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, String> getPharmaAgreement() {
        if (pharmaAgreement == null) {
            pharmaAgreement = new HashMap<>();
        }
        return pharmaAgreement;
    }

    public Map<String, String> getDrugInfo() {
        if (drugInfo == null) {
            drugInfo = new HashMap<>();
        }
        return drugInfo;
    }
}
