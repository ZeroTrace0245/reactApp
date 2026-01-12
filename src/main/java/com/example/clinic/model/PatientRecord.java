package com.example.clinic.model;

public final class PatientRecord {
    private final String name;
    private final String status;
    private final String room;

    public PatientRecord(String name, String status, String room) {
        this.name = name;
        this.status = status;
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getRoom() {
        return room;
    }
}
