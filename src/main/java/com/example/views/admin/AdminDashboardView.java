package com.example.views.admin;

import com.example.data.entity.Appointment;
import com.example.data.entity.AppointmentStatus;
import com.example.data.entity.ServiceEntity;
import com.example.data.entity.User;
import com.example.services.AppointmentService;
import com.example.services.BusinessDashboardStats;
import com.example.services.ServiceEntityService;
import com.example.services.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.dialog.Dialog;
import com.example.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Route(value = "admin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminDashboardView extends VerticalLayout {

    private final AppointmentService appointmentService;
    private final ServiceEntityService serviceEntityService;

    private final Grid<Appointment> grid;

    // admin edit form
    private final VerticalLayout editForm;
    private final Span clientInfoLabel;
    private final MultiSelectComboBox<ServiceEntity> servicesSelect;
    private final DatePicker datePicker;
    private final ComboBox<String> timePicker;
    private final ComboBox<AppointmentStatus> statusSelect;
    private final Button saveButton;
    private final Button cancelButton;

    private Appointment editingAppointment;

    // dashboard elements
    private final HorizontalLayout dashboardContainer;
    private final Span revenueValue;
    private final Span projectionValue;
    private final Grid<Map.Entry<String, Long>> popularServicesGrid;

    public AdminDashboardView(
            UserService userService,
            AppointmentService appointmentService,
            ServiceEntityService serviceEntityService
    ) {
        this.appointmentService = appointmentService;
        this.serviceEntityService = serviceEntityService;

        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        H2 title = new H2("Painel de Controles - Clientes");
        title.setText("Painel Administrativo - Controle de Agendamentos");

        dashboardContainer = new HorizontalLayout();
        dashboardContainer.setWidth("80%");
        dashboardContainer.setSpacing(true);

        // revenue weekly
        VerticalLayout revenueCard = new VerticalLayout();
        revenueCard.getStyle().set("background-color", "var(--lumo-sucess-color-10pct)");
        revenueCard.getStyle().set("border", "1px solid var(--lumo-sucess-color-50pct)");
        revenueCard.getStyle().set("border-radius", "var(--lumo-size-s)");
        revenueCard.getStyle().set("padding", "var(--lumo-space-m)");
        revenueCard.setAlignItems(Alignment.CENTER);
        Span revTitle = new Span("Faturamento da semana");
        revTitle.getStyle().set("font-size", "var(--lumo-font-size-s)");
        revTitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        revenueValue = new Span("R$ 0,00");
        revenueValue.getStyle().set("font-size", "var(--lumo-font-size-xxl)");
        revenueValue.getStyle().set("font-weight", "bold");
        revenueValue.getStyle().set("color", "var(--lumo-success-text-color)");
        revenueCard.add(revTitle, revenueValue);

        // monthly projection
        VerticalLayout projectionCard = new VerticalLayout();
        projectionCard.getStyle().set("background-color", "var(--lumo-sucess-color-10pct)");
        projectionCard.getStyle().set("border", "1px solid var(--lumo-sucess-color-50pct)");
        projectionCard.getStyle().set("border-radius", "var(--lumo-size-s)");
        projectionCard.getStyle().set("padding", "var(--lumo-space-m)");
        projectionCard.setAlignItems(Alignment.CENTER);
        Span projTitle = new Span("Projecao Mensal (Base Semanal)");
        projTitle.getStyle().set("font-size", "var(--lumo-font-size-s)");
        projTitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        projectionValue = new Span("R$ 0,00");
        projectionValue.getStyle().set("font-size", "var(--lumo-font-size-xxl)");
        projectionValue.getStyle().set("font-weight", "bold");
        projectionValue.getStyle().set("color", "var(--lumo-success-text-color)");
        projectionCard.add(projTitle, projectionValue);

        // most accessed services
        VerticalLayout popularServicesCard = new VerticalLayout();
        popularServicesCard.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        popularServicesCard.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        popularServicesCard.getStyle().set("border-radius", "var(--lumo-size-s)");
        popularServicesCard.getStyle().set("padding", "var(--lumo-space-s)");
        popularServicesCard.setWidth("40%");

        Span servicesTitle = new Span("Servicos da Semana");
        servicesTitle.getStyle().set("font-weight", "bold");
        servicesTitle.getStyle().set("font-size", "var(--lumo-font-size-s)");

        popularServicesGrid = new Grid<>();
        popularServicesGrid.setAllRowsVisible(true);
        popularServicesGrid.addColumn(Map.Entry::getKey).setHeader("Servico");
        popularServicesGrid.addColumn(Map.Entry::getValue).setHeader("Reservas");
        popularServicesGrid.getStyle().set("font-size" , "var(--lumo-font-size-xs)");

        popularServicesCard.add(servicesTitle, popularServicesGrid);

        HorizontalLayout kpisContainer = new HorizontalLayout(revenueCard, projectionCard);
        kpisContainer.setWidth("60%");
        kpisContainer.setSpacing(true);

        dashboardContainer.add(kpisContainer, popularServicesCard);

        // admin edit form
        editForm = new VerticalLayout();
        editForm.setPadding(true);
        editForm.setSpacing(true);
        editForm.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        editForm.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        editForm.getStyle().set("border-radius", "var(--lumo-size-s");
        editForm.setAlignItems(Alignment.CENTER);
        editForm.setWidth("80%");
        editForm.setVisible(false);

        H3 formTitle = new H3("Editar Agendamento");
        clientInfoLabel = new Span();
        clientInfoLabel.getStyle().set("font-weight", "bold");

        servicesSelect = new MultiSelectComboBox<>("Servicos");
        servicesSelect.setItems(serviceEntityService.listAllSerivces());
        servicesSelect.setItemLabelGenerator(ServiceEntity::getName);
        servicesSelect.setWidth("350px");

        datePicker = new DatePicker("Data");

        timePicker = new ComboBox<>("Hora");
        timePicker.setItems(List.of("09:00", "10:00", "11:00", "14:00", "15:00", "16:00"));

        statusSelect = new ComboBox<>("Status");
        statusSelect.setItems(AppointmentStatus.values());
        statusSelect.setItemLabelGenerator(status -> switch (status) {
            case PENDING -> "Pendente";
            case CONFIRMED -> "Confirmado";
            case CANCELLED -> "Cancelado";
        });
        statusSelect.setWidth("200px");

        saveButton = new Button("Salvar alteracoes", event -> handleSave());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelButton = new Button("Cancelar", event -> clearSelection());
        cancelButton.addThemeVariants(ButtonVariant.TERTIARY);

        HorizontalLayout formActions = new HorizontalLayout(saveButton, cancelButton);
        HorizontalLayout formsFields = new HorizontalLayout(datePicker, timePicker, statusSelect);
        formsFields.setAlignItems(Alignment.END);

        editForm.add(formTitle, clientInfoLabel, servicesSelect, formsFields, formActions);

        // appointment grid
        H3 gridTitle = new H3("Lista de agendamentos");
        grid = new Grid<>(Appointment.class, false);

        grid.addColumn(appointment -> appointment.getClient() != null ? appointment.getClient().getName() : "N/A")
                .setHeader("Cliente").setAutoWidth(true);

        grid.addColumn(appointment -> appointment.getClient() != null ? appointment.getClient().getTelephone() : "N/A")
                .setHeader("Telefone").setAutoWidth(true);

        grid.addColumn(Appointment::getFormattedServiceEntity).setHeader("Servicos").setAutoWidth(true);

        grid.addColumn(Appointment::getAppointmentDate).setHeader("Data").setAutoWidth(true);
        grid.addColumn(Appointment::getAppointmentTime).setHeader("Hora").setAutoWidth(true);

        grid.addColumn(appointment -> "R$ " + String.format("%.2f", appointment.getTotalPrice()))
                .setHeader("Valor Total").setAutoWidth(true);

        grid.addColumn(appointment -> appointment.isActive() ? "Ativo" : "Cancelado")
                .setHeader("Status").setAutoWidth(true);

        grid.addColumn(Appointment::getStatusDescription).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(appointment -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button editButton = new Button("Editar", event -> selectAppointmentForEdit(appointment));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            actions.add(editButton);

            // directly altering status on grid
            if (appointment.getStatus() == AppointmentStatus.PENDING) {
                Button confirmButton = new Button("Confirmar", event ->  {
                    try {
                        appointmentService.updateAppointmentStatus(appointment, AppointmentStatus.CONFIRMED);
                        Notification.show("Agendamento confirmado com sucesso!");
                        grid.setItems(appointmentService.listAllAppointments());
                    } catch (Exception e) {
                        Notification.show("Erro ao alterar status: " + e.getMessage());
                    }
                });
                confirmButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                actions.add(confirmButton);
            }

            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                Button cancelButton = new Button("Cancelar", event ->  {
                    try {
                        appointmentService.updateAppointmentStatus(appointment, AppointmentStatus.CANCELLED);
                        Notification.show("Agendamento cancelado com sucesso!");
                        grid.setItems(appointmentService.listAllAppointments());
                    } catch (Exception e) {
                        Notification.show("Erro ao alterar status: " + e.getMessage());
                    }
                });
                cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                actions.add(cancelButton);
            }


            return actions;
        }).setHeader("Acoes").setAutoWidth(true);

        grid.setItems(appointmentService.listAllAppointments());
        grid.setWidth("80%");

        HorizontalLayout tableHeader = new HorizontalLayout();
        tableHeader.setWidth("80%");
        tableHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        tableHeader.setAlignItems(Alignment.CENTER);

        Button newAppointmentButton = new Button("Novo Agendamento", event -> openNewAppointmentDialog(userService));
        newAppointmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        tableHeader.add(gridTitle, newAppointmentButton);

        add(title, new H3("Painel de Desempenho Semanal"), dashboardContainer, editForm, tableHeader, grid);
        refreshDashboard();
    }

    private void handleSave() {
        if (editingAppointment == null) {
            return;
        }
        if (datePicker.isEmpty() || timePicker.isEmpty() || datePicker.isEmpty() || statusSelect.isEmpty()) {
            Notification.show("Todos os campos devem ser preenchidos");
            return;
        }

        try {
            LocalTime selectedTime = LocalTime.parse(timePicker.getValue());
            appointmentService.updateAppointmentByAdmin(
                    editingAppointment,
                    datePicker.getValue(),
                    selectedTime,
                    servicesSelect.getValue(),
                    statusSelect.getValue()
            );

            Notification.show("Agendamento atualizado com sucesso pelo administrador.");
            clearSelection();
            grid.setItems(appointmentService.listAllAppointments());
        } catch (Exception e) {
            Notification.show("Erro ao salvar alteracoes: " + e.getMessage());
        }

    }

    private void selectAppointmentForEdit(Appointment appointment) {
        editingAppointment = appointment;
        clientInfoLabel.setText("Cliente: " +
                (appointment.getClient() != null ? appointment.getClient().getName() : "N/A") +
                " | Telefone: " + (appointment.getClient() != null ? appointment.getClient().getTelephone() : "N/A"));

        servicesSelect.setValue(new HashSet<>(appointment.getServices()));
        datePicker.setValue(appointment.getAppointmentDate());
        timePicker.setValue(appointment.getAppointmentTime().toString());
        statusSelect.setValue(appointment.getStatus());

        editForm.setVisible(true);
    }

    private void clearSelection() {
        editingAppointment = null;
        servicesSelect.clear();
        datePicker.clear();
        timePicker.clear();
        statusSelect.clear();
        editForm.setVisible(false);
    }

    private void refreshDashboard() {
        BusinessDashboardStats stats = appointmentService.getDashboardStats();

        revenueValue.setText("R$ " + String.format("%.2f", stats.weeklyRevenue()));
        projectionValue.setText("R$ " + String.format("%.2f", stats.monthlyProjection()));

        popularServicesGrid.setItems(stats.serviceCounts().entrySet());
        grid.setItems(appointmentService.listAllAppointments());
    }

    private void openNewAppointmentDialog(UserService userService) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Criar Novo Agendamento (Administrador)");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);

        ComboBox<User> clientSelect = new ComboBox<>("Selecione o Cliente");
        clientSelect.setItems(userService.listAllClients());
        clientSelect.setItemLabelGenerator(User::getName);
        clientSelect.setWidthFull();
        clientSelect.setRequiredIndicatorVisible(true);

        MultiSelectComboBox<ServiceEntity> services = new MultiSelectComboBox<>("Selecione os Serviços");
        services.setItems(serviceEntityService.listAllSerivces());
        services.setItemLabelGenerator(ServiceEntity::getName);
        services.setWidthFull();
        services.setRequiredIndicatorVisible(true);

        DatePicker date = new DatePicker("Data");
        date.setRequiredIndicatorVisible(true);
        date.setWidthFull();

        ComboBox<String> time = new ComboBox<>("Hora");
        time.setItems(List.of("09:00", "10:00", "11:00", "14:00", "15:00", "16:00"));
        time.setRequiredIndicatorVisible(true);
        time.setWidthFull();

        dialogLayout.add(clientSelect, services, date, time);

        Button confirmBtn = new Button("Agendar", event -> {
            if (clientSelect.isEmpty() || services.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Notification.show("Todos os campos obrigatórios devem ser preenchidos.");
                return;
            }
            try {
                appointmentService.createAppointment(
                        clientSelect.getValue(),
                        date.getValue(),
                        LocalTime.parse(time.getValue()),
                        services.getValue()
                );
                Notification.show("Agendamento criado com sucesso para o cliente!");
                dialog.close();
                refreshDashboard();
            } catch (Exception e) {
                Notification.show("Erro ao criar agendamento: " + e.getMessage());
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button cancelBtn = new Button("Cancelar", event -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(confirmBtn, cancelBtn);
        dialog.add(dialogLayout);
        dialog.open();
    }
}
