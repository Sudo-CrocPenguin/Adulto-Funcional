package org.adultofuncional.main.account.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;

/**
 * Puerto del dominio que define las operaciones de persistencia para
 * {@link Account}.
 *
 * <p>
 * Pertenece a la capa de dominio y sigue el principio de inversión de
 * dependencias: el dominio define el contrato, la infraestructura
 * ({@code AccountRepositoryImpl}) lo implementa delegando en
 * {@code SpringAccountJpaRepository}.
 *
 * <p>
 * El dominio nunca conoce {@code AccountEntity} ni ningún detalle de JPA.
 *
 * @author Daniel Salazar, Miguel Angel Blandon Montes
 * @since 0.0.1
 */
public interface AccountRepository {

  /**
   * Persiste o actualiza una cuenta.
   *
   * @param account modelo de dominio a persistir. No puede ser {@code null}.
   * @return modelo de dominio con los campos actualizados tras la persistencia.
   */
  Account save(Account account);

  /**
   * Busca una cuenta por su identificador único.
   *
   * @param id UUID de la cuenta.
   * @return {@link Optional} con la cuenta si existe, vacío si no.
   */
  Optional<Account> findById(UUID id);

  /**
   * Busca una cuenta por su correo electrónico.
   *
   * @param email correo electrónico a buscar.
   * @return {@link Optional} con la cuenta si existe, vacío si no.
   */
  Optional<Account> findByEmail(String email);

  /**
   * Retorna todas las cuentas registradas en el sistema.
   *
   * @return lista de cuentas, vacía si no hay ninguna.
   */
  List<Account> findAll();

  /**
   * Elimina físicamente una cuenta por su identificador.
   *
   * @param id UUID de la cuenta a eliminar.
   */
  void deleteById(UUID id);

  /**
   * Verifica si existe una cuenta con el identificador dado.
   *
   * @param id UUID de la cuenta.
   * @return {@code true} si existe, {@code false} en caso contrario.
   */
  boolean existsById(UUID id);
}
