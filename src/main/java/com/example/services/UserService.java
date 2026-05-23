package com.example.services;

import com.example.data.entity.User;
import com.example.data.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> listAllClients() {
        return repository.findAll();
    }

    public Optional<User> getClient(Long id) {
        return repository.findById(id);
    }

    public void saveClient(User user) {
        if (user == null) {
            System.err.println("Client is null.");
            return;
        }

        repository.save(user);
    }

    public void deleteClient(User user) {
        repository.delete(user);
    }

}
