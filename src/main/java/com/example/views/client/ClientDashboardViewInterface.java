package com.example.views.client;

import com.example.data.entity.Appointment;
import com.example.data.entity.ServiceEntity;
import com.example.services.GroupingRecommendation;

import java.util.List;

public interface ClientDashboardViewInterface {
    void setAppointments(List<Appointment> appointments);
    void setServices(List<ServiceEntity> services);
    void setRecommendations(List<GroupingRecommendation> recommendations);
    void showNotification(String message);
    void clearForm();
    void populateForm(Appointment appointment);
}
