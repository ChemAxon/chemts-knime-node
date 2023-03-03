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

import static com.chemaxon.chemts.knime.tabs.OptionsTabFields.CFGKEY_COUNTRIES;
import static com.chemaxon.chemts.knime.tabs.OptionsTabFields.CFGKEY_MOL_FORMAT;
import static com.chemaxon.chemts.knime.tabs.OptionsTabFields.CFGKEY_STRUCTURE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ListSelectionModel;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButton;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.util.DataValueColumnFilter;

import com.chemaxon.chemts.knime.rest.ChemTSCountryInvoker;
import com.chemaxon.chemts.knime.rest.RestConnectionDetails;

public class OptionsTabComponent {

    private final DefaultNodeSettingsPane pane;

    private final RestConnectionDetails connectionDetails;

    private final SettingsModelColumnName m_structure = new SettingsModelColumnName(CFGKEY_STRUCTURE, "");

    private final SettingsModelStringArray m_countries = new SettingsModelStringArray(CFGKEY_COUNTRIES, new String[] {});

    private final SettingsModelString m_molFormat = new SettingsModelString(CFGKEY_MOL_FORMAT, "");

    private DialogComponentStringListSelection countrySelectionComponent;

    public OptionsTabComponent(DefaultNodeSettingsPane pane, RestConnectionDetails connectionDetails) {
        this.pane = pane;
        this.connectionDetails = connectionDetails;
    }

    public void addDialogComponents() {

        DataValueColumnFilter columnFilter = new DataValueColumnFilter(StringValue.class);
        pane.addDialogComponent(
                new DialogComponentColumnNameSelection(m_structure, "Structure column:", 0, true, columnFilter));

        DialogComponentLabel errorMsgLabel = new DialogComponentLabel("");
        pane.addDialogComponent(errorMsgLabel);

        DialogComponentButton loadButton = new DialogComponentButton("Load/Refresh countries");
        countrySelectionComponent =
                new DialogComponentStringListSelection(m_countries, "Countries:", new ArrayList<>(),
                        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, false, 10);

        loadButton.addActionListener(new LoadCountriesActionListener(errorMsgLabel));

        pane.addDialogComponent(loadButton);
        pane.addDialogComponent(countrySelectionComponent);

        pane.addDialogComponent(new DialogComponentString(m_molFormat, "Molecule format:", false, 20));

    }

    public void initCountrySelectionComponent() {
        List<String> countryNames = new ChemTSCountryInvoker(connectionDetails).getCountryNames();
        Collections.sort(countryNames, String.CASE_INSENSITIVE_ORDER);
        countrySelectionComponent.replaceListItems(countryNames, (String[]) null);
    }

    private class LoadCountriesActionListener implements ActionListener {

        private DialogComponentLabel errorMsgLabel;

        public LoadCountriesActionListener(DialogComponentLabel errorMsgLabel) {
            this.errorMsgLabel = errorMsgLabel;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                initCountrySelectionComponent();
                errorMsgLabel.setText("");
            } catch (Exception e) {
                errorMsgLabel.setText("<html>Failed to connect to ChemTS."
                        + "<br>Please make sure connection settings are correct.<html>");
            }
        }
    }
}
