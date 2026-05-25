package com.example.views.admin;

import com.example.data.entity.User;
import com.example.services.UserService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("admin")
@RolesAllowed("ADMIN")
public class AdminDashboardView extends VerticalLayout {
    public AdminDashboardView(UserService userService) {
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Control Panel - Clients");

        Grid<User> grid = new Grid<>(User.class, false);

        grid.addColumn(User::getUsername).setHeader("Username").setAutoWidth(true);

        grid.setItems(userService.listAllClients());

        add(title, grid);
    }
}
