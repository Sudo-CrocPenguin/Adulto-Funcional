package org.adultofuncional.main.account.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;

//import org.adultofuncional.main.account.domain.model.Account;

/**
 * Puerto del dominio que define las operaciones de persistencia para la entidad
 * {@code Account}.
 *
 * <p>
 * Esta interfaz pertenece a la capa de dominio en Clean Architecture y sigue
 * el principio de inversión de dependencias: el dominio no conoce la
 * implementación concreta. La capa de infraestructura
 * ({@code AccountRepositoryImpl})
 * implementa este puerto y delega en {@code SpringAccountJpaRepository} para
 * acceder a MariaDB.
 *
 * <p>
 * Operaciones:
 * <ul>
 * <li>{@code deleteById(UUID)} — eliminación física de una cuenta por su UUID
 * v7</li>
 * <li>(Futuros) {@code save}, {@code findById}, {@code findByEmail},
 * {@code findAll}</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 0.0.1
 */
public interface AccountRepository {

  Account save(Account account);

  Optional<Account> findById(UUID id);

  Optional<Account> findByEmail(String email);

  List<Account> findAll();

  void deleteById(UUID id);
}
