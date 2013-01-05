package org.vsegda.admin.client;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Roman Elizarov
 */
public class IdEditor extends Composite implements LeafValueEditor<Long> {
    private Label label = new Label();

    public IdEditor() {
        initWidget(label);
    }

    @Override
    public void setValue(Long id) {
        label.setText(id == null ? "" : String.valueOf(id));
    }

    @Override
    public Long getValue() {
        String s = label.getText().trim();
        return s.isEmpty() ? null : Long.parseLong(s);
    }
}
