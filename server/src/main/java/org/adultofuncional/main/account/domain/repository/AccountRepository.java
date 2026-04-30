package org.adultofuncional.main.account.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//import org.adultofuncional.main.account.domain.model.Account;

/**
 * Interfaz de repositorio definida en la capa de dominio (domain/repository).
 * <p>
 * Declara las operaciones de persistencia que necesita la logica de negocio sin
 * incluir ninguna implementacion. Aisla los detalles tecnicos de acceso a datos
 * (JPA, SQL, MongoDB, etc.) y aplica el principio de inversion de dependencias.
 * <p>
 * Los casos de uso reciben esta interfaz por constructor e invocan sus metodos,
 * permitiendo cambiar la tecnologia de persistencia sin modificar el codigo del
 * dominio.
 *
 * @author daniel
 * @since 0.0.1
 */
public interface AccountRepository {

//    Account save(Account account);

//    Optional<Account> findById(UUID id);

//    Optional<Account> findByEmail(String email);

//    List<Account> findAll();

    /**
     * Elimina una cuenta por su identificador unico.
     *
     * @param id el UUID de la cuenta a eliminar
     */
    void deleteById(UUID id);
}