package com.simats.frontend.models;

public class DoctorNotification {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    private String headerTitle; // Only used if TYPE_HEADER

    // Used if TYPE_ITEM
    private String title;
    private String description;
    private String time;
    private boolean isUnread;
    private int iconResId;
    private int iconBgResId;
    private int iconTintColor;
    private int timeTextColor;

    // Header Constructor
    public DoctorNotification(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    // Item Constructor
    public DoctorNotification(String title, String description, String time, boolean isUnread, int iconResId,
            int iconBgResId, int iconTintColor, int timeTextColor) {
        this.type = TYPE_ITEM;
        this.title = title;
        this.description = description;
        this.time = time;
        this.isUnread = isUnread;
        this.iconResId = iconResId;
        this.iconBgResId = iconBgResId;
        this.iconTintColor = iconTintColor;
        this.timeTextColor = timeTextColor;
    }

    public int getType() {
        return type;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean isUnread) {
        this.isUnread = isUnread;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getIconBgResId() {
        return iconBgResId;
    }

    public int getIconTintColor() {
        return iconTintColor;
    }

    public int getTimeTextColor() {
        return timeTextColor;
    }
}
