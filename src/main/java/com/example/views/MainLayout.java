package com.example.views;

import com.example.views.admin.AdminDashboardView;
import com.example.views.client.ClientDashboardView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.context.SecurityContextHolder;

@PermitAll
public class MainLayout extends AppLayout {

    private final transient AuthenticationContext authenticationContext;

    public MainLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        H1 logo = new H1("Cabeleleila Leila Salao de Beleiza");
        logo.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0 var(--lumo-space-m)");

        HorizontalLayout navigation = new HorizontalLayout();
        navigation.setSpacing(true);

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            RouterLink adminLink = new RouterLink("Painel Admin", AdminDashboardView.class);
            RouterLink clientLink = new RouterLink("Area do Cliente", ClientDashboardView.class);
            navigation.add(adminLink, clientLink);
        } else {
            RouterLink clientLink = new RouterLink("Meus Agendamentos", ClientDashboardView.class);
            navigation.add(clientLink);
        }

        Button logoutButton = new Button("Sair", event -> authenticationContext.logout());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(logo, navigation, logoutButton);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.expand(navigation);
        header.getStyle().set("padding", "0 var(--lumo-space-m)");

        addToNavbar(header);
    }
}
