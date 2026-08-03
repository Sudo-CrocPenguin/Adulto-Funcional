package org.adultofuncional.main.security.infrastructure.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Almacén local cifrado y con TTL para desarrollo y pruebas.
 *
 * <p>La clave de proceso se genera al arrancar y nunca se persiste. El mapa no
 * conserva texto plano y replica la misma expiración funcional que Redis.</p>
 */
@Component
@Profile({"dev", "test"})
public class InMemoryMasterKeyService implements MasterKeySessionService {

  private static final int IV_BYTES = 12;
  private static final int TAG_BITS = 128;

  private final Map<SessionKey, EncryptedEntry> store = new ConcurrentHashMap<>();
  private final SecureRandom secureRandom = new SecureRandom();
  private final SecretKey processKey;
  private final Clock clock;
  private final long ttlMillis;

  public InMemoryMasterKeyService(
      Clock clock,
      @Value("${master-key.session.ttl:3600000}") long ttlMillis) {
    this.clock = clock;
    this.ttlMillis = ttlMillis;
    try {
      KeyGenerator generator = KeyGenerator.getInstance("AES");
      generator.init(256, secureRandom);
      processKey = generator.generateKey();
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("No fue posible crear la clave efímera de proceso", exception);
    }
  }

  @Override
  public Optional<UnlockedMasterKey> find(UUID accountId, UUID sessionId) {
    SessionKey key = new SessionKey(accountId, sessionId);
    EncryptedEntry entry = store.get(key);
    Instant now = clock.instant();
    if (entry == null || !entry.expiresAt().isAfter(now)) {
      if (entry != null) {
        store.remove(key, entry);
      }
      return Optional.empty();
    }
    return Optional.of(new UnlockedMasterKey(decrypt(key, entry.payload()), entry.expiresAt()));
  }

  @Override
  public void unlock(UUID accountId, UUID sessionId, String masterKey) {
    SessionKey key = new SessionKey(accountId, sessionId);
    store.put(key, new EncryptedEntry(
        encrypt(key, masterKey),
        clock.instant().plusMillis(ttlMillis)));
  }

  @Override
  public void clear(UUID accountId, UUID sessionId) {
    store.remove(new SessionKey(accountId, sessionId));
  }

  @Override
  public void clearAll(UUID accountId) {
    store.keySet().removeIf(key -> key.accountId().equals(accountId));
  }

  private byte[] encrypt(SessionKey key, String value) {
    try {
      byte[] iv = new byte[IV_BYTES];
      secureRandom.nextBytes(iv);
      Cipher cipher = cipher(Cipher.ENCRYPT_MODE, key, iv);
      byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
      return ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("No fue posible proteger la Master Key local", exception);
    }
  }

  private String decrypt(SessionKey key, byte[] payload) {
    try {
      ByteBuffer buffer = ByteBuffer.wrap(payload);
      byte[] iv = new byte[IV_BYTES];
      buffer.get(iv);
      byte[] encrypted = new byte[buffer.remaining()];
      buffer.get(encrypted);
      return new String(cipher(Cipher.DECRYPT_MODE, key, iv).doFinal(encrypted), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException | RuntimeException exception) {
      throw new IllegalStateException("No fue posible recuperar la Master Key local", exception);
    }
  }

  private Cipher cipher(int mode, SessionKey key, byte[] iv) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(mode, processKey, new GCMParameterSpec(TAG_BITS, iv));
    cipher.updateAAD(key.redisSuffix().getBytes(StandardCharsets.UTF_8));
    return cipher;
  }

  private record SessionKey(UUID accountId, UUID sessionId) {
    private SessionKey {
      if (accountId == null || sessionId == null) {
        throw new IllegalArgumentException("accountId y sessionId son obligatorios");
      }
    }

    String redisSuffix() {
      return accountId + ":" + sessionId;
    }
  }

  private record EncryptedEntry(byte[] payload, Instant expiresAt) {
  }
}
