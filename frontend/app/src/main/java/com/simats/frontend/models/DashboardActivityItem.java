package com.simats.frontend.models;

public class DashboardActivityItem {
    public enum Type {
        PRESCRIPTION, ALERT, EVENT
    }

    private Type type;
    private String title;
    private String subtitle; // e.g., "Antibiotics • 10:30 AM"
    private String description; // for alerts
    private String status; // "Pending", "Dispensed", null for alerts
    private String initials; // e.g., "JD"
    private int iconResId; // for EVENT type
    private String patientName; // for EVENT type
    private String time; // for EVENT type

    // Constructor for Prescription
    public DashboardActivityItem(String title, String subtitle, String status, String initials) {
        this.type = Type.PRESCRIPTION;
        this.title = title;
        this.subtitle = subtitle;
        this.status = status;
        this.initials = initials;
    }

    // Constructor for Timeline Event
    public DashboardActivityItem(String title, String patientName, String time, int iconResId) {
        this.type = Type.EVENT;
        this.title = title;
        this.patientName = patientName;
        this.time = time;
        this.iconResId = iconResId;
    }

    // Constructor for Alert
    public DashboardActivityItem(String title, String description) {
        this.type = Type.ALERT;
        this.title = title;
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getInitials() {
        return initials;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getTime() {
        return time;
    }
}
