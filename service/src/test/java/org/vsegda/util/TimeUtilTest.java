package org.vsegda.util;

import junit.framework.TestCase;

/**
 * @author Roman Elizarov
 */
public class TimeUtilTest extends TestCase {
    public void testArchiveLimit() {
        assertEquals(
            TimeUtil.INSTANCE.parseTime("20110304T000000.000"),
            TimeUtil.INSTANCE.getArchiveLimit(TimeUtil.INSTANCE.parseTime("20110303T011514.345")));
    }
}
