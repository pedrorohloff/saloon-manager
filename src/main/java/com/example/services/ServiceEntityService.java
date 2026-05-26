package com.example.services;

import com.example.data.entity.ServiceEntity;
import com.example.data.repository.ServiceEntityRepository;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ServiceEntityService {
    private final ServiceEntityRepository repository;

    public ServiceEntityService(ServiceEntityRepository repository) {
        this.repository = repository;
    }

    public List<ServiceEntity> listAllServices() {
        return repository.findAll();
    }

    public void saveService(ServiceEntity serviceEntity) {
        if (serviceEntity == null) {
            System.err.println("Service is empty.");
            return;
        }
        repository.save(serviceEntity);
    }

}
