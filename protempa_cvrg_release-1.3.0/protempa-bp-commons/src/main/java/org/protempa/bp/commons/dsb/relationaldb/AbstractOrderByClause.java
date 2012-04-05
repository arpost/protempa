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

abstract class AbstractOrderByClause implements OrderByClause {

    private final ColumnSpec startColumnSpec;
    private final ColumnSpec finishColumnSpec;
    private final SQLOrderBy order;
    private final TableAliaser referenceIndices;

    AbstractOrderByClause(ColumnSpec startColumnSpec,
            ColumnSpec finishColumnSpec, SQLOrderBy order,
            TableAliaser referenceIndices) {
        this.startColumnSpec = startColumnSpec;
        this.finishColumnSpec = finishColumnSpec;
        this.order = order;
        this.referenceIndices = referenceIndices;
    }

    @Override
    public String generateClause() {
        StringBuilder result = new StringBuilder(" order by ");
        result.append(referenceIndices.generateColumnReference(startColumnSpec));
        if (referenceIndices.getIndex(finishColumnSpec) > 0) {
            result.append(',');
            result.append(referenceIndices
                    .generateColumnReference(finishColumnSpec));
        }
        result.append(' ');
        if (order == SQLOrderBy.ASCENDING) {
            result.append("ASC");
        } else {
            result.append("DESC");
        }

        return result.toString();
    }

}
