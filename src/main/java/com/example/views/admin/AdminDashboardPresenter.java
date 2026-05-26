package com.example.views.admin;

import com.example.data.entity.Appointment;
import com.example.data.entity.AppointmentStatus;
import com.example.data.entity.ServiceEntity;
import com.example.services.AppointmentService;
import com.example.services.BusinessDashboardStats;
import com.example.services.ServiceEntityService;
import com.example.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope("prototype")
public class AdminDashboardPresenter {

    private final AppointmentService appointmentService;
    private final ServiceEntityService serviceEntityService;
    private final UserService userService;

    private AdminDashboardViewInterface view;
    private Appointment editingAppointment;

    @Autowired
    public AdminDashboardPresenter(AppointmentService appointmentService,
                                   ServiceEntityService serviceEntityService,
                                   UserService userService) {
        this.appointmentService = appointmentService;
        this.serviceEntityService = serviceEntityService;
        this.userService = userService;
    }

    public void setView(AdminDashboardViewInterface view) {
        this.view = view;
    }

    public void init() {
        view.setServices(serviceEntityService.listAllSerivces());
        refreshDashboard();
    }

    public void refreshDashboard() {
        List<Appointment> appointments = appointmentService.listAllAppointments();
        view.setAppointments(appointments);

        BusinessDashboardStats stats = appointmentService.getDashboardStats();
        List<Map.Entry<String, Long>> popularServices = new ArrayList<>(stats.serviceCounts().entrySet());
        view.setDashboardKPIs(stats, popularServices);
    }

    public void onAppointmentSelectedForEdit(Appointment appointment) {
        if (appointment == null) {
            onCancelEditClicked();
            return;
        }
        this.editingAppointment = appointment;
        view.showEditForm(appointment);
    }

    public void onUpdateStatus(Appointment appointment, AppointmentStatus status) {
        try {
            appointmentService.updateAppointmentStatus(appointment, status);
            view.showNotification("Agendamento " + (status == AppointmentStatus.CONFIRMED ? "confirmado" : "cancelado") + " com sucesso!");
            refreshDashboard();
        } catch (Exception e) {
            view.showNotification("Erro ao alterar status: " + e.getMessage());
        }
    }

    public void onSaveEditClicked(LocalDate date, String timeStr, Set<ServiceEntity> services, AppointmentStatus status) {
        if (editingAppointment == null) {
            return;
        }
        if (date == null || timeStr == null || timeStr.isEmpty() || status == null) {
            view.showNotification("Todos os campos devem ser preenchidos");
            return;
        }

        try {
            LocalTime selectedTime = LocalTime.parse(timeStr);
            appointmentService.updateAppointmentByAdmin(
                    editingAppointment,
                    date,
                    selectedTime,
                    services,
                    status
            );

            view.showNotification("Agendamento atualizado com sucesso pelo administrador.");
            onCancelEditClicked();
            refreshDashboard();
        } catch (Exception e) {
            view.showNotification("Erro ao salvar alterações: " + e.getMessage());
        }
    }

    public void onCancelEditClicked() {
        this.editingAppointment = null;
        view.hideEditForm();
    }

    public void onCreateNewAppointment(com.example.data.entity.User client, LocalDate date, String timeStr, Set<ServiceEntity> services) {
        if (client == null || date == null || timeStr == null || timeStr.isEmpty() || services == null || services.isEmpty()) {
            view.showNotification("Todos os campos obrigatórios devem ser preenchidos.");
            return;
        }
        try {
            appointmentService.createAppointment(
                    client,
                    date,
                    LocalTime.parse(timeStr),
                    services
            );
            view.showNotification("Agendamento criado com sucesso para o cliente!");
            refreshDashboard();
        } catch (Exception e) {
            view.showNotification("Erro ao criar agendamento: " + e.getMessage());
        }
    }

    public UserService getUserService() {
        return userService;
    }
}
