package org.vsegda.request;

import org.vsegda.dao.DataItemDAO;
import org.vsegda.dao.DataStreamDAO;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.util.IdList;
import org.vsegda.util.TimeInstant;
import org.vsegda.util.TimePeriod;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataRequest extends AbstractRequest {
    private static final Logger log = Logger.getLogger(DataRequest.class.getName());

    private IdList id;
    private TimeInstant to = null;
    private TimePeriod span = DataItemDAO.DEFAULT_SPAN;
    private int n = DataItemDAO.DEFAULT_N;
    private double filter = 5; // 5 sigmas by default

    private boolean hasNavigation;

    public DataRequest() {}

    public DataRequest(HttpServletRequest req) {
        init(req);
	}

    public List<DataItem> queryList() {
        Map<DataStream, List<DataItem>> map = queryMap();
        List<DataItem> result = new ArrayList<DataItem>();
        for (List<DataItem> list : map.values())
            result.addAll(list);
        if (id != null) {
            // reorder descending by time (it is stable, for order by ids is kept for same time)
            Collections.sort(result, Collections.reverseOrder(DataItem.ORDER_BY_TIME));
        }
        return result;
    }

    @SuppressWarnings({"unchecked"})
    public Map<DataStream, List<DataItem>> queryMap() {
        log.info("Performing data query " + this);
        long startTimeMillis = System.currentTimeMillis();
        Map<DataStream, List<DataItem>> map = new LinkedHashMap<DataStream, List<DataItem>>();
        if (id == null) {
            for (DataStream stream : DataStreamDAO.listDataStreams())
                map.put(stream, Collections.singletonList(DataItemDAO.findLastDataItem(stream)));
        } else {
            for (String code : this.id) {
                DataStream stream = DataStreamDAO.resolveDataStreamByCode(code, false);
                TimeInstant from = span == null ? null :
                        (to == null ? TimeInstant.now() : to).subtract(span);
                List<DataItem> items = new ArrayList<DataItem>(DataItemDAO.listDataItems(stream, from, to, n));
                filter(items);
                map.put(stream, items);
            }
            hasNavigation = span != null;
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
        updateQueryString("id", id == null ? null : id.toString());
        this.id = id;
    }

    public TimeInstant getTo() {
        return to;
    }

    public void setTo(TimeInstant to) {
        updateQueryString("to", to == null ? null : to.toString());
        this.to = to;
    }

    public TimePeriod getSpan() {
        return span;
    }

    public void setSpan(TimePeriod span) {
        updateQueryString("span", span == null ? null : span.toString());
        this.span = span;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        updateQueryString("n", n == 0 ? null : String.valueOf(n));
        this.n = n;
    }

    public double getFilter() {
        return filter;
    }

    public void setFilter(double filter) {
        this.filter = filter;
    }

    public boolean hasNavigation() {
        return hasNavigation;
    }
}
