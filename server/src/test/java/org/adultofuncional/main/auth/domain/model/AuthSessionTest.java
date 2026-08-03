package org.adultofuncional.main.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AuthSessionTest {

  private static final Instant NOW = Instant.parse("2026-08-03T15:00:00Z");

  @Test
  void createsAnActiveSessionWithUuidV7() {
    AuthSession session = AuthSession.create(
        UUID.randomUUID(),
        "a".repeat(64),
        NOW.plusSeconds(2_592_000),
        UUID.randomUUID(),
        NOW.plusSeconds(900),
        NOW);

    assertThat(session.isActive(NOW)).isTrue();
    assertThat(session.getId().version()).isEqualTo(7);
    assertThat(session.getPreviousRefreshHash()).isNull();
    assertThat(session.getRevokedAt()).isNull();
  }

  @Test
  void rotatesTokensAndRecognizesOnlyTheSafeConcurrencyWindow() {
    AuthSession session = AuthSession.create(
        UUID.randomUUID(),
        "a".repeat(64),
        NOW.plusSeconds(2_592_000),
        UUID.randomUUID(),
        NOW.plusSeconds(900),
        NOW);

    UUID nextJti = UUID.randomUUID();
    session.rotate(
        "b".repeat(64),
        NOW.plusSeconds(2_592_100),
        nextJti,
        NOW.plusSeconds(1_000),
        NOW.plusSeconds(100));

    assertThat(session.getPreviousRefreshHash()).isEqualTo("a".repeat(64));
    assertThat(session.getCurrentRefreshHash()).isEqualTo("b".repeat(64));
    assertThat(session.getAccessTokenId()).isEqualTo(nextJti);
    assertThat(session.isPreviousRefreshWithin("a".repeat(64), NOW.plusSeconds(105), 5)).isTrue();
    assertThat(session.isPreviousRefreshWithin("a".repeat(64), NOW.plusSeconds(106), 5)).isFalse();
  }

  @Test
  void preventsRotationAfterRevocation() {
    AuthSession session = AuthSession.create(
        UUID.randomUUID(),
        "a".repeat(64),
        NOW.plusSeconds(2_592_000),
        UUID.randomUUID(),
        NOW.plusSeconds(900),
        NOW);
    session.revoke(NOW.plusSeconds(10));

    assertThat(session.isActive(NOW.plusSeconds(11))).isFalse();
    assertThatThrownBy(() -> session.rotate(
        "b".repeat(64),
        NOW.plusSeconds(2_592_100),
        UUID.randomUUID(),
        NOW.plusSeconds(1_000),
        NOW.plusSeconds(11)))
        .isInstanceOf(IllegalStateException.class);
  }
}
