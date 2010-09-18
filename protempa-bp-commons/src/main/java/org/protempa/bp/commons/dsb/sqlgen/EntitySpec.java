package org.protempa.bp.commons.dsb.sqlgen;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.protempa.ProtempaUtil;
import org.protempa.bp.commons.dsb.PositionParser;
import org.protempa.proposition.value.Granularity;
import org.protempa.proposition.value.ValueType;

/**
 * Defines a mapping from propositions to entities in a relational database.
 * 
 * @author Andrew Post
 */
public final class EntitySpec implements Serializable {
    private static final long serialVersionUID = -1935588032831088001L;

    private final String name;
    private final String description;
    private final String[] propositionIds;
    private final boolean unique;
    private final ColumnSpec baseSpec;
    private final ColumnSpec[] uniqueIdSpecs;
    private final ColumnSpec startTimeOrTimestampSpec;
    private final ColumnSpec finishTimeSpec;
    private final PropertySpec[] propertySpecs;
    private final ReferenceSpec[] referenceSpecs;
    private final Map<String, String> codeToPropIdMap;
    private final ColumnSpec codeSpec;
    private final ColumnSpec[] constraintSpecs;
    private final ValueType valueType;
    private final Granularity granularity;
    private final PositionParser positionParser;

    /**
     * Creates an entity spec instance.
     * 
     * @param name a {@link String}. Cannot be <code>null</code>.
     * @param description a {@link String}. The constructor replaces a
     * <code>null</code> argument with a {@link String} of length zero.
     * @param propositionIds the proposition id {@link String[]}s to which
     * this entity spec applies. Cannot contain <code>null</code> values.
     * These propositions must all have the same set of properties.
     * @param unique <code>true</code> if every row in the database table
     * specified by the <code>baseSpec</code> argument contains a unique
     * instance of this entity, <code>false</code> otherwise.
     * @param baseSpec a {@link ColumnSpec} representing the path through the
     * database from the key's main table to this entity's main table.
     * @param uniqueIdSpec a {@link ColumnSpec[]} representing the paths
     * through the database from this entity's main table to
     * the tables and columns that together form an unique identifier for this
     * entity.
     * @param startTimeOrTimestampSpec a {@link ColumnSpec} representing
     * the path through the database from this entity's main table to
     * the table and column where the entity's start time (or timestamp, if
     * no finish time is defined) is located, or <code>null</code> if this
     * entity has no start time or timestamp.
     * @param finishTimeSpec a {@link ColumnSpec} representing
     * the path through the database from this entity's main table to
     * the table and column where the entity's finish time is located, or
     * <code>null</code> if this entity has no finish time.
     * @param propertySpecs a {@link PropertySpec[]} defining the entity's
     * properties. These properties should be the same as the corresponding
     * propositions' properties. Cannot contain <code>null</code> values.
     * @param codeToPropIdMap a one-to-one {@link Map<String,String>} from
     * code to proposition id. If <code>null</code> or a mapping for a code
     * is not defined, it is assumed that the code in the database is the
     * same as the proposition id.
     * @param codeSpec a {@link ColumnSpec} representing the path through
     * the database from this entity's main table to the table and column
     * where the entity's code is located, or <code>null</code> if this entity
     * has no code.
     * @param constraintSpecs zero or more {@link ColumnSpec[]} paths from
     * this instance's main table to another table and column whose value
     * will constrain which rows in the database are members of this entity.
     * Cannot contain <code>null</code> values.
     * @param valueType if this entity has a value, its {@link ValueType}.
     * @param granularity the granularity for interpreting this entity' start
     * and finish times.
     * @param positionParser a parser for dates/times/timestamps for this
     * entity's start and finish times. Cannot be <code>null</code>.
     */
    public EntitySpec(String name,
            String description,
            String[] propositionIds,
            boolean unique,
            ColumnSpec baseSpec,
            ColumnSpec[] uniqueIdSpecs,
            ColumnSpec startTimeOrTimestampSpec,
            ColumnSpec finishTimeSpec,
            PropertySpec[] propertySpecs,
            ReferenceSpec[] referenceSpecs,
            Map<String, String> codeToPropIdMap,
            ColumnSpec codeSpec,
            ColumnSpec[] constraintSpecs,
            ValueType valueType,
            Granularity granularity,
            PositionParser positionParser) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;

        if (positionParser == null) {
            throw new IllegalArgumentException(
                    "positionParser cannot be null");
        }

        if (propositionIds != null) {
            this.propositionIds = propositionIds.clone();
            ProtempaUtil.checkArrayForNullElement(this.propositionIds,
                    "propositionIds");
        } else {
            this.propositionIds = new String[0];
        }

        if (baseSpec == null) {
            throw new IllegalArgumentException("baseSpec cannot be null");
        }
        this.baseSpec = baseSpec;

        
        if (description == null) {
            description = "";
        }
        this.description = description;
        
        this.unique = unique;

        ProtempaUtil.checkArray(uniqueIdSpecs, "uniqueIdSpecs");
        this.uniqueIdSpecs = uniqueIdSpecs;

        this.startTimeOrTimestampSpec = startTimeOrTimestampSpec;
        this.finishTimeSpec = finishTimeSpec;

        if (propertySpecs != null) {
            this.propertySpecs = propertySpecs.clone();
            ProtempaUtil.checkArrayForNullElement(this.propertySpecs,
                    "propertySpecs");
        } else {
            this.propertySpecs = new PropertySpec[0];
        }

        if (referenceSpecs != null) {
            this.referenceSpecs = referenceSpecs.clone();
            ProtempaUtil.checkArrayForNullElement(this.referenceSpecs,
                    "referenceSpecs");
        } else {
            this.referenceSpecs = new ReferenceSpec[0];
        }

        if (codeToPropIdMap != null) {
            this.codeToPropIdMap = new HashMap<String, String>(codeToPropIdMap);
        } else {
            this.codeToPropIdMap = Collections.emptyMap();
        }

        this.codeSpec = codeSpec;

        if (constraintSpecs != null) {
            this.constraintSpecs = new ColumnSpec[constraintSpecs.length];
            System.arraycopy(constraintSpecs, 0, this.constraintSpecs, 0,
                    constraintSpecs.length);
            ProtempaUtil.checkArrayForNullElement(this.constraintSpecs,
                    "constraintSpecs");
        } else {
            this.constraintSpecs = new ColumnSpec[0];
        }

        this.valueType = valueType;
        this.granularity = granularity;
        this.positionParser = positionParser;
    }

    /**
     * Gets this entity spec's name.
     *
     * @return a {@link String}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a textual description of this entity spec.
     *
     * @return a {@link String}. Cannot be <code>null</code>.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the proposition ids to which this entity spec applies.
     *
     * @return a {@link String[]} of proposition ids.
     */
    public String[] getPropositionIds() {
        return this.propositionIds.clone();
    }

    /**
     * Returns whether each row corresponds to its own instance of this
     * entity.
     *
     * @return <code>true</code> if rows each correspond to their own instance
     * of this entity, <code>false</code> otherwise.
     */
    public boolean isUnique() {
        return this.unique;
    }

    /**
     * Gets the path through the database from the key's main table to this
     * entity's main table.
     *
     * @return a {@link ColumnSpec} representing this path.
     */
    public ColumnSpec getBaseSpec() {
        return this.baseSpec;
    }

    /**
     * Gets the paths through the database from this entity's main table to
     * the tables and columns that together form an unique identifier for this
     * entity.
     *
     * @return a {@link ColumnSpec[]} representing these paths.
     */
    public ColumnSpec[] getUniqueIdSpecs() {
        return this.uniqueIdSpecs;
    }

    /**
     * Gets the path through the database from this entity's main table to
     * the table and column where the entity's start time (or timestamp, if
     * no finish time is defined) is located, or <code>null</code> if this
     * entity has no start time or timestamp.
     *
     * @return a {@link ColumnSpec} representing this path, or
     * <code>null</code> if this entity has no start time or timestamp.
     */
    public ColumnSpec getStartTimeSpec() {
        return this.startTimeOrTimestampSpec;
    }

    /**
     * Gets the path through the database from this entity's main table to
     * the table and column where the entity's finish time (if defined) is
     * located.
     *
     * @return a {@link ColumnSpec} representing this path, or
     * <code>null</code> if this entity has no finish time.
     */
    public ColumnSpec getFinishTimeSpec() {
        return this.finishTimeSpec;
    }

    /**
     * The entity's properties.
     * 
     * @return a {@link PropertySpec[]} of the entity's properties.
     * Guaranteed not <code>null</code>.
     */
    public PropertySpec[] getPropertySpecs() {
        return this.propertySpecs.clone();
    }

    /**
     * The entity's references to other entities.
     *
     * @return a {@link ReferenceSpec[]} of the entity's references to other
     * entities. Guaranteed not <code>null</code>.
     */
    public ReferenceSpec[] getReferenceSpecs() {
        return this.referenceSpecs.clone();
    }

    /**
     * Returns a one-to-one mapping from code to proposition id. If
     * <code>null</code> or a mapping for a code is not defined, it is assumed
     * that the code in the database is the same as the proposition id.
     *
     * @return a {@link Map<String,String>}. Guaranteed not <code>null</code>.
     */
    public Map<String, String> getCodeToPropIdMap() {
        Map<String, String> result =
                new HashMap<String, String>(this.codeToPropIdMap);
        return result;
    }

    /**
     * Gets the path through the database from this entity's main table to
     * the table and column where the a code representing this entity is
     * located.
     *
     * @return a {@link ColumnSpec}.
     */
    public ColumnSpec getCodeSpec() {
        return this.codeSpec;
    }

    /**
     * Returns zero or more {@link ColumnSpec[]} paths from
     * this instance's main table to another table and column whose value
     * will constrain which rows in the database are members of this entity.
     * Cannot contain <code>null</code> values.
     * 
     * @return a {@link ColumnSpec[]}.
     */
    public ColumnSpec[] getConstraintSpecs() {
        return this.constraintSpecs.clone();
    }

    /**
     * Returns this entity's value.
     * 
     * @return a {@link ValueType}, or <code>null</code> if this
     * entity does not have a value.
     */
    public ValueType getValueType() {
        return this.valueType;
    }

    /**
     * Returns the granularity for interpreting this entity' start and finish
     * times.
     *
     * @return a {@link Granularity}.
     */
    public Granularity getGranularity() {
        return this.granularity;
    }

    /**
     * Returns a parser for dates/times/timestamps for this entity's start and
     * finish times in the database.
     * @return a {@link PositionParser}. Cannot be <code>null</code>.
     */
    public PositionParser getPositionParser() {
        return this.positionParser;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", this.name)
                .append("description", this.description)
                .append("propositionIds", this.propositionIds)
                .append("unique", this.unique)
                .append("baseSpec", this.baseSpec)
                .append("uniqueIdSpecs", this.uniqueIdSpecs)
                .append("startTimeOrTimestampSpec", this.startTimeOrTimestampSpec)
                .append("finishTimeSpec", this.finishTimeSpec)
                .append("propertySpecs", this.propertySpecs)
                .append("referenceSpecs", this.referenceSpecs)
                .append("codeToPropIdMap", this.codeToPropIdMap)
                .append("codeSpec", this.codeSpec)
                .append("constraintSpecs", this.constraintSpecs)
                .append("valueType", this.valueType)
                .append("granularity", this.granularity)
                .append("positionParser", this.positionParser)
                .toString();
    }
}
