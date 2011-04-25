package org.protempa.proposition.value;

import java.math.BigDecimal;



import junit.framework.TestCase;

/**
 * @author Andrew Post
 */
public class ValueFormatTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPointZero() {
        assertEquals(new NumberValue(6.0), ValueFactory.VALUE.parseValue("6.0"));
        assertEquals("6.0", ValueFactory.VALUE.parseValue("6.0").getFormatted());
        assertEquals(new NumberValue(6.0), ValueFormat.parse("6.0", ValueType.VALUE));
        assertEquals("6.0", ValueFormat.parse("6.0", ValueType.VALUE).getFormatted());
    }

    public void testValueParseInequalityValue() {
        assertEquals(new InequalityNumberValue(ValueComparator.LESS_THAN,
                new BigDecimal("0.1")),
                ValueFactory.VALUE.parseValue("<0.1"));
    }

    public void testInequalityDoubleParseValue() {
        assertEquals(new InequalityNumberValue(ValueComparator.LESS_THAN,
                new BigDecimal("0.1")),
                ValueFactory.INEQUALITY.parseValue("<0.1"));
        assertEquals(new InequalityNumberValue(ValueComparator.LESS_THAN,
                new BigDecimal("0.4")),
                ValueFactory.INEQUALITY.parseValue("<0.4"));
    }

    public void testNumberValueParserLeadingWhitespace() {
        assertEquals(new NumberValue(new BigDecimal(3)),
                ValueFactory.NUMBER.parseValue("    3"));
    }

    public void testNumberFormatParserTrailingWhitespace() {
        assertEquals(new NumberValue(new BigDecimal(3)),
                ValueFactory.NUMBER.parseValue("3    "));
    }

    public void testNumberFormatParserWhitespace() {
        assertEquals(new NumberValue(new BigDecimal(3)),
                ValueFactory.NUMBER.parseValue("  3    "));
    }

    public void testPrecedingWhitespace() {
        assertEquals(new InequalityNumberValue(ValueComparator.LESS_THAN,
                new BigDecimal("0.4")),
                ValueFactory.INEQUALITY.parseValue("       <0.4"));
    }

    public void testInterveningWhitespace() {
        assertEquals(new InequalityNumberValue(ValueComparator.LESS_THAN,
                new BigDecimal("0.4")),
                ValueFactory.INEQUALITY.parseValue("<    0.4"));
    }

    public void testWhitespaceAfter() {
        assertEquals(new InequalityNumberValue(ValueComparator.LESS_THAN,
                new BigDecimal("0.4")),
                ValueFactory.INEQUALITY.parseValue("<0.4     "));
    }

    public void testWhitespaceAllOver() {
        assertEquals(new InequalityNumberValue(ValueComparator.LESS_THAN,
                new BigDecimal("0.4")),
                ValueFactory.INEQUALITY.parseValue("      <   0.4     "));
    }
}
