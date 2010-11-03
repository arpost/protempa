package org.protempa.bp.commons.dsb.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.arp.javautil.collections.Collections;
import org.protempa.DatabaseDataSourceType;
import org.protempa.proposition.PrimitiveParameter;
import org.protempa.proposition.UniqueIdentifier;
import org.protempa.proposition.value.Value;
import org.protempa.proposition.value.ValueFactory;
import org.protempa.proposition.value.ValueType;

class PrimitiveParameterResultProcessor extends
        AbstractMainResultProcessor<PrimitiveParameter> {

    @Override
    public void process(ResultSet resultSet) throws SQLException {
        Map<String, List<PrimitiveParameter>> results = getResults();
        EntitySpec entitySpec = getEntitySpec();
        String[] propIds = entitySpec.getPropositionIds();
        Logger logger = SQLGenUtil.logger();
        while (resultSet.next()) {
            int i = 1;
            String keyId = resultSet.getString(i++);
            String[] uniqueIds = generateUniqueIdsArray(entitySpec);
            i = readUniqueIds(uniqueIds, resultSet, i);
            UniqueIdentifier uniqueIdentifer = generateUniqueIdentifier(
                    entitySpec, uniqueIds);
            ValueType vf = entitySpec.getValueType();
            Value cpVal = ValueFactory.get(vf).parseValue(
                    resultSet.getString(i++));
            String propId = null;
            if (!isCasePresent()) {
                if (propIds.length == 1) {
                    propId = propIds[0];
                } else {
                    propId = resultSet.getString(i++);
                }
            } else {
                i++;
            }
            Long timestamp = null;
            try {
                timestamp = entitySpec.getPositionParser().toLong(resultSet,
                        i++);
            } catch (SQLException e) {
                logger.log(Level.WARNING,
                        "Could not parse timestamp. Ignoring data value.", e);
                continue;
            }
            if (isCasePresent()) {
                propId = resultSet.getString(i++);
            }
            PrimitiveParameter p = new PrimitiveParameter(propId);
            p.setTimestamp(timestamp);
            p.setDataSourceType(new DatabaseDataSourceType(getDataSourceBackendId()));
            p.setUniqueIdentifier(uniqueIdentifer);
            p.setGranularity(entitySpec.getGranularity());
            p.setValue(cpVal);
            PropertySpec[] propertySpecs = entitySpec.getPropertySpecs();
            for (PropertySpec propertySpec : propertySpecs) {
                ValueType vf2 = propertySpec.getValueType();
                Value value = ValueFactory.get(vf2).parseValue(
                        resultSet.getString(i++));
                p.setProperty(propertySpec.getName(), value);
            }
            p.setDataSourceType(
                    new DatabaseDataSourceType(getDataSourceBackendId()));
            logger.log(Level.FINEST, "Created primitive parameter {0}", p);
            Collections.putList(results, keyId, p);
        }
    }
}