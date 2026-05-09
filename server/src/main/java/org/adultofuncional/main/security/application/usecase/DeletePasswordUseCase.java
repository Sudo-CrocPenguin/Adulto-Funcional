package org.adultofuncional.main.security.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Eliminar una contraseña del gestor.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que elimina permanentemente una entrada del gestor de contraseñas.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Permite al usuario borrar credenciales que ya no necesita.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Verifica que la entrada pertenezca a la cuenta y la elimina.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class DeletePasswordUseCase {

    private final PasswordRepository passwordRepository;

    @Transactional
    public void execute(UUID accountId, UUID passwordId) {
        if (!passwordRepository.existsByIdAndAccountId(passwordId, accountId)) {
            throw new NotFoundException("Contraseña no encontrada con id: " + passwordId);
        }
        passwordRepository.deleteById(passwordId);
    }
}
