/*
 * #%L
 * Protempa Commons Backend Provider
 * %%
 * Copyright (C) 2012 Emory University
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
package org.protempa.bp.commons.dsb.relationaldb;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.protempa.bp.commons.dsb.relationaldb.ColumnSpec.KnowledgeSourceIdToSqlCode;

final class StagingSelectClause implements SelectClause {

    private final StagingSpec stagingSpec;
    private final EntitySpec entitySpec;
    private final TableAliaser referenceIndices;

    public StagingSelectClause(StagingSpec stagingSpec,
            EntitySpec entitySpec, TableAliaser referenceIndices) {
        this.stagingSpec = stagingSpec;
        this.entitySpec = entitySpec;
        this.referenceIndices = referenceIndices;
    }

    @Override
    public String generateClause() {
        StringBuilder result = new StringBuilder();

        result.append("SELECT ");

        List<String> cols = new ArrayList<String>();
        for (StagedColumnSpec columnSpec : this.stagingSpec.getStagedColumns()) {
            StringBuilder colStr = new StringBuilder(
                    referenceIndices.generateColumnReference(columnSpec
                            .toColumnSpec()));
            if (null != columnSpec.getAsName()
                    && !columnSpec.getAsName().isEmpty()) {
                if (isForEntitySpec(columnSpec, entitySpec)) {
                    String asCol = colStr.toString() + " AS " + columnSpec.getAsName();
                    cols.add(0, asCol);
                }
            }
            cols.add(colStr.toString());
        }
        result.append(StringUtils.join(cols, ','));

        return result.toString();
    }

    private static boolean isForEntitySpec(StagedColumnSpec columnSpec, EntitySpec entitySpec) {
        for (String esName : columnSpec.getForEntitySpecs()) {
            if (esName.equals(entitySpec.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setCaseClause(Object[] sqlCodes, ColumnSpec columnSpec,
            KnowledgeSourceIdToSqlCode[] filteredConstraintValues) {
        throw new UnsupportedOperationException();
    }

}
