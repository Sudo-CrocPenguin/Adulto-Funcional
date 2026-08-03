package org.adultofuncional.main.account.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del repositorio de cuentas en la capa de infraestructura.
 *
 * <p>
 * Conecta el puerto del dominio con Spring Data JPA y MariaDB,
 * convirtiendo entre {@link Account} (modelo de dominio) y
 * {@link AccountEntity} (entidad JPA) mediante {@link AccountMapper}.
 *
 * @author Jeronimo Ospina Zapata,Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

  private final SpringAccountJpaRepository jpaRepository;
  private final AccountMapper mapper;

  /**
   * Guarda o actualiza una cuenta.
   *
   * <p>
   * Flujo de creación: {@code Account} → {@code mapper.toEntity()}
   * → {@code jpaRepository.save()} → {@code mapper.toDomain()}.
   *
   * <p>
   * Flujo de actualización: si el ID ya existe, se carga la entidad gestionada y
   * se copian únicamente campos escalares. Esto evita que Hibernate interprete
   * colecciones hijas ausentes como huérfanas y borre datos relacionados por las
   * relaciones {@code orphanRemoval}.
   *
   * @param account modelo de dominio a persistir. No puede ser {@code null}.
   * @return modelo de dominio con los campos actualizados tras la persistencia.
   */
  @Override
  public Account save(Account account) {
    if (account.getId() != null) {
      Optional<AccountEntity> existing = jpaRepository.findById(account.getId());
      if (existing.isPresent()) {
        AccountEntity managedEntity = existing.get();
        mapper.copyScalarsToEntity(account, managedEntity);
        AccountEntity saved = jpaRepository.save(managedEntity);
        return mapper.toDomain(saved);
      }
    }

    AccountEntity entity = mapper.toEntity(account);
    AccountEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }
 
  /**
   * Busca una cuenta por su identificador UUID.
   *
   * @param id identificador de la cuenta. No puede ser {@code null}.
   * @return {@link Optional} con la cuenta si existe, vacío si no.
   */
  @Override
  public Optional<Account> findById(UUID id) {
    return jpaRepository.findById(id)
        .map(mapper::toDomain);
  }

  @Override
  public Optional<Account> findByIdForUpdate(UUID id) {
    return jpaRepository.findByIdForUpdate(id)
        .map(mapper::toDomain);
  }

/**
   * Busca una cuenta por su correo electrónico.
   *
   * <p>
   * Delega en {@link SpringAccountJpaRepository#findByAccountEmail(String)},
   * que Spring Data traduce a una consulta sobre la columna UNIQUE
   * {@code account_email}.
   *
   * @param email correo electrónico a buscar; no puede ser {@code null}
   * @return {@link Optional} con la cuenta si existe, vacío si no
   */
  @Override
  public Optional<Account> findByEmail(String email) {
    return jpaRepository.findByAccountEmail(email)
        .map(mapper::toDomain);
  }

  /**
   * Retorna todas las cuentas registradas en el sistema.
   *
   * @return lista de cuentas, vacía si no hay ninguna.
   */
  @Override
  public List<Account> findAll() {
    return jpaRepository.findAll()
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Elimina físicamente una cuenta por su identificador.
   * Todos los datos relacionados se eliminan en cascada.
   *
   * @param id identificador de la cuenta a eliminar. No puede ser {@code null}.
   */
  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpaRepository.existsById(id);
  }
}
