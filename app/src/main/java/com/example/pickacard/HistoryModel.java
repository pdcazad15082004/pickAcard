package com.example.pickacard;

public class HistoryModel {
    private String action;
    private String time;
    private String icon;
    private String key;

    // ✅ Empty constructor required by Firebase
    public HistoryModel() {
    }

    // ✅ Constructor with action, time, icon
    public HistoryModel(String action, String time, String icon, String key) {
        this.action = action;
        this.time = time;
        this.icon = icon;
        this.key = key;
    }

    // ✅ Getter & Setter for 'action'
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    // ✅ Getter & Setter for 'time'
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    // ✅ Getter & Setter for 'icon'
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    // ✅ Getter & Setter for 'key'
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
