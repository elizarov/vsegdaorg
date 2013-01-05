package org.vsegda.data;

import junit.framework.TestCase;

/**
 * @author Roman Elizarov
 */
public class DeltaUtilTest extends TestCase {
    public void testPrecision() {
        assertEquals(0, DeltaUtil.getPrecision(0));
        assertEquals(0, DeltaUtil.getPrecision(1));
        assertEquals(0, DeltaUtil.getPrecision(10));
        assertEquals(0, DeltaUtil.getPrecision(100));
        assertEquals(1, DeltaUtil.getPrecision(0.1));
        assertEquals(1, DeltaUtil.getPrecision(12.5));
        assertEquals(1, DeltaUtil.getPrecision(12.6));
        assertEquals(1, DeltaUtil.getPrecision(12.6 - 12.5));
        assertEquals(2, DeltaUtil.getPrecision(0.01));
        assertEquals(2, DeltaUtil.getPrecision(0.05));
        assertEquals(2, DeltaUtil.getPrecision(0.06));
        assertEquals(2, DeltaUtil.getPrecision(1.23));
    }
}
