package com.example.data.repository;

import com.example.data.entity.Appointment;
import com.example.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClient(User client);
    List<Appointment> findByClientAndAppointmentDateBetween(User client, LocalDate startDate, LocalDate endDate);
}
