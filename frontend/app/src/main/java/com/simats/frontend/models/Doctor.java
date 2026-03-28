package com.simats.frontend.models;

import java.io.Serializable;

public class Doctor implements Serializable {
    private String id;
    private String name;
    private String department;
    private String status;
    private String specialization;
    private String experience;
    private String mobile;
    private String email;

    public Doctor(String id, String name, String department, String status, 
                  String specialization, String experience, String mobile, String email) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.status = status;
        this.specialization = specialization;
        this.experience = experience;
        this.mobile = mobile;
        this.email = email;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getStatus() { return status; }
    public String getSpecialization() { return specialization; }
    public String getExperience() { return experience; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
}
