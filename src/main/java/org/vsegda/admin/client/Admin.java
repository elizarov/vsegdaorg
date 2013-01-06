package org.vsegda.admin.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.vsegda.admin.shared.DataStreamDTO;

import java.util.Date;
import java.util.List;

/**
 * @author Roman Elizarov
 */
public class Admin implements EntryPoint, DataStreamEditorListener, DataStreamTableListener, ClickHandler {
    private final AdminServiceAsync adminService = GWT.create(AdminService.class);
    private final DataStreamTable table = new DataStreamTable(this);
    private final Label status = new Label();

    private final Button refresh = new Button("Refresh");

    @Override
    public void onModuleLoad() {
        initListeners();
        initLayout();
        refreshTable();
    }

    private void initLayout() {
        FlowPanel actions = new FlowPanel();
        actions.add(refresh);
        RootPanel.get("actions").add(actions);
        RootPanel.get("table").add(table);
        RootPanel.get("status").add(status);
    }

    private void initListeners() {
        refresh.addClickHandler(this);
    }

    @Override
    public void onClick(ClickEvent event) {
        if (event.getSource() == refresh)
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
                table.updateTable(ss);
            }
        });
    }

    @Override
    public void editDataStream(DataStreamDTO sd) {
        new DataStreamEditor().edit(sd, this);
    }

    @Override
    public void dataStreamEditorSaved(DataStreamDTO sd) {
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

    @Override
    public void dataStreamEditorCanceled() {
        // todo:
    }
}
