package org.protempa.dsb;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.protempa.dsb.datasourceconstraint.DataSourceConstraint;

import org.protempa.DataSourceReadException;
import org.protempa.backend.BackendInstanceSpec;
import org.protempa.proposition.ConstantParameter;
import org.protempa.proposition.Event;
import org.protempa.proposition.PrimitiveParameter;
import org.protempa.proposition.value.GranularityFactory;
import org.protempa.proposition.value.UnitFactory;


/**
 * Translates local database queries in the standard interface into the local
 * database's schema.
 * 
 * @author Andrew Post
 */
public interface SchemaAdaptor {

    /**
     * Initializes a schema adaptor. This method must be called before any
     * others.
     *
     * @param config
     *            configuration <code>Properties</code>, specific to an
     *            implementation of this interface.
     * @return <code>true</code> if initialization succeeded,
     *         <code>false</code> otherwise.
     */
    void initialize(BackendInstanceSpec config)
        throws SchemaAdaptorInitializationException ;

    /**
     * Returns an object for accessing the granularity of returned data from
     * this schema adaptor.
     *
     * @return a {@link GranularityFactory}, should never be <code>null</code>.
     */
    GranularityFactory getGranularityFactory();

    /**
     * Returns an object for accessing the length units of returned data from
     * this schema adaptor.
     *
     * @return a {@link UnitFactory}, should never be <code>null</code>.
     */
    UnitFactory getUnitFactory();

    Map<String, List<PrimitiveParameter>> getPrimitiveParametersAsc(
                    Set<String> paramIds,
                    DataSourceConstraint dataSourceConstraints)
        throws DataSourceReadException;

    Map<String, List<PrimitiveParameter>> getPrimitiveParametersAsc(
                    Set<String> keyIds, Set<String> paramIds,
                    DataSourceConstraint dataSourceConstraints)
        throws DataSourceReadException;

    /**
     * Gets all primitive parameters with the given attribute id and parameter
     * ids in ascending order.
     *
     * @param keyId
     *            a key id <code>String</code>.
     * @param attributeId
     *            an attribute id <code>String</code>.
     * @param paramIds
     *            a <code>Set</code> of parameter id <code>String</code>
     *            objects.
     * @param minValid
     *            the minimum valid period as a <code>Long</code>.
     * @param maxValid
     *            the maximum valid period as a <code>Long</code>.
     * @return a newly-created {@link List<PrimitiveParameter>} in ascending
     *         order.
     */
    List<PrimitiveParameter> getPrimitiveParametersAsc(String keyId,
                    Set<String> paramIds, 
                    DataSourceConstraint dataSourceConstraints)
        throws DataSourceReadException;

    /**
     * Gets all primitive parameters with the given attribute id and parameter
     * ids in descending order.
     *
     * @param keyId
     *            a key id <code>String</code>.
     * @param attributeId
     *            an attribute id <code>String</code>.
     * @param paramIds
     *            a <code>Set</code> of parameter id <code>String</code>
     *            objects.
     * @param minValid
     *            the minimum valid period as a <code>Long</code>.
     * @param maxValid
     *            the maximum valid period as a <code>Long</code>.
     * @return a newly-created {@link List<PrimitiveParameter>} in descending
     *         order.
     */
    List<PrimitiveParameter> getPrimitiveParametersDesc(String keyId,
                    Set<String> paramIds, 
                    DataSourceConstraint dataSourceConstraints)
        throws DataSourceReadException;

    /**
     * Returns a list of all key ids in this data set optionally that meet
     * a set of specified constraints.
     *
     * @param start the first key id to retrieve, must be >= 0.
     * @param count the number of key ids to retrieve,
     * must be > 1.
     * @param dataSourceConstraints a {@link DataSourceConstraint} with
     * position and value constraints that reduce the number of key ids
     * returned. If <code>null</code>, no constraints will be applied.
     * @return a newly-created {@link List} of {@link String}s.
     * @throws DataSourceReadException if there was an error reading from
     * the database.
     */
    List<String> getAllKeyIds(int start, int finish,
            DataSourceConstraint dataSourceConstraints)
        throws DataSourceReadException;

    List<ConstantParameter> getConstantParameters(String keyId,
        Set<String> paramIds)
        throws DataSourceReadException;

    Map<String, List<ConstantParameter>> getConstantParameters(
            Set<String> keyIds, Set<String> paramIds)
            throws DataSourceReadException;

    Map<String, List<Event>> getEventsAsc(Set<String> eventIds, 
                    DataSourceConstraint dataSourceConstraints)
        throws DataSourceReadException;

    Map<String, List<Event>> getEventsAsc(Set<String> keyIds,
                    Set<String> paramIds, 
                    DataSourceConstraint dataSourceConstraints)
        throws DataSourceReadException;

    /**
     * Gets all events with the given attribute id and event ids in ascending
     * order.
     *
     * @param keyId
     *            a key id <code>String</code>.
     * @param attributeId
     *            an attribute id <code>String</code>.
     * @param eventIds
     *            a <code>Set</code> of event id <code>String</code>s.
     * @param minValid
     *            the minimum valid period as a <code>Long</code>.
     * @param maxValid
     *            the maximum valid period as a <code>Long</code>.
     * @return a newly-created {@link List<Event>} in ascending order.
     */
    List<Event> getEventsAsc(String keyId,
                    Set<String> eventIds, 
                    DataSourceConstraint dataSourceConstraints)
        throws DataSourceReadException;

    /**
     * Gets all events with the given attribute id and event ids in descending
     * order.
     *
     * @param keyId
     *            a key id <code>String</code>.
     * @param attributeId
     *            an attribute id <code>String</code>.
     * @param eventIds
     *            a <code>Set</code> of event id <code>String</code>s.
     * @param minValid
     *            the minimum valid period as a <code>Long</code>.
     * @param maxValid
     *            the maximum valid period as a <code>Long</code>.
     * @return a newly-created {@link List<Event>} in descending order.
     */
    List<Event> getEventsDesc(String keyId,
                    Set<String> eventIds, 
                    DataSourceConstraint dataSourceConstraints)
        throws DataSourceReadException;

    /**
     * Registers a listener that gets called whenever the schema adaptor
     * changes.
     *
     * @param listener
     *            a <code>SchemaAdaptorListener</code>.
     */
    void addSchemaAdaptorListener(SchemaAdaptorListener listener);

    /**
     * Unregisters a listener so that changes to the schema adaptor are no
     * longer sent.
     *
     * @param listener
     *            a <code>SchemaAdaptorListener</code>.
     */
    void removeSchemaAdaptorListener(SchemaAdaptorListener listener);

    /**
     * Cleans up any resources created by this schema adaptor.
     */
    void close();

    String getKeyType();

    String getKeyTypeDisplayName();

    String getKeyTypePluralDisplayName();
}
