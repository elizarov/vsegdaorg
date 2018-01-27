package org.vsegda.util;

import junit.framework.TestCase;

/**
 * @author Roman Elizarov
 */
public class TimeUtilTest extends TestCase {
    public void testArchiveLimit() {
        assertEquals(
            TimeUtil.parseTime("20110304T000000.000"),
            TimeUtil.getArchiveLimit(TimeUtil.parseTime("20110303T011514.345")));
    }
}
