package com.example.views.client;

import com.example.data.entity.Appointment;
import com.example.data.entity.ServiceEntity;
import com.example.data.entity.User;
import com.example.services.AppointmentService;
import com.example.services.GroupingRecommendation;
import com.example.services.ServiceEntityService;
import com.example.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Component
@Scope("prototype")
public class ClientDashboardPresenter {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final ServiceEntityService serviceEntityService;

    private ClientDashboardViewInterface view;
    private User loggedInClient;
    private Appointment editingAppointment;

    @Value("${saloon.support.phone}")
    private String saloonNumber;

    @Autowired
    public ClientDashboardPresenter(AppointmentService appointmentService,
                                    UserService userService,
                                    ServiceEntityService serviceEntityService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.serviceEntityService = serviceEntityService;
    }

    public void setView(ClientDashboardViewInterface view) {
        this.view = view;
    }

    public void init() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        this.loggedInClient = userService.findByUsername(currentUser);

        view.setServices(serviceEntityService.listAllServices());
        refreshAppointments();
        refreshRecommendations();
    }

    public void refreshAppointments() {
        view.setAppointments(appointmentService.findAppointmentsByClient(loggedInClient));
    }

    public void refreshRecommendations() {
        List<GroupingRecommendation> recommendations = appointmentService.getGroupingRecommendations(loggedInClient);
        view.setRecommendations(recommendations);
    }

    public void onFilterApplied(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            view.showNotification("Selecione ambas as datas para aplicar o filtro.");
            return;
        }

        try {
            List<Appointment> filtered = appointmentService.findAppointmentsByClientAndPeriod(
                    loggedInClient, start, end
            );
            view.setAppointments(filtered);
            view.showNotification("Filtro aplicado com sucesso. Total encontrado: " + filtered.size());
        } catch (Exception e) {
            view.showNotification("Erro ao filtrar: " + e.getMessage());
        }
    }

    public void onFilterCleared() {
        refreshAppointments();
    }

    public void onAppointmentSelectedForEdit(Appointment appointment) {
        if (appointment == null) {
            onCancelClicked();
            return;
        }

        if (!appointmentService.isModifiable(appointment)) {
            view.showNotification("Não é possível alterar este agendamento online (limite de dois dias úteis). Por favor entre em contato pelo telefone: " + saloonNumber);
            return;
        }

        this.editingAppointment = appointment;
        view.populateForm(appointment);

    }

    public void onSaveClicked(LocalDate date, String timeStr, Set<ServiceEntity> services) {
        if (date == null || timeStr == null || services == null || services.isEmpty()) {
            view.showNotification("Campos obrigatórios não preenchidos");
            return;
        }

        try {
            LocalTime selectedTime = LocalTime.parse(timeStr);

            if (editingAppointment == null) {
                appointmentService.createAppointment(
                        loggedInClient,
                        date,
                        selectedTime,
                        services
                );
                view.showNotification("Agendamento realizado com sucesso!");
            } else {
                appointmentService.updateAppointment(
                        editingAppointment,
                        date,
                        selectedTime,
                        services
                );
                view.showNotification("Agendamento atualizado com sucesso!");
            }

            onCancelClicked();
            refreshAppointments();
            refreshRecommendations();
        } catch (Exception e) {
            view.showNotification("Erro: " + e.getMessage());
        }
    }

    public void onCancelClicked() {
        this.editingAppointment = null;
        view.clearForm();
    }

    public void onGroupRecommendationClicked(GroupingRecommendation rec) {
        try {
            appointmentService.groupAppointments(rec.appointmentsToReschedule(), rec.targetDate());
            view.showNotification("Agendamentos reagendados com sucesso!");
            refreshRecommendations();
            refreshAppointments();
        } catch (Exception e) {
            view.showNotification("Erro ao reagendar: " + e.getMessage());
        }
    }

    public User getLoggedInClient() {
        return loggedInClient;
    }
}
