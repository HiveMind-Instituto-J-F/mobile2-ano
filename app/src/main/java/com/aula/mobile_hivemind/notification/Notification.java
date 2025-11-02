package com.aula.mobile_hivemind.notification;

import java.util.Date;

public class Notification {
    private String title;
    private String message;
    private String type;
    private long timestamp;
    private boolean isRead;

    public Notification() {
        // Construtor vazio necessário para Firebase/GSON
    }

    public Notification(String title, String message, String type, long timestamp, boolean isRead) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    // Getters e Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Date getDate() {
        return new Date(timestamp);
    }
}