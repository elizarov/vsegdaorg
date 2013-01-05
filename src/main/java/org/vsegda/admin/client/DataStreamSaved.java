package org.vsegda.admin.client;

import org.vsegda.admin.shared.DataStreamDTO;

/**
 * @author Roman Elizarov
 */
public interface DataStreamSaved {
    public void onDataStreamSaved(DataStreamDTO sd);
}
