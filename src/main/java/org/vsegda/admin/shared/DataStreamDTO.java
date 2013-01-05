package org.vsegda.admin.shared;

import org.vsegda.shared.DataStreamMode;

import java.io.Serializable;

/**
 * @author Roman Elizarov
 */
public class DataStreamDTO implements Serializable {
    private long id;
    private String tag;
    private String name;
    private String alert;
    private DataStreamMode mode;
    private double value;
    private String time;
    private String ago;
    private String formatClass;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public DataStreamMode getMode() {
        return mode;
    }

    public void setMode(DataStreamMode mode) {
        this.mode = mode;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAgo() {
        return ago;
    }

    public void setAgo(String ago) {
        this.ago = ago;
    }

    public void setFormatClass(String formatClass) {
        this.formatClass = formatClass;
    }

    public String getFormatClass() {
        return formatClass;
    }
}
