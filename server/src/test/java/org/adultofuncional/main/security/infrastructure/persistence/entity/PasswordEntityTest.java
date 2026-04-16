package org.adultofuncional.main.security.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordEntityTest {

  private PasswordEntity password;

  @BeforeEach
  void setUp() {
    password = new PasswordEntity();
  }

  @Test
  void testSettersAndGetters() {
    UUID id = UUID.randomUUID();
    LocalDate lastChange = LocalDate.now();

    password.setPassword_id(id);
    password.setPassword_application_name("Gmail");
    password.setPassword_application("miPassword123");
    password.setPassword_last_change_date(lastChange);

    assertEquals(id, password.getPassword_id());
    assertEquals("Gmail", password.getPassword_application_name());
    assertEquals("miPassword123", password.getPassword_application());
    assertEquals(lastChange, password.getPassword_last_change_date());
  }

  @Test
  void testLastChangeDateCanBeNull() {
    password.setPassword_last_change_date(null);
    assertNull(password.getPassword_last_change_date());
  }
}
