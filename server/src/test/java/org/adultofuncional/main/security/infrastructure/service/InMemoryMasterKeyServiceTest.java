package org.adultofuncional.main.security.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class InMemoryMasterKeyServiceTest {

  @Test
  void isolatesUnlocksByAccountAndSession() {
    Clock clock = Clock.fixed(Instant.parse("2026-08-03T15:00:00Z"), ZoneOffset.UTC);
    InMemoryMasterKeyService service = new InMemoryMasterKeyService(clock, 3_600_000L);
    UUID accountId = UUID.randomUUID();
    UUID firstSession = UUID.randomUUID();
    UUID secondSession = UUID.randomUUID();

    service.unlock(accountId, firstSession, "clave-única-por-sesión");

    assertThat(service.find(accountId, firstSession)).get()
        .extracting(unlocked -> unlocked.value())
        .isEqualTo("clave-única-por-sesión");
    assertThat(service.find(accountId, secondSession)).isEmpty();
  }

  @Test
  void treatsExpiredEntryAsLockedInsteadOfThrowing() {
    Clock clock = Clock.fixed(Instant.parse("2026-08-03T15:00:00Z"), ZoneOffset.UTC);
    InMemoryMasterKeyService service = new InMemoryMasterKeyService(clock, 0L);
    UUID accountId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();

    service.unlock(accountId, sessionId, "clave-efímera");

    assertThat(service.find(accountId, sessionId)).isEmpty();
  }
}
