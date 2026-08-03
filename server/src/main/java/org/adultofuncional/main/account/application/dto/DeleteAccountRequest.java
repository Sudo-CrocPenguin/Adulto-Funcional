package org.adultofuncional.main.account.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Solicitud de eliminación irreversible con reautenticación explícita. */
public record DeleteAccountRequest(
    @NotBlank(message = "La contraseña actual es obligatoria")
    @Size(max = 128, message = "La contraseña actual no puede exceder 128 caracteres")
    String currentPassword) {
}
