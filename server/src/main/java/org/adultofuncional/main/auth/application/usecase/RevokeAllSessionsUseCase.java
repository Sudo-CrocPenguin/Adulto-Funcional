package org.adultofuncional.main.auth.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.auth.application.service.AuthenticationSessionService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** Revoca todas las familias activas pertenecientes a una cuenta. */
@Service
@RequiredArgsConstructor
public class RevokeAllSessionsUseCase {

  private final AuthenticationSessionService sessionService;

  public void execute(UUID accountId) {
    sessionService.revokeAll(accountId);
  }
}
