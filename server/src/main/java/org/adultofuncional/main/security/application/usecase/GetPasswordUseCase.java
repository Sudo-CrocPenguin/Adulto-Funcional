package org.adultofuncional.main.security.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Obtener una contraseña por su ID.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que recupera una entrada del gestor de contraseñas.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Devuelve los datos (incluyendo la contraseña desencriptada) tras verificar
 * que la entrada pertenece a la cuenta autenticada.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Busca en el repositorio por ID y accountId, y desencripta el campo.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class GetPasswordUseCase {

    private final PasswordRepository passwordRepository;
    // TODO: Inyectar servicio de desencriptación

    @Transactional(readOnly = true)
    public PasswordResponse execute(UUID accountId, UUID passwordId) {
        var password = passwordRepository.findByIdAndAccountId(passwordId, accountId)
                .orElseThrow(() -> new NotFoundException("Contraseña no encontrada con id: " + passwordId));

        // TODO: String decrypted = decryptionService.decrypt(password.getEncryptedPassword(), masterKey);
        String decrypted = "DECRYPTED_" + password.getEncryptedPassword();

        return PasswordResponse.builder()
                .id(password.getId())
                .applicationName(password.getApplicationName())
                .password(decrypted)
                .category(password.getCategory())
                .lastChangeDate(password.getLastChangeDate())
                .build();
    }
}
