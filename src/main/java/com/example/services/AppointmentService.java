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
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<Appointment> findAppointmentsByClientAndPeriod(User client, LocalDate start, LocalDate end) {
        if (client == null || start == null || end == null) {
            throw new IllegalArgumentException("Parametros de busca sao obrigatorios");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("A data de inicio nao pode ser depois da data final");
        }
        return appointmentRepository.findByClientAndAppointmentDateBetween(client, start, end);
    }

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

    public List<GroupingRecommendation> getGroupingRecommendations(User client) {
        if (client == null) {
            return List.of();
        }

        // search all future appointments for a client
        List<Appointment> upcomingAppointments = appointmentRepository.findByClient(client).stream()
                .filter(Appointment::isActive)
                .filter(a -> !a.getAppointmentDate().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Appointment::getAppointmentDate).thenComparing(Appointment::getAppointmentTime))
                .toList();

        // group the appointments by the start of the week
        Map<LocalDate, List<Appointment>> appointmentsByWeek = upcomingAppointments.stream()
                .collect(Collectors.groupingBy(a -> a.getAppointmentDate()
                        .with(TemporalAdjusters.previousOrSame((DayOfWeek.MONDAY)))));

        List<GroupingRecommendation> recommendations = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Appointment>> entry :appointmentsByWeek.entrySet()) {
            LocalDate weekStart = entry.getKey();
            List<Appointment> weekAppointments = entry.getValue();

            if (weekAppointments.size() > 1) {
                Appointment firstAppointment = weekAppointments.get(0);
                LocalDate firstDate = firstAppointment.getAppointmentDate();

                // identify other appointments in the same week with different dates and are allowed to be changed
                List<Appointment> toReschedule = weekAppointments.stream()
                        .skip(1)
                        .filter(a -> !a.getAppointmentDate().equals(firstDate))
                        .filter(this::isModifiable)
                        .toList();

                if (!toReschedule.isEmpty()) {
                    recommendations.add(new GroupingRecommendation(
                            weekStart,
                            firstAppointment,
                            toReschedule,
                            firstDate
                    ));
                }
            }
        }
        return recommendations;
    }

    @Transactional
    public void groupAppointments(List<Appointment> appointments, LocalDate targetDate) {
        if (appointments.isEmpty() || appointments == null || targetDate == null) {
            throw new IllegalArgumentException("Parametros invalidos para o agrupamento");
        }

        for (Appointment appointment : appointments) {
            if (!isModifiable(appointment)) {
                throw new IllegalStateException("O agendamento do dia " + appointment.getAppointmentDate() + "nao pode ser alterado online");
            }
            appointment.setAppointmentDate(targetDate);
            appointmentRepository.save(appointment);
        }
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
