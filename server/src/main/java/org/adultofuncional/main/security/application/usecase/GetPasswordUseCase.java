package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.domain.service.EncryptionService;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Obtener una credencial por su identificador.
 *
 * <p>
 * Recupera una credencial específica verificando que pertenezca a la cuenta
 * y que la Master Key esté verificada. Incluye la contraseña descifrada en la
 * respuesta usando {@link EncryptionService#decrypt}.
 *
 * <p>
 * <strong>Reglas de negocio:</strong>
 * <ul>
 * <li>La cuenta debe existir.</li>
 * <li>La credencial debe pertenecer a la cuenta.</li>
 * <li>La Master Key debe estar verificada.</li>
 * <li>La contraseña se descifra usando la Master Key en sesión.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see PasswordRepository
 * @see EncryptionService
 * @see MasterKeySessionService
 * @see PasswordResponse
 */
@Service
@RequiredArgsConstructor
public class GetPasswordUseCase {

  private final PasswordRepository passwordRepository;
  private final AccountRepository accountRepository;
  private final MasterKeySessionService masterKeyService;
  private final EncryptionService encryptionService;

  /**
   * Ejecuta la consulta de una credencial con su contraseña descifrada.
   *
   * @param accountId  Identificador de la cuenta propietaria.
   * @param passwordId Identificador de la credencial.
   * @return {@link PasswordResponse} con los datos y la contraseña en texto
   *         plano.
   * @throws NotFoundException  si la cuenta o la credencial no existen.
   * @throws ForbiddenException si la Master Key no está verificada.
   */
  @Transactional(readOnly = true)
  public PasswordResponse execute(UUID accountId, UUID passwordId) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (!masterKeyService.isVerified(accountId)) {
      throw new ForbiddenException("Master Key no verificada");
    }

    Password password = passwordRepository.findByIdAndAccountId(passwordId, accountId)
        .orElseThrow(() -> new NotFoundException("Contraseña no encontrada con id: " + passwordId));

    String masterKey = masterKeyService.getMasterKey(accountId);
    String plainPassword = encryptionService.decrypt(
        password.getSalt(),
        password.getIv(),
        password.getCiphertext(),
        masterKey);

    return PasswordResponse.builder()
        .id(password.getId())
        .applicationName(password.getApplicationName())
        .password(plainPassword)
        .lastChangeDate(password.getLastChangeDate())
        .build();
  }
}
