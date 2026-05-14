package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Eliminar una credencial almacenada.
 *
 * <p>
 * Elimina una credencial verificando que pertenezca a la cuenta y que la
 * Master Key esté verificada.
 *
 * <p>
 * <strong>Reglas de negocio:</strong>
 * <ul>
 * <li>La cuenta debe existir.</li>
 * <li>La credencial debe pertenecer a la cuenta.</li>
 * <li>La Master Key debe estar verificada.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see PasswordRepository
 * @see MasterKeySessionService
 */
@Service
@RequiredArgsConstructor
public class DeletePasswordUseCase {

  private final PasswordRepository passwordRepository;
  private final AccountRepository accountRepository;
  private final MasterKeySessionService masterKeyService;

  /**
   * Ejecuta la eliminación de una credencial.
   *
   * @param accountId  Identificador de la cuenta propietaria.
   * @param passwordId Identificador de la credencial a eliminar.
   * @throws NotFoundException  si la cuenta o la credencial no existen.
   * @throws ForbiddenException si la Master Key no está verificada.
   */
  @Transactional
  public void execute(UUID accountId, UUID passwordId) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (!masterKeyService.isVerified(accountId)) {
      throw new ForbiddenException("Master Key no verificada");
    }

    if (!passwordRepository.existsByIdAndAccountId(passwordId, accountId)) {
      throw new NotFoundException("Contraseña no encontrada con id: " + passwordId);
    }

    passwordRepository.deleteById(passwordId);
  }
}
