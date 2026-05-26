package com.example.data.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "appointment_services",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<ServiceEntity> services = new ArrayList<>();
    private LocalTime appointmentTime;
    private LocalDate appointmentDate;
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    // JPA constructor
    public Appointment() {
        isActive = true;
        status = AppointmentStatus.PENDING;
    }

    public Appointment(LocalDate appointmentDate, LocalTime appointmentTime) {
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.isActive = true;
        this.status = AppointmentStatus.PENDING;
    }

    // business rules
    public Double getTotalPrice() {
        return services.stream()
                .mapToDouble(ServiceEntity::getPrice)
                .sum();
    }

    public String getFormattedServiceEntity() {
        return services.stream()
                .map(ServiceEntity::getName)
                .collect(Collectors.joining(", "));
    }

    public String getStatusDescription() {
        if (status == null) {
            return "Pendente";
        }
        return status.getDescription();
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

    public List<ServiceEntity> getServices() {
        return services;
    }

    public void setServices(List<ServiceEntity> services) {
        this.services = services;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}
