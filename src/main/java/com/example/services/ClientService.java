package com.example.services;

import com.example.data.entity.Client;
import com.example.data.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public List<Client> listAllClients() {
        return repository.findAll();
    }

    public Optional<Client> getClient(Long id) {
        return repository.findById(id);
    }

    public void saveClient(Client client) {
        if (client == null) {
            System.err.println("Client is null.");
            return;
        }

        repository.save(client);
    }

    public void deleteClient(Client client) {
        repository.delete(client);
    }

}
