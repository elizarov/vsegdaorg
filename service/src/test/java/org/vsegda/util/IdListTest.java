package org.vsegda.util;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author Roman Elizarov
 */
public class IdListTest extends TestCase {
    public void testList() {
        IdList id = new IdList("1-3,code,5,7");
        assertEquals(Arrays.<String>asList("1", "2", "3", "code", "5", "7"), id);
        assertEquals("1-3,code,5,7", id.toString());
    }
}
