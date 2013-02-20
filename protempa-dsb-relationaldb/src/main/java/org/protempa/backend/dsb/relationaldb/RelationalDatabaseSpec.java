/*
 * #%L
 * Protempa Commons Backend Provider
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
package org.protempa.backend.dsb.relationaldb;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.arp.javautil.arrays.Arrays;
import org.protempa.backend.DataSourceBackendFailedValidationException;
import org.protempa.KnowledgeSource;
import org.protempa.KnowledgeSourceReadException;
import org.protempa.PropertyDefinition;
import org.protempa.PropositionDefinition;
import org.protempa.proposition.value.GranularityFactory;
import org.protempa.proposition.value.UnitFactory;

/**
 * Specifies mappings from propositions to relational database tables. Instances
 * of this class tell a {@link RelationalDatabaseDataSourceBackend} how to query
 * a relational database for propositions.
 * 
 * @author Andrew Post
 */
public final class RelationalDatabaseSpec implements Serializable {

    private static final long serialVersionUID = -7404642542962229266L;
    private static final EntitySpec[] EMPTY_ES_ARR = new EntitySpec[0];
    private static final StagingSpec[] EMPTY_SS_ARR = new StagingSpec[0];
    private EntitySpec[] primitiveParameterSpecs = EMPTY_ES_ARR;
    private EntitySpec[] eventSpecs = EMPTY_ES_ARR;
    private EntitySpec[] constantSpecs = EMPTY_ES_ARR;
    private StagingSpec[] stagedSpecs = EMPTY_SS_ARR;
    private UnitFactory units;
    private GranularityFactory granularities;

    /**
     * Instantiates this class with no mappings or other field values.
     */
    public RelationalDatabaseSpec() {
    }

    /**
     * Instantiates this class with the specified mappings from primitive
     * parameters, events and constant parameters to relational database tables,
     * and unit and granularity types.
     * 
     * You can specify alternative versions of the same entity by creating
     * sequential entity specs with the same name, different column specs for
     * one or more properties or references, and mutually exclusive constraint
     * specs. One example of where this is useful is inpatient versus clinic
     * hospital encounters, for which dates might be stored differently.
     * 
     * @param primitiveParameterSpecs
     *            an {@link EntitySpec} array containing mappings from primitive
     *            parameters to relational database tables.
     * @param eventSpecs
     *            an {@link EntitySpec} array containing mappings from events to
     *            relational database tables.
     * @param constantSpecs
     *            an {@link EntitySpec} array containing mappings from constants to
     *            relational database tables.
     * @param stagedSpecs
     *            a {@link TableSpec} array containing the names of database tables
     *            to be staged before querying
     * @param units
     *            what {@link UnitFactory} from which to get units.
     * @param granularities
     *            what {@link GranularityFactory} from which to get
     *            granularities.
     */
    public RelationalDatabaseSpec(EntitySpec[] primitiveParameterSpecs,
            EntitySpec[] eventSpecs, EntitySpec[] constantSpecs,
            StagingSpec[] stagedSpecs, UnitFactory units,
            GranularityFactory granularities) {
        setPrimitiveParameterSpecs(primitiveParameterSpecs);
        setConstantSpecs(constantSpecs);
        setEventSpecs(eventSpecs);
        setStagedSpecs(stagedSpecs);
        this.units = units;
        this.granularities = granularities;
    }

    /**
     * Gets the granularity factory from which the
     * {@link RelationalDatabaseDataSourceBackend} should get granularities.
     * 
     * @return a {@link GranularityFactory}.
     */
    public GranularityFactory getGranularities() {
        return granularities;
    }

    /**
     * Sets the granularity factory from which the
     * {@link RelationalDatabaseDataSourceBackend} should get granularities.
     * 
     * @param granularities
     *            a {@link GranularityFactory}.
     */
    public void setGranularities(GranularityFactory granularities) {
        this.granularities = granularities;
    }

    public UnitFactory getUnits() {
        return units;
    }

    public void setUnits(UnitFactory units) {
        this.units = units;
    }

    public EntitySpec[] getConstantSpecs() {
        return constantSpecs.clone();
    }

    /**
     * You can specify alternative versions of the same entity by creating
     * sequential entity specs with the same name, different column specs for
     * one or more properties or references, and mutually exclusive constraint
     * specs. One example of where this is useful is inpatient versus clinic
     * hospital encounters, for which dates might be stored differently.
     * 
     * @param constantParameterSpecs
     */
    public void setConstantSpecs(EntitySpec[] constantParameterSpecs) {
        if (constantParameterSpecs == null) {
            this.constantSpecs = EMPTY_ES_ARR;
        } else {
            this.constantSpecs = constantParameterSpecs.clone();
        }
    }

    public EntitySpec[] getEventSpecs() {
        return eventSpecs.clone();
    }

    /**
     * You can specify alternative versions of the same entity by creating
     * sequential entity specs with the same name, different column specs for
     * one or more properties or references, and mutually exclusive constraint
     * specs. One example of where this is useful is inpatient versus clinic
     * hospital encounters, for which dates might be stored differently.
     * 
     * @param eventSpecs
     */
    public void setEventSpecs(EntitySpec[] eventSpecs) {
        if (eventSpecs == null) {
            this.eventSpecs = EMPTY_ES_ARR;
        } else {
            this.eventSpecs = eventSpecs.clone();
        }
    }

    public EntitySpec[] getPrimitiveParameterSpecs() {
        return primitiveParameterSpecs.clone();
    }

    /**
     * You can specify alternative versions of the same entity by creating
     * sequential entity specs with the same name, different column specs for
     * one or more properties or references, and mutually exclusive constraint
     * specs. One example of where this is useful is inpatient versus clinic
     * hospital encounters, for which dates might be stored differently.
     * 
     * @param primitiveParameterSpecs
     */
    public void setPrimitiveParameterSpecs(EntitySpec[] primitiveParameterSpecs) {
        if (primitiveParameterSpecs == null) {
            this.primitiveParameterSpecs = EMPTY_ES_ARR;
        } else {
            this.primitiveParameterSpecs = primitiveParameterSpecs.clone();
        }
    }
    
    public StagingSpec[] getStagedSpecs() {
        return stagedSpecs.clone();
    }
    
    public void setStagedSpecs(StagingSpec[] stagedSpecs) {
        if (stagedSpecs == null) {
            this.stagedSpecs = EMPTY_SS_ARR;
        } else {
            this.stagedSpecs = stagedSpecs.clone();
        }
    }

    public void validate(KnowledgeSource knowledgeSource)
            throws KnowledgeSourceReadException,
            DataSourceBackendFailedValidationException {
        List<EntitySpec> allSpecs = Arrays.asList(this.eventSpecs,
                this.constantSpecs, this.primitiveParameterSpecs);

        Logger logger = SQLGenUtil.logger();
        for (EntitySpec entitySpec : allSpecs) {
            String entitySpecName = entitySpec.getName();
            logger.log(Level.FINER, "Validating entity spec {0}",
                    entitySpecName);
            String[] propIds = entitySpec.getPropositionIds();
            Set<String> propNames = new HashSet<String>();
            PropertySpec[] propSpecs = entitySpec.getPropertySpecs();
            logger.finer("Checking for duplicate properties");
            for (PropertySpec propSpec : propSpecs) {
                String propSpecName = propSpec.getName();
                if (!propNames.add(propSpecName)) {
                    throw new DataSourceBackendFailedValidationException(
                            "Duplicate property name " + propSpecName
                                    + " in entity spec " + entitySpecName);
                }
            }
            logger.finer("No duplicate properties found");
            logger.finer("Checking for invalid proposition ids and properties");
            for (String propId : propIds) {
                PropositionDefinition propDef = knowledgeSource
                        .readPropositionDefinition(propId);
                if (!knowledgeSource.hasPropositionDefinition(propId)) {
                    throw new DataSourceBackendFailedValidationException(
                            "Invalid proposition id named in entity spec "
                                    + entitySpecName + ": " + propId);
                }
                PropertyDefinition[] propertyDefs = propDef
                        .getPropertyDefinitions();
                for (PropertyDefinition propertyDef : propertyDefs) {
                    String propName = propertyDef.getName();
                    if (!propNames.contains(propName)) {
                        throw new DataSourceBackendFailedValidationException(
                                "Required property " + propName
                                        + " missing from entity spec "
                                        + entitySpecName);
                    }

                }
            }
            logger.finer("No invalid proposition ids or properties found");
        }
    }
}
