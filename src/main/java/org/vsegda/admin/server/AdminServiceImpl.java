package org.vsegda.admin.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vsegda.admin.client.rpc.AdminService;
import org.vsegda.admin.shared.DataStreamDTO;
import org.vsegda.dao.DataRequest;
import org.vsegda.dao.DataStreamDAO;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.factory.PM;
import org.vsegda.util.TimePeriod;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class AdminServiceImpl extends RemoteServiceServlet implements AdminService {
    private static final Logger log = Logger.getLogger(AdminServiceImpl.class.getName());

    @Override
    public List<DataStreamDTO> getDataStreams() {
        List<DataItem> items = new DataRequest().queryListDescending();
        List<DataStreamDTO> result = new ArrayList<DataStreamDTO>();
        for (DataItem item : items) {
            DataStream stream = item.getStream();
            DataStreamDTO sd = new DataStreamDTO();
            sd.setId(item.getStreamId());
            sd.setTag(stream.getTag());
            sd.setName(stream.getName());
            sd.setAlert(stream.getAlertTimeout() == null ? "" : TimePeriod.valueOf(stream.getAlertTimeout()).toString());
            sd.setMode(stream.getMode());
            sd.setValue(item.getValue());
            sd.setTime(item.getTime());
            sd.setAgo(item.getAgo());
            sd.setFormatClass(item.getFormatClass());
            result.add(sd);
        }
        return result;
    }

    @Override
    public void updateDataStream(DataStreamDTO sd) {
        log.info("Updating data stream " + ReflectionToStringBuilder.toString(sd, ToStringStyle.SHORT_PREFIX_STYLE));
        // update stream
        PM.beginTransaction();
        DataStream stream = DataStreamDAO.resolveDataStreamById(sd.getId(), true);
        stream.setTag(sd.getTag());
        stream.setName(sd.getName());
        stream.setAlertTimeout(TimePeriod.valueOf(sd.getAlert()).periodOrNull());
        stream.setMode(sd.getMode());
    }
}
