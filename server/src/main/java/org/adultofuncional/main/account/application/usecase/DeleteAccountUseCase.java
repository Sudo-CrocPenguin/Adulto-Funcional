package org.adultofuncional.main.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.application.dto.DeleteAccountRequest;
import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Eliminar una cuenta y todos sus datos asociados en cascada.
 *
 * <p>
 * Bloquea la cuenta, comprueba de nuevo la contraseña principal y elimina la
 * raíz mediante una sentencia directa. Las claves foráneas con
 * {@code ON DELETE CASCADE} retiran los datos dependientes sin cargar grandes
 * colecciones en memoria. Finalmente borra cualquier Master Key temporal.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see AccountRepository
 */
@Service
@RequiredArgsConstructor
public class DeleteAccountUseCase {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final MasterKeySessionService masterKeySessionService;

  /**
   * Ejecuta la eliminación de una cuenta por su identificador.
   *
   * @param accountId Identificador único de la cuenta. No puede ser {@code null}.
   * @param request contraseña principal actual para confirmar la operación
   * @throws NotFoundException si no existe ninguna cuenta con el ID
   *                           proporcionado.
   */
  @Transactional
  public void execute(UUID accountId, DeleteAccountRequest request) {
    Account account = accountRepository.findByIdForUpdate(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));
    if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
      throw new ForbiddenException(
          "La contraseña actual es incorrecta",
          ApiErrorCode.REAUTHENTICATION_FAILED);
    }
    accountRepository.deleteById(accountId);
    masterKeySessionService.clearAll(accountId);
  }
}
