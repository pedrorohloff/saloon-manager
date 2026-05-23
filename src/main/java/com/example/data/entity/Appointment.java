package com.example.data.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;


@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    private LocalTime appointmentTime;
    private LocalDate appointmentDate;

    private boolean isActive;

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    // JPA constructor
    public Appointment() {
        isActive = true;
    }

    public Appointment(String description, LocalDate appointmentDate, LocalTime appointmentTime) {
        this.description = description;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.isActive = true;
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
