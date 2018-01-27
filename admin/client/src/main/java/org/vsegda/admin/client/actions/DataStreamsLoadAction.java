package org.vsegda.admin.client.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.vsegda.admin.shared.DataStreamDTO;

import java.util.List;

/**
 * @author Roman Elizarov
 */
public class DataStreamsLoadAction extends AdminAction<List<DataStreamDTO>> implements AsyncCallback<List<DataStreamDTO>> {
    public DataStreamsLoadAction(AdminActionListener<List<DataStreamDTO>> listener) {
        super("refresh", "Loading data streams...", listener);
    }

    @Override
    public void execute() {
        adminService.getDataStreams(this);
    }

    @Override
    public void onFailure(Throwable caught) {
        listener.actionCompleted(this, false, "Failed to load data streams: " + caught, null);
    }

    @Override
    public void onSuccess(List<DataStreamDTO> result) {
        listener.actionCompleted(this, true, "Loaded data streams", result);
    }
}
