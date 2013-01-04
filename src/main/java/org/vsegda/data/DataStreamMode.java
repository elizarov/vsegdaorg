package org.vsegda.data;

/**
 * @author Roman Elizarov
 */
public enum DataStreamMode {
    /**
     * Keeps only last value.
     */
    LAST,

    /**
     * Keeps only recent values.
     */
    RECENT,

    /**
     * Keeps recent values and full archive.
     */
    ARCHIVE
}
