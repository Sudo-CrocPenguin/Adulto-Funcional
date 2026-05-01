package org.adultofuncional.main.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;
import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Actualizar los datos de una cuenta existente.
 *
 * <p>
 * Reglas de negocio:
 * <ul>
 * <li>La cuenta debe existir.</li>
 * <li>El email debe ser único — si cambia, se verifica que no esté en uso.</li>
 * <li>Solo se pueden modificar: nombres, apellidos, teléfono y email.</li>
 * <li>Contraseña y master key no se modifican aquí.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class UpdateAccountUseCase {

  private final AccountRepository accountRepository;
  private final AccountMapper accountMapper;

  /**
   * Ejecuta la actualización de los datos de una cuenta.
   *
   * @param accountId Identificador de la cuenta a modificar.
   * @param request   DTO con los nuevos valores.
   * @return {@link AccountResponse} con los datos actualizados.
   * @throws NotFoundException Si no existe una cuenta con el ID proporcionado.
   * @throws BusinessException Si el nuevo email ya está registrado en otra
   *                           cuenta.
   */
  @Transactional
  public AccountResponse execute(UUID accountId, UpdateAccountRequest request) {

    // 1. Verificar que la cuenta existe
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    // 2. Validar unicidad del email solo si cambió
    if (!account.getEmail().equals(request.getEmail())) {
      accountRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
        throw new BusinessException(
            "El email " + request.getEmail() + " ya está registrado por otra cuenta");
      });
    }

    // 3. Aplicar cambios sobre el modelo de dominio
    account.updateDetails(request.getNames(), request.getLastnames(), request.getPhone());
    account.updateEmail(request.getEmail());

    // 4. Persistir y retornar
    Account updated = accountRepository.save(account);

    return accountMapper.toResponse(updated);
  }
}
