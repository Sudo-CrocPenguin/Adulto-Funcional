package org.adultofuncional.main.account.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de {@link AccountRepositoryImpl}.
 *
 * <p>
 * Verifica que el repositorio delega correctamente en
 * {@code SpringAccountJpaRepository} y convierte entre dominio y entidad
 * mediante {@code AccountMapper}. Usa mocks para aislar la capa de
 * infraestructura.
 *
 * <p>
 * Casos cubiertos:
 * <ul>
 * <li>{@code save()} — convierte dominio a entidad, persiste y retorna
 * dominio</li>
 * <li>{@code findById()} — busca por UUID y mapea a dominio</li>
 * <li>{@code findByEmail()} — busca por email y mapea a dominio</li>
 * <li>{@code deleteById()} — delega la eliminación al repositorio JPA</li>
 * </ul>
 *
 * @author Jeronimo Ospina
 * @since 0.0.1
 * @see AccountRepositoryImpl
 */
public class AccountRepositoryImplTest {

  /**
   * Verifica que {@code save()} convierte a entidad, llama a
   * {@code jpaRepository.save()}
   * y convierte el resultado de vuelta a dominio.
   */
  @Test
  void shouldSaveAccount() {
    // Arrange
    SpringAccountJpaRepository jpaRepository = mock(SpringAccountJpaRepository.class);
    AccountMapper mapper = mock(AccountMapper.class);
    AccountRepositoryImpl repository = new AccountRepositoryImpl(jpaRepository, mapper);

    Account account = Account.create("Juan", "juan@test.com", "123");
    AccountEntity entity = new AccountEntity();

    when(mapper.toEntity(account)).thenReturn(entity);
    when(jpaRepository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(account);

    // Act
    Account result = repository.save(account);
    // Assert
    assertNotNull(result);
    verify(jpaRepository).save(entity);
  }

  /**
   * Verifica que {@code findById()} retorna un Optional con la cuenta
   * cuando el repositorio JPA encuentra la entidad.
   */
  @Test
  void shouldFindById() {
    // Arrange
    SpringAccountJpaRepository jpaRepository = mock(SpringAccountJpaRepository.class);
    AccountMapper mapper = mock(AccountMapper.class);
    AccountRepositoryImpl repository = new AccountRepositoryImpl(jpaRepository, mapper);

    UUID id = UUID.randomUUID();
    AccountEntity entity = new AccountEntity();
    Account account = Account.create("Juan", "juan@test.com", "123");

    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(account);

    // Act
    Optional<Account> result = repository.findById(id);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(account, result.get());
  }

  /**
   * Verifica que {@code findByEmail()} retorna un Optional con la cuenta
   * cuando el repositorio JPA encuentra una entidad con ese email.
   */
  @Test
  void shouldFindByEmail() {
    // Arrange
    SpringAccountJpaRepository jpaRepository = mock(SpringAccountJpaRepository.class);
    AccountMapper mapper = mock(AccountMapper.class);
    AccountRepositoryImpl repository = new AccountRepositoryImpl(jpaRepository, mapper);

    String email = "juan@test.com";
    AccountEntity entity = new AccountEntity();
    Account account = Account.create("Juan", email, "123");

    when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(account);

    // Act
    Optional<Account> result = repository.findByEmail(email);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(account, result.get());
  }

  /**
   * Verifica que {@code deleteById()} delega la eliminación al repositorio JPA.
   */
  @Test
  void shouldDeleteById() {
    // Arrange
    SpringAccountJpaRepository jpaRepository = mock(SpringAccountJpaRepository.class);
    AccountMapper mapper = mock(AccountMapper.class);
    AccountRepositoryImpl repository = new AccountRepositoryImpl(jpaRepository, mapper);

    UUID id = UUID.randomUUID();

    // Act
    repository.deleteById(id);

    // Assert
    verify(jpaRepository).deleteById(id);
  }
}
