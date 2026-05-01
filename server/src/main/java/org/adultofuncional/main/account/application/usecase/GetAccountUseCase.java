package org.adultofuncional.main.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Obtener una cuenta por su identificador.
 *
 * <p>
 * Sirve como intermediario entre el controlador REST y el repositorio,
 * verificando que la cuenta exista antes de retornar sus datos.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class GetAccountUseCase {

  private final AccountRepository accountRepository;
  private final AccountMapper accountMapper;

  /**
   * Ejecuta la consulta de una cuenta por su ID.
   *
   * @param accountId Identificador único de la cuenta. No puede ser {@code null}.
   * @return {@link AccountResponse} con los datos no sensibles de la cuenta.
   * @throws NotFoundException si no existe ninguna cuenta con el ID
   *                           proporcionado.
   */
  @Transactional(readOnly = true)
  public AccountResponse execute(UUID accountId) {
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    return accountMapper.toResponse(account);
  }
}
