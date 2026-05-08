package org.adultofuncional.main.security.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.adultofuncional.main.security.infrastructure.persistence.mapper.PasswordMapper;
import org.adultofuncional.main.security.infrastructure.persistence.repository.SpringPasswordJpaRepository;
import org.springframework.stereotype.Repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Implementación del puerto {@link PasswordRepository}.
 *
 * <p>
 * Adaptador encargado de conectar el dominio con la capa
 * de persistencia usando Spring Data JPA.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 */
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordRepositoryImpl implements PasswordRepository {

  SpringPasswordJpaRepository jpaRepository;
  PasswordMapper mapper;

  /**
   * Persiste una contraseña en la base de datos.
   *
   * @param password modelo de dominio
   * @return contraseña persistida
   */
  @Override
  public Password save(Password password) {

    PasswordEntity entity = mapper.toEntity(password);

    PasswordEntity savedEntity = jpaRepository.save(entity);

    return mapper.toDomain(savedEntity);
  }

  /**
   * Busca una contraseña por su identificador.
   *
   * @param id identificador
   * @return contraseña encontrada (si existe)
   */
  @Override
  public Optional<Password> findById(UUID id) {

    return jpaRepository.findById(id)
        .map(mapper::toDomain);
  }

  /**
   * Retorna todas las contraseñas registradas.
   *
   * @return lista de contraseñas
   */
  @Override
  public List<Password> findAll() {

    return jpaRepository.findAll()
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Verifica si una contraseña existe por su identificador.
   *
   * @param id identificador
   * @return {@code true} si existe
   */
  @Override
  public boolean existsById(UUID id) {

    return jpaRepository.existsById(id);
  }
}