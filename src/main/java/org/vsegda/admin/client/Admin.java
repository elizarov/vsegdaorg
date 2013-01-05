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
public class Admin implements EntryPoint {
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
        int c = 0;
        table.setText(0, c++, "id");
        table.setText(0, c++, "tag");
        table.setText(0, c++, "name");
        table.setText(0, c++, "alert");
        table.setText(0, c++, "mode");
        table.setText(0, c++, "value");
        table.setText(0, c++, "time");
        table.setText(0, c++, "ago");
        table.setText(0, c++, "edit");
    }

    private void updateTable(List<DataStreamDTO> ss) {
        int r = 0;
        for (final DataStreamDTO s : ss) {
            r++;
            int c = 0;
            table.setText(r, c++, String.valueOf(s.getId()));
            table.setText(r, c++, s.getTag());
            table.setText(r, c++, s.getName());
            table.setText(r, c++, s.getAlert());
            table.setText(r, c++, String.valueOf(s.getMode()));
            table.setText(r, c++, String.valueOf(s.getValue()));
            table.setText(r, c++, s.getTime());
            table.setText(r, c++, s.getAgo());
            Label edit = new Label("[...]");
            edit.addStyleName("edit");
            edit.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    editStream(s);
                }
            });
            table.setWidget(r, c++, edit);
        }
    }

    private void editStream(DataStreamDTO s) {
        final PopupPanel popup = new PopupPanel(false);

        Label contents = new Label();
        contents.setText("Not implemented");

        Button cancel = new Button("Cancel");
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                popup.hide();
            }
        });

        HorizontalPanel buttons = new HorizontalPanel();
        buttons.add(cancel);

        VerticalPanel vPanel = new VerticalPanel();

        vPanel.add(contents);
        vPanel.add(buttons);

        popup.setTitle("Edit stream");
        popup.setWidget(vPanel);
        popup.setModal(true);
        popup.center();
    }
}
