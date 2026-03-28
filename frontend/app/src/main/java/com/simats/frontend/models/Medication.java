package com.simats.frontend.models;

import java.io.Serializable;

public class Medication implements Serializable {
    private String id;
    private String name;
    private String strength;
    private int quantity;
    private String frequency;
    private String instructions;
    private String duration;

    public Medication(String id, String name, String strength, int quantity, String frequency, String instructions, String duration) {
        this.id = id;
        this.name = name;
        this.strength = strength;
        this.quantity = quantity;
        this.frequency = frequency;
        this.instructions = instructions;
        this.duration = duration;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getStrength() { return strength; }
    public int getQuantity() { return quantity; }
    public String getFrequency() { return frequency; }
    public String getInstructions() { return instructions; }
    public String getDuration() { return duration; }
}
