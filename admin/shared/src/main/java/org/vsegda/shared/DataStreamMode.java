package org.vsegda.shared;

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
    ARCHIVE,

    /**
     * Deletes the stream.
     */
    DELETE
}
