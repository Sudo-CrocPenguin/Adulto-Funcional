package org.adultofuncional.main.auth.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad JPA que materializa una familia de refresh tokens revocable. */
@Entity
@Table(name = "auth_sessions")
@Getter
@Setter
@NoArgsConstructor
public class AuthSessionEntity {

  @Id
  @Column(name = "auth_session_id", columnDefinition = "CHAR(36)")
  private UUID sessionId;

  @Column(name = "auth_session_fk_account_id", columnDefinition = "CHAR(36)", nullable = false)
  private UUID accountId;

  @Column(
      name = "auth_session_current_refresh_hash",
      columnDefinition = "CHAR(64)",
      nullable = false,
      unique = true)
  private String currentRefreshHash;

  @Column(name = "auth_session_previous_refresh_hash", columnDefinition = "CHAR(64)")
  private String previousRefreshHash;

  @Column(name = "auth_session_previous_rotated_at")
  private Instant previousRotatedAt;

  @Column(name = "auth_session_refresh_expires_at", nullable = false)
  private Instant refreshExpiresAt;

  @Column(name = "auth_session_access_jti", columnDefinition = "CHAR(36)", nullable = false)
  private UUID accessTokenId;

  @Column(name = "auth_session_access_expires_at", nullable = false)
  private Instant accessExpiresAt;

  @Column(name = "auth_session_created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "auth_session_last_seen_at", nullable = false)
  private Instant lastSeenAt;

  @Column(name = "auth_session_revoked_at")
  private Instant revokedAt;

  @Version
  @Column(name = "auth_session_version", nullable = false)
  private long version;
}
