package com.example.views.client;

import com.example.data.entity.User;
import com.example.services.UserService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("manage-clients")
@RolesAllowed("ADMIN")
public class ClientManagementView extends VerticalLayout {
    public ClientManagementView(UserService userService) {
        // set header name
        H2 header = new H2("Client Database");

        // set grid to display clients
        Grid<User> grid = new Grid<>(User.class);

        // add which list is going to be used to display the data
        grid.setItems(userService.listAllClients());

        // add both header and grid to the vertical layout
        add(header, grid);
    }
}
