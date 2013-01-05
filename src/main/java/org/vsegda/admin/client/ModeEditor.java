package org.vsegda.admin.client;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import org.vsegda.shared.DataStreamMode;

/**
 * @author Roman Elizarov
 */
public class ModeEditor extends Composite implements LeafValueEditor<DataStreamMode> {
    private ListBox list = new ListBox();

    public ModeEditor() {
        for (DataStreamMode m : DataStreamMode.values())
            list.addItem(m.name());
        initWidget(list);
    }

    @Override
    public void setValue(DataStreamMode value) {
        list.setSelectedIndex(value.ordinal());
    }

    @Override
    public DataStreamMode getValue() {
        return DataStreamMode.values()[list.getSelectedIndex()];
    }
}
