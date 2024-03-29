/*
 * #%L
 * Protempa Test Suite
 * %%
 * Copyright (C) 2012 - 2013 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.protempa.test;

import java.io.File;
import org.protempa.backend.annotations.BackendInfo;
import org.protempa.backend.dsb.relationaldb.ColumnSpec;
import org.protempa.backend.dsb.relationaldb.EntitySpec;
import org.protempa.backend.dsb.relationaldb.JDBCDateTimeTimestampDateValueFormat;
import org.protempa.backend.dsb.relationaldb.JDBCDateTimeTimestampPositionParser;
import org.protempa.backend.dsb.relationaldb.JDBCPositionFormat;
import org.protempa.backend.dsb.relationaldb.JoinSpec;
import org.protempa.backend.dsb.relationaldb.PropertySpec;
import org.protempa.backend.dsb.relationaldb.ReferenceSpec;
import org.protempa.backend.dsb.relationaldb.RelationalDbDataSourceBackend;
import org.protempa.proposition.value.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.protempa.BackendCloseException;
import org.protempa.backend.BackendInitializationException;
import org.protempa.backend.BackendInstanceSpec;
import org.protempa.backend.DataSourceBackendInitializationException;
import org.protempa.backend.dsb.relationaldb.Operator;
import org.protempa.backend.dsb.relationaldb.mappings.Mappings;
import org.protempa.backend.dsb.relationaldb.mappings.ResourceMappingsFactory;

/**
 * Test data source backend (based on RegistryVM).
 * 
 * @author Michel Mansour
 */
@BackendInfo(displayName = "Protempa Test Database")
public final class TestDataSourceBackend extends RelationalDbDataSourceBackend {
    
    private static final AbsoluteTimeUnitFactory absTimeUnitFactory = new AbsoluteTimeUnitFactory();
    private static final AbsoluteTimeGranularityFactory absTimeGranularityFactory = new AbsoluteTimeGranularityFactory();
    private static final JDBCPositionFormat dtPositionParser = new JDBCDateTimeTimestampPositionParser();
    private static Logger logger = Logger.getLogger(TestDataSourceBackend.class.getName());
    private DataProvider dataProvider;
    private DataInserter inserter;

    /**
     * Initializes a new backend.
     */
    public TestDataSourceBackend() {
        setSchemaName("TEST");
        setDefaultKeyIdTable("PATIENT");
        setDefaultKeyIdColumn("PATIENT_KEY");
        setDefaultKeyIdJoinKey("PATIENT_KEY");
        setMappingsFactory(new ResourceMappingsFactory("/etc/mappings/", getClass()));
    }

    @Override
    public void initialize(BackendInstanceSpec config) throws BackendInitializationException {
        try {
            super.initialize(config);
            logger.log(Level.INFO, "Populating database");
            dataProvider = new XlsxDataProvider(new File("src/test/resources/dsb/sample-data.xlsx"));
            inserter = new DataInserter("jdbc:h2:mem:TEST;INIT=RUNSCRIPT FROM './src/test/resources/dsb/test-schema.sql'");
            inserter.insertPatients(dataProvider.getPatients());
            inserter.insertEncounters(dataProvider.getEncounters());
            inserter.insertProviders(dataProvider.getProviders());
            inserter.insertIcd9Diagnoses(dataProvider.getIcd9Diagnoses());
            inserter.insertIcd9Procedures(dataProvider.getIcd9Procedures());
            inserter.insertLabs(dataProvider.getLabs());
            inserter.insertVitals(dataProvider.getVitals());
            logger.log(Level.INFO, "Database populated");
        } catch (SQLException | DataProviderException ex) {
            throw new DataSourceBackendInitializationException(ex);
        }
    }

    @Override
    public void close() throws BackendCloseException {
        BackendCloseException superException = null;
        try {
            super.close();
        } catch (BackendCloseException ex) {
            superException = ex;
        }
        Exception exceptionToThrow = null;
        try {
            inserter.truncateTables();
        } catch (SQLException ex) {
            exceptionToThrow = ex;
        }

        try {
            inserter.close();
        } catch (SQLException ex) {
            if (exceptionToThrow == null) {
                exceptionToThrow = ex;
            } else {
                exceptionToThrow.addSuppressed(ex);
            }
        }

        if (superException != null) {
            if (exceptionToThrow != null) {
                superException.addSuppressed(exceptionToThrow);
            }
            throw superException;
        }
        if (exceptionToThrow != null) {
            throw new BackendCloseException(exceptionToThrow);
        }
    }
    
    
    
    @Override
    public String getKeyType() {
        return "Patient";
    }

    @Override
    public String getKeyTypeDisplayName() {
        return "patient";
    }

    @Override
    protected EntitySpec[] constantSpecs(String keyIdSchema, String keyIdTable, String keyIdColumn, String keyIdJoinKey) throws IOException {
        String schemaName = getSchemaName();
        EntitySpec[] constantSpecs = new EntitySpec[] {

                new EntitySpec(
                        "Patients",
                        null,
                        new String[] { "PatientAll" },
                        false,
                        new ColumnSpec(keyIdSchema, keyIdTable,
                                keyIdColumn),
                        new ColumnSpec[] { new ColumnSpec(keyIdSchema,
                                keyIdTable, keyIdColumn) },
                        null,
                        null,
                        new PropertySpec[] { new PropertySpec("patientId",
                                null, new ColumnSpec(keyIdSchema,
                                        keyIdTable, "PATIENT_KEY"),
                                ValueType.NOMINALVALUE) },
                        new ReferenceSpec[] {
                                new ReferenceSpec(
                                        "encounters",
                                        "Encounters",
                                        new ColumnSpec[] { new ColumnSpec(
                                                keyIdSchema,
                                                keyIdTable,
                                                new JoinSpec(
                                                        "PATIENT_KEY",
                                                        "PATIENT_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "ENCOUNTER",
                                                                "ENCOUNTER_KEY"))) },
                                        ReferenceSpec.Type.MANY),
                                new ReferenceSpec(
                                        "patientDetails",
                                        "Patient Details",
                                        new ColumnSpec[] { new ColumnSpec(
                                                keyIdSchema,
                                                keyIdTable, "PATIENT_KEY") },
                                        ReferenceSpec.Type.MANY)
                        }, null, null,
                        null, null, null, null, null, null,
                        new ColumnSpec(keyIdSchema, keyIdTable, "CREATE_DATE"),
                        new ColumnSpec(keyIdSchema, keyIdTable, "UPDATE_DATE"),
                        new ColumnSpec(keyIdSchema, keyIdTable, "DELETE_DATE")
                ),
                new EntitySpec(
                        "Patient Details",
                        null,
                        new String[] { "Patient" },
                        true,
                        new ColumnSpec(keyIdSchema, keyIdTable, keyIdColumn, new JoinSpec(keyIdJoinKey, "PATIENT_KEY", new ColumnSpec(schemaName, "PATIENT"))),
                        new ColumnSpec[] { new ColumnSpec(schemaName,
                                "PATIENT", "PATIENT_KEY") },
                        null,
                        null,
                        new PropertySpec[] {
                                new PropertySpec(
                                        "dateOfBirth",
                                        null,
                                        new ColumnSpec(schemaName,
                                                "PATIENT", "DOB"),
                                        ValueType.DATEVALUE,
                                        new JDBCDateTimeTimestampDateValueFormat()),
                                new PropertySpec(
                                        "patientId",
                                        null,
                                        new ColumnSpec(schemaName,
                                                "PATIENT", "PATIENT_KEY"),
                                        ValueType.NOMINALVALUE),
                                new PropertySpec("firstName", null,
                                        new ColumnSpec(schemaName, "PATIENT",
                                                "FIRST_NAME"),
                                        ValueType.NOMINALVALUE),
                                new PropertySpec("lastName", null,
                                        new ColumnSpec(schemaName, "PATIENT",
                                                "LAST_NAME"),
                                        ValueType.NOMINALVALUE),
                                new PropertySpec(
                                        "gender",
                                        null,
                                        new ColumnSpec(
                                                schemaName,
                                                "PATIENT",
                                                "GENDER",
                                                Operator.EQUAL_TO,
                                                getMappingsFactory()
                                                        .getInstance("gender_02232012.txt"),
                                                true), ValueType.NOMINALVALUE),
                                new PropertySpec(
                                        "race",
                                        null,
                                        new ColumnSpec(
                                                schemaName,
                                                "PATIENT",
                                                "RACE",
                                                Operator.EQUAL_TO,
                                                getMappingsFactory()
                                                        .getInstance("race_02232012.txt"),
                                                true), ValueType.NOMINALVALUE),
                                new PropertySpec(
                                        "ethnicity",
                                        null,
                                        new ColumnSpec(
                                                schemaName,
                                                "PATIENT",
                                                "RACE",
                                                Operator.EQUAL_TO,
                                                getMappingsFactory()
                                                        .getInstance("ethnicity_02232012.txt"),
                                                true), ValueType.NOMINALVALUE) },
                        new ReferenceSpec[] {
                                new ReferenceSpec(
                                        "encounters",
                                        "Encounters",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName,
                                                "PATIENT",
                                                new JoinSpec(
                                                        "PATIENT_KEY",
                                                        "PATIENT_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "ENCOUNTER",
                                                                "ENCOUNTER_KEY"))) },
                                        ReferenceSpec.Type.MANY),
                                new ReferenceSpec("patient", "Patients",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName, "PATIENT",
                                                "PATIENT_KEY") },
                                        ReferenceSpec.Type.ONE) }, null, null,
                        null, null, null, null, null, null,
                        new ColumnSpec(schemaName, "PATIENT", "CREATE_DATE"),
                        new ColumnSpec(schemaName, "PATIENT", "UPDATE_DATE"),
                        new ColumnSpec(schemaName, "PATIENT", "DELETE_DATE")),
                new EntitySpec(
                        "Providers",
                        null,
                        new String[] { "AttendingPhysician" },
                        false,
                        new ColumnSpec(keyIdSchema, keyIdTable,
                                keyIdColumn, new JoinSpec("PATIENT_KEY",
                                        "PATIENT_KEY", new ColumnSpec(
                                                schemaName, "ENCOUNTER",
                                                new JoinSpec("PROVIDER_KEY",
                                                        "PROVIDER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "PROVIDER"))))),
                        new ColumnSpec[] { new ColumnSpec(schemaName,
                                "PROVIDER", "PROVIDER_KEY") }, null, null,
                        new PropertySpec[] {
                                new PropertySpec("firstName", null,
                                        new ColumnSpec(schemaName, "PROVIDER",
                                                "FIRST_NAME"),
                                        ValueType.NOMINALVALUE),
                                new PropertySpec("lastName", null,
                                        new ColumnSpec(schemaName, "PROVIDER",
                                                "LAST_NAME"),
                                        ValueType.NOMINALVALUE) }, null, null,
                        null, null, null, null, null, null, null,
                        new ColumnSpec(schemaName, "PROVIDER", "CREATE_DATE"),
                        new ColumnSpec(schemaName, "PROVIDER", "UPDATE_DATE"),
                        new ColumnSpec(schemaName, "PROVIDER", "DELETE_DATE")),
        };
        return constantSpecs;
    }

    @Override
    protected EntitySpec[] eventSpecs(String keyIdSchema, String keyIdTable, String keyIdColumn, String keyIdJoinKey) throws IOException {
        String schemaName = getSchemaName();
        Mappings icd9DiagnosesMappings = getMappingsFactory().getInstance("icd9_diagnosis_02232012.txt");
        Mappings icd9ProcedureMappings = getMappingsFactory().getInstance("icd9_procedure_02232012.txt");
        Mappings medsMappings = getMappingsFactory().getInstance("meds_02232012.txt");
        EntitySpec[] eventSpecs = new EntitySpec[] {
                new EntitySpec(
                        "Encounters",
                        null,
                        new String[] { "Encounter" },
                        true,
                        new ColumnSpec(keyIdSchema, keyIdTable, keyIdColumn, new JoinSpec(keyIdJoinKey, "PATIENT_KEY", new ColumnSpec(schemaName, "PATIENT", new JoinSpec("PATIENT_KEY", "PATIENT_KEY", new ColumnSpec(schemaName, "ENCOUNTER"))))),
                        new ColumnSpec[] { new ColumnSpec(schemaName, "ENCOUNTER", "ENCOUNTER_KEY") },
                        new ColumnSpec(schemaName, "ENCOUNTER", "TS_START"),
                        new ColumnSpec(schemaName, "ENCOUNTER", "TS_END"),
                        new PropertySpec[] {
                                new PropertySpec("encounterId", null,
                                        new ColumnSpec(schemaName, "ENCOUNTER",
                                                "ENCOUNTER_KEY"),
                                        ValueType.NOMINALVALUE),
                                new PropertySpec(
                                        "type",
                                        null,
                                        new ColumnSpec(
                                                schemaName,
                                                "ENCOUNTER",
                                                "ENCOUNTER_TYPE",
                                                Operator.EQUAL_TO,
                                                getMappingsFactory()
                                                        .getInstance("type_encounter_02232012.txt"),
                                                true), ValueType.NOMINALVALUE),
                                new PropertySpec(
                                        "dischargeDisposition",
                                        null,
                                        new ColumnSpec(
                                                schemaName,
                                                "ENCOUNTER",
                                                "DISCHARGE_DISP",
                                                Operator.EQUAL_TO,
                                                getMappingsFactory()
                                                        .getInstance("disposition_discharge_02232012.txt"),
                                                true), ValueType.NOMINALVALUE), },
                        new ReferenceSpec[] {
                                new ReferenceSpec("patient", "Patients",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName, "ENCOUNTER",
                                                "PATIENT_KEY") },
                                        ReferenceSpec.Type.ONE),
                                new ReferenceSpec(
                                        "labs",
                                        "Labs",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName, "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "LABS_EVENT",
                                                                "EVENT_KEY"))) },
                                        ReferenceSpec.Type.MANY),
                                new ReferenceSpec(
                                        "meds",
                                        "Medication Orders",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName, "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "MEDS_EVENT",
                                                                "EVENT_KEY"))) },
                                        ReferenceSpec.Type.MANY),
                                new ReferenceSpec(
                                        "vitals",
                                        "Vitals",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName, "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "VITALS_EVENT",
                                                                "EVENT_KEY"))) },
                                        ReferenceSpec.Type.MANY),
                                new ReferenceSpec(
                                        "diagnosisCodes",
                                        "Diagnosis Codes",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName, "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "ICD9D_EVENT",
                                                                "EVENT_KEY"))) },
                                        ReferenceSpec.Type.MANY),
                                new ReferenceSpec(
                                        "procedures",
                                        "ICD9 Procedure Codes",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName, "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "ICD9P_EVENT",
                                                                "EVENT_KEY"))) },
                                        ReferenceSpec.Type.MANY),
                                new ReferenceSpec(
                                        "procedures",
                                        "CPT Procedure Codes",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName, "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "CPT_EVENT",
                                                                "EVENT_KEY"))) },
                                        ReferenceSpec.Type.MANY),
                                new ReferenceSpec("provider", "Providers",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName, "ENCOUNTER",
                                                "PROVIDER_KEY") },
                                        ReferenceSpec.Type.ONE),
                                new ReferenceSpec("patientDetails",
                                        "Patient Details",
                                        new ColumnSpec[] { new ColumnSpec(
                                                schemaName, "ENCOUNTER",
                                                "PATIENT_KEY") },
                                        ReferenceSpec.Type.ONE),
                        }, null,
                        null, null, null, null, AbsoluteTimeGranularity.DAY,
                        dtPositionParser, null,
                        new ColumnSpec(schemaName, "ENCOUNTER", "CREATE_DATE"),
                        new ColumnSpec(schemaName, "ENCOUNTER", "UPDATE_DATE"),
                        new ColumnSpec(schemaName, "ENCOUNTER", "DELETE_DATE")),
                new EntitySpec(
                        "Diagnosis Codes",
                        null,
                        icd9DiagnosesMappings.readTargets(),
                        true,
                        new ColumnSpec(
                                keyIdSchema,
                                keyIdTable,
                                keyIdColumn,
                                new JoinSpec(keyIdJoinKey, "PATIENT_KEY", new ColumnSpec(schemaName, "PATIENT", new JoinSpec(
                                        "PATIENT_KEY",
                                        "PATIENT_KEY",
                                        new ColumnSpec(schemaName, "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "ICD9D_EVENT"))))))),
                        new ColumnSpec[] { new ColumnSpec(schemaName,
                                "ICD9D_EVENT", "EVENT_KEY") },
                        new ColumnSpec(schemaName, "ICD9D_EVENT", "TS_OBX"),
                        null,
                        new PropertySpec[] { new PropertySpec(
                                "code",
                                null,
                                new ColumnSpec(
                                        schemaName,
                                        "ICD9D_EVENT",
                                        "ENTITY_ID",
                                        Operator.EQUAL_TO,
                                        icd9DiagnosesMappings),
                                ValueType.NOMINALVALUE), },
                        null,
                        null,
                        new ColumnSpec(
                                schemaName,
                                "ICD9D_EVENT",
                                "ENTITY_ID",
                                Operator.EQUAL_TO,
                                icd9DiagnosesMappings,
                                true), null, null, null,
                        AbsoluteTimeGranularity.MINUTE, dtPositionParser, null,
                        new ColumnSpec(schemaName, "ICD9D_EVENT", "CREATE_DATE"),
                        new ColumnSpec(schemaName, "ICD9D_EVENT", "UPDATE_DATE"),
                        new ColumnSpec(schemaName, "ICD9D_EVENT", "DELETE_DATE")),
                new EntitySpec(
                        "ICD9 Procedure Codes",
                        null,
                        icd9ProcedureMappings.readTargets(),
                        true,
                        new ColumnSpec(
                                keyIdSchema,
                                keyIdTable,
                                keyIdColumn,
                                new JoinSpec(keyIdJoinKey, "PATIENT_KEY", new ColumnSpec(schemaName, "PATIENT", 
                                new JoinSpec(
                                        "PATIENT_KEY",
                                        "PATIENT_KEY",
                                        new ColumnSpec(schemaName, "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "ICD9P_EVENT"))))))),
                        new ColumnSpec[] { new ColumnSpec(schemaName,
                                "ICD9P_EVENT", "EVENT_KEY") },
                        new ColumnSpec(schemaName, "ICD9P_EVENT", "TS_OBX"),
                        null,
                        new PropertySpec[] { new PropertySpec(
                                "code",
                                null,
                                new ColumnSpec(
                                        schemaName,
                                        "ICD9P_EVENT",
                                        "ENTITY_ID",
                                        Operator.EQUAL_TO,
                                        icd9ProcedureMappings),
                                ValueType.NOMINALVALUE) },
                        null,
                        null,
                        new ColumnSpec(
                                schemaName,
                                "ICD9P_EVENT",
                                "ENTITY_ID",
                                Operator.EQUAL_TO,
                                icd9ProcedureMappings,
                                true), null, null, null,
                        AbsoluteTimeGranularity.MINUTE, dtPositionParser, null,
                        new ColumnSpec(schemaName, "ICD9P_EVENT", "CREATE_DATE"),
                        new ColumnSpec(schemaName, "ICD9P_EVENT", "UPDATE_DATE"),
                        new ColumnSpec(schemaName, "ICD9P_EVENT", "DELETE_DATE")),
                new EntitySpec(
                        "Medication Orders",
                        null,
                        medsMappings.readTargets(),
                        true,
                        new ColumnSpec(
                                keyIdSchema,
                                keyIdTable,
                                keyIdColumn,
                                new JoinSpec(keyIdJoinKey, "PATIENT_KEY", new ColumnSpec(schemaName, "PATIENT", 
                                new JoinSpec("PATIENT_KEY", "PATIENT_KEY",
                                        new ColumnSpec(schemaName, "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "MEDS_EVENT"))))))),
                        new ColumnSpec[] { new ColumnSpec(schemaName,
                                "MEDS_EVENT", "EVENT_KEY") },
                        new ColumnSpec(schemaName, "MEDS_EVENT", "TS_OBX"),
                        null,
                        new PropertySpec[] {},
                        null,
                        null,
                        new ColumnSpec(
                                schemaName,
                                "MEDS_EVENT",
                                "ENTITY_ID",
                                Operator.EQUAL_TO,
                                medsMappings,
                                true), null, null, null,
                        AbsoluteTimeGranularity.MINUTE, dtPositionParser, null,
                        new ColumnSpec(schemaName, "MEDS_EVENT", "CREATE_DATE"),
                        new ColumnSpec(schemaName, "MEDS_EVENT", "UPDATE_DATE"),
                        new ColumnSpec(schemaName, "MEDS_EVENT", "DELETE_DATE")),
};
        return eventSpecs;
    }

    @Override
    protected EntitySpec[] primitiveParameterSpecs(String keyIdSchema, String keyIdTable, String keyIdColumn, String keyIdJoinKey) throws IOException {
        String schemaName = getSchemaName();
        Mappings labsMappings = getMappingsFactory().getInstance("labs_02232012.txt");
        Mappings vitalsMappings = getMappingsFactory().getInstance("vitals_result_types_02232012.txt");
        EntitySpec[] primitiveParameterSpecs = new EntitySpec[] {
                new EntitySpec(
                        "Labs",
                        null,
                        labsMappings.readTargets(),
                        true,
                        new ColumnSpec(
                                keyIdSchema,
                                keyIdTable,
                                keyIdColumn,
                                new JoinSpec(keyIdJoinKey, "PATIENT_KEY",
                                new ColumnSpec(schemaName, "PATIENT",
                                new JoinSpec("PATIENT_KEY", "PATIENT_KEY",
                                        new ColumnSpec(schemaName, "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "LABS_EVENT"))))))),
                        new ColumnSpec[] { new ColumnSpec(schemaName,
                                "LABS_EVENT", "EVENT_KEY") },
                        new ColumnSpec(schemaName, "LABS_EVENT", "TS_OBX"),
                        null,
                        new PropertySpec[] {
                                new PropertySpec("unitOfMeasure", null,
                                        new ColumnSpec(schemaName,
                                                "LABS_EVENT", "UNITS"),
                                        ValueType.NOMINALVALUE),
                                new PropertySpec("interpretation", null,
                                        new ColumnSpec(schemaName,
                                                "LABS_EVENT", "FLAG"),
                                        ValueType.NOMINALVALUE) },
                        null,
                        null,
                        new ColumnSpec(
                                schemaName,
                                "LABS_EVENT",
                                "ENTITY_ID",
                                Operator.EQUAL_TO,
                                labsMappings,
                                true), null, new ColumnSpec(schemaName,
                                "LABS_EVENT", "RESULT_STR"), ValueType.VALUE,
                        AbsoluteTimeGranularity.MINUTE, dtPositionParser, null,
                        new ColumnSpec(schemaName, "LABS_EVENT", "CREATE_DATE"),
                        new ColumnSpec(schemaName, "LABS_EVENT", "UPDATE_DATE"),
                        new ColumnSpec(schemaName, "LABS_EVENT", "DELETE_DATE")),
                new EntitySpec(
                        "Vitals",
                        null,
                        vitalsMappings.readTargets(),
                        true,
                        new ColumnSpec(
                                keyIdSchema,
                                keyIdTable,
                                keyIdColumn,
                                new JoinSpec(keyIdJoinKey, "PATIENT_KEY",
                                new ColumnSpec(schemaName, "PATIENT",
                                new JoinSpec(
                                        "PATIENT_KEY",
                                        "PATIENT_KEY",
                                        new ColumnSpec(
                                                schemaName,
                                                "ENCOUNTER",
                                                new JoinSpec("ENCOUNTER_KEY",
                                                        "ENCOUNTER_KEY",
                                                        new ColumnSpec(
                                                                schemaName,
                                                                "VITALS_EVENT"))))))),
                        new ColumnSpec[] { new ColumnSpec(schemaName,
                                "VITALS_EVENT", "EVENT_KEY") },
                        new ColumnSpec(schemaName, "VITALS_EVENT", "TS_OBX"),
                        null,
                        new PropertySpec[] {
                                new PropertySpec("unitOfMeasure", null,
                                        new ColumnSpec(schemaName,
                                                "VITALS_EVENT", "UNITS"),
                                        ValueType.NOMINALVALUE),
                                new PropertySpec("interpretation", null,
                                        new ColumnSpec(schemaName,
                                                "VITALS_EVENT", "FLAG"),
                                        ValueType.NOMINALVALUE) },
                        null,
                        null,
                        new ColumnSpec(
                                schemaName,
                                "VITALS_EVENT",
                                "ENTITY_ID",
                                Operator.EQUAL_TO,
                                vitalsMappings,
                                true), null, new ColumnSpec(schemaName,
                                "VITALS_EVENT", "RESULT_STR"), ValueType.VALUE,
                        AbsoluteTimeGranularity.MINUTE, dtPositionParser, AbsoluteTimeUnit.YEAR,
                        new ColumnSpec(schemaName, "VITALS_EVENT", "CREATE_DATE"),
                        new ColumnSpec(schemaName, "VITALS_EVENT", "UPDATE_DATE"),
                        new ColumnSpec(schemaName, "VITALS_EVENT", "DELETE_DATE")),

        };
        return primitiveParameterSpecs;
    }

    @Override
    public GranularityFactory getGranularityFactory() {
        return absTimeGranularityFactory;
    }

    @Override
    public UnitFactory getUnitFactory() {
        return absTimeUnitFactory;
    }

}
