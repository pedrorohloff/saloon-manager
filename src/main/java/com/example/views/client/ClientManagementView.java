package com.example.views.client;

import com.example.data.entity.Client;
import com.example.services.ClientService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("manage-clients")
@RolesAllowed("ADMIN")
public class ClientManagementView extends VerticalLayout {
    public ClientManagementView(ClientService clientService) {
        // set header name
        H2 header = new H2("Client Database");

        // set grid to display clients
        Grid<Client> grid = new Grid<>(Client.class);

        // add which list is going to be used to display the data
        grid.setItems(clientService.listAllClients());

        // add both header and grid to the vertical layout
        add(header, grid);
    }
}
