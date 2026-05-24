package com.example.infrastructure;

import com.example.data.entity.RoleType;
import com.example.data.entity.ServiceEntity;
import com.example.data.entity.User;
import com.example.data.repository.ServiceEntityRepository;
import com.example.data.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ServiceEntityRepository serviceEntityRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, ServiceEntityRepository serviceEntityRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.serviceEntityRepository = serviceEntityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = new User();

            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin1234"));
            admin.setRole(RoleType.ADMIN);

            userRepository.save(admin);
            System.out.println("Test admin user created successfully\n(\nUser: admin\nPassword: admin1234\n)");
        }

        if (serviceEntityRepository.count() == 0) {
            serviceEntityRepository.saveAll(List.of(
                    new ServiceEntity("Corte de Cabelo Masculino", 45.00),
                    new ServiceEntity("Corte de Cabelo Masculino", 70.00),
                    new ServiceEntity("Manicure", 30.00),
                    new ServiceEntity("Pedicure", 35.00),
                    new ServiceEntity("Design de Sobrancelha", 25.00),
                    new ServiceEntity("Escova e Hidratação", 80.00)
            ));
            System.out.println("Default services seeded successfully.");
        }
    }
}
