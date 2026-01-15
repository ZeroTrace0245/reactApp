package com.example.clinic.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Appointment {
    private final String id;
    private final String patientName;
    private final String clinician;
    private final LocalDateTime scheduledAt;
    private final String notes;

    public Appointment(String id, String patientName, String clinician, LocalDateTime scheduledAt, String notes) {
        this.id = id;
        this.patientName = patientName;
        this.clinician = clinician;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getClinician() {
        return clinician;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment)) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
