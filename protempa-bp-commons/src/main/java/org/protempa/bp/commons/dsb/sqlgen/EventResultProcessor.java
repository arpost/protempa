package org.protempa.bp.commons.dsb.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.arp.javautil.collections.Collections;
import org.protempa.DatabaseDataSourceType;
import org.protempa.proposition.Event;
import org.protempa.proposition.Interval;
import org.protempa.proposition.IntervalFactory;
import org.protempa.proposition.UniqueIdentifier;
import org.protempa.proposition.value.Granularity;
import org.protempa.proposition.value.Value;
import org.protempa.proposition.value.ValueFactory;
import org.protempa.proposition.value.ValueType;

class EventResultProcessor extends AbstractMainResultProcessor<Event> {
    private static final IntervalFactory intervalFactory =
            new IntervalFactory();

    @Override
    public void process(ResultSet resultSet) throws SQLException {
        Map<String, List<Event>> results = getResults();
        EntitySpec entitySpec = getEntitySpec();
        Logger logger = SQLGenUtil.logger();
        String[] propIds = entitySpec.getPropositionIds();
        while (resultSet.next()) {
            int i = 1;
            String keyId = resultSet.getString(i++);
            String[] uniqueIds = generateUniqueIdsArray(entitySpec);
            i = readUniqueIds(uniqueIds, resultSet, i);
            UniqueIdentifier uniqueIdentifier = generateUniqueIdentifier(
                    entitySpec, uniqueIds);
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
            ColumnSpec finishTimeSpec = entitySpec.getFinishTimeSpec();
            Granularity gran = entitySpec.getGranularity();
            Interval interval = null;
            if (finishTimeSpec == null) {
                Long d = null;
                try {
                    d = entitySpec.getPositionParser().toLong(
                            resultSet, i++);
                } catch (SQLException e) {
                    logger.log(Level.WARNING,
                            "Could not parse timestamp. Leaving the finish time unset.",
                            e);
                }
                interval = intervalFactory.getInstance(d, gran);
            } else {
                Long start = null;
                try {
                    start = entitySpec.getPositionParser().toLong(
                            resultSet, i++);
                } catch (SQLException e) {
                    logger.log(Level.WARNING,
                            "Could not parse start time. Leaving the start time/timestamp unset.",
                            e);
                }
                Long finish = null;
                try {
                    finish = entitySpec.getPositionParser().toLong(resultSet,
                            i++);
                } catch (SQLException e) {
                    logger.log(Level.WARNING,
                            "Could not parse start time. Leaving the finish time unset.",
                            e);
                }
                try {
                    interval = intervalFactory.getInstance(start, gran,
                            finish, gran);
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING,
                            "Finish is before start", e);
                    interval = intervalFactory.getInstance(null, gran,
                            null, gran);
                }
            }
            if (isCasePresent()) {
                propId = resultSet.getString(i++);
            }
            Event event = new Event(propId);
            event.setDataSourceType(
                    new DatabaseDataSourceType(getDataSourceBackendId()));
            event.setUniqueIdentifier(uniqueIdentifier);
            event.setInterval(interval);
            PropertySpec[] propertySpecs = entitySpec.getPropertySpecs();
            for (PropertySpec propertySpec : propertySpecs) {
                ValueType valueType = propertySpec.getValueType();
                Value value = ValueFactory.get(valueType).parseValue(
                        resultSet.getString(i++));
                event.setProperty(propertySpec.getName(), value);
            }
            logger.log(Level.FINEST, "Created event {0}", event);
            Collections.putList(results, keyId, event);
        }
    }

    private int eventSetUniqueIdentifier(String[] uniqueIds,
            EntitySpec entitySpec,
            ResultSet resultSet, int i, Event event) throws SQLException {
        i = readUniqueIds(uniqueIds, resultSet, i);
        UniqueIdentifier uniqueIdentifer = generateUniqueIdentifier(entitySpec,
                uniqueIds);
        event.setUniqueIdentifier(uniqueIdentifer);
        return i;
    }
}