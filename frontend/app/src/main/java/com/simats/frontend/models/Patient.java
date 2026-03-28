package com.simats.frontend.models;

import java.io.Serializable;

public class Patient implements Serializable {
    private String name;
    private String departmentBadge;
    private String pid;
    private String ageGender;
    private String lastVisit;
    private String age;
    private String gender;
    private String bloodGroup;
    private String dob;
    private String mobile;
    private String email;
    private String address;
    private String assignedDoctor;
    private String regDate;

    public Patient(String name, String departmentBadge, String pid, String ageGender, String lastVisit,
                   String age, String gender, String bloodGroup, String dob, 
                   String mobile, String email, String address, String assignedDoctor, String regDate) {
        this.name = name;
        this.departmentBadge = departmentBadge;
        this.pid = pid;
        this.ageGender = ageGender;
        this.lastVisit = lastVisit;
        this.age = age;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.dob = dob;
        this.mobile = mobile;
        this.email = email;
        this.address = address;
        this.assignedDoctor = assignedDoctor;
        this.regDate = regDate;
    }

    public String getName() { return name; }
    public String getDepartmentBadge() { return departmentBadge; }
    public String getPid() { return pid; }
    public String getAgeGender() { return ageGender; }
    public String getLastVisit() { return lastVisit; }
    public String getAge() { return age; }
    public String getGender() { return gender; }
    public String getBloodGroup() { return bloodGroup; }
    public String getDob() { return dob; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getAssignedDoctor() { return assignedDoctor; }
    public String getRegDate() { return regDate; }
}
