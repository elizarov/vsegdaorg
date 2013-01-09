package org.vsegda.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.util.IdList;
import org.vsegda.util.TimeInstant;

import javax.servlet.ServletRequest;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataRequest {
    private static final Logger log = Logger.getLogger(DataRequest.class.getName());

	private IdList id;
	private int first;
	private int last = 1000; // last 1000 items by default
    private double filter = 5; // 5 sigmas by default
    private TimeInstant since;

    public DataRequest() {}

    public DataRequest(ServletRequest req) {
        RequestUtil.populate(this, req);
	}

    public List<DataItem> queryListDescending() {
        Map<DataStream, List<DataItem>> map = queryMapAscending();
        List<DataItem> result = new ArrayList<DataItem>();
        for (List<DataItem> list : map.values())
            result.addAll(list);
        // reorder descending by time (it is stable, for order by ids is kept for same time)
        Collections.sort(result, Collections.reverseOrder(DataItem.ORDER_BY_TIME));
        return result;
    }

    @SuppressWarnings({"unchecked"})
    public Map<DataStream, List<DataItem>> queryMapAscending() {
        log.info("Performing data query " + this);
        long startTimeMillis = System.currentTimeMillis();
        Map<DataStream, List<DataItem>> map = new LinkedHashMap<DataStream, List<DataItem>>();
        if (id == null) {
            for (DataStream stream : DataStreamDAO.listDataStreams(first, last)) {
                DataItem item = null;
                if (stream.getLastItemKey() != null) {
                    item = DataItemDAO.getDataItemByKey(stream.getLastItemKey());
                    if (item == null)
                        stream.setLastItemKey(null);
                }
                if (item == null)
                    item = new DataItem(stream.getStreamId(), Double.NaN, 0);
                item.setStream(stream);
                map.put(stream, Collections.singletonList(item));
            }
        } else {
            for (String code : this.id) {
                DataStream stream = DataStreamDAO.resolveDataStreamByCode(code, false);
                List<DataItem> items = new ArrayList<DataItem>(DataItemDAO.listDataItems(stream, since, first, last));
                filter(items);
                map.put(stream, items);
            }
        }
        log.info("Completed data query in " + (System.currentTimeMillis() - startTimeMillis) + " ms");
        return map;
    }

    private void filter(List<DataItem> items) {
        if (filter <= 0)
            return;
        for (int repeat = 0; repeat < 20; repeat++) {
            int n = items.size() - 1;
            if (n < 2)
                return;
            double sum = 0;
            double sum2 = 0;
            for (int i = 0; i < n; i++) {
                double d = diff(items, i);
                sum += d;
                sum2 += d * d;
            }
            double m = sum / n;
            double s2 = sum2 / (n - 1) - sum * sum / (n * n - n);
            if (s2 <= 0)
                return;
            double limit = Math.sqrt(s2) * filter;
            int j = 0;
            for (int i = 0; i < items.size(); i++) {
                if (absError(items, i, m) <= limit || absError(items, i - 1, m) <= limit) {
                    items.set(j++, items.get(i));
                }
            }
            if (j == items.size())
                return;
            items.subList(j, items.size()).clear();
        }
    }

    private double absError(List<DataItem> items, int i, double m) {
        if (i < 0 || i >= items.size() - 1)
            return Double.POSITIVE_INFINITY;
        return Math.abs(m - Math.abs(diff(items, i)));
    }

    private double diff(List<DataItem> items, int i) {
        return items.get(i).getValue() - items.get(i + 1).getValue();
    }


    public IdList getId() {
        return id;
    }

    public void setId(IdList id) {
        this.id = id;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getLast() {
        return last;
    }

    public void setLast(int last) {
        this.last = last;
    }

    public double getFilter() {
        return filter;
    }

    public void setFilter(double filter) {
        this.filter = filter;
    }

    public TimeInstant getSince() {
        return since;
    }

    public void setSince(TimeInstant since) {
        this.since = since;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
