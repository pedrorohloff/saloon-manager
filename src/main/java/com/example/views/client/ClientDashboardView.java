package com.example.views.client;

import com.example.data.entity.Appointment;
import com.example.data.entity.ServiceEntity;
import com.example.data.entity.User;
import com.example.services.AppointmentService;
import com.example.services.ServiceEntityService;
import com.example.services.UserService;
import com.example.services.GroupingRecommendation;
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
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

@Route("agendamento")
@RolesAllowed({"CLIENT", "ADMIN"})
public class ClientDashboardView extends VerticalLayout {

    // dependency injection
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final ServiceEntityService serviceEntityService;

    // create/edit forms
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

    // recommendations
    private final VerticalLayout recommendationsContainer = new VerticalLayout();

    // others
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

        recommendationsContainer.setWidth("80%");
        recommendationsContainer.setPadding(false);
        recommendationsContainer.setSpacing(true);


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

        filterStartDate = new DatePicker("De");
        filterEndDate = new DatePicker("Ate");
        filterButton = new Button("Filtrar", event -> handleFilter());
        clearFilterButton = new Button("Limpar Filtro", event -> handleClearFilter());

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

            if (appointmentService.isModifiable(appointment)) {
                Button editButton = new Button("Editar", event -> handleGridSelection(appointment));
                editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                actions.add(editButton);
            }

            return actions;
        }).setHeader("Acoes").setAutoWidth(true);

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
        // grid.asSingleSelect().addValueChangeListener(event -> handleGridSelection(event.getValue()));

        HorizontalLayout dateTimeLine = new HorizontalLayout(datePicker, timePicker);
        HorizontalLayout actionsLine = new HorizontalLayout(appointmentButton, cancelButton);

        add(title, servicesSelect, dateTimeLine, actionsLine, recommendationsContainer, new H3("Filtrar Historico"), filterLayout, gridTitle, grid);
        refreshRecommendation();
    }

    // auxiliary methods
    private void showDetails(Appointment appointment) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Detalhes do Agendamento");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dialogLayout.add(new Span("Client: " + appointment.getClient().getName()));
        dialogLayout.add(new Span("Telefone: " + appointment.getClient().getTelephone()));
        dialogLayout.add(new Span("Data: " + appointment.getAppointmentDate().format(dateFormatter)
                + " as " + appointment.getAppointmentTime()));

        dialogLayout.add(new H3("Servicos Contratados: "));
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

    private void handleFilter() {
        if (filterStartDate.isEmpty() || filterEndDate.isEmpty()) {
            Notification.show("Selecine ambas as datas para aplicar o filtro.");
            return;
        }

        try {
            List<Appointment> filtered = appointmentService.findAppointmentsByClientAndPeriod(
                    loggedInClient,
                    filterStartDate.getValue(),
                    filterEndDate.getValue()
            );
            grid.setItems(filtered);
            Notification.show("Filtro aplicado com sucesso. Total encontrado: " + filtered.size());
        } catch (Exception e) {
            Notification.show("Erro ao filtrar: " + e.getMessage());
        }
    }

    private void handleClearFilter() {
        filterStartDate.clear();
        filterEndDate.clear();
        grid.setItems(appointmentService.findAppointmentsByClient(loggedInClient));
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

    private void refreshRecommendation() {
        recommendationsContainer.removeAll();
        List<GroupingRecommendation> recommendations = appointmentService.getGroupingRecommendations(loggedInClient);

        if (recommendations.isEmpty()) {
            recommendationsContainer.setVisible(false);
            return;
        }

        recommendationsContainer.setVisible(true);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (GroupingRecommendation rec : recommendations) {
            HorizontalLayout card = new HorizontalLayout();
            card.setWidthFull();
            card.getStyle().set("background-color", "var(--lumo-primary-color-10pct)");
            card.getStyle().set("border", "1px solid var(--lumo-primary-color-50pct)");
            card.getStyle().set("border-radius", "var(--lumo-size-s)");
            card.getStyle().set("padding", "var(--lumo-space-m)");
            card.getStyle().set("align-items", "center)");
            card.setJustifyContentMode(JustifyContentMode.BETWEEN);

            VerticalLayout textLayout = new VerticalLayout();
            textLayout.setPadding(false);
            textLayout.setSpacing(false);

            Span titleSpan = new Span("Recomendacao de agrupamento de agendamentos.");
            titleSpan.getStyle().set("font-weight", "bold");
            titleSpan.getStyle().set("color", "var(--lumu-primary-text-color)");

            Span descSpan = new Span("Identificamos que voce possui agendamentos na semana de " +
                    rec.weekStart().format(formatter) + ". Sugerimos reagenda-las para o dia do seu primeiro agendamento (" +
                    rec.targetDate().format(formatter) + ") para sua conveniencia");
            descSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");

            UnorderedList list = new UnorderedList();
            list.getStyle().set("margin-top", "var(--lumo-space-xs)");
            list.getStyle().set("font-size", "var(--lumo-font-size-xs)");

            for (Appointment appointment : rec.appointmentsToReschedule()) {
                list.add(new ListItem(appointment.getFormattedServiceEntity() + " - atual: " +
                        appointment.getAppointmentDate().format(formatter) + " as " +
                        appointment.getAppointmentTime()));
            }

            textLayout.add(titleSpan, descSpan, list);

            Button groupButton = new Button("Reagendar para " + rec.targetDate().format(formatter));
            groupButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            groupButton.addClickListener(event -> {
                try {
                    appointmentService.groupAppointments(rec.appointmentsToReschedule(), rec.targetDate());
                    Notification.show("Agendamentos reagendados com sucesso!");
                    refreshRecommendation();
                    grid.setItems(appointmentService.findAppointmentsByClient(loggedInClient));
                } catch (Exception e) {
                    Notification.show("Erro ao reagendar: " + e.getMessage());
                }
            });

            card.add(textLayout, groupButton);
            recommendationsContainer.add(card);
        }
    }

}
