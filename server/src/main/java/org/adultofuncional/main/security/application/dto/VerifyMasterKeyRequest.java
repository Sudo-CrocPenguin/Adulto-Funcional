package org.adultofuncional.main.security.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud tipada para verificar la Master Key de la cuenta autenticada.
 *
 * <p>La clave solo existe durante el procesamiento y nunca forma parte de una
 * respuesta. Los límites coinciden con el contrato de registro vigente para
 * evitar diferencias entre creación y verificación.</p>
 *
 * @param masterKey clave maestra en texto plano, entre 12 y 24 caracteres
 */
public record VerifyMasterKeyRequest(
    @NotBlank(message = "La Master Key es obligatoria")
    @Size(min = 12, max = 24, message = "La Master Key debe tener entre 12 y 24 caracteres")
    String masterKey) {
}
