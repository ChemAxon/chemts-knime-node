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

public class SystemInfoRespose {

    private String status;
    private String knowledgeBaseVersion;
    private String softwareVersion;
    private boolean newKnowledgeBaseAvailable;
    private String licenseExpirationDate;
    private boolean bigdataAvailable;
    private boolean backendAvailable;
    private boolean trainingRunning;
    private boolean knowledgeBaseUpdateRunning;
    private String knowledgeBaseVersionBeingInstalled;
    private boolean htsKnowledgeBaseUpdateRunning;
    private String htsKnowledgeBaseVersion;
    private boolean newHtsKnowledgeBaseAvailable;

    public String getStatus() {
        return status;
    }
    public String getKnowledgeBaseVersion() {
        return knowledgeBaseVersion;
    }
    public String getSoftwareVersion() {
        return softwareVersion;
    }
    public boolean isNewKnowledgeBaseAvailable() {
        return newKnowledgeBaseAvailable;
    }
    public String getLicenseExpirationDate() {
        return licenseExpirationDate;
    }
    public boolean isBigdataAvailable() {
        return bigdataAvailable;
    }
    public boolean isBackendAvailable() {
        return backendAvailable;
    }
    public boolean isTrainingRunning() {
        return trainingRunning;
    }
    public boolean isKnowledgeBaseUpdateRunning() {
        return knowledgeBaseUpdateRunning;
    }
    public String getKnowledgeBaseVersionBeingInstalled() {
        return knowledgeBaseVersionBeingInstalled;
    }
    public boolean isHtsKnowledgeBaseUpdateRunning() {
        return htsKnowledgeBaseUpdateRunning;
    }
    public String getHtsKnowledgeBaseVersion() {
        return htsKnowledgeBaseVersion;
    }
    public boolean isNewHtsKnowledgeBaseAvailable() {
        return newHtsKnowledgeBaseAvailable;
    }
}
