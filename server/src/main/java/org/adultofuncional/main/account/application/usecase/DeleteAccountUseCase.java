package org.adultofuncional.main.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Eliminar una cuenta y todos sus datos asociados en cascada.
 *
 * <p>
 * <strong>¿Qué es?</strong><br>
 * Servicio de aplicación que encapsula la lógica de negocio para eliminar
 * una cuenta de usuario de forma permanente. La eliminación es en cascada:
 * todos los movimientos, gastos fijos, eventos y contraseñas asociados
 * se eliminan automáticamente gracias a las configuraciones JPA
 * ({@code CascadeType.ALL} y {@code orphanRemoval = true}) en
 * {@link org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity}.
 *
 * <p>
 * <strong>¿Para qué sirve?</strong><br>
 * Sirve para borrar completamente la cuenta de un usuario y todo su historial
 * financiero, de agenda y de contraseñas. Es una operación irreversible.
 *
 * <p>
 * <strong>¿Cómo funciona?</strong><br>
 * <ol>
 *   <li>Recibe el identificador de la cuenta a eliminar.</li>
 *   <li>Verifica que la cuenta exista en el repositorio (si no, lanza {@link NotFoundException}).</li>
 *   <li>Elimina la cuenta mediante {@code accountRepository.deleteById()}. La cascada se encarga del resto.</li>
 * </ol>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.account.domain.repository.AccountRepository
 * @see org.adultofuncional.main.account.infrastructure.controller.AccountController
 */
@Service
@RequiredArgsConstructor
public class DeleteAccountUseCase {

    private final AccountRepository accountRepository;

    /**
     * Ejecuta la eliminación de una cuenta por su ID.
     *
     * @param accountId Identificador de la cuenta a eliminar. No puede ser {@code null}.
     * @throws NotFoundException si no existe ninguna cuenta con el ID proporcionado.
     */
    @Transactional
    public void execute(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new NotFoundException("Cuenta no encontrada con id: " + accountId);
        }
        accountRepository.deleteById(accountId);
    }
}
