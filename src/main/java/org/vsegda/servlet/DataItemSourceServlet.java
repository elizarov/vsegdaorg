package org.vsegda.servlet;

import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import org.vsegda.data.DataItem;
import org.vsegda.util.DataRequest;
import org.vsegda.util.TimeUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author Roman Elizarov
 */
public class DataItemSourceServlet extends DataSourceServlet {
    private static final Comparator<DataItem> ORDER_BY_TIME_ID = new Comparator<DataItem>() {
        public int compare(DataItem o1, DataItem o2) {
            if (o1.getTimeMillis() < o2.getTimeMillis())
                return -1;
            if (o1.getTimeMillis() > o2.getTimeMillis())
                return 1;
            if (o1.getStreamId() < o2.getStreamId())
                return -1;
            if (o1.getStreamId() > o2.getStreamId())
                return 1;
            return 0;
        }
    };

    public DataTable generateDataTable(Query query, HttpServletRequest req) throws DataSourceException {
        List<DataItem> items = new DataRequest(req).query();
        Collections.sort(items, ORDER_BY_TIME_ID);
        SortedSet<Long> ids = new TreeSet<Long>();
        for (DataItem item : items) {
            ids.add(item.getStreamId());
        }

        DataTable data = new DataTable();
        ArrayList<ColumnDescription> cd = new ArrayList<ColumnDescription>();
        cd.add(new ColumnDescription("time", ValueType.DATETIME, "Time"));
        for (Long id : ids) {
            cd.add(new ColumnDescription("value" + id, ValueType.NUMBER, "Value " + id));
        }
        data.addColumns(cd);

        Calendar c = Calendar.getInstance(TimeUtil.TIMEZONE);
        for (DataItem item : items) {
            TableRow row = new TableRow();
            c.setTimeInMillis(item.getTimeMillis());
            row.addCell(new DateTimeValue(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND)));
            for (Long id : ids) {
                row.addCell(id == item.getStreamId() ? item.getValue() : Double.NaN);
            }
            data.addRow(row);
        }
        return data;
    }

    @Override
    protected boolean isRestrictedAccessMode() {
        return false;
    }
}
