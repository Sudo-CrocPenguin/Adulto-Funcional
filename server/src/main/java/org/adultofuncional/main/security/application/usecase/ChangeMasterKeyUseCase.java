package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.ChangeMasterKeyRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.domain.service.EncryptionService;
import org.adultofuncional.main.security.domain.service.EncryptionService.EncryptionContext;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ConflictException;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** Rota la Master Key y recifra toda la bóveda en una única transacción. */
@Service
@RequiredArgsConstructor
public class ChangeMasterKeyUseCase {

  private final AccountRepository accountRepository;
  private final PasswordRepository passwordRepository;
  private final PasswordEncoder passwordEncoder;
  private final EncryptionService encryptionService;
  private final MasterKeySessionService sessionService;

  @Transactional
  public MasterKeyStatusResponse execute(UUID accountId, ChangeMasterKeyRequest request) {
    Account account = accountRepository.findByIdForUpdate(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
    if (account.getMasterKeyHash() == null) {
      throw new ConflictException(
          "La cuenta no tiene una Master Key configurada",
          ApiErrorCode.MASTER_KEY_NOT_CONFIGURED);
    }
    if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
      throw new ForbiddenException(
          "La contraseña actual es incorrecta",
          ApiErrorCode.REAUTHENTICATION_FAILED);
    }
    if (!passwordEncoder.matches(request.currentMasterKey(), account.getMasterKeyHash())) {
      throw new ForbiddenException("Master Key incorrecta", ApiErrorCode.MASTER_KEY_INVALID);
    }
    if (passwordEncoder.matches(request.newMasterKey(), account.getMasterKeyHash())) {
      throw new ConflictException("La nueva Master Key debe ser diferente de la actual");
    }

    for (Password password : passwordRepository.findAllByAccountId(accountId)) {
      EncryptionContext context = new EncryptionContext(accountId, password.getId());
      String plainSecret = encryptionService.decrypt(
          password.getSalt(),
          password.getIv(),
          password.getCiphertext(),
          request.currentMasterKey(),
          password.getCryptoVersion(),
          context);
      EncryptionService.EncryptedData encrypted = encryptionService.encrypt(
          plainSecret,
          request.newMasterKey(),
          context);
      password.update(
          password.getApplicationName(),
          encrypted.salt(),
          encrypted.cryptoVersion(),
          encrypted.iv(),
          encrypted.ciphertext(),
          password.getLastChangeDate());
      passwordRepository.save(password);
    }

    account.updateMasterKeyHash(passwordEncoder.encode(request.newMasterKey()));
    accountRepository.save(account);
    sessionService.clearAll(accountId);
    return MasterKeyStatusResponse.locked(true);
  }
}
