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
 * @param masterKey clave en texto plano; admite las cuentas históricas y
 *                  limita el coste máximo de Argon2
 */
public record VerifyMasterKeyRequest(
    @NotBlank(message = "La Master Key es obligatoria")
    @Size(max = 128, message = "La Master Key no puede exceder 128 caracteres")
    String masterKey) {
}
