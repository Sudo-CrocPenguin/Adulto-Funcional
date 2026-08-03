package org.adultofuncional.main.auth.infrastructure.persistence.mapper;

import org.adultofuncional.main.auth.domain.model.AuthSession;
import org.adultofuncional.main.auth.infrastructure.persistence.entity.AuthSessionEntity;
import org.springframework.stereotype.Component;

/** Traduce familias de autenticación entre dominio y persistencia. */
@Component
public class AuthSessionMapper {

  public AuthSession toDomain(AuthSessionEntity entity) {
    return AuthSession.reconstitute(
        entity.getSessionId(),
        entity.getAccountId(),
        entity.getCurrentRefreshHash(),
        entity.getPreviousRefreshHash(),
        entity.getPreviousRotatedAt(),
        entity.getRefreshExpiresAt(),
        entity.getAccessTokenId(),
        entity.getAccessExpiresAt(),
        entity.getCreatedAt(),
        entity.getLastSeenAt(),
        entity.getRevokedAt(),
        entity.getVersion());
  }

  public AuthSessionEntity toEntity(AuthSession session) {
    AuthSessionEntity entity = new AuthSessionEntity();
    copyToEntity(session, entity);
    return entity;
  }

  public void copyToEntity(AuthSession session, AuthSessionEntity entity) {
    entity.setSessionId(session.getId());
    entity.setAccountId(session.getAccountId());
    entity.setCurrentRefreshHash(session.getCurrentRefreshHash());
    entity.setPreviousRefreshHash(session.getPreviousRefreshHash());
    entity.setPreviousRotatedAt(session.getPreviousRotatedAt());
    entity.setRefreshExpiresAt(session.getRefreshExpiresAt());
    entity.setAccessTokenId(session.getAccessTokenId());
    entity.setAccessExpiresAt(session.getAccessExpiresAt());
    entity.setCreatedAt(session.getCreatedAt());
    entity.setLastSeenAt(session.getLastSeenAt());
    entity.setRevokedAt(session.getRevokedAt());
  }
}
