package org.adultofuncional.main.auth.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.auth.application.service.AuthenticationSessionService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** Revoca únicamente la familia autenticada por el access token actual. */
@Service
@RequiredArgsConstructor
public class RevokeCurrentSessionUseCase {

  private final AuthenticationSessionService sessionService;

  public void execute(UUID accountId, UUID sessionId) {
    sessionService.revokeCurrent(accountId, sessionId);
  }
}
