package com.example.views.admin;

import com.example.data.entity.Appointment;
import com.example.data.entity.ServiceEntity;
import com.example.services.BusinessDashboardStats;

import java.util.List;
import java.util.Map;

public interface AdminDashboardViewInterface {
    void setAppointments(List<Appointment> appointments);
    void setServices(List<ServiceEntity> services);
    void setDashboardKPIs(BusinessDashboardStats stats, List<Map.Entry<String, Long>> popularServices);
    void showNotification(String message);
    void showEditForm(Appointment appointment);
    void hideEditForm();
}
