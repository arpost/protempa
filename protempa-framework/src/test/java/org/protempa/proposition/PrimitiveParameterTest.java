/*
 * #%L
 * Protempa Framework
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
package org.protempa.proposition;

import org.protempa.proposition.interval.Interval;
import java.util.Calendar;
import java.util.TimeZone;

import org.protempa.DataSourceBackendSourceSystem;
import org.protempa.ProtempaTestCase;
import org.protempa.proposition.value.AbsoluteTimeGranularity;
import org.protempa.proposition.value.NumberValue;
import static 
        org.protempa.proposition.value.AbsoluteTimeGranularityUtil.asPosition;

/**
 * @author Andrew Post
 */
public class PrimitiveParameterTest extends ProtempaTestCase {

    private PrimitiveParameter p;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
        cal.clear();
        cal.set(2007, Calendar.MARCH, 1, 15, 11);

        p = new PrimitiveParameter("TEST", getUid());
        p.setSourceSystem(DataSourceBackendSourceSystem.getInstance("TEST"));
        p.setValue(new NumberValue(13));
        p.setPosition(asPosition(cal.getTime()));
        p.setGranularity(AbsoluteTimeGranularity.MINUTE);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        p = null;
    }

    public void testIntervalMinStart() {
        Interval i = p.getInterval();
        assertEquals(Long.valueOf(1172779860000L), i.getMinimumStart());
    }

    public void testIntervalMaxStart() {
        Interval i = p.getInterval();
        assertEquals(Long.valueOf(1172779919999L), i.getMaximumStart());
    }

    public void testIntervalMinFinish() {
        Interval i = p.getInterval();
        assertEquals(Long.valueOf(1172779860000L), i.getMinimumFinish());
    }

    public void testIntervalMaxFinish() {
        Interval i = p.getInterval();
        assertEquals(Long.valueOf(1172779919999L), i.getMaximumFinish());
    }

    public void testIntervalMinDuration() {
        Interval i = p.getInterval();
        assertEquals(Long.valueOf(0), i.getMinimumLength());
    }

    public void testIntervalMaxDuration() {
        Interval i = p.getInterval();
        assertEquals(Long.valueOf(0), i.getMaximumLength());
    }

    public void testEqualAll() {
        PrimitiveParameter p2 = new PrimitiveParameter("TEST", getUid());
        p2.setSourceSystem(DataSourceBackendSourceSystem.getInstance("TEST"));
        p2.setValue(new NumberValue(13));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
        cal.clear();
        cal.set(2007, Calendar.MARCH, 1, 15, 11);
        p2.setPosition(asPosition(cal.getTime()));
        p2.setGranularity(AbsoluteTimeGranularity.MINUTE);
        assertTrue(p.isEqual(p2));
    }

    public void testIdsNotEqual() {
        PrimitiveParameter p2 = new PrimitiveParameter("TEST2", getUid());
        p2.setSourceSystem(DataSourceBackendSourceSystem.getInstance("TEST"));
        p2.setValue(new NumberValue(13));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
        cal.clear();
        cal.set(2007, Calendar.MARCH, 1, 15, 11);
        p2.setPosition(asPosition(cal.getTime()));
        p2.setGranularity(AbsoluteTimeGranularity.MINUTE);
        assertFalse("expected: " + p + "; actual: " + p2, p.isEqual(p2));
    }

    public void testTimestampsNotEqual() {
        PrimitiveParameter p2 = new PrimitiveParameter("TEST2", getUid());
        p2.setSourceSystem(DataSourceBackendSourceSystem.getInstance("TEST"));
        p2.setValue(new NumberValue(13));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
        cal.clear();
        cal.set(2007, Calendar.MARCH, 1, 15, 12);
        p2.setPosition(asPosition(cal.getTime()));
        p2.setGranularity(AbsoluteTimeGranularity.MINUTE);
        assertFalse("expected: " + p + "; actual: " + p2, p.isEqual(p2));
    }

    public void testGranularitiesNotEqual() {
        PrimitiveParameter p2 = new PrimitiveParameter("TEST2", getUid());
        p2.setSourceSystem(DataSourceBackendSourceSystem.getInstance("TEST"));
        p2.setValue(new NumberValue(13));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
        cal.clear();
        cal.set(2007, Calendar.MARCH, 1, 15, 11);
        p2.setPosition(asPosition(cal.getTime()));
        p2.setGranularity(AbsoluteTimeGranularity.SECOND);
        assertFalse("expected: " + p + "; actual: " + p2, p.isEqual(p2));
    }
}
