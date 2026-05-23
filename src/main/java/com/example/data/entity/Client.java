package com.example.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@Entity

public class Client {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String telephone;

    public @NonNull String getName() {
        return this.name;
    }

    public @NonNull String getTelephone() {
        return this.telephone;
    }
}
