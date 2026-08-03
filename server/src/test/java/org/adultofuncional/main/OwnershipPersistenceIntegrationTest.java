package org.adultofuncional.main;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.agenda.infrastructure.persistence.repository.SpringEventJpaRepository;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringCategoryJpaRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringMovementJpaRepository;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.adultofuncional.main.security.infrastructure.persistence.repository.PasswordJpaRepository;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Verifica en MariaDB que los repositorios con recursos privados incorporen
 * {@code accountId} en consultas y eliminaciones, no después de materializar
 * una entidad de otra cuenta.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OwnershipPersistenceIntegrationTest extends MariaDbIntegrationTestSupport {

  @Autowired
  SpringAccountJpaRepository accountRepository;

  @Autowired
  SpringCategoryJpaRepository categoryRepository;

  @Autowired
  SpringMovementJpaRepository movementRepository;

  @Autowired
  SpringEventJpaRepository eventRepository;

  @Autowired
  PasswordJpaRepository passwordRepository;

  AccountEntity accountA;
  AccountEntity accountB;
  CategoryEntity financeCategory;
  CategoryEntity agendaCategory;

  @BeforeEach
  void setUp() {
    accountA = persistAccount("ownership-a@example.com");
    accountB = persistAccount("ownership-b@example.com");
    financeCategory = categoryRepository.findById(
        UUID.fromString("01988e6b-0c00-7000-8000-000000000006")).orElseThrow();
    agendaCategory = categoryRepository.findById(
        UUID.fromString("01988e6b-0c00-7000-8000-000000000009")).orElseThrow();
  }

  @Test
  void scopesMovementQueriesAndDeletionByAccount() {
    MovementEntity movement = persistMovement(accountA);
    UUID movementId = movement.getMovementId();

    assertThat(movementRepository.findByMovementIdAndAccount_AccountId(
        movementId, accountA.getAccountId())).isPresent();
    assertThat(movementRepository.findByMovementIdAndAccount_AccountId(
        movementId, accountB.getAccountId())).isEmpty();

    assertThat(movementRepository.deleteByMovementIdAndAccountId(
        movementId, accountB.getAccountId())).isZero();
    assertThat(movementRepository.existsById(movementId)).isTrue();

    assertThat(movementRepository.deleteByMovementIdAndAccountId(
        movementId, accountA.getAccountId())).isEqualTo(1);
    assertThat(movementRepository.existsById(movementId)).isFalse();
  }

  @Test
  void scopesEventQueriesAndDeletionByAccount() {
    EventEntity event = persistEvent(accountA);
    UUID eventId = event.getEventId();

    assertThat(eventRepository.findByEventIdAndAccount_AccountId(
        eventId, accountA.getAccountId())).isPresent();
    assertThat(eventRepository.findByEventIdAndAccount_AccountId(
        eventId, accountB.getAccountId())).isEmpty();

    assertThat(eventRepository.deleteByEventIdAndAccountId(
        eventId, accountB.getAccountId())).isZero();
    assertThat(eventRepository.existsById(eventId)).isTrue();

    assertThat(eventRepository.deleteByEventIdAndAccountId(
        eventId, accountA.getAccountId())).isEqualTo(1);
    assertThat(eventRepository.existsById(eventId)).isFalse();
  }

  @Test
  void scopesCredentialQueriesAndDeletionByAccount() {
    PasswordEntity credential = persistCredential(accountA);
    UUID passwordId = credential.getPasswordId();

    assertThat(passwordRepository.findByPasswordIdAndAccount_AccountId(
        passwordId, accountA.getAccountId())).isPresent();
    assertThat(passwordRepository.findByPasswordIdAndAccount_AccountId(
        passwordId, accountB.getAccountId())).isEmpty();

    assertThat(passwordRepository.deleteByPasswordIdAndAccountId(
        passwordId, accountB.getAccountId())).isZero();
    assertThat(passwordRepository.existsById(passwordId)).isTrue();

    assertThat(passwordRepository.deleteByPasswordIdAndAccountId(
        passwordId, accountA.getAccountId())).isEqualTo(1);
    assertThat(passwordRepository.existsById(passwordId)).isFalse();
  }

  private AccountEntity persistAccount(String email) {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Usuario");
    account.setAccountLastNames("Prueba");
    account.setAccountEmail(email);
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-password");
    return accountRepository.saveAndFlush(account);
  }

  private MovementEntity persistMovement(AccountEntity owner) {
    MovementEntity movement = new MovementEntity();
    movement.setMovementId(UUID.randomUUID());
    movement.setMovementType(MovementType.EXPENSE.name());
    movement.setMovementAmount(new BigDecimal("25000.00"));
    movement.setMovementDescription("Compra");
    movement.setMovementDate(LocalDate.now());
    movement.setAccount(owner);
    movement.setCategory(financeCategory);
    return movementRepository.saveAndFlush(movement);
  }

  private EventEntity persistEvent(AccountEntity owner) {
    LocalDateTime start = LocalDateTime.now().plusDays(1);
    EventEntity event = new EventEntity();
    event.setEventId(UUID.randomUUID());
    event.setEventTitle("Reunión");
    event.setEventPriority("Media");
    event.setEventDate(start.toLocalDate());
    event.setEventFrequency(0);
    event.setEventReminder(start.minusHours(1));
    event.setEventStartHour(start);
    event.setEventEndHour(start.plusHours(1));
    event.setEventDescription("Seguimiento");
    event.setEventStatus("Pendiente");
    event.setAccount(owner);
    event.setCategory(agendaCategory);
    return eventRepository.saveAndFlush(event);
  }

  private PasswordEntity persistCredential(AccountEntity owner) {
    PasswordEntity credential = new PasswordEntity();
    credential.setPasswordId(UUID.randomUUID());
    credential.setPasswordApplicationName("Aplicación");
    credential.setPasswordSalt("c2FsdA==");
    credential.setPasswordIv(new byte[12]);
    credential.setPasswordCiphertext(new byte[] {1, 2, 3, 4});
    credential.setPasswordCryptoVersion((short) 1);
    credential.setPasswordLastChangeDate(LocalDate.now());
    credential.setAccount(owner);
    return passwordRepository.saveAndFlush(credential);
  }
}
