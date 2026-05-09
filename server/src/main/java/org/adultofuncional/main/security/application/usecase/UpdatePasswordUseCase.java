package org.adultofuncional.main.security.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.application.dto.PasswordUpdateRequest;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.domain.service.EncryptionService;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Actualizar una credencial existente (PATCH).
 *
 * <p>
 * Permite modificar parcialmente el nombre de la aplicación, la contraseña
 * o la fecha de último cambio. Si se cambia la contraseña, se genera nuevo
 * material criptográfico usando {@link EncryptionService#encrypt}. La Master
 * Key debe estar verificada.
 *
 * <p>
 * <strong>Reglas de negocio:</strong>
 * <ul>
 * <li>La credencial debe existir y pertenecer a la cuenta.</li>
 * <li>Si se cambia el nombre, debe ser único por cuenta.</li>
 * <li>La Master Key debe estar verificada.</li>
 * <li>La nueva contraseña se cifra antes de persistir.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see PasswordRepository
 * @see EncryptionService
 * @see MasterKeySessionService
 * @see PasswordUpdateRequest
 * @see PasswordResponse
 */
@Service
@RequiredArgsConstructor
public class UpdatePasswordUseCase {

  private final PasswordRepository passwordRepository;
  private final AccountRepository accountRepository;
  private final EncryptionService encryptionService;
  private final MasterKeySessionService masterKeyService;

  /**
   * Ejecuta la actualización parcial de una credencial.
   *
   * @param accountId  Identificador de la cuenta propietaria.
   * @param passwordId Identificador de la credencial a modificar.
   * @param request    DTO con los campos a actualizar (nulos o vacíos se
   *                   ignoran).
   * @return {@link PasswordResponse} con los datos actualizados.
   * @throws NotFoundException  si la cuenta o la credencial no existen.
   * @throws ForbiddenException si la Master Key no está verificada.
   * @throws BusinessException  si el nuevo nombre de aplicación ya existe.
   */
  @Transactional
  public PasswordResponse execute(UUID accountId, UUID passwordId, PasswordUpdateRequest request) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (!masterKeyService.isVerified(accountId)) {
      throw new ForbiddenException("Master Key no verificada");
    }

    Password password = passwordRepository.findByIdAndAccountId(passwordId, accountId)
        .orElseThrow(() -> new NotFoundException("Contraseña no encontrada con id: " + passwordId));

    // Verificar unicidad del nuevo nombre
    if (StringUtils.hasText(request.getApplicationName()) &&
        !request.getApplicationName().equals(password.getApplicationName())) {
      if (passwordRepository.existsByAccountIdAndApplicationName(accountId, request.getApplicationName())) {
        throw new BusinessException(
            "Ya existe una contraseña para la aplicación: " + request.getApplicationName());
      }
    }

    // Preparar valores actualizados
    String newApplicationName = StringUtils.hasText(request.getApplicationName()) ? request.getApplicationName()
        : password.getApplicationName();

    String newSalt = password.getSalt();
    byte[] newIv = password.getIv();
    byte[] newCiphertext = password.getCiphertext();

    // Si se envía nueva contraseña, cifrarla
    if (StringUtils.hasText(request.getPassword())) {
      String masterKey = masterKeyService.getMasterKey(accountId);
      EncryptionService.EncryptedData data = encryptionService.encrypt(request.getPassword(), masterKey);
      newSalt = data.salt();
      newIv = data.iv();
      newCiphertext = data.ciphertext();
    }

    LocalDate newLastChangeDate = request.getLastChangeDate() != null ? request.getLastChangeDate()
        : (request.getPassword() != null ? LocalDate.now() : password.getLastChangeDate());

    password.update(newApplicationName, newSalt, newIv, newCiphertext, newLastChangeDate);

    Password saved = passwordRepository.save(password);

    return PasswordResponse.builder()
        .id(saved.getId())
        .applicationName(saved.getApplicationName())
        .lastChangeDate(saved.getLastChangeDate())
        .build();
  }
}
