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

import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

public class OptionsTabFields {

    public static final String CFGKEY_STRUCTURE = "Structure";

    public static final String CFGKEY_COUNTRIES = "Countries";

    public static final String CFGKEY_MOL_FORMAT = "Molecule format";

    private final SettingsModelColumnName m_stucture = new SettingsModelColumnName(CFGKEY_STRUCTURE, "");

    private final SettingsModelString m_molFormat = new SettingsModelString(CFGKEY_MOL_FORMAT, "");

    private final SettingsModelStringArray m_countries = new SettingsModelStringArray(CFGKEY_COUNTRIES, new String[0]);

    public String getStructure() {
        return m_stucture.getStringValue();
    }

    public String getStructureColName() {
        return m_stucture.getColumnName();
    }

    public List<String> getCountries() {
        return Arrays.asList(m_countries.getStringArrayValue());
    }

    public String getMolFormat() {
        return m_molFormat.getStringValue();
    }

    public void saveSettingsTo(NodeSettingsWO settings) {
        m_stucture.saveSettingsTo(settings);
        m_countries.saveSettingsTo(settings);
        m_molFormat.saveSettingsTo(settings);
    }

    public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
        m_stucture.loadSettingsFrom(settings);
        m_countries.loadSettingsFrom(settings);
        m_molFormat.loadSettingsFrom(settings);
    }

    public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
        m_stucture.validateSettings(settings);
        m_countries.validateSettings(settings);
        m_molFormat.validateSettings(settings);
    }
}
