package org.vsegda.servlet;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.vsegda.data.DataArchive;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.factory.PM;
import org.vsegda.service.DataItemService;
import org.vsegda.shared.DataStreamMode;
import org.vsegda.storage.DataArchiveStorage;
import org.vsegda.storage.DataItemStorage;
import org.vsegda.util.TimeUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataArchiveTaskServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DataArchiveTaskServlet.class.getName());
    private static final int MAX_ARCHIVE_ITEMS = 1000; // max in one batch

    public static void enqueueTask(long streamId) {
        log.info("Enqueueing data archive task for id=" + streamId);
        Queue queue = QueueFactory.getQueue("archiveTaskQueue");
        queue.add(TaskOptions.Builder
                .withUrl("/task/dataArchive")
                .param("id", String.valueOf(streamId)));
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long streamId = Long.parseLong(req.getParameter("id"));
        DataStream stream = PM.instance().getObjectById(DataStream.class, streamId);
        log.info("Archiving data for streamId=" + streamId + ", mode=" + stream.getMode());
        DataItem firstItem = DataItemService.getFirstDataItem(stream);
        if (firstItem == null || (stream.getMode() != DataStreamMode.LAST && firstItem.isRecent())) {
            log.info("First stream item is recent or missing: " + firstItem);
            return;
        }
        DataItem lastItem = DataItemService.getLastDataItem(stream);
        log.info("First item is " + firstItem + "; last item is " + lastItem);
        // Archive up to next midnight (or up to last item for LAST mode)
        long limit =
                stream.getMode() == DataStreamMode.LAST ? lastItem.getTimeMillis() :
                TimeUtil.getArchiveLimit(firstItem.getTimeMillis());

        List<DataItem> items = DataItemStorage.queryFirstDataItems(streamId, limit, MAX_ARCHIVE_ITEMS);

        // never remove or archive the last item !!!
        if (!items.isEmpty() && items.get(items.size() - 1).getKey().equals(lastItem.getKey()))
            items.remove(items.size() - 1);
        if (items.isEmpty()) {
            log.warning("No items to archive");
            return;
        }

        switch (stream.getMode()) {
            case LAST:
                log.info("Only last item is kept, removing other ones");
                DataItemService.removeDataItems(stream, items);
                break;
            case RECENT:
                log.info("Only recent items are kept, removing old ones");
                DataItemService.removeDataItems(stream, items);
                break;
            case ARCHIVE:
                // Actually archive
                DataArchive archive = new DataArchive(streamId);
                archive.encodeItems(items);
                items.subList(archive.getCount(), items.size()).clear(); // remove all that wasn't encoded
                log.info("Creating archive " + archive);
                DataArchiveStorage.storeDataArchive(archive);
                DataItemService.removeDataItems(stream, items);
                break;
        }
        // create next task to check this stream again
        enqueueTask(streamId);
    }

}
