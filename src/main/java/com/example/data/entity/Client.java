package com.example.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity

public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String telephone;

    // JPA constructor
    public Client() {}

    public Client(String name, String telephone) {
        this.name = name;
        this.telephone = telephone;
    }

    public String getName() {
        return this.name;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
