package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.application.dto.VerifyMasterKeyRequest;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ConflictException;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.ratelimit.RateLimitGuard;
import org.adultofuncional.main.shared.ratelimit.RateLimitPolicy;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** Verifica y desbloquea exclusivamente la sesión autenticada. */
@Service
@RequiredArgsConstructor
public class VerifyMasterKeyUseCase {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final MasterKeySessionService sessionService;
  private final RateLimitGuard rateLimitGuard;

  @Transactional(readOnly = true)
  public MasterKeyStatusResponse execute(
      UUID accountId,
      UUID sessionId,
      VerifyMasterKeyRequest request) {
    String subject = accountId + ":" + sessionId;
    rateLimitGuard.check(RateLimitPolicy.MASTER_KEY_SESSION, subject);
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
    if (account.getMasterKeyHash() == null) {
      throw new ConflictException(
          "La cuenta no tiene una Master Key configurada",
          ApiErrorCode.MASTER_KEY_NOT_CONFIGURED);
    }
    if (!passwordEncoder.matches(request.masterKey(), account.getMasterKeyHash())) {
      rateLimitGuard.recordFailure(RateLimitPolicy.MASTER_KEY_SESSION, subject);
      rateLimitGuard.check(RateLimitPolicy.MASTER_KEY_SESSION, subject);
      throw new ForbiddenException("Master Key incorrecta", ApiErrorCode.MASTER_KEY_INVALID);
    }

    rateLimitGuard.reset(RateLimitPolicy.MASTER_KEY_SESSION, subject);
    sessionService.unlock(accountId, sessionId, request.masterKey());
    return sessionService.find(accountId, sessionId)
        .map(unlocked -> new MasterKeyStatusResponse(true, true, unlocked.expiresAt()))
        .orElseThrow(() -> new IllegalStateException(
            "La sesión de Master Key no pudo conservarse"));
  }
}
