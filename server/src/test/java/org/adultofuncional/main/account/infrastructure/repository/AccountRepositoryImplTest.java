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
 * <p>
 * Clase de pruebas unitarias para la implementación concreta del repositorio
 * {@link AccountRepositoryImpl}. Verifica que la capa de infraestructura
 * interactúe correctamente con el repositorio JPA (simulado) y el mapeador
 * entre entidades JPA y objetos de dominio.
 *
 * <p>
 * <strong>Propósito:</strong>
 * Asegurar que el adaptador de persistencia (repositorio) delegue adecuadamente
 * las operaciones de acceso a datos en el repositorio Spring Data JPA,
 * transformando correctamente entre el modelo de dominio ({@link Account})
 * y la entidad JPA ({@link AccountEntity}) mediante el mapper.
 *
 * <p>
 * <strong>Técnicas de prueba:</strong>
 * <ul>
 *   <li><b>Mockito:</b> Se simulan {@link SpringAccountJpaRepository} y
 *       {@link AccountMapper} para aislar la lógica del repositorio de la base
 *       de datos real y del comportamiento de mapeo.</li>
 *   <li><b>JUnit 5:</b> Se utilizan aserciones estándar para validar resultados.</li>
 * </ul>
 *
 * <p>
 * <strong>Casos cubiertos:</strong>
 * <ul>
 *   <li>Guardado de una cuenta ({@link #shouldSaveAccount()}).</li>
 *   <li>Búsqueda por identificador ({@link #shouldFindById()}).</li>
 *   <li>Búsqueda por email ({@link #shouldFindByEmail()}).</li>
 *   <li>Eliminación por identificador ({@link #shouldDeleteById()}).</li>
 * </ul>
 *
 * <p>
 * <strong>⚠️ Importante:</strong>
 * Estas pruebas no requieren base de datos real; toda interacción con JPA
 * se verifica mediante comprobaciones de llamadas a los métodos simulados
 * (verificaciones con {@code verify()}).
 *
 * @author Juan
 * @since 0.0.1
 * @see AccountRepositoryImpl
 * @see SpringAccountJpaRepository
 * @see AccountMapper
 */
public class AccountRepositoryImplTest {

    /**
     * Prueba la operación de guardado de una cuenta.
     *
     * <p>
     * <strong>Escenario:</strong>
     * Se crea un objeto de dominio {@link Account}, se simula la conversión
     * a entidad JPA mediante el mapper, se simula la llamada a {@code save()}
     * del repositorio JPA (retornando la misma entidad), y luego la conversión
     * inversa a dominio.
     *
     * <p>
     * <strong>Resultado esperado:</strong>
     * <ul>
     *   <li>El método {@code repository.save(account)} retorna un objeto
     *       {@link Account} no nulo.</li>
     *   <li>Se verifica que el repositorio JPA recibió la llamada {@code save()}
     *       exactamente una vez.</li>
     *   <li>El mapper se invoca para la transformación dominio → entidad
     *       y luego entidad → dominio.</li>
     * </ul>
     *
     * @see AccountRepositoryImpl#save(Account)
     * @see SpringAccountJpaRepository#save(Object)
     * @see AccountMapper#toEntity(Account)
     * @see AccountMapper#toDomain(AccountEntity)
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
     * Prueba la búsqueda de una cuenta por su identificador UUID.
     *
     * <p>
     * <strong>Escenario:</strong>
     * Se simula que el repositorio JPA encuentra una entidad para un ID dado
     * (retorna {@code Optional.of(entity)}), y que el mapper convierte esa
     * entidad en un objeto de dominio {@link Account}.
     *
     * <p>
     * <strong>Resultado esperado:</strong>
     * <ul>
     *   <li>El {@link Optional} retornado por {@code repository.findById(id)}
     *       está presente ({@code isPresent() == true}).</li>
     *   <li>El objeto {@code Account} contenido es igual al esperado
     *       (comparación por referencia o por contenido).</li>
     *   <li>Se verifica que el repositorio JPA invocó {@code findById()}
     *       con el ID correcto.</li>
     * </ul>
     *
     * @see AccountRepositoryImpl#findById(UUID)
     * @see SpringAccountJpaRepository#findById(Object)
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
     * Prueba la búsqueda de una cuenta por su dirección de correo electrónico.
     *
     * <p>
     * <strong>Escenario:</strong>
     * Se simula que el repositorio JPA encuentra una entidad cuyo email
     * coincide con la cadena proporcionada, y el mapper convierte la entidad
     * en un objeto de dominio.
     *
     * <p>
     * <strong>Resultado esperado:</strong>
     * <ul>
     *   <li>El {@code Optional} retornado por {@code repository.findByEmail(email)}
     *       contiene la cuenta esperada.</li>
     *   <li>Se verifica la invocación a {@code jpaRepository.findByEmail(email)}.</li>
     *   <li>Se verifica la conversión mediante {@code mapper.toDomain(entity)}.</li>
     * </ul>
     *
     * <p>
     * <strong>Nota:</strong>
     * El método {@code findByEmail} asume que el repositorio JPA expone
     * este método de consulta personalizado (derived query o {@code @Query}).
     *
     * @see AccountRepositoryImpl#findByEmail(String)
     * @see SpringAccountJpaRepository#findByEmail(String)
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
     * Prueba la eliminación de una cuenta por su identificador.
     *
     * <p>
     * <strong>Escenario:</strong>
     * Se invoca el método {@code deleteById(id)} del repositorio.
     *
     * <p>
     * <strong>Resultado esperado:</strong>
     * <ul>
     *   <li>No se retorna ningún valor (void).</li>
     *   <li>Se verifica que el repositorio JPA llame a su método
     *       {@code deleteById(id)} exactamente una vez con el ID proporcionado.</li>
     *   <li>No se requiere interacción con el mapper, ya que la eliminación
     *       trabaja directamente con la clave primaria.</li>
     * </ul>
     *
     * @see AccountRepositoryImpl#deleteById(UUID)
     * @see SpringAccountJpaRepository#deleteById(Object)
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