package org.vsegda.admin.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.vsegda.admin.client.AdminService;
import org.vsegda.admin.shared.DataStreamDTO;
import org.vsegda.dao.DataRequest;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.util.TimePeriod;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Elizarov
 */
public class AdminServiceImpl extends RemoteServiceServlet implements AdminService {
    @Override
    public List<DataStreamDTO> getDataStreams() {
        List<DataItem> items = new DataRequest().query();
        List<DataStreamDTO> result = new ArrayList<DataStreamDTO>();
        for (DataItem item : items) {
            DataStream stream = item.getStream();
            DataStreamDTO s = new DataStreamDTO();
            s.setId(item.getStreamId());
            s.setTag(stream.getTag());
            s.setName(stream.getName());
            s.setAlert(stream.getAlertTimeout() == null ? "" : TimePeriod.valueOf(stream.getAlertTimeout()).toString());
            s.setMode(stream.getMode());
            s.setValue(item.getValue());
            s.setTime(item.getTime());
            s.setAgo(item.getAgo());
            result.add(s);
        }
        return result;
    }
}
