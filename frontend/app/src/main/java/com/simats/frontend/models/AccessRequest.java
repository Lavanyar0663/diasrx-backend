package com.simats.frontend.models;

import java.io.Serializable;

public class AccessRequest implements Serializable {
    private String id;
    private String name;
    private String role;
    private String department;
    private String timeAgo;
    private String status;
    private String phone;
    private String email;
    private int avatarResId;

    public AccessRequest(String id, String name, String role, String department, String email, String phone, String timeAgo, String status, int avatarResId) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.department = department;
        this.email = email;
        this.phone = phone;
        this.timeAgo = timeAgo;
        this.status = status;
        this.avatarResId = avatarResId;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }
}
