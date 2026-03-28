package com.simats.frontend.models;

import java.io.Serializable;

public class DoctorPatient implements Serializable {
    private String id;
    private String name;
    private String initials;
    private String opdId;
    private String ageGender;
    private String lastVisit;
    private String phone;
    private String email;
    private String visitTime;
    private int avatarColorResId;
    private int avatarTextColorResId;
    private boolean isNew;

    public DoctorPatient(String name, String opdId, String ageGender, String visitTime, String lastVisit) {
        this.name = name;
        this.opdId = opdId;
        this.ageGender = ageGender;
        this.visitTime = visitTime;
        this.lastVisit = lastVisit;
        this.initials = generateInitials(name);
    }

    private String generateInitials(String name) {
        if (name == null || name.isEmpty()) return "??";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    public DoctorPatient(String id, String name, String initials, String opdId, String ageGender, String lastVisit, String phone,
            int avatarColorResId, int avatarTextColorResId, boolean isNew) {
        this(id, name, initials, opdId, ageGender, lastVisit, phone, null, avatarColorResId, avatarTextColorResId, isNew);
    }

    public DoctorPatient(String id, String name, String initials, String opdId, String ageGender, String lastVisit, String phone, String email,
            int avatarColorResId, int avatarTextColorResId, boolean isNew) {
        this.id = id;
        this.name = name;
        this.initials = initials;
        this.opdId = opdId;
        this.ageGender = ageGender;
        this.lastVisit = lastVisit;
        this.phone = phone;
        this.email = email;
        this.avatarColorResId = avatarColorResId;
        this.avatarTextColorResId = avatarTextColorResId;
        this.isNew = isNew;
    }

    public boolean isNew() {
        return isNew;
    }

    public String getName() {
        return name;
    }

    public String getInitials() {
        return initials;
    }

    public String getOpdId() {
        return opdId;
    }

    public String getAgeGender() {
        return ageGender;
    }

    public String getLastVisit() {
        return lastVisit;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public int getAvatarColorResId() {
        return avatarColorResId;
    }

    public int getAvatarTextColorResId() {
        return avatarTextColorResId;
    }

    public String getVisitTime() {
        return visitTime;
    }

    public String getId() {
        return id;
    }
}
