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

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Map;

/**
 * Tests {@link JDBCDecimalDayParser} with a mock {@link ResultSet}.
 * 
 * @author Andrew Post
 */
public class JDBCDecimalDayParserTest {

    /**
     * Tests whether a date in the middle of a year is parsed correctly.
     *
     * @throws SQLException
     */
    @Test
    public void testDate() throws SQLException {
        ResultSet resultSet = new MockResultSet(20100723);
        JDBCPositionFormat pf = new JDBCDecimalDayParser();
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2010, Calendar.JULY, 23);
        Assert.assertEquals(Long.valueOf(cal.getTimeInMillis()),
                pf.toPosition(resultSet, 1, Types.INTEGER));
    }

    /**
     * Tests whether a date in the last month of a year is parsed correctly.
     *
     * @throws SQLException if the date could not be parsed correctly.
     */
    @Test
    public void testDateAtYearBoundary() throws SQLException {
        ResultSet resultSet = new MockResultSet(20101223);
        JDBCPositionFormat pf = new JDBCDecimalDayParser();
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2010, Calendar.DECEMBER, 23);
        Assert.assertEquals(Long.valueOf(cal.getTimeInMillis()),
                pf.toPosition(resultSet, 1, Types.INTEGER));
    }

    /**
     * Deprecation warnings are suppressed because ResultSet defines some
     * deprecated methods that must be overridden nonetheless.
     *
     * @author Andrew Post
     */
    @SuppressWarnings("deprecation")
    private static class MockResultSet implements ResultSet {

        private boolean hasNext = true;
        private int value;

        MockResultSet(int value) {
            this.value = value;
        }

        @Override
        public boolean next() throws SQLException {
            if (hasNext) {
                hasNext = false;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void close() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean wasNull() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getString(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean getBoolean(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte getByte(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public short getShort(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getInt(int i) throws SQLException {
            if (hasNext) {
                if (i == 1) {
                    return this.value;
                } else {
                    throw new SQLException("Only 1 column");
                }
            } else {
                throw new SQLException("No more rows");
            }
        }

        @Override
        public long getLong(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getFloat(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getDouble(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BigDecimal getBigDecimal(int i, int i1) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte[] getBytes(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Date getDate(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Time getTime(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Timestamp getTimestamp(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream getAsciiStream(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream getUnicodeStream(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream getBinaryStream(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getString(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean getBoolean(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte getByte(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public short getShort(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getInt(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getLong(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getFloat(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getDouble(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BigDecimal getBigDecimal(String string, int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte[] getBytes(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Date getDate(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Time getTime(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Timestamp getTimestamp(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream getAsciiStream(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream getUnicodeStream(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream getBinaryStream(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void clearWarnings() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getCursorName() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getObject(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getObject(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T> T getObject(String string, Class<T> type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T> T getObject(int i, Class<T> type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int findColumn(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Reader getCharacterStream(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Reader getCharacterStream(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BigDecimal getBigDecimal(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BigDecimal getBigDecimal(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isBeforeFirst() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isAfterLast() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isFirst() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isLast() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void beforeFirst() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void afterLast() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean first() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean last() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getRow() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean absolute(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean relative(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean previous() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFetchDirection(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getFetchDirection() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFetchSize(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getFetchSize() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getType() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getConcurrency() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean rowUpdated() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean rowInserted() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean rowDeleted() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNull(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBoolean(int i, boolean bln) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateByte(int i, byte b) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateShort(int i, short s) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateInt(int i, int i1) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateLong(int i, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateFloat(int i, float f) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateDouble(int i, double d) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBigDecimal(int i, BigDecimal bd) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateString(int i, String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBytes(int i, byte[] bytes) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateDate(int i, Date date) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateTime(int i, Time time) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateTimestamp(int i, Timestamp tmstmp) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateAsciiStream(int i, InputStream in, int i1) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBinaryStream(int i, InputStream in, int i1) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateCharacterStream(int i, Reader reader, int i1) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateObject(int i, Object o, int i1) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateObject(int i, Object o) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNull(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBoolean(String string, boolean bln) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateByte(String string, byte b) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateShort(String string, short s) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateInt(String string, int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateLong(String string, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateFloat(String string, float f) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateDouble(String string, double d) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBigDecimal(String string, BigDecimal bd) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateString(String string, String string1) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBytes(String string, byte[] bytes) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateDate(String string, Date date) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateTime(String string, Time time) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateTimestamp(String string, Timestamp tmstmp) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateAsciiStream(String string, InputStream in, int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBinaryStream(String string, InputStream in, int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateCharacterStream(String string, Reader reader, int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateObject(String string, Object o, int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateObject(String string, Object o) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void insertRow() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateRow() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void deleteRow() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void refreshRow() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void cancelRowUpdates() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void moveToInsertRow() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void moveToCurrentRow() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Statement getStatement() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Ref getRef(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Blob getBlob(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Clob getClob(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Array getArray(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getObject(String string, Map<String, Class<?>> map) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Ref getRef(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Blob getBlob(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Clob getClob(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Array getArray(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Date getDate(int i, Calendar clndr) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Date getDate(String string, Calendar clndr) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Time getTime(int i, Calendar clndr) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Time getTime(String string, Calendar clndr) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Timestamp getTimestamp(int i, Calendar clndr) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Timestamp getTimestamp(String string, Calendar clndr) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URL getURL(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URL getURL(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateRef(int i, Ref ref) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateRef(String string, Ref ref) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBlob(int i, Blob blob) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBlob(String string, Blob blob) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateClob(int i, Clob clob) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateClob(String string, Clob clob) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateArray(int i, Array array) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateArray(String string, Array array) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public RowId getRowId(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public RowId getRowId(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateRowId(int i, RowId rowid) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateRowId(String string, RowId rowid) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getHoldability() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isClosed() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNString(int i, String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNString(String string, String string1) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNClob(int i, NClob nclob) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNClob(String string, NClob nclob) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public NClob getNClob(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public NClob getNClob(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public SQLXML getSQLXML(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public SQLXML getSQLXML(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateSQLXML(int i, SQLXML sqlxml) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateSQLXML(String string, SQLXML sqlxml) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getNString(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getNString(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Reader getNCharacterStream(int i) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Reader getNCharacterStream(String string) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNCharacterStream(int i, Reader reader, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNCharacterStream(String string, Reader reader, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateAsciiStream(int i, InputStream in, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBinaryStream(int i, InputStream in, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateCharacterStream(int i, Reader reader, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateAsciiStream(String string, InputStream in, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBinaryStream(String string, InputStream in, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateCharacterStream(String string, Reader reader, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBlob(int i, InputStream in, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBlob(String string, InputStream in, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateClob(int i, Reader reader, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateClob(String string, Reader reader, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNClob(int i, Reader reader, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNClob(String string, Reader reader, long l) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNCharacterStream(int i, Reader reader) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNCharacterStream(String string, Reader reader) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateAsciiStream(int i, InputStream in) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBinaryStream(int i, InputStream in) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateCharacterStream(int i, Reader reader) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateAsciiStream(String string, InputStream in) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBinaryStream(String string, InputStream in) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateCharacterStream(String string, Reader reader) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBlob(int i, InputStream in) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateBlob(String string, InputStream in) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateClob(int i, Reader reader) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateClob(String string, Reader reader) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNClob(int i, Reader reader) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateNClob(String string, Reader reader) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T> T unwrap(Class<T> type) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isWrapperFor(Class<?> type) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
