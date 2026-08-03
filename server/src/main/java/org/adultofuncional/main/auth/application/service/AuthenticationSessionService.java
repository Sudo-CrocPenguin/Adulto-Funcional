package org.adultofuncional.main.auth.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.auth.application.dto.SessionTokens;
import org.adultofuncional.main.auth.domain.model.AccountRole;
import org.adultofuncional.main.auth.domain.model.AuthSession;
import org.adultofuncional.main.auth.domain.repository.AccountRoleRepository;
import org.adultofuncional.main.auth.domain.repository.AuthSessionRepository;
import org.adultofuncional.main.auth.domain.service.AccessTokenRevocationService;
import org.adultofuncional.main.config.security.AuthSessionProperties;
import org.adultofuncional.main.config.security.IssuedAccessToken;
import org.adultofuncional.main.config.security.JwtService;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ConflictException;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Orquesta creación, rotación y revocación de familias de autenticación.
 *
 * <p>La rotación se ejecuta dentro de una transacción. El repositorio adquiere
 * un bloqueo pesimista sobre la fila antes de comparar y reemplazar el hash,
 * evitando que dos solicitudes emitan pares diferentes con el mismo refresh.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthenticationSessionService {

  private final AuthSessionRepository sessionRepository;
  private final AccountRoleRepository roleRepository;
  private final AccountRepository accountRepository;
  private final AccessTokenRevocationService revocationService;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;
  private final AuthSessionProperties properties;
  private final Clock clock;
  private final MasterKeySessionService masterKeySessionService;

  /** Crea una nueva familia después de autenticar o registrar una cuenta. */
  @Transactional
  public SessionTokens create(Account account) {
    Instant now = clock.instant();
    String refreshToken = refreshTokenService.generate();
    Instant refreshExpiresAt = now.plusMillis(properties.getRefreshExpiration());
    UUID accessTokenId = Generators.timeBasedEpochGenerator().generate();
    Instant accessExpiresAt = now.plusMillis(jwtService.getExpiration());

    AuthSession session = AuthSession.create(
        account.getId(),
        refreshTokenService.hash(refreshToken),
        refreshExpiresAt,
        accessTokenId,
        accessExpiresAt,
        now);
    sessionRepository.save(session);

    Set<AccountRole> roles = rolesFor(account.getId());
    IssuedAccessToken accessToken = issueAccessToken(account, session, now, roles);
    return new SessionTokens(
        account.getId(),
        session.getId(),
        accessToken.value(),
        accessToken.expiresAt(),
        refreshToken,
        refreshExpiresAt,
        roles.stream().map(AccountRole::asAuthority).sorted().toList());
  }

  /** Rota un refresh token de un solo uso bajo bloqueo de fila. */
  @Transactional
  public SessionTokens refresh(String refreshToken) {
    String refreshHash = refreshTokenService.hash(refreshToken);
    if (refreshHash.isEmpty()) {
      throw invalidRefresh();
    }

    return sessionRepository.findByCurrentRefreshHashForUpdate(refreshHash)
        .map(this::rotateCurrent)
        .orElseGet(() -> handlePreviousOrUnknown(refreshHash));
  }

  /** Revoca solamente la familia indicada, si pertenece a la cuenta. */
  @Transactional
  public void revokeCurrent(UUID accountId, UUID sessionId) {
    if (sessionId == null) {
      return;
    }
    sessionRepository.findById(sessionId)
        .filter(session -> session.getAccountId().equals(accountId))
        .ifPresent(this::revoke);
    masterKeySessionService.clear(accountId, sessionId);
  }

  /** Revoca todas las familias activas de una cuenta. */
  @Transactional
  public void revokeAll(UUID accountId) {
    sessionRepository.findActiveByAccountId(accountId).forEach(this::revoke);
    masterKeySessionService.clearAll(accountId);
  }

  private SessionTokens rotateCurrent(AuthSession session) {
    Instant now = clock.instant();
    if (session.getRevokedAt() != null) {
      throw invalidRefresh();
    }
    if (!session.getRefreshExpiresAt().isAfter(now)) {
      revoke(session);
      throw new UnauthorizedException(
          "El refresh token expiró",
          ApiErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    Account account = accountRepository.findById(session.getAccountId())
        .orElseThrow(this::invalidRefresh);

    revocationService.revoke(session.getAccessTokenId(), session.getAccessExpiresAt());

    String nextRefreshToken = refreshTokenService.generate();
    Instant nextRefreshExpiresAt = now.plusMillis(properties.getRefreshExpiration());
    UUID nextAccessTokenId = Generators.timeBasedEpochGenerator().generate();
    Instant nextAccessExpiresAt = now.plusMillis(jwtService.getExpiration());
    session.rotate(
        refreshTokenService.hash(nextRefreshToken),
        nextRefreshExpiresAt,
        nextAccessTokenId,
        nextAccessExpiresAt,
        now);
    sessionRepository.save(session);

    Set<AccountRole> roles = rolesFor(account.getId());
    IssuedAccessToken accessToken = issueAccessToken(account, session, now, roles);
    return new SessionTokens(
        account.getId(),
        session.getId(),
        accessToken.value(),
        accessToken.expiresAt(),
        nextRefreshToken,
        nextRefreshExpiresAt,
        roles.stream().map(AccountRole::asAuthority).sorted().toList());
  }

  private SessionTokens handlePreviousOrUnknown(String refreshHash) {
    AuthSession previous = sessionRepository.findByPreviousRefreshHashForUpdate(refreshHash)
        .orElseThrow(this::invalidRefresh);
    Instant now = clock.instant();
    if (previous.isPreviousRefreshWithin(
        refreshHash,
        now,
        properties.getReplayWindowSeconds())) {
      throw new ConflictException(
          "El refresh token ya fue rotado",
          ApiErrorCode.REFRESH_ALREADY_ROTATED);
    }

    revoke(previous);
    throw new UnauthorizedException(
        "Se detectó reutilización del refresh token y la sesión fue revocada",
        ApiErrorCode.REFRESH_TOKEN_REUSED);
  }

  private IssuedAccessToken issueAccessToken(
      Account account,
      AuthSession session,
      Instant issuedAt,
      Set<AccountRole> roles) {
    List<SimpleGrantedAuthority> authorities = roles.stream()
        .map(AccountRole::asAuthority)
        .map(SimpleGrantedAuthority::new)
        .toList();
    return jwtService.issueToken(
        account.getId(),
        session.getId(),
        account.getEmail(),
        authorities,
        session.getAccessTokenId(),
        issuedAt,
        session.getAccessExpiresAt());
  }

  private Set<AccountRole> rolesFor(UUID accountId) {
    Set<AccountRole> roles = roleRepository.findByAccountId(accountId);
    return roles.isEmpty() ? Set.of(AccountRole.USER) : roles;
  }

  private void revoke(AuthSession session) {
    session.revoke(clock.instant());
    sessionRepository.save(session);
    revocationService.revoke(session.getAccessTokenId(), session.getAccessExpiresAt());
    masterKeySessionService.clear(session.getAccountId(), session.getId());
  }

  private UnauthorizedException invalidRefresh() {
    return new UnauthorizedException(
        "Refresh token inválido",
        ApiErrorCode.REFRESH_TOKEN_INVALID);
  }
}
