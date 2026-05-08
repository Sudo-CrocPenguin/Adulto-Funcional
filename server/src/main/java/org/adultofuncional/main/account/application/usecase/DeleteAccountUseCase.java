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
 * Verifica que la cuenta exista y la elimina a través del puerto
 * {@link AccountRepository}. La eliminación en cascada de movimientos,
 * gastos fijos, eventos y contraseñas asociados es gestionada automáticamente
 * por la configuración JPA ({@code CascadeType.ALL} y
 * {@code orphanRemoval = true}) en
 * {@link org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity},
 * por lo que este caso de uso no itera manualmente sobre las entidades
 * dependientes.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see AccountRepository
 */
@Service
@RequiredArgsConstructor
public class DeleteAccountUseCase {

  private final AccountRepository accountRepository;

  /**
   * Ejecuta la eliminación de una cuenta por su identificador.
   *
   * @param accountId Identificador único de la cuenta. No puede ser {@code null}.
   * @throws NotFoundException si no existe ninguna cuenta con el ID
   *                           proporcionado.
   */
  @Transactional
  public void execute(UUID accountId) {
    if (!accountRepository.existsById(accountId)) {
      throw new NotFoundException("Cuenta no encontrada con id: " + accountId);
    }
    accountRepository.deleteById(accountId);
  }
}
