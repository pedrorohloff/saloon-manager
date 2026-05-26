package com.example.data.entity;

public enum AppointmentStatus {
    PENDING("Pendente"),
    CONFIRMED("Confirmado"),
    CANCELLED("Cancelado");

    private final String description;

    AppointmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
