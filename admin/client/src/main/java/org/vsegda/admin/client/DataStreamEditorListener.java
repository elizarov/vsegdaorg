package org.vsegda.admin.client;

import org.vsegda.admin.shared.DataStreamDTO;

/**
 * @author Roman Elizarov
 */
public interface DataStreamEditorListener {
    public void dataStreamEditorSaved(long originalId, DataStreamDTO sd);
}
