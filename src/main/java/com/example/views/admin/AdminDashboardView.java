package com.example.views.admin;

import com.example.data.entity.Appointment;
import com.example.data.entity.ServiceEntity;
import com.example.data.entity.User;
import com.example.services.AppointmentService;
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
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;

@Route("admin")
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
    private final Button saveButton;
    private final Button cancelButton;

    private Appointment editingAppointment;

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

        saveButton = new Button("Salvar alteracoes", event -> handleSave());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelButton = new Button("Cancelar", event -> clearSelection());
        cancelButton.addThemeVariants(ButtonVariant.TERTIARY);

        HorizontalLayout formActions = new HorizontalLayout(saveButton, cancelButton);
        HorizontalLayout formsFields = new HorizontalLayout(datePicker, timePicker);
        formsFields.setAlignItems(Alignment.END);

        editForm.add(formTitle, clientInfoLabel, servicesSelect, formsFields, formActions);

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

        grid.addComponentColumn(appointment -> {
            Button editButton = new Button("Editar", event -> selectAppointmentForEdit(appointment));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            return editButton;
        }).setHeader("Acoes").setAutoWidth(true);

        grid.setItems(appointmentService.listAllAppointments());
        grid.setWidth("80%");

        add(title, editForm, gridTitle, grid);
    }

    private void handleSave() {
        if (editingAppointment == null) {
            return;
        }
        if (datePicker.isEmpty() || timePicker.isEmpty() || datePicker.isEmpty()) {
            Notification.show("Todos os campos devem ser preenchidos");
            return;
        }

        try {
            LocalTime selectedTime = LocalTime.parse(timePicker.getValue());
            appointmentService.updateAppointmentByAdmin(
                    editingAppointment,
                    datePicker.getValue(),
                    selectedTime,
                    servicesSelect.getValue()
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

        editForm.setVisible(true);
    }

    private void clearSelection() {
        editingAppointment = null;
        servicesSelect.clear();
        datePicker.clear();
        timePicker.clear();
        editForm.setVisible(false);
    }
}
