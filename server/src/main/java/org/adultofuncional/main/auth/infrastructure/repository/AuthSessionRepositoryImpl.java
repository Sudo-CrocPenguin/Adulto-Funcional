package org.adultofuncional.main.auth.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.auth.domain.model.AuthSession;
import org.adultofuncional.main.auth.domain.repository.AuthSessionRepository;
import org.adultofuncional.main.auth.infrastructure.persistence.entity.AuthSessionEntity;
import org.adultofuncional.main.auth.infrastructure.persistence.mapper.AuthSessionMapper;
import org.adultofuncional.main.auth.infrastructure.persistence.repository.AuthSessionJpaRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/** Adaptador JPA para las familias de autenticación. */
@Repository
@RequiredArgsConstructor
public class AuthSessionRepositoryImpl implements AuthSessionRepository {

  private final AuthSessionJpaRepository jpaRepository;
  private final AuthSessionMapper mapper;

  @Override
  public AuthSession save(AuthSession session) {
    AuthSessionEntity entity = jpaRepository.findById(session.getId())
        .orElseGet(() -> mapper.toEntity(session));
    mapper.copyToEntity(session, entity);
    return mapper.toDomain(jpaRepository.save(entity));
  }

  @Override
  public Optional<AuthSession> findById(UUID sessionId) {
    return jpaRepository.findById(sessionId).map(mapper::toDomain);
  }

  @Override
  public Optional<AuthSession> findByCurrentRefreshHashForUpdate(String refreshHash) {
    return jpaRepository.findByCurrentRefreshHash(refreshHash).map(mapper::toDomain);
  }

  @Override
  public Optional<AuthSession> findByPreviousRefreshHashForUpdate(String refreshHash) {
    return jpaRepository.findByPreviousRefreshHash(refreshHash).map(mapper::toDomain);
  }

  @Override
  public List<AuthSession> findActiveByAccountId(UUID accountId) {
    return jpaRepository.findByAccountIdAndRevokedAtIsNull(accountId).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
