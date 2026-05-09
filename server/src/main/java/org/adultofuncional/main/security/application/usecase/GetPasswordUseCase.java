package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Obtener una contraseña por su ID.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que recupera una entrada del gestor de contraseñas.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Devuelve los datos de la credencial verificando que pertenezca
 * a la cuenta autenticada.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Busca en el repositorio por ID y accountId y mapea al DTO de respuesta.
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

        return PasswordResponse.builder()
                .id(password.getId())
                .applicationName(password.getApplicationName())
                .lastChangeDate(password.getLastChangeDate())
                .build();
    }
}