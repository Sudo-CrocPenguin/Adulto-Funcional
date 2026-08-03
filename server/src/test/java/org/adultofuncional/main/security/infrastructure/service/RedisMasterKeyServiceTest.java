package org.adultofuncional.main.security.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

class RedisMasterKeyServiceTest {

  @Test
  void storesEncryptedPayloadAndDecryptsMasterKeyOnRead() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    @SuppressWarnings("unchecked")
    SetOperations<String, String> setOperations = mock(SetOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(redisTemplate.opsForSet()).thenReturn(setOperations);

    RedisMasterKeyService service = new RedisMasterKeyService(
        redisTemplate,
        "test-master-key-session-secret-with-32-characters");
    UUID accountId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    String redisKey = "master-key:" + accountId + ":" + sessionId;
    String masterKey = "mi-master-key-real";

    service.unlock(accountId, sessionId, masterKey);

    ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
    verify(valueOperations).set(eq(redisKey), payloadCaptor.capture(), eq(3_600_000L), eq(TimeUnit.MILLISECONDS));

    String encryptedPayload = payloadCaptor.getValue();
    assertThat(encryptedPayload).contains(":");
    assertThat(encryptedPayload).doesNotContain(masterKey);

    when(valueOperations.get(redisKey)).thenReturn(encryptedPayload);
    when(redisTemplate.getExpire(redisKey, TimeUnit.SECONDS)).thenReturn(3_500L);

    assertThat(service.find(accountId, sessionId)).get()
        .extracting(MasterKeySessionService.UnlockedMasterKey::value)
        .isEqualTo(masterKey);
  }

  @Test
  void rejectsShortSessionSecret() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);

    assertThatThrownBy(() -> new RedisMasterKeyService(redisTemplate, "secreto-corto"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MASTER_KEY_SESSION_SECRET");
  }
}
