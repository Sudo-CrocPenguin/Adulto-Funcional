package org.adultofuncional.main.security.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Solicitud reautenticada para configurar la primera Master Key. */
public record ConfigureMasterKeyRequest(
    @NotBlank(message = "La contraseña actual es obligatoria")
    @Size(max = 128, message = "La contraseña actual no puede exceder 128 caracteres")
    String currentPassword,

    @NotBlank(message = "La nueva Master Key es obligatoria")
    @Size(min = 15, max = 128, message = "La nueva Master Key debe tener entre 15 y 128 caracteres")
    String newMasterKey) {
}
