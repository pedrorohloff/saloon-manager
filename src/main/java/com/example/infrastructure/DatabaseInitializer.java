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

    public DatabaseInitializer(
            UserRepository userRepository,
            ServiceEntityRepository serviceEntityRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.serviceEntityRepository = serviceEntityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = new User(
                    "Admin Leila",
                    "(11) 91111-2222",
                    "admin",
                    passwordEncoder.encode("admin123"),
                    RoleType.ADMIN
            );

            User client1 = new User(
                    "Pedro",
                    "(77) 92311-5522",
                    "pedro rohloff",
                    passwordEncoder.encode("pedro123"),
                    RoleType.CLIENT
            );

            User client2 = new User(
                    "Maria Joaquina",
                    "(67) 95667-4556",
                    "maria joaquina",
                    passwordEncoder.encode("maria123"),
                    RoleType.CLIENT
            );

            userRepository.save(admin);
            userRepository.save(client1);
            userRepository.save(client2);

            System.out.println("Test user created successfully\n(\nUser: " + admin.getUsername() +
                    "\nPassword: " + admin.getPassword() + "\n)");
            System.out.println("Test user created successfully\n(\nUser: " + client1.getUsername() +
                    "\nPassword: pedro123\n)");
            System.out.println("Test user created successfully\n(\nUser: " + client2.getUsername() +
                    "\nPassword: maria123\n)");
        }

        if (serviceEntityRepository.count() == 0) {
            serviceEntityRepository.saveAll(List.of(
                    new ServiceEntity("Corte de Cabelo Masculino", 45.00),
                    new ServiceEntity("Corte de Cabelo Feminino", 70.00),
                    new ServiceEntity("Manicure", 30.00),
                    new ServiceEntity("Pedicure", 35.00),
                    new ServiceEntity("Design de Sobrancelha", 25.00),
                    new ServiceEntity("Escova e Hidratação", 80.00)
            ));
            System.out.println("Default services seeded successfully.");
        }
    }
}
