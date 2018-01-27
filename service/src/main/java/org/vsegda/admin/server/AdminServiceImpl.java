package org.vsegda.admin.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vsegda.admin.shared.rpc.AdminService;
import org.vsegda.admin.shared.DataStreamDTO;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.request.DataRequest;
import org.vsegda.service.DataItemService;
import org.vsegda.service.DataStreamService;
import org.vsegda.shared.DataStreamMode;
import org.vsegda.storage.DataStreamStorage;
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
        List<DataItem> items = new DataRequest().queryList();
        List<DataStreamDTO> result = new ArrayList<>();
        for (DataItem item : items) {
            DataStream stream = item.getStream();
            DataStreamDTO sd = new DataStreamDTO();
            sd.setId(item.getStreamId());
            sd.setTag(stream.getTag());
            sd.setName(stream.getName());
            sd.setAlert(stream.getAlertTimeout() == 0 ? "" : TimePeriod.valueOf(stream.getAlertTimeout()).toString());
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
    // use id == 0 to create new stream
    public void updateDataStream(long id, DataStreamDTO sd) {
        log.info("Updating data stream id=" + id + " with " + ReflectionToStringBuilder.toString(sd, ToStringStyle.SHORT_PREFIX_STYLE));
        // update stream
        DataStream stream = DataStreamService.resolveDataStreamById(id, id == 0);
        if (sd.getMode() == DataStreamMode.DELETE) {
            // delete existing stream
            DataItemService.INSTANCE.removeAllDataItems(stream);
            DataStreamService.removeDataStream(stream);
            return;
        }
        if (id != 0 && sd.getId() != id) {
            // change existing stream id
            if (DataStreamService.resolveDataStreamById(sd.getId(), false) != null)
                throw new IllegalArgumentException("Stream with id=" + sd.getId() + " already exists");
            DataStream newStream = DataStreamService.resolveDataStreamById(sd.getId(), true);
            updateStream(newStream, sd);
            DataItemService.INSTANCE.updateStreamId(id, sd.getId());
            DataStreamService.removeDataStream(stream);
            return;
        }
        // only update
        updateStream(stream, sd);
    }

    private void updateStream(DataStream stream, DataStreamDTO sd) {
        stream.setTag(sd.getTag());
        stream.setName(sd.getName());
        stream.setAlertTimeout(TimePeriod.valueOf(sd.getAlert()).period());
        stream.setMode(sd.getMode());
        DataStreamStorage.INSTANCE.storeDataStream(stream);
    }
}
