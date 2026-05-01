package org.adultofuncional.main.security.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResponse {

    private UUID id;

    private String applicationName;

    private String password;

    private LocalDate lastChangeDate;
}