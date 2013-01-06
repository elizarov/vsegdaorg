package org.vsegda.admin.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.vsegda.admin.client.actions.AdminAction;
import org.vsegda.admin.client.actions.AdminActionListener;
import org.vsegda.admin.client.actions.DataStreamUpdateAction;
import org.vsegda.admin.client.actions.DataStreamsLoadAction;
import org.vsegda.admin.shared.DataStreamDTO;

import java.util.Date;
import java.util.List;

/**
 * @author Roman Elizarov
 */
public class Admin implements EntryPoint, DataStreamEditorListener, DataStreamTableListener, ClickHandler {
    private static final String REFRESH = "Refresh";
    private static final int INITIAL_RETRY_INTERVAL = 500; // 0.5 sec
    private static final int MAX_RETRY_INTERVAL = 5000; // 5 sec

    private final Button actionButton = new Button(REFRESH);
    private final DataStreamTable table = new DataStreamTable(this);
    private final Label status = new Label();

    // at most one active action
    private AdminAction action;
    private int retryInterval = INITIAL_RETRY_INTERVAL;
    private Timer retryTimer;

    @Override
    public void onModuleLoad() {
        initListeners();
        initLayout();
        refreshTable();
    }

    private void initLayout() {
        FlowPanel actions = new FlowPanel();
        actions.add(actionButton);
        RootPanel.get("actions").add(actions);
        RootPanel.get("table").add(table);
        RootPanel.get("status").add(status);
    }

    private void initListeners() {
        actionButton.addClickHandler(this);
    }

    private void executeAction(AdminAction action) {
        this.action = action;
        status.setText(action.getActiveStatusString());
        actionButton.setHTML("Cancel " + action.getName());
        action.execute();
    }

    public void completeAction(final AdminAction action, boolean success, String status) {
        this.status.setText(status + " on " + new Date());
        if (this.action != action)
            return; // quit in case the action was already canceled
        if (!success) {
            // retry in 1 sec
            retryTimer = new Timer() {
                @Override
                public void run() {
                    retryTimer = null;
                    executeAction(action);
                }
            };
            retryTimer.schedule(retryInterval);
            retryInterval = Math.min(2 * retryInterval, MAX_RETRY_INTERVAL);
            return;
        }
        // success
        updateOnCompletedAction();
    }

    private void updateOnCompletedAction() {
        retryInterval = INITIAL_RETRY_INTERVAL;
        actionButton.setHTML(REFRESH);
        this.action = null;
    }

    public void cancelAction() {
        if (retryTimer != null) {
            retryTimer.cancel();
            retryTimer = null;
        }
        status.setText("Canceled " + action.getName());
        updateOnCompletedAction();
    }

    @Override
    public void onClick(ClickEvent event) {
        if (event.getSource() == actionButton) {
            if (action == null)
                refreshTable();
            else
                cancelAction();
        }
    }

    private void refreshTable() {
        executeAction(new DataStreamsLoadAction(new AdminActionListener<List<DataStreamDTO>>() {
            @Override
            public void actionCompleted(AdminAction action, boolean success, String status, List<DataStreamDTO> result) {
                completeAction(action, success, status);
                table.updateTable(result);
            }
        }));
    }

    @Override
    public void editDataStream(DataStreamDTO sd) {
        new DataStreamEditor().edit(sd, this);
    }

    @Override
    public void dataStreamEditorSaved(DataStreamDTO sd) {
        executeAction(new DataStreamUpdateAction(sd, new AdminActionListener<Void>() {
            @Override
            public void actionCompleted(AdminAction action, boolean success, String status, Void result) {
                completeAction(action, success, status);
                if (success)
                    refreshTable();
            }
        }));
    }
}
