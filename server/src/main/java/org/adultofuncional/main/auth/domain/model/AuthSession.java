package org.adultofuncional.main.auth.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Familia de autenticación revocable asociada a un único inicio de sesión.
 *
 * <p>Una sesión conserva exclusivamente hashes de refresh tokens. El valor en
 * texto plano se entrega una vez al cliente y nunca se persiste. También
 * registra el último access token emitido para poder revocarlo al cerrar,
 * refrescar o invalidar la sesión.</p>
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class AuthSession {

  @EqualsAndHashCode.Include
  final UUID id;
  final UUID accountId;
  String currentRefreshHash;
  String previousRefreshHash;
  Instant previousRotatedAt;
  Instant refreshExpiresAt;
  UUID accessTokenId;
  Instant accessExpiresAt;
  final Instant createdAt;
  Instant lastSeenAt;
  Instant revokedAt;
  long version;

  /** Crea una familia activa con su primer par de tokens. */
  public static AuthSession create(
      UUID accountId,
      String refreshHash,
      Instant refreshExpiresAt,
      UUID accessTokenId,
      Instant accessExpiresAt,
      Instant now) {
    Objects.requireNonNull(accountId, "accountId es obligatorio");
    Objects.requireNonNull(refreshHash, "refreshHash es obligatorio");
    Objects.requireNonNull(refreshExpiresAt, "refreshExpiresAt es obligatorio");
    Objects.requireNonNull(accessTokenId, "accessTokenId es obligatorio");
    Objects.requireNonNull(accessExpiresAt, "accessExpiresAt es obligatorio");
    Objects.requireNonNull(now, "now es obligatorio");
    if (!refreshExpiresAt.isAfter(now) || !accessExpiresAt.isAfter(now)) {
      throw new IllegalArgumentException("Los tokens de una sesión deben expirar en el futuro");
    }

    return new AuthSession(
        Generators.timeBasedEpochGenerator().generate(),
        accountId,
        refreshHash,
        null,
        null,
        refreshExpiresAt,
        accessTokenId,
        accessExpiresAt,
        now,
        now,
        null,
        0L);
  }

  /** Reconstituye una familia persistida sin volver a ejecutar la fábrica. */
  public static AuthSession reconstitute(
      UUID id,
      UUID accountId,
      String currentRefreshHash,
      String previousRefreshHash,
      Instant previousRotatedAt,
      Instant refreshExpiresAt,
      UUID accessTokenId,
      Instant accessExpiresAt,
      Instant createdAt,
      Instant lastSeenAt,
      Instant revokedAt,
      long version) {
    return new AuthSession(
        Objects.requireNonNull(id),
        Objects.requireNonNull(accountId),
        Objects.requireNonNull(currentRefreshHash),
        previousRefreshHash,
        previousRotatedAt,
        Objects.requireNonNull(refreshExpiresAt),
        Objects.requireNonNull(accessTokenId),
        Objects.requireNonNull(accessExpiresAt),
        Objects.requireNonNull(createdAt),
        Objects.requireNonNull(lastSeenAt),
        revokedAt,
        version);
  }

  /**
   * Rota el refresh token y el access token de la familia de forma atómica.
   */
  public void rotate(
      String nextRefreshHash,
      Instant nextRefreshExpiresAt,
      UUID nextAccessTokenId,
      Instant nextAccessExpiresAt,
      Instant now) {
    requireActive(now);
    previousRefreshHash = currentRefreshHash;
    previousRotatedAt = now;
    currentRefreshHash = Objects.requireNonNull(nextRefreshHash);
    refreshExpiresAt = Objects.requireNonNull(nextRefreshExpiresAt);
    accessTokenId = Objects.requireNonNull(nextAccessTokenId);
    accessExpiresAt = Objects.requireNonNull(nextAccessExpiresAt);
    lastSeenAt = Objects.requireNonNull(now);
  }

  /** Marca la familia como revocada. La operación es idempotente. */
  public void revoke(Instant now) {
    if (revokedAt == null) {
      revokedAt = Objects.requireNonNull(now);
      lastSeenAt = now;
    }
  }

  /** Indica si la sesión sigue siendo utilizable en el instante indicado. */
  public boolean isActive(Instant now) {
    return revokedAt == null && refreshExpiresAt.isAfter(now);
  }

  /** Detecta el refresh inmediatamente anterior dentro de la ventana segura. */
  public boolean isPreviousRefreshWithin(String refreshHash, Instant now, long windowSeconds) {
    return previousRefreshHash != null
        && previousRefreshHash.equals(refreshHash)
        && previousRotatedAt != null
        && !now.isAfter(previousRotatedAt.plusSeconds(windowSeconds));
  }

  private void requireActive(Instant now) {
    if (!isActive(now)) {
      throw new IllegalStateException("La sesión está revocada o expirada");
    }
  }
}
