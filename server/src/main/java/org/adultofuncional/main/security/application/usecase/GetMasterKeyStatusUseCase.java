package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** Consulta el estado canónico sin revelar ni recalcular la Master Key. */
@Service
@RequiredArgsConstructor
public class GetMasterKeyStatusUseCase {

  private final AccountRepository accountRepository;
  private final MasterKeySessionService sessionService;

  @Transactional(readOnly = true)
  public MasterKeyStatusResponse execute(UUID accountId, UUID sessionId) {
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
    boolean configured = account.getMasterKeyHash() != null;
    return sessionService.find(accountId, sessionId)
        .map(unlocked -> new MasterKeyStatusResponse(
            configured,
            true,
            unlocked.expiresAt()))
        .orElseGet(() -> MasterKeyStatusResponse.locked(configured));
  }
}
