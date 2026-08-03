package org.adultofuncional.main.auth.application.usecase;

import java.time.Clock;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.SessionTokens;
import org.adultofuncional.main.auth.application.service.AuthenticationSessionService;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** Caso de uso canónico para rotar una sesión y construir su respuesta. */
@Service
@RequiredArgsConstructor
public class RefreshSessionUseCase {

  private final AuthenticationSessionService sessionService;
  private final AccountRepository accountRepository;
  private final Clock clock;

  public AuthResponse execute(String refreshToken) {
    SessionTokens tokens = sessionService.refresh(refreshToken);
    Account account = accountRepository.findById(tokens.accountId())
        .orElseThrow(() -> new UnauthorizedException(
            "Refresh token inválido",
            ApiErrorCode.REFRESH_TOKEN_INVALID));
    return AuthResponse.from(account, tokens, clock.instant());
  }
}
