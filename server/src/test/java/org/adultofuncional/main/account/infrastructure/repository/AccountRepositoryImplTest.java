package org.adultofuncional.main.account.infrastructure.repository;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tuapp.account.domain.model.Account;
import com.tuapp.account.infrastructure.persistence.entity.AccountEntity;
import com.tuapp.account.infrastructure.persistence.mapper.AccountMapper;
import com.tuapp.account.infrastructure.persistence.repository.SpringAccountJpaRepository;

/**
 * ============================================================
 * AccountRepositoryImplTest
 * ============================================================
 *
 * ¿Qué es?
 * Clase de pruebas unitarias para la implementación del repositorio
 * AccountRepositoryImpl.
 *
 * ¿Para qué sirve?
 * Permite verificar que la capa de infraestructura funcione correctamente,
 * simulando la interacción con la base de datos mediante el uso de Mockito.
 *
 * ¿Qué hace?
 * - Simula el comportamiento del repositorio JPA
 * - Verifica el guardado de cuentas
 * - Verifica la búsqueda por ID
 * - Verifica la búsqueda por email
 * - Verifica la eliminación de cuentas
 *
 * ============================================================
 */
public class AccountRepositoryImplTest {

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