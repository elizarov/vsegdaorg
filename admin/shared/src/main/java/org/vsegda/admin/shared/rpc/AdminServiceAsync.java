package org.vsegda.admin.shared.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.vsegda.admin.shared.DataStreamDTO;

import java.util.List;

public interface AdminServiceAsync {
    void getDataStreams(AsyncCallback<List<DataStreamDTO>> async);

    void updateDataStream(long id, DataStreamDTO sd, AsyncCallback<Void> async);
}
