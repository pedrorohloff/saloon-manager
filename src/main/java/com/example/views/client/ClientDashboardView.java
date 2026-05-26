package com.example.views.client;

import com.example.data.entity.Appointment;
import com.example.data.entity.ServiceEntity;
import com.example.data.entity.User;
import com.example.services.AppointmentService;
import com.example.services.ServiceEntityService;
import com.example.services.UserService;
import com.example.services.GroupingRecommendation;
import com.example.views.MainLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.html.ListItem;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

@Route(value = "agendamento", layout = MainLayout.class)
@RolesAllowed({"CLIENT", "ADMIN"})
public class ClientDashboardView extends VerticalLayout implements ClientDashboardViewInterface{

    private final ClientDashboardPresenter presenter;

    // formatter
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // forms
    private final MultiSelectComboBox<ServiceEntity> servicesSelect;
    private final DatePicker datePicker;
    private final ComboBox<String> timePicker;
    private final Button appointmentButton;
    private final Button cancelButton;

    // filters
    private final DatePicker filterStartDate;
    private final DatePicker filterEndDate;
    private final Button filterButton;
    private final Button clearFilterButton;

    // weekly recommendations container
    private final VerticalLayout recommendationsContainer = new VerticalLayout();

    // appointments grid
    private final Grid<Appointment> grid;

    @Autowired
    public ClientDashboardView(ClientDashboardPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);

        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        H2 title = new H2("Agendar novo horário");

        recommendationsContainer.setWidth("80%");
        recommendationsContainer.setPadding(false);
        recommendationsContainer.setSpacing(true);


        // input fields
        servicesSelect = new MultiSelectComboBox<>("Selecione um ou mais serviços");
        servicesSelect.setWidth("350px");

        datePicker = new DatePicker("Data");
        datePicker.setMin(LocalDate.now());

        timePicker = new ComboBox<>("Hora");
        timePicker.setItems(List.of("09:00", "10:00", "11:00", "14:00", "15:00", "16:00"));
        timePicker.setEnabled(false);

        datePicker.addValueChangeListener(event -> {
            timePicker.setEnabled(event.getValue() != null);
        });

        filterStartDate = new DatePicker("De");
        filterEndDate = new DatePicker("Até");
        filterButton = new Button("Filtrar", event -> presenter.onFilterApplied(filterStartDate.getValue(), filterEndDate.getValue()));
        clearFilterButton = new Button("Limpar Filtro", event -> {
            filterStartDate.clear();
            filterEndDate.clear();
            presenter.onFilterCleared();
        });

        HorizontalLayout filterLayout = new HorizontalLayout(filterStartDate, filterEndDate, filterButton, clearFilterButton);
        filterLayout.setAlignItems(Alignment.END);

        H3 gridTitle = new H3("Lista de agendamentos");
        grid = new Grid<>(Appointment.class, false);

        grid.addColumn(Appointment::getFormattedServiceEntity).setHeader("Serviços").setAutoWidth(true);
        grid.addColumn(Appointment::getAppointmentDate).setHeader("Data").setAutoWidth(true);
        grid.addColumn(Appointment::getAppointmentTime).setHeader("Hora").setAutoWidth(true);

        grid.addColumn(appointment -> "R$ " + String.format("%.2f", appointment.getTotalPrice()))
                .setHeader("Valor Total")
                .setAutoWidth(true);

        grid.addColumn(Appointment::getStatusDescription).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(appointment -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button detailsButton = new Button("Detalhes", event -> showDetails(appointment));
            detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            actions.add(detailsButton);

            Button editButton = new Button("Editar", event -> presenter.onAppointmentSelectedForEdit(appointment));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            actions.add(editButton);

            return actions;
        }).setHeader("Ações").setAutoWidth(true);

        grid.setWidth("80%");

        appointmentButton = new Button("Confirmar Agendamento");
        appointmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // cancel button
        cancelButton = new Button("Cancelar", event -> presenter.onCancelClicked());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.setVisible(false);

        // save button
        appointmentButton.addClickListener(event -> presenter.onSaveClicked(
                datePicker.getValue(),
                timePicker.getValue(),
                servicesSelect.getValue()
        ));

        HorizontalLayout dateTimeLine = new HorizontalLayout(datePicker, timePicker);
        HorizontalLayout actionsLine = new HorizontalLayout(appointmentButton, cancelButton);

        add(title, servicesSelect, dateTimeLine, actionsLine, recommendationsContainer, new H3("Filtrar Historico"), filterLayout, gridTitle, grid);

        // initializes presenter
        this.presenter.init();
    }

    // auxiliary methods
    private void showDetails(Appointment appointment) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Detalhes do Agendamento");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialogLayout.add(new Span("Client: " + appointment.getClient().getName()));
        dialogLayout.add(new Span("Telefone: " + appointment.getClient().getTelephone()));
        dialogLayout.add(new Span("Data: " + appointment.getAppointmentDate().format(DATE_FORMATTER)
                + " às " + appointment.getAppointmentTime()));

        dialogLayout.add(new H3("Serviços Contratados: "));
        UnorderedList list = new UnorderedList();
        for (ServiceEntity service : appointment.getServices()) {
            list.add(new ListItem(service.getName() + " - R$ " + String.format("%.2f", appointment.getTotalPrice())));
        }
        dialogLayout.add(list);

        dialogLayout.add(new Span("Valor Total: R$ " + String.format("%.2f", appointment.getTotalPrice())));

        Button closeButton = new Button("Fechar", event -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeButton);

        dialog.add(dialogLayout);
        dialog.open();
    }


    @Override
    public void setAppointments(List<Appointment> appointments) {
        grid.setItems(appointments);
    }

    @Override
    public void setServices(List<ServiceEntity> services) {
        servicesSelect.setItems(services);
        servicesSelect.setItemLabelGenerator(ServiceEntity::getName);
    }

    @Override
    public void setRecommendations(List<GroupingRecommendation> recommendations) {
        recommendationsContainer.removeAll();

        if (recommendations.isEmpty()) {
            recommendationsContainer.setVisible(false);
            return;
        }

        recommendationsContainer.setVisible(true);

        for (GroupingRecommendation rec : recommendations) {
            HorizontalLayout card = new HorizontalLayout();
            card.setWidthFull();
            card.getStyle().set("background-color", "var(--lumo-primary-color-10pct)");
            card.getStyle().set("border", "1px solid var(--lumo-primary-color-50pct)");
            card.getStyle().set("border-radius", "var(--lumo-size-s)");
            card.getStyle().set("padding", "var(--lumo-space-m)");
            card.getStyle().set("align-items", "center");
            card.setJustifyContentMode(JustifyContentMode.BETWEEN);

            VerticalLayout textLayout = new VerticalLayout();
            textLayout.setPadding(false);
            textLayout.setSpacing(false);

            Span titleSpan = new Span("Recomendação de agrupamento de agendamentos.");
            titleSpan.getStyle().set("font-weight", "bold");
            titleSpan.getStyle().set("color", "var(--lumo-primary-text-color)");

            Span descSpan = new Span("Identificamos que voce possui agendamentos na semana de " +
                    rec.weekStart().format(DATE_FORMATTER) + ". Sugerimos reagenda-las para o dia do seu primeiro agendamento (" +
                    rec.targetDate().format(DATE_FORMATTER) + ") para sua conveniência");
            descSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");

            UnorderedList list = new UnorderedList();
            list.getStyle().set("margin-top", "var(--lumo-space-xs)");
            list.getStyle().set("font-size", "var(--lumo-font-size-xs)");

            for (Appointment appointment : rec.appointmentsToReschedule()) {
                list.add(new ListItem(appointment.getFormattedServiceEntity() + " - atual: " +
                        appointment.getAppointmentDate().format(DATE_FORMATTER) + " às " +
                        appointment.getAppointmentTime()));
            }

            textLayout.add(titleSpan, descSpan, list);

            Button groupButton = new Button("Reagendar para " + rec.targetDate().format(DATE_FORMATTER));
            groupButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            groupButton.addClickListener(event -> presenter.onGroupRecommendationClicked(rec));

            card.add(textLayout, groupButton);
            recommendationsContainer.add(card);
        }
    }

    @Override
    public void showNotification(String message) {
        Notification.show(message);
    }

    @Override
    public void clearForm() {
        servicesSelect.clear();
        datePicker.clear();
        timePicker.clear();
        timePicker.setEnabled(false);
        appointmentButton.setText("Confirmar Agendamento");
        cancelButton.setVisible(false);
        grid.asSingleSelect().clear();
    }

    @Override
    public void populateForm(Appointment appointment) {
        servicesSelect.setValue(new HashSet<>(appointment.getServices()));
        datePicker.setValue(appointment.getAppointmentDate());
        timePicker.setValue(appointment.getAppointmentTime().toString());
        timePicker.setEnabled(true);

        appointmentButton.setText("Salvar Alterações");
        cancelButton.setVisible(true);
    }

}
