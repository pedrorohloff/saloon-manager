package com.example.services;

import com.example.data.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

public record GroupingRecommendation (
        LocalDate weekStart,
        Appointment firstAppointment,
        List<Appointment> appointmentsToReschedule,
        LocalDate targetDate
){}
