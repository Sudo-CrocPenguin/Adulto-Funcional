package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** Elimina únicamente el desbloqueo de la familia autenticada. */
@Service
@RequiredArgsConstructor
public class CloseMasterKeySessionUseCase {

  private final MasterKeySessionService sessionService;
  private final GetMasterKeyStatusUseCase statusUseCase;

  public MasterKeyStatusResponse execute(UUID accountId, UUID sessionId) {
    sessionService.clear(accountId, sessionId);
    return statusUseCase.execute(accountId, sessionId);
  }
}
