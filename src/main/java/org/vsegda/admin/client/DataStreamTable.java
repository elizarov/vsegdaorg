package org.vsegda.admin.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import org.vsegda.admin.shared.DataStreamDTO;

import java.util.List;

/**
 * @author Roman Elizarov
 */
public class DataStreamTable extends FlexTable {
    private final DataStreamTableListener listener;

    public DataStreamTable(DataStreamTableListener listener) {
        this.listener = listener;
        addStyleName("data");
        String[] ss =  { "id", "tag", "name", "alert", "mode", "value", "time", "ago", "edit" };
        for (int c = 0; c < ss.length; c++) {
            Label label = new Label(ss[c]);
            label.addStyleName("th");
            setWidget(0, c, label);
        }
    }

    public void updateTable(List<DataStreamDTO> sds) {
        int r = 1;
        for (final DataStreamDTO sd : sds) {
            String[] ss = {
                    String.valueOf(sd.getId()),
                    sd.getTag(),
                    sd.getName(),
                    sd.getAlert(),
                    String.valueOf(sd.getMode()),
                    String.valueOf(sd.getValue()),
                    sd.getTime(),
                    sd.getAgo()
            };
            for (int c = 0; c < ss.length; c++) {
                Label label = new Label(ss[c]);
                label.addStyleName(sd.getFormatClass());
                setWidget(r, c, label);
            }
            Label edit = new Label("[...]");
            edit.addStyleName("edit");
            edit.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    listener.editDataStream(sd);
                }
            });
            setWidget(r, ss.length, edit);
            r++;
        }
        while (getRowCount() > r)
            removeRow(getRowCount() - 1);
    }
}
