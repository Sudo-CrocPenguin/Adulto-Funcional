package org.adultofuncional.main.auth.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.auth.domain.model.AuthSession;

/** Puerto de persistencia de familias de autenticación revocables. */
public interface AuthSessionRepository {

  AuthSession save(AuthSession session);

  Optional<AuthSession> findById(UUID sessionId);

  /** Busca y bloquea la fila que posee el refresh token actual. */
  Optional<AuthSession> findByCurrentRefreshHashForUpdate(String refreshHash);

  /** Busca y bloquea la fila que ya rotó el refresh recibido. */
  Optional<AuthSession> findByPreviousRefreshHashForUpdate(String refreshHash);

  List<AuthSession> findActiveByAccountId(UUID accountId);
}
