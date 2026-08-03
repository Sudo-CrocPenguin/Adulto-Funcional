package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.ConfigureMasterKeyRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ConflictException;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** Configura la primera Master Key después de reautenticar la cuenta. */
@Service
@RequiredArgsConstructor
public class ConfigureMasterKeyUseCase {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final MasterKeySessionService sessionService;

  @Transactional
  public MasterKeyStatusResponse execute(UUID accountId, ConfigureMasterKeyRequest request) {
    Account account = accountRepository.findByIdForUpdate(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
    if (account.getMasterKeyHash() != null) {
      throw new ConflictException("La cuenta ya tiene una Master Key configurada");
    }
    if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
      throw new ForbiddenException(
          "La contraseña actual es incorrecta",
          ApiErrorCode.REAUTHENTICATION_FAILED);
    }

    account.updateMasterKeyHash(passwordEncoder.encode(request.newMasterKey()));
    accountRepository.save(account);
    sessionService.clearAll(accountId);
    return MasterKeyStatusResponse.locked(true);
  }
}
