package org.adultofuncional.main.security.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRequest {

    @NotBlank(message = "El nombre de la aplicación es obligatorio")
    @Size(max = 35, message = "El nombre de la aplicación no puede exceder 35 caracteres")
    private String applicationName;
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 1, message = "La contraseña no puede estar vacía")
    private String password;
    private LocalDate lastChangeDate;
}