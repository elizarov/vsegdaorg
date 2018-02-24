package org.vsegda.admin.server

import com.google.gwt.user.server.rpc.*
import org.apache.commons.lang3.builder.*
import org.vsegda.admin.shared.*
import org.vsegda.admin.shared.rpc.*
import org.vsegda.data.*
import org.vsegda.request.*
import org.vsegda.service.*
import org.vsegda.shared.*
import org.vsegda.util.*
import java.util.*

class AdminServiceImpl : RemoteServiceServlet(), AdminService {
    override fun getDataStreams(): List<DataStreamDTO> {
        val items = DataRequest().queryList()
        val result = ArrayList<DataStreamDTO>()
        for (item in items) {
            val stream = item.stream
            val sd = DataStreamDTO()
            sd.id = item.streamId
            sd.tag = stream.tag
            sd.name = stream.name
            sd.alert = if (stream.alertTimeout == 0L) "" else TimePeriod.valueOf(stream.alertTimeout).toString()
            sd.mode = stream.mode
            sd.value = item.value
            sd.time = item.time
            sd.ago = item.ago
            sd.formatClass = item.formatClass
            result.add(sd)
        }
        return result
    }

    // use id == 0 to create new stream
    override fun updateDataStream(id: Long, sd: DataStreamDTO) {
        log.info("Updating data stream id=" + id + " with " +
            ReflectionToStringBuilder.toString(sd, ToStringStyle.SHORT_PREFIX_STYLE))
        // update stream
        val stream = if (id == 0L)
            DataStreamService.resolveDataStreamByTagOrCreate(sd.tag) else
            DataStreamService.resolveDataStreamById(id)!!
        if (sd.mode == DataStreamMode.DELETE) {
            // delete existing stream
            DataItemService.removeAllDataItems(stream)
            DataStreamService.removeDataStream(stream)
            return
        }
        if (id != 0L && sd.id != id) {
            // change existing stream id
            val newStream = DataStreamService.resolveDataStreamByIdOrCreate(sd.id, failWhenExists = true)
            updateStream(newStream, sd)
            DataItemService.updateStreamId(id, sd.id)
            DataStreamService.removeDataStream(stream)
            return
        }
        // only update
        updateStream(stream, sd)
    }

    private fun updateStream(stream: DataStream, sd: DataStreamDTO) {
        stream.tag = sd.tag
        stream.name = sd.name ?: ""
        stream.alertTimeout = TimePeriod.valueOf(sd.alert).period
        stream.mode = sd.mode
        DataStreamService.storeDataStream(stream)
    }
}
