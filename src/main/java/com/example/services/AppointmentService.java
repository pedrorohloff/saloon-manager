package com.example.services;

import com.example.data.entity.Appointment;
import com.example.data.entity.ServiceEntity;
import com.example.data.entity.User;
import com.example.data.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public void createAppointment(User client, LocalDate date, LocalTime time, Collection<ServiceEntity> services) {
        if (client == null || date == null || time == null || services.isEmpty()) {
            throw new IllegalArgumentException("Todos os campos devem ser preenchidos.");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data do agendamento nao pode ser anterior a data atual.");
        }

        Appointment appointment = new Appointment(date, time);
        appointment.setClient(client);
        appointment.setServices(new ArrayList<>(services));

        appointmentRepository.save(appointment);
    }

    public List<Appointment> findAppointmentsByClient(User client) {
        return appointmentRepository.findByClient(client);
    }

    public List<Appointment> listAllAppointments() {
        return appointmentRepository.findAll();
    }

    public void saveAppointment(Appointment appointment) {
        appointmentRepository.save(appointment);
    }

}
