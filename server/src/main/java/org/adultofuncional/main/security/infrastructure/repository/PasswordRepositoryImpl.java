package org.adultofuncional.main.security.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.adultofuncional.main.security.infrastructure.persistence.mapper.PasswordMapper;
import org.adultofuncional.main.security.infrastructure.persistence.repository.SecurityJpaRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador concreto del puerto {@link PasswordRepository}.
 *
 * <p>
 * Implementa las operaciones de persistencia de credenciales delegando en
 * {@link SecurityJpaRepository} (Spring Data JPA) y utilizando el
 * {@link PasswordMapper} para convertir entre las entidades JPA
 * ({@link PasswordEntity}) y el modelo de dominio ({@link Password}).
 *
 * <p>
 * <strong>Métodos implementados:</strong>
 * <ul>
 * <li>{@link #findById(UUID)} — busca una credencial por ID y la convierte
 * a dominio.</li>
 * <li>{@link #findAllByAccountId(UUID)} — lista todas las credenciales de
 * una cuenta.</li>
 * <li>{@link #save(Password)} — persiste una credencial nueva o actualizada,
 * devolviendo el modelo de dominio resultante.</li>
 * <li>{@link #deleteById(UUID)} — elimina una credencial por su ID.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see PasswordRepository
 * @see SecurityJpaRepository
 * @see PasswordMapper
 */
@Repository
@RequiredArgsConstructor
public class PasswordRepositoryImpl implements PasswordRepository {

  private final SecurityJpaRepository jpaRepository;
  private final PasswordMapper mapper;

  /**
   * Busca una credencial por su identificador único.
   *
   * <p>
   * Consulta el repositorio Spring Data JPA y convierte la entidad resultante
   * al modelo de dominio mediante
   * {@link PasswordMapper#toDomain(PasswordEntity)}.
   *
   * @param id UUID de la credencial. No debe ser {@code null}.
   * @return {@link Optional} con la credencial si existe;
   *         {@code Optional.empty()} en caso contrario.
   */
  @Override
  public Optional<Password> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  /**
   * Lista todas las credenciales asociadas a una cuenta específica.
   *
   * <p>
   * Utiliza el método {@code findByAccount_AccountId} de Spring Data JPA
   * para recuperar las entidades y luego las convierte una a una al modelo
   * de dominio {@link Password} mediante el mapper.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de credenciales de la cuenta (vacía si no hay registros).
   */
  @Override
  public List<Password> findAllByAccountId(UUID accountId) {
    return jpaRepository.findByAccount_AccountId(accountId)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Persiste una credencial nueva o actualiza una existente.
   *
   * <p>
   * Convierte el modelo de dominio a entidad JPA con
   * {@link PasswordMapper#toEntity(Password)}, la guarda mediante
   * Spring Data JPA y vuelve a convertir el resultado a dominio para retornar
   * la versión persistida.
   *
   * @param password la credencial a guardar. No debe ser {@code null}.
   * @return la credencial persistida como modelo de dominio.
   */
  @Override
  public Password save(Password password) {
    PasswordEntity entity = mapper.toEntity(password);
    PasswordEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  /**
   * Elimina una credencial por su identificador único.
   *
   * <p>
   * Si no existe ninguna credencial con el ID dado, la operación no tiene efecto
   * (comportamiento silencioso de Spring Data JPA). La validación de existencia
   * previa se realiza en la capa de aplicación.
   *
   * @param id UUID de la credencial a eliminar. No debe ser {@code null}.
   */
  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
