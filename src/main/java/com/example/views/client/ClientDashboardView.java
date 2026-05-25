package com.example.views.client;

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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;

@Route("agendamento")
@RolesAllowed({"CLIENT", "ADMIN"})
public class ClientDashboardView extends VerticalLayout {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final ServiceEntityService serviceEntityService;
    private final MultiSelectComboBox<ServiceEntity> servicesSelect;
    private final DatePicker datePicker;
    private final ComboBox<String> timePicker;
    private final Button appointmentButton;
    private final Button cancelButton;
    private final Grid<Appointment> grid;
    private Appointment editingAppointment = null;
    private final User loggedInClient;

    public ClientDashboardView(AppointmentService appointmentService,
                               UserService userService,
                               ServiceEntityService serviceEntityService) {

        this.appointmentService = appointmentService;
        this.userService = userService;
        this.serviceEntityService = serviceEntityService;

        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        this.loggedInClient = userService.findByUsername(currentUser);

        H2 title = new H2("Agendar novo horario");

        // input fields
        servicesSelect = new MultiSelectComboBox<>("Selecione um ou mais serviços");
        servicesSelect.setItems(serviceEntityService.listAllSerivces());
        servicesSelect.setItemLabelGenerator(ServiceEntity::getName);
        servicesSelect.setWidth("350px");

        datePicker = new DatePicker("Data");
        datePicker.setMin(LocalDate.now());

        timePicker = new ComboBox<>("Hora");
        timePicker.setItems(List.of("09:00", "10:00", "11:00", "14:00", "15:00", "16:00"));
        timePicker.setEnabled(false);

        datePicker.addValueChangeListener(event -> {
            timePicker.setEnabled(event.getValue() != null);
        });

        H3 gridTitle = new H3("Lista de agendamentos");
        grid = new Grid<>(Appointment.class, false);

        grid.addColumn(Appointment::getFormattedServiceEntity).setHeader("Serviços").setAutoWidth(true);
        grid.addColumn(Appointment::getAppointmentDate).setHeader("Data").setAutoWidth(true);
        grid.addColumn(Appointment::getAppointmentTime).setHeader("Hora").setAutoWidth(true);
        grid.addColumn(appointment -> "R$ " + String.format("%.2f", appointment.getTotalPrice()))
                .setHeader("Valor Total")
                .setAutoWidth(true);

        grid.setItems(appointmentService.findAppointmentsByClient(loggedInClient));
        grid.setWidth("80%");

        appointmentButton = new Button("Confirmar Agendamento");
        appointmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // botao cancelar
        cancelButton = new Button("Cancelar", event -> clearForm());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.setVisible(false);

        // botao confirmar salvar
        appointmentButton.addClickListener(event -> handleAppointmentSave());

        // grid selecion
        grid.asSingleSelect().addValueChangeListener(event -> handleGridSelection(event.getValue()));

        HorizontalLayout dateTimeLine = new HorizontalLayout(datePicker, timePicker);
        HorizontalLayout actionsLine = new HorizontalLayout(appointmentButton, cancelButton);

        add(title, servicesSelect, dateTimeLine, appointmentButton, gridTitle, grid);
    }

    private void handleGridSelection(Appointment selected) {
        if (selected == null) {
            clearForm();
            return;
        }

        if (!appointmentService.isModifiable(selected)) {
            Notification.show("Nao e possivel alterar este agendamento online (limite de dois dias uteis). Por favor entre em contato pelo telefone: (11) 91111-2222");
            grid.asSingleSelect().clear();
            return;
        }

        editingAppointment = selected;
        servicesSelect.setValue(new HashSet<>(selected.getServices()));
        datePicker.setValue(selected.getAppointmentDate());
        timePicker.setValue(selected.getAppointmentTime().toString());
        timePicker.setEnabled(true);

        appointmentButton.setText("Salvar Alteracoes");
        cancelButton.setVisible(true);
    }

    private void handleAppointmentSave() {
        if (datePicker.isEmpty() || timePicker.isEmpty() || servicesSelect.isEmpty()) {
            Notification.show("Campos obrigatorios nao preenchidos");
            return;
        }

        try {
            LocalTime selectedTime = LocalTime.parse(timePicker.getValue());

            if (editingAppointment == null) {
                appointmentService.createAppointment(
                        loggedInClient,
                        datePicker.getValue(),
                        selectedTime,
                        servicesSelect.getValue()
                );
                Notification.show("Agendamento realizado com sucesso!");
            } else {
                appointmentService.updateAppointment(
                        editingAppointment,
                        datePicker.getValue(),
                        selectedTime,
                        servicesSelect.getValue()
                );
                Notification.show("Agendamento atualizado com sucesso!");
            }

            clearForm();
            grid.setItems(appointmentService.findAppointmentsByClient(loggedInClient));
        } catch (Exception e) {
            Notification.show("Erro: " + e.getMessage());
        }
    }

    private void clearForm() {
        editingAppointment = null;
        servicesSelect.clear();
        datePicker.clear();
        timePicker.clear();
        timePicker.setEnabled(false);
        appointmentButton.setText("Confirmar Agendamento");
        cancelButton.setVisible(false);
        grid.asSingleSelect().clear();
    }

}
