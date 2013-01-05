package org.vsegda.dao;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.factory.Factory;
import org.vsegda.util.IdList;
import org.vsegda.util.RequestUtil;
import org.vsegda.util.TimeInstant;

import javax.jdo.Query;
import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    public DataRequest(ServletRequest req) {
        RequestUtil.populate(this, req);
	}

    @SuppressWarnings({"unchecked"})
    public List<DataItem> query() {
        log.info("Performing data query " + this);
        long startTimeMillis = System.currentTimeMillis();
        List<DataItem> result = new ArrayList<DataItem>();
        if (id == null) {
            Query query = Factory.getPM().newQuery(DataStream.class);
            query.setOrdering("streamId asc");
            query.setRange(first, first + last);
            query.getFetchPlan().setFetchSize(last);
            for (DataStream stream : (Collection<DataStream>)query.execute()) {
                DataItem item = null;
                if (stream.getLastItemKey() != null) {
                    item = DataItemDAO.getDataItemByKey(stream.getLastItemKey());
                    if (item == null)
                        stream.setLastItemKey(null);
                }
                if (item == null)
                    item = new DataItem(stream.getStreamId(), Double.NaN, 0);
                item.setStream(stream);
                result.add(item);
            }
        } else {
            for (String code : this.id) {
                DataStream stream = DataStreamDAO.resolveStreamByCode(code);
                List<DataItem> items = DataItemDAO.listDataItems(stream, since, first, last);
                int s0 = result.size();
                result.addAll(items);
                List<DataItem> subList = result.subList(s0, result.size());
                Collections.reverse(subList);
                filter(subList);
            }
        }
        log.info("Completed data query in " + (System.currentTimeMillis() - startTimeMillis) + " ms");
        return result;
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
