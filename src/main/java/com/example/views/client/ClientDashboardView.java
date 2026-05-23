package com.example.views.client;

import com.example.data.entity.Appointment;
import com.example.data.entity.User;
import com.example.data.repository.UserRepository;
import com.example.services.AppointmentService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
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
    public ClientDashboardView(AppointmentService appointmentService, UserRepository userRepository) {
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        H2 title = new H2("Agendar novo horario");

        // input fields
        TextField descriptionField = new TextField("Descricao (Ex: corte de cabelo)");
        DatePicker datePicker = new DatePicker("Data");
        datePicker.setMin(LocalDate.now());

        ComboBox<String> timePicker = new ComboBox<>("Hora");
        timePicker.setItems(List.of("09:00", "10:00", "11:00", "14:00", "15:00", "16:00"));
        timePicker.setEnabled(false);

        datePicker.addValueChangeListener(event -> {
            timePicker.setEnabled(event.getValue() != null);
        });

        Button appointmentButton = new Button("Confirmar Agendamento");
        appointmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        appointmentButton.addClickListener(event -> {
            if (datePicker.isEmpty() || timePicker.isEmpty() || descriptionField.isEmpty()) {
                Notification.show("Campo(s) obrigatorio(s) nao preenchido(s).");
                return;
            }

            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            User loggerInClient = userRepository.findByUsername(currentUser);

            LocalTime selectedTime = LocalTime.parse(timePicker.getValue());

            Appointment appointment = new Appointment(
                    descriptionField.getValue(),
                    datePicker.getValue(),
                    selectedTime
            );
            appointment.setClient(loggerInClient);

            appointmentService.saveAppointment(appointment);

            Notification.show("Agendamento realizado com sucesso");

            descriptionField.clear();
            datePicker.clear();
            timePicker.clear();
        });

        HorizontalLayout dateTimeLine = new HorizontalLayout(datePicker, timePicker);
        add(title, descriptionField, dateTimeLine, appointmentButton);
    }

}
