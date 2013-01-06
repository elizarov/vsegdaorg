package org.vsegda.admin.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.vsegda.admin.shared.DataStreamDTO;

import java.util.List;

public interface AdminServiceAsync {
    void getDataStreams(AsyncCallback<List<DataStreamDTO>> async);

    void updateDataStream(DataStreamDTO sd, AsyncCallback<Void> async);
}
