package org.vsegda.admin.client.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.vsegda.admin.shared.DataStreamDTO;

/**
 * @author Roman Elizarov
 */
public class DataStreamUpdateAction extends AdminAction<Void> implements AsyncCallback<Void> {
    private final DataStreamDTO sd;

    public DataStreamUpdateAction(DataStreamDTO sd, AdminActionListener<Void> listener) {
        super("update", "Updating data stream with id=" + sd.getId() + "...", listener);
        this.sd = sd;
    }

    @Override
    public void execute() {
        adminService.updateDataStream(sd, this);

    }

    @Override
    public void onFailure(Throwable caught) {
        listener.actionCompleted(this, false, "Failed to update data stream with id=" + sd.getId() + ": " + caught, null);
    }

    @Override
    public void onSuccess(Void result) {
        listener.actionCompleted(this, true, "Updated data stream with id=" + sd.getId(), null);
    }
}
