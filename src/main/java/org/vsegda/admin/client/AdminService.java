package org.vsegda.admin.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.vsegda.admin.shared.DataStreamDTO;

import java.util.List;

/**
 * @author Roman Elizarov
 */
@RemoteServiceRelativePath("admin.rpc")
public interface AdminService extends RemoteService {
    public List<DataStreamDTO> getDataStreams();
}
