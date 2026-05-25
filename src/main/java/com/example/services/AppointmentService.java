package com.example.services;

import com.example.data.entity.Appointment;
import com.example.data.entity.ServiceEntity;
import com.example.data.entity.User;
import com.example.data.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
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

    public boolean isModifiable(Appointment appointment) {
        if (appointment == null || appointment.getAppointmentDate() == null) {
            return false;
        }
        return calculateBusinessDaysBetween(LocalDate.now(), appointment.getAppointmentDate()) >= 2;
    }

    @Transactional
    public void updateAppointment(Appointment appointment,
                                  LocalDate date, LocalTime time,
                                  Collection<ServiceEntity> services) {
        if (appointment == null) {
            throw new IllegalArgumentException("Agendamento Invalido");
        }
        if (!isModifiable(appointment)) {
            throw new IllegalStateException("Esse agendamento nao pode mais ser alterado online pois excedeu o tempo limite");
        }
        if (date == null || time == null || services == null || services.isEmpty()) {
            throw new IllegalArgumentException("Todos os campos obrigatorios devem ser preenchidos");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Nao e possivel agendar para uma data passada");
        }

        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setServices(new ArrayList<>(services));

        appointmentRepository.save(appointment);
    }

    // auxiliary methods
    private long calculateBusinessDaysBetween(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            return 0;
        }
        long businessDays = 0;
        LocalDate current = start.plusDays(1);

        while (!current.isAfter(end)) {
            if (isBusinessDay(current)) {
                businessDays++;
            }
            current = current.plusDays(1);
        }
        return businessDays;
    }

    private boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

}
