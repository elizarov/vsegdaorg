package org.vsegda.admin.client.actions;

/**
 * @author Roman Elizarov
 */
public interface AdminActionListener<T> {
    public void actionCompleted(AdminAction action, boolean success, String status, T result);
}
