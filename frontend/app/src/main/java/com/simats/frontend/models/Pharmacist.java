package com.simats.frontend.models;

import java.io.Serializable;

public class Pharmacist implements Serializable {
    private String id;
    private String name;
    private String status;
    private String mobile;
    private String email;
    private String experience;

    public Pharmacist(String id, String name, String status, String mobile, String email, String experience) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.mobile = mobile;
        this.email = email;
        this.experience = experience;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
    public String getExperience() { return experience; }
}
