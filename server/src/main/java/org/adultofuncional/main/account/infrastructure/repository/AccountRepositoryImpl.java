package com.tuapp.account.infrastructure.repository;

import com.tuapp.account.domain.model.Account;
import com.tuapp.account.domain.repository.AccountRepository;
import com.tuapp.account.infrastructure.persistence.entity.AccountEntity;
import com.tuapp.account.infrastructure.persistence.mapper.AccountMapper;
import com.tuapp.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de cuentas en la capa de infraestructura.
 * 
 * <p>
 * Esta clase implementa el contrato definido en el dominio
 * {@link AccountRepository} siguiendo los principios de
 * <strong>Arquitectura Hexagonal</strong> (también conocida como
 * Puertos y Adaptadores).
 * 
 * <p>
 * <strong>Arquitectura Hexagonal - Roles:</strong>
 * <ul>
 * <li><b>Puerto (Port):</b> {@link AccountRepository} - Define la interfaz
 *     que el dominio necesita para persistir cuentas</li>
 * <li><b>Adaptador (Adapter):</b> {@code AccountRepositoryImpl} - Conecta
 *     el puerto del dominio con la tecnología de persistencia real</li>
 * <li><b>Infraestructura concreta:</b> Spring Data JPA, Hibernate, PostgreSQL</li>
 * </ul>
 * 
 * <p>
 * <strong>Responsabilidades principales:</strong>
 * <ul>
 * <li><b>Conversión entre modelos:</b> Traduce {@link Account} (modelo de dominio)
 *     a {@link AccountEntity} (modelo de persistencia JPA) y viceversa</li>
 * <li><b>Delegación a Spring Data:</b> Utiliza {@link SpringAccountJpaRepository}
 *     para operaciones reales de base de datos</li>
 * <li><b>Aislamiento del dominio:</b> El dominio no conoce detalles de JPA,
 *     anotaciones, ni base de datos</li>
 * </ul>
 * 
 * <p>
 * <strong>Beneficios de esta separación:</strong>
 * <table border="1">
 * <tr>
 *     <th>Aspecto</th>
 *     <th>Beneficio</th>
 * </tr>
 * <tr>
 *     <td>Testabilidad</td>
 *     <td>El dominio se prueba con mocks del repositorio, sin infraestructura real</td>
 * </tr>
 * <tr>
 *     <td>Flexibilidad tecnológica</td>
 *     <td>Se puede cambiar JPA por JDBC, MongoDB, o cualquier otra tecnología
 *         sin modificar el dominio</td>
 * </tr>
 * <tr>
 *     <td>Enfoque en negocio</td>
 *     <td>El modelo {@link Account} permanece puro, sin anotaciones JPA</td>
 * </tr>
 * <tr>
 *     <td>Cumplimiento DDD</td>
 *     <td>El repositorio actúa como una "fachada" que parece una colección
 *         en memoria desde la perspectiva del dominio</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <strong>Flujo completo de una operación de guardado:</strong>
 * <pre>
 * // Capa de aplicación/servicio
 * Account account = Account.create("Juan", "juan@email.com", "123456789");
 * repository.save(account);
 * 
 * // └─ AccountRepositoryImpl.save()
 * //     ├─ mapper.toEntity(account) → Convertir Domain → Entity JPA
 * //     ├─ jpaRepository.save(entity) → Hibernate inserta en PostgreSQL
 * //     └─ mapper.toDomain(savedEntity) → Convertir Entity JPA → Domain
 * //         (para reflejar IDs generados, timestamps, etc.)
 * </pre>
 * 
 * <p>
 * <strong>⚠️ Consideraciones importantes:</strong>
 * <ul>
 * <li>Esta clase reside en {@code infrastructure}, NO en {@code domain}</li>
 * <li>Depende de Spring ({@code @Repository}), JPA, y el mapper - el dominio no conoce esto</li>
 * <li>La inyección de dependencias es por constructor (recomendado sobre field injection)</li>
 * <li>El mapper puede ser implementado con MapStruct, ModelMapper, o manual</li>
 * </ul>
 * 
 * <p>
 * <strong>Patrones de diseño utilizados:</strong>
 * <ul>
 * <li><b>Adapter Pattern:</b> Adapta la interfaz de Spring Data JPA al puerto del dominio</li>
 * <li><b>Repository Pattern:</b> Proporciona una colección similar a la memoria para agregados DDD</li>
 * <li><b>Dependency Injection:</b> Spring IoC container inyecta dependencias vía constructor</li>
 * </ul>
 * 
 * @author Juan David Ruiz Garcia
 * @since 1.0.0
 * @version 1.0
 * @see AccountRepository Puerto definido en el dominio
 * @see AccountEntity Modelo de persistencia JPA
 * @see SpringAccountJpaRepository Repositorio Spring Data
 * @see <a href="https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/">Arquitectura Hexagonal</a>
 * @see <a href="https://martinfowler.com/eaaCatalog/repository.html">Repository Pattern - Martin Fowler</a>
 */
@Repository
public class AccountRepositoryImpl implements AccountRepository {

    /**
     * Repositorio Spring Data JPA para operaciones CRUD básicas.
     * 
     * <p>
     * <strong>Responsabilidades:</strong>
     * <ul>
     * <li>Proporciona métodos CRUD estándar: {@code save()}, {@code findById()},
     *     {@code deleteById()}</li>
     * <li>Implementa métodos de consulta personalizados como {@code findByEmail()}</li>
     * <li>Maneja la transaccionalidad a nivel de método (con {@code @Transactional})</li>
     * <li>Gestiona el ciclo de vida de Hibernate y las sesiones de persistencia</li>
     * </ul>
     * 
     * <p>
     * <strong>Por qué no se usa directamente en el dominio:</strong>
     * <pre>
     * // ❌ MAL: El dominio NO debe depender de Spring Data JPA
     * public class Account {
     *     &#64;Autowired
     *     private SpringAccountJpaRepository repository; // DOMINIO CONTAMINADO
     * }
     * 
     * // ✅ BIEN: El dominio solo conoce la interfaz AccountRepository
     * public class AccountService {
     *     private final AccountRepository repository; // PUERTO DEL DOMINIO
     * }
     * </pre>
     * 
     * <p>
     * <strong>Configuración típica de Spring Data:</strong>
     * <pre>
     * // En application.properties
     * spring.datasource.url=jdbc:postgresql://localhost:5432/account_db
     * spring.jpa.hibernate.ddl-auto=validate
     * spring.jpa.properties.hibernate.jdbc.batch_size=25
     * </pre>
     */
    private final SpringAccountJpaRepository jpaRepository;
    
    /**
     * Mapper para convertir entre modelo de dominio y entidad JPA.
     * 
     * <p>
     * <strong>Responsabilidades:</strong>
     * <ul>
     * <li>{@code toEntity(Account)}: Domain → JPA Entity (para persistencia)</li>
     * <li>{@code toDomain(AccountEntity)}: JPA Entity → Domain (para consultas)</li>
     * </ul>
     * 
     * <p>
     * <strong>Implementaciones recomendadas:</strong>
     * <table border="1">
     * <tr>
     *     <th>Biblioteca</th>
     *     <th>Ventajas</th>
     *     <th>Desventajas</th>
     * </tr>
     * <tr>
     *     <td>MapStruct (RECOMENDADO)</td>
     *     <td>Compile-time, sin reflexión, muy rápido</td>
     *     <td>Requiere configuración inicial del plugin</td>
     * </tr>
     * <tr>
     *     <td>ModelMapper</td>
     *     <td>Configuración flexible, poco boilerplate</td>
     *     <td>Runtime reflection, más lento, difícil de depurar</td>
     * </tr>
     * <tr>
     *     <td>Manual (getters/setters)</td>
     *     <td>Máximo control, sin dependencias</td>
     *     <td>Mucho código boilerplate, propenso a errores</td>
     * </tr>
     * </table>
     * 
     * <p>
     * <strong>Ejemplo de implementación con MapStruct:</strong>
     * <pre>
     * &#64;Mapper(componentModel = "spring")
     * public interface AccountMapper {
     *     AccountEntity toEntity(Account domain);
     *     Account toDomain(AccountEntity entity);
     * }
     * </pre>
     * 
     * <p>
     * <strong>Mapeo de campos complejos:</strong>
     * <pre>
     * // El mapper debe manejar diferencias entre modelos:
     * // Domain: Account.name = "Juan Pérez"
     * // Entity: AccountEntity.account_names = "Juan", account_lastnames = "Pérez"
     * 
     * default AccountEntity toEntity(Account domain) {
     *     AccountEntity entity = new AccountEntity();
     *     String[] nameParts = domain.getName().split(" ", 2);
     *     entity.setAccount_names(nameParts[0]);
     *     entity.setAccount_lastnames(nameParts.length > 1 ? nameParts[1] : "");
     *     entity.setAccount_email(domain.getEmail());
     *     entity.setAccount_phone(domain.getPhone());
     *     entity.setActive(domain.isActive());
     *     return entity;
     * }
     * </pre>
     */
    private final AccountMapper mapper;

    /**
     * Constructor para inyección de dependencias.
     * 
     * <p>
     * <strong>Ventajas de inyección por constructor sobre field injection:</strong>
     * <ul>
     * <li><b>Inmutabilidad:</b> Los campos pueden ser {@code final}</li>
     * <li><b>Testabilidad:</b> Facilita el paso de mocks en pruebas unitarias</li>
     * <li><b>Null-safety:</b> Spring garantiza que las dependencias no sean null</li>
     * <li><b>Mejor diseño:</b> Evita el "dependency injection container coupling"</li>
     * <li><b>Circular dependencies:</b> Se detectan en tiempo de construcción</li>
     * </ul>
     * 
     * <p>
     * <strong>Ejemplo de prueba unitaria:</strong>
     * <pre>
     * &#64;Test
     * void save_ShouldReturnSavedAccount() {
     *     // Given
     *     SpringAccountJpaRepository mockJpa = mock(SpringAccountJpaRepository.class);
     *     AccountMapper mockMapper = mock(AccountMapper.class);
     *     AccountRepositoryImpl repository = new AccountRepositoryImpl(mockJpa, mockMapper);
     *     
     *     Account account = Account.create("Test", "test@email.com", "123");
     *     AccountEntity entity = new AccountEntity();
     *     AccountEntity savedEntity = new AccountEntity();
     *     Account savedDomain = Account.create("Test", "test@email.com", "123");
     *     
     *     when(mockMapper.toEntity(account)).thenReturn(entity);
     *     when(mockJpa.save(entity)).thenReturn(savedEntity);
     *     when(mockMapper.toDomain(savedEntity)).thenReturn(savedDomain);
     *     
     *     // When
     *     Account result = repository.save(account);
     *     
     *     // Then
     *     assertThat(result).isNotNull();
     *     verify(mockJpa).save(entity);
     * }
     * </pre>
     * 
     * @param jpaRepository Repositorio Spring Data JPA inyectado
     * @param mapper Mapper para conversión entre modelos
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
     * <strong>Flujo detallado de la operación:</strong>
     * <ol>
     * <li>Convierte el modelo de dominio {@link Account} a entidad JPA
     *     {@link AccountEntity} usando el mapper</li>
     * <li>Delega la persistencia a Spring Data JPA ({@link SpringAccountJpaRepository})</li>
     * <li>Hibernate ejecuta {@code INSERT} (si es nuevo) o {@code UPDATE}
     *     (si ya existe) basado en el estado del ID</li>
     * <li>Recupera la entidad persistida (incluyendo campos generados como
     *     {@code created_at} o UUIDs si son autogenerados)</li>
     * <li>Convierte nuevamente a modelo de dominio para reflejar cualquier
     *     cambio realizado por la base de datos (ej: timestamps generados)</li>
     * <li>Retorna el modelo de dominio actualizado</li>
     * </ol>
     * 
     * <p>
     * <strong>Comportamiento transaccional:</strong>
     * <ul>
     * <li>Si se llama sin transacción, Spring Data JPA creará una transacción
     *     de corta duración automáticamente</li>
     * <li>Si se llama dentro de una transacción existente (ej: servicio con
     *     {@code @Transactional}), participará en ella</li>
     * <li>El nivel de aislamiento por defecto es READ_COMMITTED (PostgreSQL)</li>
     * </ul>
     * 
     * <p>
     * <strong>Ejemplo de uso en servicio de aplicación:</strong>
     * <pre>
     * &#64;Service
     * &#64;Transactional
     * public class AccountRegistrationService {
     *     private final AccountRepository repository;
     *     
     *     public Account registerAccount(RegisterCommand command) {
     *         // Crear agregado de dominio con reglas de negocio
     *         Account newAccount = Account.create(
     *             command.getName(),
     *             command.getEmail(),
     *             command.getPhone()
     *         );
     *         
     *         // Persistir - TODO dentro de la transacción del servicio
     *         Account savedAccount = repository.save(newAccount);
     *         
     *         // Disparar evento de dominio
     *         eventPublisher.publish(new AccountRegisteredEvent(savedAccount));
     *         
     *         return savedAccount;
     *     }
     * }
     * </pre>
     * 
     * <p>
     * <strong>⚠️ Consideraciones de rendimiento:</strong>
     * <ul>
     * <li>Para operaciones batch, considere usar {@code saveAll()} si está disponible</li>
     * <li>El mapper se ejecuta en memoria - no hay impacto significativo de rendimiento</li>
     * <li>Si la entidad tiene relaciones (OneToMany), considere el fetching (LAZY vs EAGER)</li>
     * </ul>
     * 
     * @param account Modelo de dominio a persistir (no puede ser {@code null})
     * @return Modelo de dominio persistido con campos actualizados (incluyendo ID si es nuevo)
     * @throws IllegalArgumentException Si {@code account} es {@code null}
     * @throws org.springframework.dao.DataAccessException Si hay error de base de datos
     *          (duplicado de email, constraint violation, etc.)
     */
    @Override
    public Account save(Account account) {
        // Validación defensiva
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        
        // 1. Domain → JPA Entity
        AccountEntity entity = mapper.toEntity(account);
        
        // 2. Persistir usando Spring Data JPA
        AccountEntity savedEntity = jpaRepository.save(entity);
        
        // 3. JPA Entity → Domain (reflejar cambios de BD)
        return mapper.toDomain(savedEntity);
    }

    /**
     * Busca una cuenta por su identificador único.
     * 
     * <p>
     * <strong>Semántica de la operación:</strong>
     * <ul>
     * <li>Retorna {@code Optional.empty()} si no se encuentra la cuenta,
     *     en lugar de lanzar excepción o retornar {@code null}</li>
     * <li>Esto sigue las mejores prácticas de Java 8+ para valores ausentes</li>
     * <li>El cliente debe decidir cómo manejar el caso "no encontrado"</li>
     * </ul>
     * 
     * <p>
     * <strong>Comportamiento de caché de Hibernate:</strong>
     * <ul>
     * <li>Si la entidad ya está en el contexto de persistencia (primer nivel de caché),
     *     se retorna sin consultar BD</li>
     * <li>El segundo nivel de caché (L2 cache) puede configurarse pero está deshabilitado
     *     por defecto</li>
     * <li>La caché de consultas (query cache) no aplica para {@code findById()}</li>
     * </ul>
     * 
     * <p>
     * <strong>Ejemplo de uso con manejo de errores:</strong>
     * <pre>
     * public class AccountQueryService {
     *     private final AccountRepository repository;
     *     
     *     public Account getAccountOrThrow(UUID id) {
     *         return repository.findById(id)
     *             .orElseThrow(() -> new AccountNotFoundException(
     *                 String.format("Account with id %s not found", id)
     *             ));
     *     }
     *     
     *     public Optional<Account> findActiveAccount(UUID id) {
     *         return repository.findById(id)
     *             .filter(Account::isActive);
     *     }
     * }
     * </pre>
     * 
     * <p>
     * <strong>⚠️ Consideraciones de rendimiento:</strong>
     * <ul>
     * <li>Esta operación es O(1) en base de datos indexada (PK index)</li>
     * <li>Tiempo típico: &lt;5ms en PostgreSQL con índices apropiados</li>
     * <li>Para consultas frecuentes, considere un caché distribuido como Redis</li>
     * </ul>
     * 
     * @param id Identificador único de la cuenta (no puede ser {@code null})
     * @return {@link Optional} que contiene la cuenta si existe, {@code Optional.empty()} si no
     * @throws IllegalArgumentException Si {@code id} es {@code null}
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
     * Busca una cuenta por su dirección de correo electrónico.
     * 
     * <p>
     * <strong>Importancia del email como identificador alternativo:</strong>
     * <ul>
     * <li>El email es único en el sistema (restricción {@code UNIQUE} en base de datos)</li>
     * <li>Se usa como username en autenticación (Spring Security)</li>
     * <li>Permite búsquedas en casos de uso como "Olvidé mi contraseña"</li>
     * </ul>
     * 
     * <p>
     * <strong>Comportamiento de búsqueda:</strong>
     * <ul>
     * <li>La consulta debe estar indexada en base de datos (índice único en email)</li>
     * <li>Retorna {@code Optional.empty()} si ningún email coincide</li>
     * <li>La comparación es case-sensitive o insensitive según la configuración de BD</li>
     * </ul>
     * 
     * <p>
     * <strong>Ejemplo de uso en autenticación:</strong>
     * <pre>
     * @Service
     * public class AuthenticationService {
     *     private final AccountRepository accountRepository;
     *     private final PasswordEncoder passwordEncoder;
     *     
     *     public LoginResult authenticate(String email, String rawPassword) {
     *         Account account = accountRepository.findByEmail(email)
     *             .orElseThrow(() -> new InvalidCredentialsException());
     *         
     *         if (!account.isActive()) {
     *             throw new AccountDeactivatedException();
     *         }
     *         
     *         // Verificar contraseña (solo si Account tuviera ese campo)
     *         // Nota: Esto está simplificado - la cuenta real tiene password hash
     *         if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
     *             throw new InvalidCredentialsException();
     *         }
     *         
     *         return LoginResult.success(account);
     *     }
     * }
     * </pre>
     * 
     * <p>
     * <strong>Consultas SQL generadas (típicas):</strong>
     * <pre>
     * -- La implementación de findByEmail en SpringDataJpaRepository genera:
     * SELECT * FROM accounts WHERE account_email = ?
     * 
     * -- Con índice:
     * CREATE UNIQUE INDEX idx_accounts_email ON accounts(account_email);
     * </pre>
     * 
     * <p>
     * <strong>⚠️ Consideraciones de seguridad:</strong>
     * <ul>
     * <li>No debe revelar si un email existe en respuestas de error genéricas
     *     (ej: "Credenciales inválidas" vs "Email no encontrado")</li>
     * <li>Proteger contra email enumeration attacks</li>
     * <li>Considerar rate limiting en endpoints que usan esta búsqueda</li>
     * </ul>
     * 
     * @param email Dirección de correo electrónico a buscar (no puede ser {@code null} o vacío)
     * @return {@link Optional} que contiene la cuenta si el email existe
     * @throws IllegalArgumentException Si {@code email} es {@code null} o está vacío
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
     * Elimina una cuenta por su identificador.
     * 
     * <p>
     * <strong>⚠️ ADVERTENCIA DE USO:</strong>
     * Esta operación realiza una eliminación FÍSICA (hard delete) de la base de datos.
     * Antes de usarla, considere si es apropiada según las reglas de negocio.
     * 
     * <p>
     * <strong>Hard Delete vs Soft Delete:</strong>
     * <table border="1">
     * <tr>
     *     <th>Tipo</th>
     *     <th>Ventajas</th>
     *     <th>Desventajas</th>
     *     <th>Cuándo usarlo</th>
     * </tr>
     * <tr>
     *     <td>Hard Delete (este método)</td>
     *     <td>Libera espacio, mejora rendimiento, no acumula datos</td>
     *     <td>Pérdida permanente de datos, difícil de auditar</td>
     *     <td>Datos temporales, cumplimiento GDPR (derecho al olvido)</td>
     * </tr>
     * <tr>
     *     <td>Soft Delete ({@code active = false})</td>
     *     <td>Datos recuperables, historial de acciones, auditoría</td>
     *     <td>Acumula datos, requiere filtrado en consultas</td>
     *     <td>La mayoría de casos de negocio, cuentas de usuario</td>
     * </tr>
     * </table>
     * 
     * <p>
     * <strong>Recomendación para el módulo Account:</strong>
     * <pre>
     * // En lugar de eliminar físicamente, usar desactivación:
     * public void deactivateAccount(UUID id) {
     *     Account account = repository.findById(id)
     *         .orElseThrow(AccountNotFoundException::new);
     *     account.deactivate();  // Domain behavior
     *     repository.save(account);
     * }
     * 
     * // Solo usar deleteById en casos excepcionales (GDPR, testing, cleanup)
     * </pre>
     * 
     * <p>
     * <strong>Efectos secundarios en cascada:</strong>
     * <ul>
     * <li>Si {@link AccountEntity} tiene {@code CascadeType.ALL} hacia otras entidades
     *     (movimientos, eventos, contraseñas), todos los datos relacionados se eliminarán</li>
     * <li>La base de datos debe tener foreign keys con {@code ON DELETE CASCADE} o
     *     Hibernate manejará las eliminaciones en el orden correcto</li>
     * <li>Esta operación es IRREVERSIBLE a menos que se tenga un backup</li>
     * </ul>
     * 
     * <p>
     * <strong>Ejemplo de uso (solo en contextos específicos):</strong>
     * <pre>
     * // Administración - Limpieza de datos de prueba
     * &#64;Scheduled(cron = "0 0 3 * * *") // 3 AM diario
     * public void cleanupTestAccounts() {
     *     List<Account> testAccounts = repository.findByEmailDomain("@test.com");
     *     testAccounts.forEach(account -> repository.deleteById(account.getId()));
     * }
     * 
     * // GDPR - Derecho al olvido
     * public void eraseUserData(UUID userId, GdprRequest request) {
     *     validateRequest(request);
     *     repository.deleteById(userId);
     *     auditLogger.log("User data erased for " + userId, request);
     * }
     * </pre>
     * 
     * <p>
     * <strong>Comportamiento transaccional:</strong>
     * <ul>
     * <li>Si el ID no existe, la operación no lanza excepción (comportamiento de Spring Data)</li>
     * <li>Para validar existencia antes de eliminar, consulte con {@code findById()} primero</li>
     * <li>En entorno transaccional, la eliminación se confirma solo al finalizar la transacción</li>
     * </ul>
     * 
     * @param id Identificador único de la cuenta a eliminar
     * @throws IllegalArgumentException Si {@code id} es {@code null}
     */
    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        jpaRepository.deleteById(id);
    }
}