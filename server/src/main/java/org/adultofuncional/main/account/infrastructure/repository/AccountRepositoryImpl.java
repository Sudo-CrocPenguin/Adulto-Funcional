package org.adultofuncional.main.account.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Implementación del repositorio de cuentas en la capa de infraestructura.
 *
 * <p>
 * Conecta el puerto del dominio con Spring Data JPA y MariaDB,
 * convirtiendo entre {@link Account} (modelo de dominio) y
 * {@link AccountEntity} (entidad JPA) mediante {@link AccountMapper}.
 *
 * @author Jeronimo Ospina
 * @since 0.0.1
 */
@Repository
public class AccountRepositoryImpl implements AccountRepository {
  /**
   * Repositorio Spring Data JPA para operaciones CRUD sobre
   * {@code AccountEntity}.
   */
  private final SpringAccountJpaRepository jpaRepository;
  /** Mapper para convertir entre {@link Account} y {@link AccountEntity}. */
  private final AccountMapper mapper;

  /**
   * Construye el repositorio con las dependencias inyectadas por Spring.
   *
   * @param jpaRepository repositorio Spring Data JPA
   * @param mapper        conversor entre dominio y entidad
   */
  public AccountRepositoryImpl(SpringAccountJpaRepository jpaRepository,
      AccountMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  /**
   * Guarda una cuenta en la base de datos.
   *
   * <p>
   * Flujo: {@code Account} → {@code mapper.toEntity()} →
   * {@code jpaRepository.save()} → {@code mapper.toDomain()} → retorna
   * el modelo de dominio con los campos actualizados.
   *
   * @param account modelo de dominio a persistir
   * @return modelo de dominio persistido
   * @throws IllegalArgumentException si account es null
   */
  @Override
  public Account save(Account account) {
    if (account == null) {
      throw new IllegalArgumentException("Account cannot be null");
    }
    AccountEntity entity = mapper.toEntity(account);
    AccountEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  /**
   * Busca una cuenta por su identificador UUID v7.
   *
   * @param id identificador de la cuenta
   * @return Optional con la cuenta si existe, o empty si no se encuentra
   * @throws IllegalArgumentException si id es null
   */
  @Override
  public Optional<Account> findById(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("ID cannot be null");
    }
    return jpaRepository.findById(id)
        .map(mapper::toDomain);
  }

  /**
   * Busca una cuenta por su correo electrónico (columna {@code account_email},
   * que es UNIQUE en la base de datos).
   *
   * @param email correo electrónico a buscar
   * @return Optional con la cuenta si existe, o empty si no se encuentra
   * @throws IllegalArgumentException si email es null o vacío
   */
  @Override
  public Optional<Account> findByEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }
    return jpaRepository.findByEmail(email)
        .map(mapper::toDomain);
  }

  /**
   * Elimina una cuenta por su identificador. Esta es una eliminación física
   * (hard delete). Todos los datos relacionados (movimientos, gastos fijos,
   * eventos y contraseñas) se eliminan en cascada.
   *
   * @param id identificador de la cuenta a eliminar
   * @throws IllegalArgumentException si id es null
   */
  @Override
  public void deleteById(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("ID cannot be null");
    }
    jpaRepository.deleteById(id);
  }
}
