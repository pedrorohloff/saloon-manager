package com.example.views;

import com.example.data.entity.RoleType;
import com.example.data.entity.User;
import com.example.services.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route("register")
@PageTitle("Cadastro - Salão Leila")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    public RegisterView(UserService userService, PasswordEncoder passwordEncoder) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Cadastro do cliente");

        TextField nameField = new TextField("Nome Completo");
        nameField.setWidth("300px");
        nameField.setRequiredIndicatorVisible(true);

        TextField telephoneField = new TextField("Telefone");
        telephoneField.setPlaceholder("(11) 99999-9999");
        telephoneField.setWidth("300px");
        telephoneField.setRequiredIndicatorVisible(true);

        TextField usernameField = new TextField("Usuario");
        usernameField.setWidth("300px");
        usernameField.setRequiredIndicatorVisible(true);

        PasswordField passwordField = new PasswordField("Senha");
        passwordField.setWidth("300px");
        passwordField.setRequiredIndicatorVisible(true);

        Button registerButton = new Button("Registrar", event -> {
            String name = nameField.getValue();
            String telephone = telephoneField.getValue();
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            if (name.isEmpty() || telephone.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Notification.show("Todos os campos sao obrigatorios.");
                return;
            }
            if (userService.findByUsername(username) != null) {
                Notification.show("Nome de usuario ja cadastrado");
                return;
            }

            try {
                User newUser = new User(
                        name,
                        telephone,
                        username,
                        passwordEncoder.encode(password),
                        RoleType.CLIENT
                );
                userService.saveClient(newUser);
                Notification.show("Cadastro realizado com sucesso, faca login para continuar");
                UI.getCurrent().navigate("login");
            } catch (Exception e) {
                Notification.show("Erro ao realizar cadastro: " + e.getMessage());
            }
        });

        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidth("300px");

        RouterLink loginLink = new RouterLink("Ja possui conta? Faca login", LoginView.class);
        loginLink.getStyle().set("margin-top", "var(--lumo-space-m)");

        add(title, nameField, telephoneField, usernameField, passwordField, registerButton, loginLink);
    }
}
