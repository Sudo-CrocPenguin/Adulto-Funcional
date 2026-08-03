package org.adultofuncional.main.auth.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.auth.infrastructure.persistence.entity.AuthSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

/** Repositorio Spring Data con bloqueo pesimista para rotar refresh tokens. */
public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionEntity, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<AuthSessionEntity> findByCurrentRefreshHash(String currentRefreshHash);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<AuthSessionEntity> findByPreviousRefreshHash(String previousRefreshHash);

  List<AuthSessionEntity> findByAccountIdAndRevokedAtIsNull(UUID accountId);
}
