package org.adultofuncional.main.account.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO que se devuelve al cliente después de consultar o actualizar una cuenta.
 * No contiene campos sensibles (contraseña, master key).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private UUID id;               // account_id
    private String names;          // account_names
    private String lastnames;      // account_lastnames
    private String email;          // account_email
    private String phone;          // account_phone
    private LocalDateTime createdAt; // account_created_at
}