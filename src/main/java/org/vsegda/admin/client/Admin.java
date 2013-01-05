package org.vsegda.admin.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.vsegda.admin.shared.DataStreamDTO;

import java.util.Date;
import java.util.List;

/**
 * @author Roman Elizarov
 */
public class Admin implements EntryPoint, DataStreamSaved {
    private final AdminServiceAsync adminService = GWT.create(AdminService.class);
    private final FlexTable table = new FlexTable();
    private final Label status = new Label();

    @Override
    public void onModuleLoad() {
        Button refresh = new Button("Refresh");
        refresh.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                refreshTable();
            }
        });

        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(refresh);
        RootPanel.get("actions").add(hPanel);

        table.addStyleName("data");
        setupTableColumns();
        RootPanel.get("table").add(table);

        RootPanel.get("status").add(status);

        refreshTable();
    }

    private void refreshTable() {
        status.setText("Loading...");
        adminService.getDataStreams(new AsyncCallback<List<DataStreamDTO>>() {
            @Override
            public void onFailure(Throwable t) {
                status.setText("Failed to load: " + t);
            }

            @Override
            public void onSuccess(List<DataStreamDTO> ss) {
                status.setText("Last updated on " + new Date());
                updateTable(ss);
            }
        });
    }

    private void setupTableColumns() {
        String[] ss =  { "id", "tag", "name", "alert", "mode", "value", "time", "ago", "edit" };
        for (int c = 0; c < ss.length; c++) {
            Label label = new Label(ss[c]);
            label.addStyleName("th");
            table.setWidget(0, c, label);
        }
    }

    private Label createTH(String s) {
        Label label = new Label(s);
        label.addStyleName("th");
        return label;
    }

    private void updateTable(List<DataStreamDTO> sds) {
        int r = 0;
        for (final DataStreamDTO sd : sds) {
            r++;
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
                table.setWidget(r, c, label);
            }
            Label edit = new Label("[...]");
            edit.addStyleName("edit");
            edit.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    editStream(sd);
                }
            });
            table.setWidget(r, ss.length, edit);
        }
    }

    private void editStream(DataStreamDTO stream) {
        new DataStreamEditor().edit(stream, this);
    }

    @Override
    public void onDataStreamSaved(DataStreamDTO sd) {
        adminService.updateDataStream(sd, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                status.setText("Failed to save: " + t);
            }

            @Override
            public void onSuccess(Void aVoid) {
                refreshTable();
            }
        });
    }
}
