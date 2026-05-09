package org.adultofuncional.main.security.application.usecase;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Listar todas las contraseñas de una cuenta.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que recupera todas las entradas del gestor de contraseñas
 * para un usuario.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Devuelve una lista de DTOs con los datos no sensibles de cada credencial.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Usa el repositorio para obtener todas las entradas de la cuenta
 * y las mapea a DTOs de respuesta.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class ListPasswordsUseCase {

    private final PasswordRepository passwordRepository;
    // TODO: Inyectar servicio de desencriptación

    @Transactional(readOnly = true)
    public List<PasswordResponse> execute(UUID accountId) {
        return passwordRepository.findAllByAccountId(accountId)
                .stream()
                .map(p -> PasswordResponse.builder()
                        .id(p.getId())
                        .applicationName(p.getApplicationName())
                        .lastChangeDate(p.getLastChangeDate())
                        .build())
                .toList();
    }
}