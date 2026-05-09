package org.adultofuncional.main.security.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.PasswordRequest;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.domain.service.EncryptionService;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Guardar una nueva credencial en el gestor de contraseñas.
 *
 * <p>
 * Registra una nueva credencial para una cuenta, cifrando la contraseña en
 * texto plano con AES‑256 antes de persistir. Verifica que la cuenta exista,
 * que la Master Key esté verificada en la sesión y que no haya duplicados
 * del nombre de aplicación para esa cuenta.
 *
 * <p>
 * <strong>Reglas de negocio:</strong>
 * <ul>
 * <li>La cuenta debe existir.</li>
 * <li>La Master Key debe estar verificada en la sesión actual.</li>
 * <li>El nombre de aplicación debe ser único por cuenta.</li>
 * <li>La contraseña se cifra con AES‑256 usando la Master Key y un salt
 * único.</li>
 * <li>La fecha de último cambio se asigna a hoy si no se especifica.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see Password
 * @see PasswordRepository
 * @see EncryptionService
 * @see MasterKeySessionService
 * @see PasswordRequest
 * @see PasswordResponse
 */
@Service
@RequiredArgsConstructor
public class CreatePasswordUseCase {

  private final PasswordRepository passwordRepository;
  private final AccountRepository accountRepository;
  private final EncryptionService encryptionService;
  private final MasterKeySessionService masterKeySessionService;

  /**
   * Ejecuta la creación de una nueva credencial.
   *
   * @param accountId Identificador de la cuenta propietaria.
   * @param request   DTO con el nombre de la aplicación, la contraseña en
   *                  texto plano y la fecha de último cambio (opcional).
   * @return {@link PasswordResponse} con los datos no sensibles de la
   *         credencial creada.
   * @throws NotFoundException  si la cuenta no existe.
   * @throws ForbiddenException si la Master Key no ha sido verificada.
   * @throws BusinessException  si ya existe una credencial con el mismo
   *                            nombre de aplicación.
   */
  @Transactional
  public PasswordResponse execute(UUID accountId, PasswordRequest request) {
    // 1. Verificar cuenta
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    // 2. Verificar Master Key en sesión
    if (!masterKeySessionService.isVerified(accountId)) {
      throw new ForbiddenException("Master Key no verificada");
    }

    // 3. Verificar unicidad del nombre de aplicación por cuenta
    if (passwordRepository.existsByAccountIdAndApplicationName(accountId, request.getApplicationName())) {
      throw new BusinessException(
          "Ya existe una contraseña para la aplicación: " + request.getApplicationName());
    }

    // 4. Cifrar contraseña con AES‑256
    String masterKey = masterKeySessionService.getMasterKey(accountId);
    EncryptionService.EncryptedData encryptedData = encryptionService.encrypt(
        request.getPassword(), masterKey);

    // 5. Fecha de último cambio
    LocalDate lastChangeDate = request.getLastChangeDate() != null
        ? request.getLastChangeDate()
        : LocalDate.now();

    // 6. Crear modelo de dominio
    Password password = Password.create(
        request.getApplicationName(),
        encryptedData.salt(),
        encryptedData.iv(),
        encryptedData.ciphertext(),
        lastChangeDate,
        accountId);

    Password saved = passwordRepository.save(password);

    // 7. Retornar DTO sin exponer material criptográfico
    return PasswordResponse.builder()
        .id(saved.getId())
        .applicationName(saved.getApplicationName())
        .lastChangeDate(saved.getLastChangeDate())
        .build();
  }
}
