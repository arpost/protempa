package org.protempa.proposition.value;

/**
 * A {@link String} value.
 * 
 * @author Andrew Post
 */
public final class NominalValue extends ValueImpl {

    private static final long serialVersionUID = 440118249272295573L;
    private final String val;
    private transient volatile int hashCode;

    /**
     * Creates a new <code>NominalValue</code> with the given value.
     *
     * @param val
     *            a <code>String</code>. If <code>null</code>, the default
     *            string is used (<code>""</code>).
     */
    public NominalValue(String val) {
        super(ValueType.NOMINALVALUE);
        if (val != null) {
            this.val = val;
        } else {
            this.val = "";
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = val.hashCode();
        }
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NominalValue other = (NominalValue) obj;
        return this.val.equals(other.val);
    }

    /**
     * Gets the value as a Java {@link String}.
     *
     * @return a {@link String}.
     */
    public String getString() {
        return val;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.protempa.proposition.value.Value#getFormatted()
     */
    @Override
    public String getFormatted() {
        return val;
    }

    /**
     * Returns the canonical string representing this value. Returns
     * "NOMINAL_VALUE:the string as-is".
     *
     * @return a {@link String}.
     *
     * @see org.protempa.proposition.value.Value#getRepr()
     */
    @Override
    public String getRepr() {
        return reprType() + val;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.protempa.proposition.value.ValueImpl#compareNominalValue(org.protempa.proposition.value.NominalValue)
     */
    @Override
    protected ValueComparator compareNominalValue(NominalValue d2) {
        int comp = val.compareTo(d2.val);
        return comp > 0 ? ValueComparator.GREATER_THAN
                : (comp < 0 ? ValueComparator.LESS_THAN
                : ValueComparator.EQUAL_TO);
    }

    @Override
    public void accept(ValueVisitor valueVisitor) {
        if (valueVisitor == null) {
            throw new IllegalArgumentException("valueVisitor cannot be null");
        }
        valueVisitor.visit(this);
    }
}
