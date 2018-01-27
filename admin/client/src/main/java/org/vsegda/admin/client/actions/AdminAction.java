package org.vsegda.admin.client.actions;

import com.google.gwt.core.shared.GWT;
import org.vsegda.admin.shared.rpc.AdminService;
import org.vsegda.admin.shared.rpc.AdminServiceAsync;

/**
 * @author Roman Elizarov
 */
public abstract class AdminAction<T> {
    protected final AdminServiceAsync adminService = GWT.create(AdminService.class);
    protected final String name;
    protected final String activeStatusString;
    protected final AdminActionListener<T> listener;

    protected AdminAction(String name, String activeStatusString, AdminActionListener<T> listener) {
        this.name = name;
        this.activeStatusString = activeStatusString;
        this.listener = listener;
    }

    public String getName() {
        return name;
    }

    public String getActiveStatusString() {
        return activeStatusString;
    }

    public abstract void execute();
}
