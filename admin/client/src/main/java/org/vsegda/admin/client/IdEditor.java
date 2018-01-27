package org.vsegda.admin.client;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Roman Elizarov
 */
public class IdEditor extends Composite implements LeafValueEditor<Long> {
    private TextBox text = new TextBox();

    public IdEditor() {
        initWidget(text);
    }

    @Override
    public void setValue(Long id) {
        text.setText(id == null ? "" : String.valueOf(id));
    }

    @Override
    public Long getValue() {
        String s = text.getText().trim();
        return s.isEmpty() ? null : Long.parseLong(s);
    }
}
