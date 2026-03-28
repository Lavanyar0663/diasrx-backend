package com.simats.frontend.models;

import java.io.Serializable;
import java.util.List;

public class MedicationHistoryEntry extends Object implements Serializable {
    public MedicationHistoryEntry() {
        super();
    }

    private String date;
    private String time;
    private String department;
    private String diagnosis;
    private List<DrugEntry> drugs;

    public MedicationHistoryEntry(String date, String time, String department, String diagnosis, List<DrugEntry> drugs) {
        super();

        this.date = date;
        this.time = time;
        this.department = department;
        this.diagnosis = diagnosis;
        this.drugs = drugs;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getDepartment() { return department; }
    public String getDiagnosis() { return diagnosis; }
    public List<DrugEntry> getDrugs() { return drugs; }

    public static class DrugEntry extends Object implements Serializable {
        public DrugEntry() {
            super();
        }

        private String id;
        private String name;
        private String strength;
        private String frequency;
        private String duration;
        private String instructions;

        public DrugEntry(String id, String name, String strength, String frequency, String duration, String instructions) {
            super();

            this.id = id;
            this.name = name;
            this.strength = strength;
            this.frequency = frequency;
            this.duration = duration;
            this.instructions = instructions;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getStrength() { return strength; }
        public String getFrequency() { return frequency; }
        public String getDuration() { return duration; }
        public String getInstructions() { return instructions; }

        public com.simats.frontend.models.Medication toMedication() {
            // Defaulting quantity to 1 for re-issue; doctor can adjust in the form
            return new com.simats.frontend.models.Medication(id, name, strength, 1, frequency, instructions, duration);
        }
    }
}
