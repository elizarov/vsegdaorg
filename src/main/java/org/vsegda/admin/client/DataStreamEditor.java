package org.vsegda.admin.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.vsegda.admin.shared.DataStreamDTO;

import java.util.List;

/**
 * @author Roman Elizarov
 */
public class DataStreamEditor extends DialogBox implements Editor<DataStreamDTO>,ClickHandler {
    IdEditor id = new IdEditor();
    TextBox tag = new TextBox();
    TextBox name = new TextBox();
    TextBox alert = new TextBox();
    ModeEditor mode = new ModeEditor();

    private Label status = new Label();
    private Button save = new Button("Save");
    private Button cancel = new Button("Cancel");

    private DataStreamEditorDriver driver = GWT.create(DataStreamEditorDriver.class);
    private DataStreamEditorListener onSave;

    public DataStreamEditor() {
        super(false);
        initLayout();
        initListeners();
    }

    public void edit(DataStreamDTO sd, DataStreamEditorListener onSave) {
        driver.initialize(this);
        driver.edit(sd);
        this.onSave = onSave;
        center();
    }

    private void initLayout() {
        setText("Edit stream");
        setModal(true);
        setGlassEnabled(true);

        tag.setMaxLength(10);
        name.setMaxLength(100);
        alert.setMaxLength(10);

        FlexTable contents = new FlexTable();
        int r = 0;
        contents.setWidget(r, 0, createLabel("id:"));
        contents.setWidget(r, 1, id);
        r++;
        contents.setWidget(r, 0, createLabel("tag:"));
        contents.setWidget(r, 1, tag);
        r++;
        contents.setWidget(r, 0, createLabel("name:"));
        contents.setWidget(r, 1, name);
        r++;
        contents.setWidget(r, 0, createLabel("alert:"));
        contents.setWidget(r, 1, alert);
        r++;
        contents.setWidget(r, 0, createLabel("mode:"));
        contents.setWidget(r, 1, mode);

        FlowPanel p = new FlowPanel();
        p.add(contents);
        p.add(status);
        save.addStyleName("frt");
        save.addStyleName("mrt");
        cancel.addStyleName("frt");
        p.add(cancel);
        p.add(save);
        setWidget(p);
    }

    private Label createLabel(String s) {
        Label label = new Label(s);
        label.addStyleName("label");
        return label;
    }

    void initListeners() {
        save.addClickHandler(this);
        cancel.addClickHandler(this);
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
        if (clickEvent.getSource() == save) {
            DataStreamDTO sd = driver.flush();
            List<EditorError> errors = driver.getErrors();
            if (!errors.isEmpty()) {
                status.setText("Error: " + errors.get(0).getMessage());
                return; // just show first error and return
            }
            onSave.dataStreamEditorSaved(sd);
        }
        hide();
    }
}
