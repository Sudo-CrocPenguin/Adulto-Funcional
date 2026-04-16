package org.adultofuncional.main.account.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountEntityTest {

  private AccountEntity account;

  @BeforeEach
  void setUp() {
    account = new AccountEntity();
  }

  @Test
  void testPrePersistSetsCreatedAt() {
    account.onCreate();
    assertNotNull(account.getAccount_created_at());
    assertTrue(account.getAccount_created_at() instanceof LocalDateTime);
  }

  @Test
  void testCollectionsInitialization() {
    assertNotNull(account.getMovements());
    assertNotNull(account.getFixed_expenses());
    assertNotNull(account.getEvents());
    assertNotNull(account.getPasswords());

    assertTrue(account.getMovements().isEmpty());
    assertTrue(account.getFixed_expenses().isEmpty());
    assertTrue(account.getEvents().isEmpty());
    assertTrue(account.getPasswords().isEmpty());
  }

  @Test
  void testSettersAndGetters() {
    UUID id = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    account.setAccount_id(id);
    account.setAccount_names("Juan");
    account.setAccount_lastnames("Pérez");
    account.setAccount_email("juan@email.com");
    account.setAccount_phone("123456789");
    account.setAccount_password("encodedPass123");
    account.setAccount_master_key("masterKey123");
    account.setAccount_created_at(now);

    assertEquals(id, account.getAccount_id());
    assertEquals("Juan", account.getAccount_names());
    assertEquals("Pérez", account.getAccount_lastnames());
    assertEquals("juan@email.com", account.getAccount_email());
    assertEquals("123456789", account.getAccount_phone());
    assertEquals("encodedPass123", account.getAccount_password());
    assertEquals("masterKey123", account.getAccount_master_key());
    assertEquals(now, account.getAccount_created_at());
  }
}
