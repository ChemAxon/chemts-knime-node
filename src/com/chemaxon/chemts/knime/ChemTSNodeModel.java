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

package com.chemaxon.chemts.knime;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.chemaxon.chemts.knime.dto.CheckListRequest;
import com.chemaxon.chemts.knime.dto.CheckListResults;
import com.chemaxon.chemts.knime.dto.HtsData;
import com.chemaxon.chemts.knime.rest.ChemTSCheckListInvoker;
import com.chemaxon.chemts.knime.rest.ChemTSCountryInvoker;
import com.chemaxon.chemts.knime.rest.ChemTSSystemInfoInvoker;
import com.chemaxon.chemts.knime.tabs.ConnectionSettingsTabFields;
import com.chemaxon.chemts.knime.tabs.OptionsTabFields;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class ChemTSNodeModel extends NodeModel {

    private static final NodeLogger logger = NodeLogger.getLogger(ChemTSNodeModel.class);

    private static final int DATA_CHUNK_SIZE = 10;

    private final OptionsTabFields optionFields;

    private final ConnectionSettingsTabFields connectionFields;

    protected ChemTSNodeModel() {
        super(1, 2);
        this.optionFields = new OptionsTabFields();
        this.connectionFields = new ConnectionSettingsTabFields();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {

        BufferedDataTable inputTable = inData[0];

        Iterable<List<DataRow>> inputTableChunks = Iterables.partition(inputTable, DATA_CHUNK_SIZE);

        int structureColIndex = inputTable.getDataTableSpec().findColumnIndex(optionFields.getStructureColName());

        DataTableSpec spec = new DataTableSpec(inputTable.getDataTableSpec(), getResultTableSpec());
        BufferedDataContainer resultDataContainer = exec.createDataContainer(spec);

        spec = new DataTableSpec(inputTable.getDataTableSpec(), getErrorTableSpec());
        BufferedDataContainer errorDataContainer = exec.createDataContainer(spec);

        List<String> countryCodes;
        if (optionFields.getCountries().isEmpty()) {
            countryCodes = new ChemTSCountryInvoker(connectionFields).getCountries().keySet().stream().collect(toList());
        } else {
            countryCodes = new ChemTSCountryInvoker(connectionFields).getCountryCodesByNames(optionFields.getCountries());
        }

        ChemTSCheckListInvoker checkListInvoker = new ChemTSCheckListInvoker(connectionFields);

        int count = 0;
        for (List<DataRow> inputDataRows : inputTableChunks) {

            CheckListRequest checkListRequest = new CheckListRequest();
            inputDataRows.stream()
                    .map(dataRow -> dataRow.getCell(structureColIndex))
                    .map(DataCell::toString)
                    .collect(toCollection(checkListRequest::getInputs));
            checkListRequest.setCountryCodes(countryCodes);
            checkListRequest.setMolFormat(optionFields.getMolFormat());

            try {
                List<CheckListResults> results = checkListInvoker.check(checkListRequest).getResults();

                int inputIndex = 0;

                for (CheckListResults result : results) {
                    for (String countryCode : countryCodes) {
                        if (Strings.isNullOrEmpty(result.getErrorMessage())) {
                            List<HtsData> htsDatas = result.getHtsData().stream()
                                    .filter(htsData -> htsData.getCountryCode().equals(countryCode)).collect(toList());
                            if (htsDatas.isEmpty()) {
                                List<DataCell> dataCells = inputDataRows.get(inputIndex).stream().collect(toList());
                                dataCells.add(new StringCell(countryCode));
                                dataCells.add(new StringCell("No cHemTS data found."));
                                DataRow row = new DefaultRow(new RowKey("Row" + errorDataContainer.size()), dataCells);
                                errorDataContainer.addRowToTable(row);
                            }
                            for (HtsData htsData : htsDatas) {
                                List<DataCell> dataCells = inputDataRows.get(inputIndex).stream().collect(toList());
                                dataCells.add(new StringCell(countryCode));
                                dataCells.add(new StringCell(htsData.getHtsNumber()));
                                dataCells.add(new StringCell(htsData.getFullDescription()));
                                dataCells.add(new StringCell(String.join(", ", htsData.getUnits())));
                                dataCells.add(new StringCell(htsData.getGeneral() == null ? "" : htsData.getGeneral()));
                                dataCells.add(new StringCell(htsData.getSpecial() == null ? "" : htsData.getSpecial()));
                                dataCells.add(new StringCell(
                                        htsData.getQuotaQuantity() == null ? "" : htsData.getQuotaQuantity()));
                                dataCells.add(new StringCell(htsData.getOther() == null ? "" : htsData.getOther()));
                                dataCells.add(new StringCell(result.getPharmaAgreement().get(countryCode)));
                                dataCells.add(new StringCell(result.getDrugInfo().get(countryCode)));
                                DataRow row = new DefaultRow(new RowKey("Row" + resultDataContainer.size()), dataCells);
                                resultDataContainer.addRowToTable(row);
                            }
                        } else {
                            List<DataCell> dataCells = inputDataRows.get(inputIndex).stream().collect(toList());
                            dataCells.add(new StringCell(countryCode));
                            dataCells.add(new StringCell(result.getErrorMessage()));
                            DataRow row = new DefaultRow(new RowKey("Row" + errorDataContainer.size()), dataCells);
                            errorDataContainer.addRowToTable(row);
                        }
                    }
                    inputIndex++;
                }
            } catch (Exception e) {
                for (DataRow inputRow : inputDataRows) {
                    for (String countryCode : countryCodes) {
                        RowKey key = new RowKey("Row" + errorDataContainer.size());
                        List<DataCell> dataCells = inputRow.stream().collect(Collectors.toList());
                        dataCells.add(new StringCell(countryCode));
                        if (e.getCause() instanceof SocketTimeoutException) {
                            dataCells.add(new StringCell(e.getCause().getMessage()));
                        } else {
                            dataCells.add(new StringCell(e.getMessage()));
                        }
                        DataRow outputRow = new DefaultRow(key, dataCells.toArray(new DataCell[0]));
                        errorDataContainer.addRowToTable(outputRow);
                    }
                }
                logger.error("Error during service call.", e);
            }
            // check if the execution monitor was canceled
            exec.checkCanceled();

            double checkedStructNum = DATA_CHUNK_SIZE * count++;
            double progress = checkedStructNum / inputTable.size();
            exec.setProgress(progress, "Number of checked structures: " + checkedStructNum);
        }

        errorDataContainer.close();
        resultDataContainer.close();
        return new BufferedDataTable[] { resultDataContainer.getTable(), errorDataContainer.getTable() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        validateConnectionDetails();
        return new DataTableSpec[] { new DataTableSpec(inSpecs[0], getResultTableSpec()),
                new DataTableSpec(inSpecs[0], getErrorTableSpec()) };
    }

    private DataTableSpec getResultTableSpec() {
        return new DataTableSpec(
                new DataColumnSpec[] {
                        new DataColumnSpecCreator("Country code", StringCell.TYPE).createSpec(),
                        new DataColumnSpecCreator("HS code", StringCell.TYPE).createSpec(),
                        new DataColumnSpecCreator("HS description", StringCell.TYPE).createSpec(),
                        new DataColumnSpecCreator("Units", StringCell.TYPE).createSpec(),
                        new DataColumnSpecCreator("General", StringCell.TYPE).createSpec(),
                        new DataColumnSpecCreator("Special", StringCell.TYPE).createSpec(),
                        new DataColumnSpecCreator("Quota quantity", StringCell.TYPE).createSpec(),
                        new DataColumnSpecCreator("Other", StringCell.TYPE).createSpec(),
                        new DataColumnSpecCreator("Pharma Agreement", StringCell.TYPE).createSpec(),
                        new DataColumnSpecCreator("Drug Info", StringCell.TYPE).createSpec() });
    }

    private DataTableSpec getErrorTableSpec() {
        return new DataTableSpec(
                new DataColumnSpec[] {
                        new DataColumnSpecCreator("Country code", StringCell.TYPE).createSpec(),
                        new DataColumnSpecCreator("Error message", StringCell.TYPE).createSpec() });
    }

    private void validateConnectionDetails() throws InvalidSettingsException {

        if (connectionFields.getHost().isEmpty()) {
            throw new InvalidSettingsException("cHemTS host is not specified.");
        }
        if (connectionFields.getAuthType().equals(ConnectionSettingsTabFields.AUTH_OAUTH2)
                && connectionFields.getTokenUrl().isEmpty()) {
            throw new InvalidSettingsException("Token endpoint is not specified.");
        }

        try {
            // validate connection settings by invoking a service
            new ChemTSSystemInfoInvoker(connectionFields).getSystemInfo();
        } catch (Exception e) {
            setWarningMessage("Failed to connect to cHemTS. "
                    + "Please check if connection settings are correct.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        optionFields.saveSettingsTo(settings);
        connectionFields.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        optionFields.loadSettingsFrom(settings);
        connectionFields.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        connectionFields.validateSettings(settings);
        optionFields.validateSettings(settings);
    }

    @Override
    protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        /*
         * Advanced method, usually left empty. Everything that is
         * handed to the output ports is loaded automatically (data returned by the execute
         * method, models loaded in loadModelContent, and user settings set through
         * loadSettingsFrom - is all taken care of). Only load the internals
         * that need to be restored (e.g. data used by the views).
         */
    }

    @Override
    protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        /*
         * Advanced method, usually left empty. Everything
         * written to the output ports is saved automatically (data returned by the execute
         * method, models saved in the saveModelContent, and user settings saved through
         * saveSettingsTo - is all taken care of). Save only the internals
         * that need to be preserved (e.g. data used by the views).
         */
    }

    @Override
    protected void reset() {
        /*
         * Code executed on a reset of the node. Models built during execute are cleared
         * and the data handled in loadInternals/saveInternals will be erased.
         */
    }
}
