package com.example.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "application_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String telephone;
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    // JPA constructor
    public User() {}

    public User(String name, String telephone, String username, String password,RoleType role) {
        this.name = name;
        this.telephone = telephone;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }
}
