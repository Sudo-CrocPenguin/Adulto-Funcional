package org.adultofuncional.main.account.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;

@Getter
public class Account {
    private final UUID id;
    private String name;
    private String email;
    private String phone;
    private final LocalDateTime createdAt;
    private boolean active;

    // Private constructor (forces use of factory method)
    public Account(UUID id, String name, String email, String phone, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }

    // =========================
    // Factory Method
    // =========================
    public static Account create(String name, String email, String phone) {
        validateName(name);
        validateEmail(email);

        return new Account(
                UUID.randomUUID(),
                name,
                email,
                phone,
                true
        );
    }

    // =========================
    // Domain Behavior
    // =========================

    public void updateDetails(String name, String phone) {
        validateName(name);
        this.name = name;
        this.phone = phone;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    // =========================
    // Business Rules / Validation
    // =========================

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }

    public boolean isActive() {
        return active;
    }
}
