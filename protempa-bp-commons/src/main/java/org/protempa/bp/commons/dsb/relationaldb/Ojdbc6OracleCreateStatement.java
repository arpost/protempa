package org.protempa.bp.commons.dsb.relationaldb;

import java.util.List;
import java.util.Set;

import org.protempa.backend.dsb.filter.Filter;

class Ojdbc6OracleCreateStatement extends AbstractCreateStatement {

    Ojdbc6OracleCreateStatement(SimpleStagingSpec stagingSpec,
            ReferenceSpec referenceSpec, List<EntitySpec> entitySpecs,
            Set<Filter> filters, Set<String> propIds, Set<String> keyIds,
            SQLOrderBy order, SQLGenResultProcessor resultProcessor,
            StagingSpec[] stagedTables) {
        super(stagingSpec, referenceSpec, entitySpecs, filters, propIds,
                keyIds, order, resultProcessor);
    }

    @Override
    public SelectStatement getSelectStatement(EntitySpec entitySpec,
            ReferenceSpec referenceSpec, List<EntitySpec> entitySpecs,
            Set<Filter> filters, Set<String> propIds, Set<String> keyIds,
            SQLOrderBy order, SQLGenResultProcessor resultProcessor) {
        return new Ojdbc6OracleSelectStatement(entitySpec, referenceSpec,
                entitySpecs, filters, propIds, keyIds, order, resultProcessor,
                null);
    }

}
