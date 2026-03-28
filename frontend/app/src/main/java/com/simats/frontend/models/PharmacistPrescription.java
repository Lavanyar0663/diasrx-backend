package com.simats.frontend.models;

public class PharmacistPrescription {
    private String id;
    private String patientName;
    private String patientInitials;
    private String doctorName;
    private String status; // PENDING, READY, DISPENSED
    private String time;

    // Status color resource id for the left border indicator
    private int statusColorResId;

    // Status text color resource id
    private int statusTextColorResId;

    // Status background resource id
    private int statusBgResId;

    public PharmacistPrescription(String id, String patientName, String patientInitials, String doctorName,
            String status,
            String time, int statusColorResId, int statusTextColorResId, int statusBgResId) {
        this.id = id;
        this.patientName = patientName;
        this.patientInitials = patientInitials;
        this.doctorName = doctorName;
        this.status = status;
        this.time = time;
        this.statusColorResId = statusColorResId;
        this.statusTextColorResId = statusTextColorResId;
        this.statusBgResId = statusBgResId;
    }

    public String getId() {
        return id;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientInitials() {
        return patientInitials;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public int getStatusColorResId() {
        return statusColorResId;
    }

    public int getStatusTextColorResId() {
        return statusTextColorResId;
    }

    public int getStatusBgResId() {
        return statusBgResId;
    }
}
