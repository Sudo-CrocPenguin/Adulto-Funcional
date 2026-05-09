package org.adultofuncional.main.security.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.application.dto.PasswordUpdateRequest;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Actualizar una contraseña existente.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que permite modificar parcialmente una entrada del gestor.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Actualiza el nombre de la aplicación, la contraseña (re‑encriptándola)
 * o la fecha de último cambio.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Recibe el ID y los campos a modificar; aplica solo los que no son nulos
 * usando los valores actuales como base.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class UpdatePasswordUseCase {

    private final PasswordRepository passwordRepository;
    // TODO: Inyectar servicio de encriptación AES-256

    @Transactional
    public PasswordResponse execute(UUID accountId, UUID passwordId, PasswordUpdateRequest request) {
        Password password = passwordRepository.findByIdAndAccountId(passwordId, accountId)
                .orElseThrow(() -> new NotFoundException("Contraseña no encontrada con id: " + passwordId));

        // Tomar valores actuales como base
        String applicationName = password.getApplicationName();
        String salt = password.getSalt();
        byte[] iv = password.getIv();
        byte[] ciphertext = password.getCiphertext();
        LocalDate lastChangeDate = password.getLastChangeDate();

        // Aplicar solo los campos que llegaron
        if (StringUtils.hasText(request.getApplicationName())) {
            applicationName = request.getApplicationName();
        }
        if (StringUtils.hasText(request.getPassword())) {
            // TODO: usar encryptionService para generar salt, iv y ciphertext reales
            salt = "NEW_SALT_" + request.getPassword();
            iv = new byte[16];
            ciphertext = request.getPassword().getBytes();
        }
        if (request.getLastChangeDate() != null) {
            lastChangeDate = request.getLastChangeDate();
        }

        password.update(applicationName, salt, iv, ciphertext, lastChangeDate);

        Password updated = passwordRepository.save(password);

        return PasswordResponse.builder()
                .id(updated.getId())
                .applicationName(updated.getApplicationName())
                .lastChangeDate(updated.getLastChangeDate())
                .build();
    }
}