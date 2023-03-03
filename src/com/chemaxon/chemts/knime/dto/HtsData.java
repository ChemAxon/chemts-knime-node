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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HtsData {
    private String htsNumber;
    private String description;
    private String fullDescription;
    private String other;
    private String quotaQuantity;
    private String special;
    private String additionalDuties;
    private List<String> units;
    private String general;
    private String countryCode;

    public String getHtsNumber() {
        return htsNumber;
    }

    public void setHtsNumber(String htsNumber) {
        this.htsNumber = htsNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getQuotaQuantity() {
        return quotaQuantity;
    }

    public void setQuotaQuantity(String quotaQuantity) {
        this.quotaQuantity = quotaQuantity;
    }

    public String getSpecial() {
        return special;
    }

    public void setSpecial(String special) {
        this.special = special;
    }

    public String getAdditionalDuties() {
        return additionalDuties;
    }

    public void setAdditionalDuties(String additionalDuties) {
        this.additionalDuties = additionalDuties;
    }

    public List<String> getUnits() {
        if (units == null) {
            units = new ArrayList<>();
        }
        return units;
    }

    public String getGeneral() {
        return general;
    }

    public void setGeneral(String general) {
        this.general = general;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
