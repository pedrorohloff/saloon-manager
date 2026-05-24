package com.example.views.client;

import com.example.data.entity.Appointment;
import com.example.data.entity.ServiceEntity;
import com.example.data.entity.User;
import com.example.data.repository.UserRepository;
import com.example.services.AppointmentService;
import com.example.services.ServiceEntityService;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Route("agendamento")
@RolesAllowed({"CLIENT", "ADMIN"})
public class ClientDashboardView extends VerticalLayout {
    public ClientDashboardView(AppointmentService appointmentService,
                               UserRepository userRepository,
                               ServiceEntityService serviceEntityService) {
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggerInClient = userRepository.findByUsername(currentUser);

        H2 title = new H2("Agendar novo horario");

        // input fields
        MultiSelectComboBox<ServiceEntity> servicesSelect = new MultiSelectComboBox<>("Selecione um ou mais serviços");
        servicesSelect.setItems(serviceEntityService.listAllSerivces());
        servicesSelect.setItemLabelGenerator(ServiceEntity::getName);
        servicesSelect.setWidth("350px");

        DatePicker datePicker = new DatePicker("Data");
        datePicker.setMin(LocalDate.now());

        ComboBox<String> timePicker = new ComboBox<>("Hora");
        timePicker.setItems(List.of("09:00", "10:00", "11:00", "14:00", "15:00", "16:00"));
        timePicker.setEnabled(false);

        datePicker.addValueChangeListener(event -> {
            timePicker.setEnabled(event.getValue() != null);
        });

        H3 gridTitle = new H3("Lista de agendamentos");
        Grid<Appointment> grid = new Grid<>(Appointment.class, false);

        grid.addColumn(Appointment::getFormattedServiceEntity).setHeader("Serviços").setAutoWidth(true);
        grid.addColumn(Appointment::getAppointmentDate).setHeader("Data").setAutoWidth(true);
        grid.addColumn(Appointment::getAppointmentTime).setHeader("Hora").setAutoWidth(true);
        grid.addColumn(appointment -> "R$ " + String.format("%.2f", appointment.getTotalPrice()))
                .setHeader("Valor Total")
                .setAutoWidth(true);

        grid.setItems(appointmentService.findAppointmentsByClient(loggerInClient));
        grid.setWidth("80%");

        Button appointmentButton = new Button("Confirmar Agendamento");
        appointmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        appointmentButton.addClickListener(event -> {
            if (datePicker.isEmpty() || timePicker.isEmpty() || servicesSelect.isEmpty()) {
                Notification.show("Campo(s) obrigatorio(s) nao preenchido(s).");
                return;
            }


            try {
                LocalTime selectedTime = LocalTime.parse(timePicker.getValue());

                appointmentService.createAppointment(
                        loggerInClient,
                        datePicker.getValue(),
                        selectedTime,
                        servicesSelect.getValue()
                );

                Notification.show("Agendamento realizado com sucesso");

                servicesSelect.clear();
                datePicker.clear();
                timePicker.clear();

                grid.setItems(appointmentService.findAppointmentsByClient(loggerInClient));
            } catch (Exception e) {
                Notification.show("Erro ao agendar: " + e.getMessage());
            }
        });

        HorizontalLayout dateTimeLine = new HorizontalLayout(datePicker, timePicker);
        add(title, servicesSelect, dateTimeLine, appointmentButton, gridTitle, grid);
    }

}
