package org.adultofuncional.main.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.adultofuncional.main.security.application.dto.ChangeMasterKeyRequest;
import org.adultofuncional.main.security.application.usecase.ChangeMasterKeyUseCase;
import org.adultofuncional.main.security.domain.service.EncryptionException;
import org.adultofuncional.main.security.domain.service.EncryptionService;
import org.adultofuncional.main.security.domain.service.EncryptionService.EncryptedData;
import org.adultofuncional.main.security.domain.service.EncryptionService.EncryptionContext;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.adultofuncional.main.security.infrastructure.persistence.repository.PasswordJpaRepository;
import org.adultofuncional.main.security.infrastructure.service.AesEncryptionService;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Comprueba el rollback de hash y ciphertext ante un fallo a mitad del recifrado. */
@SpringBootTest
@Import(MasterKeyRotationRollbackIntegrationTest.FailingCryptoConfiguration.class)
class MasterKeyRotationRollbackIntegrationTest extends MariaDbIntegrationTestSupport {

  private static final String LOGIN_PASSWORD = "frase principal extensa";
  private static final String OLD_MASTER_KEY = "MasterKey-Original-2026";
  private static final String NEW_MASTER_KEY = "MasterKey-Rotada-2026";

  @Autowired
  ChangeMasterKeyUseCase changeUseCase;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  @Autowired
  PasswordJpaRepository passwordRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  JdbcTemplate jdbcTemplate;

  @Autowired
  MasterKeySessionService sessionService;

  @Autowired
  FailingEncryptionService encryptionService;

  private UUID accountId;

  @AfterEach
  void cleanDatabase() {
    if (accountId != null) {
      sessionService.clearAll(accountId);
    }
    passwordRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @Test
  void rollsBackEveryCiphertextAndHashWhenOneReEncryptionFails() {
    String originalMasterHash = passwordEncoder.encode(OLD_MASTER_KEY);
    AccountEntity account = persistAccount(originalMasterHash);
    accountId = account.getAccountId();
    List<CredentialSnapshot> originals = persistCredentials(account);
    UUID sessionId = UUID.randomUUID();
    sessionService.unlock(accountId, sessionId, OLD_MASTER_KEY);
    encryptionService.failOnSecondCurrentEncryption();

    ChangeMasterKeyRequest request = new ChangeMasterKeyRequest(
        LOGIN_PASSWORD,
        OLD_MASTER_KEY,
        NEW_MASTER_KEY);

    assertThatThrownBy(() -> changeUseCase.execute(accountId, request))
        .isInstanceOf(EncryptionException.class);

    String storedHash = jdbcTemplate.queryForObject(
        "SELECT account_master_key FROM accounts WHERE account_id = ?",
        String.class,
        accountId.toString());
    assertThat(storedHash).isEqualTo(originalMasterHash);
    for (CredentialSnapshot original : originals) {
      byte[] storedCiphertext = jdbcTemplate.queryForObject(
          "SELECT password_ciphertext FROM passwords WHERE password_id = ?",
          byte[].class,
          original.id().toString());
      assertThat(storedCiphertext).isEqualTo(original.ciphertext());
    }
    assertThat(sessionService.find(accountId, sessionId)).isPresent();
  }

  private AccountEntity persistAccount(String masterKeyHash) {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Rollback");
    account.setAccountLastNames("Master Key");
    account.setAccountEmail("rollback-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("+573001234567");
    account.setAccountPassword(passwordEncoder.encode(LOGIN_PASSWORD));
    account.setAccountMasterKey(masterKeyHash);
    return accountRepository.saveAndFlush(account);
  }

  private List<CredentialSnapshot> persistCredentials(AccountEntity account) {
    List<CredentialSnapshot> snapshots = new ArrayList<>();
    for (int index = 0; index < 2; index++) {
      UUID passwordId = UUID.randomUUID();
      EncryptionContext context = new EncryptionContext(account.getAccountId(), passwordId);
      EncryptedData encrypted = encryptionService.delegate().encrypt(
          "secreto-" + index,
          OLD_MASTER_KEY,
          context);
      PasswordEntity entity = new PasswordEntity();
      entity.setPasswordId(passwordId);
      entity.setPasswordApplicationName("Servicio " + index);
      entity.setPasswordSalt(encrypted.salt());
      entity.setPasswordCryptoVersion((short) encrypted.cryptoVersion());
      entity.setPasswordIv(encrypted.iv());
      entity.setPasswordCiphertext(encrypted.ciphertext());
      entity.setPasswordLastChangeDate(LocalDate.of(2026, 8, 3));
      entity.setAccount(account);
      passwordRepository.saveAndFlush(entity);
      snapshots.add(new CredentialSnapshot(passwordId, encrypted.ciphertext().clone()));
    }
    return snapshots;
  }

  private record CredentialSnapshot(UUID id, byte[] ciphertext) {
  }

  @TestConfiguration
  static class FailingCryptoConfiguration {

    @Bean
    @Primary
    FailingEncryptionService failingEncryptionService() {
      return new FailingEncryptionService();
    }
  }

  static final class FailingEncryptionService implements EncryptionService {

    private final AesEncryptionService delegate = new AesEncryptionService();
    private final AtomicInteger currentEncryptions = new AtomicInteger();
    private volatile boolean failureEnabled;

    void failOnSecondCurrentEncryption() {
      currentEncryptions.set(0);
      failureEnabled = true;
    }

    AesEncryptionService delegate() {
      return delegate;
    }

    @Override
    public EncryptedData encrypt(String plainPassword, String masterKey) {
      return delegate.encrypt(plainPassword, masterKey);
    }

    @Override
    public EncryptedData encrypt(
        String plainPassword,
        String masterKey,
        EncryptionContext context) {
      if (failureEnabled && currentEncryptions.incrementAndGet() == 2) {
        throw new EncryptionException(
            EncryptionException.Reason.INTERNAL_FAILURE,
            "Fallo criptográfico simulado");
      }
      return delegate.encrypt(plainPassword, masterKey, context);
    }

    @Override
    public String decrypt(String salt, byte[] iv, byte[] ciphertext, String masterKey) {
      return delegate.decrypt(salt, iv, ciphertext, masterKey);
    }

    @Override
    public String decrypt(
        String salt,
        byte[] iv,
        byte[] ciphertext,
        String masterKey,
        int cryptoVersion,
        EncryptionContext context) {
      return delegate.decrypt(salt, iv, ciphertext, masterKey, cryptoVersion, context);
    }
  }
}
