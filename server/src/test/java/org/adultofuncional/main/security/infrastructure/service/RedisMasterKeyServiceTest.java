package org.adultofuncional.main.security.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.adultofuncional.main.config.security.JwtProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisMasterKeyServiceTest {

  @Test
  void storesEncryptedPayloadAndDecryptsMasterKeyOnRead() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    JwtProperties jwtProperties = new JwtProperties();
    jwtProperties.setSecret("test-jwt-secret-with-at-least-32-characters");

    RedisMasterKeyService service = new RedisMasterKeyService(redisTemplate, jwtProperties);
    UUID accountId = UUID.randomUUID();
    String redisKey = "master-key:" + accountId;
    String masterKey = "mi-master-key-real";

    service.verify(accountId, masterKey);

    ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
    verify(valueOperations).set(eq(redisKey), payloadCaptor.capture(), eq(3_600L), eq(TimeUnit.SECONDS));

    String encryptedPayload = payloadCaptor.getValue();
    assertThat(encryptedPayload).contains(":");
    assertThat(encryptedPayload).doesNotContain(masterKey);

    when(valueOperations.get(redisKey)).thenReturn(encryptedPayload);

    assertThat(service.getMasterKey(accountId)).isEqualTo(masterKey);
  }
}
