package org.vsegda.util;

import junit.framework.TestCase;

/**
 * @author Roman Elizarov
 */
public class TimeUtilTest extends TestCase {
    public void testArchiveLimit() {
        assertEquals(
            TimeUtil.parseTime("20110303T235959.999", 0),
            TimeUtil.getArchiveLimit(TimeUtil.parseTime("20110303T011514.345", 0)));

    }
}
