package org.adultofuncional.main.security.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.application.dto.PasswordUpdateRequest;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Caso de uso: Actualizar una contraseña existente.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que permite modificar parcialmente una entrada del gestor.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Actualiza el nombre de la aplicación, la contraseña (re‑encriptándola),
 * la categoría o la fecha de último cambio.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Recibe el ID y los campos a modificar; aplica solo los que no son nulos.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class UpdatePasswordUseCase {

    private final PasswordRepository passwordRepository;
    // TODO: Inyectar servicio de encriptación

    @Transactional
    public PasswordResponse execute(UUID accountId, UUID passwordId, PasswordUpdateRequest request) {
        var password = passwordRepository.findByIdAndAccountId(passwordId, accountId)
                .orElseThrow(() -> new NotFoundException("Contraseña no encontrada con id: " + passwordId));

        if (StringUtils.hasText(request.getApplicationName())) {
            password.updateApplicationName(request.getApplicationName());
        }
        if (StringUtils.hasText(request.getPassword())) {
            // TODO: String encrypted = encryptionService.encrypt(request.getPassword(), masterKey);
            String encrypted = "ENCRYPTED_" + request.getPassword();
            password.updateEncryptedPassword(encrypted);
        }
        if (StringUtils.hasText(request.getCategory())) {
            password.updateCategory(request.getCategory());
        }
        if (request.getLastChangeDate() != null) {
            password.updateLastChangeDate(request.getLastChangeDate());
        } else {
            // Si no se envía, mantener la existente o poner hoy? Mantenemos la existente.
            // No hacemos nada.
        }

        var updated = passwordRepository.save(password);

        // TODO: Desencriptar para la respuesta (si se necesita mostrar)
        String decrypted = "DECRYPTED_" + updated.getEncryptedPassword();

        return PasswordResponse.builder()
                .id(updated.getId())
                .applicationName(updated.getApplicationName())
                .password(decrypted)
                .category(updated.getCategory())
                .lastChangeDate(updated.getLastChangeDate())
                .build();
    }
}
