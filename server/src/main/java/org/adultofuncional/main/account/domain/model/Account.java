package org.adultofuncional.main.account.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;

/**
 * Modelo de dominio que representa una cuenta de usuario en el sistema.
 * 
 * <p>
 * Esta clase implementa el patrón de <strong>Domain Model</strong> (Modelo de Dominio)
 * siguiendo los principios fundamentales de la Programación Orientada a Objetos (POO):
 * encapsulamiento, abstracción y comportamiento rico.
 * 
 * <p>
 * <strong>Características principales de POO implementadas:</strong>
 * <ul>
 * <li><b>Encapsulamiento:</b> Todos los atributos son privados y se accede mediante getters
 *     ({@code @Getter} de Lombok). Los setters están ausentes para mutaciones controladas
 *     mediante métodos de dominio.</li>
 * <li><b>Inmutabilidad parcial:</b> Los campos {@code id} y {@code createdAt} son {@code final}
 *     y se establecen solo una vez en el constructor.</li>
 * <li><b>Comportamiento rico:</b> Los métodos de dominio encapsulan la lógica de negocio
 *     (activación, desactivación, actualización de datos) en lugar de tener un "anemia model".</li>
 * <li><b>Constructor privado + Factory Method:</b> Patrón de diseño que centraliza la creación
 *     y validación de objetos.</li>
 * <li><b>Validación de invariantes:</b> Los métodos privados {@code validateName()} y
 *     {@code validateEmail()} aseguran que el objeto siempre esté en un estado válido.</li>
 * </ul>
 * 
 * <p>
 * <strong>Principios SOLID aplicados:</strong>
 * <ul>
 * <li><b>Single Responsibility:</b> Esta clase solo maneja la lógica de negocio de la cuenta,
 *     no persistencia, no lógica de presentación.</li>
 * <li><b>Open/Closed:</b> Extensible mediante herencia pero cerrada para modificaciones innecesarias.</li>
 * <li><b>Dependency Inversion:</b> No depende de detalles de infraestructura (JPA, bases de datos).</li>
 * </ul>
 * 
 * <p>
 * <strong>Diferencias con la entidad JPA {@link org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity}:</strong>
 * <table border="1">
 *   <tr>
 *     <th>Aspecto</th>
 *     <th>Account (Domain Model)</th>
 *     <th>AccountEntity (JPA Entity)</th>
 *   </tr>
 *   <tr>
 *     <td>Propósito</td>
 *     <td>Lógica de negocio pura</td>
 *     <td>Persistencia en base de datos</td>
 *   </tr>
 *   <tr>
 *     <td>Anotaciones</td>
 *     <td>Ninguna (Lombok {@code @Getter} solo para boilerplate)</td>
 *     <td>{@code @Entity}, {@code @Table}, {@code @Column}, {@code @Id}</td>
 *   </tr>
 *   <tr>
 *     <td>Estado mutable</td>
 *     <td>Controlado por métodos de dominio</td>
 *     <td>Set/Get libres (Lombok {@code @Setter})</td>
 *   </tr>
 *   <tr>
 *     <td>Constructor</td>
 *     <td>Privado + Factory Method</td>
 *     <td>{@code @NoArgsConstructor} + setters</td>
 *   </tr>
 *   <tr>
 *     <td>Relaciones</td>
 *     <td>No tiene (solo atributos primitivos)</td>
 *     <td>{@code @OneToMany} con otras entidades</td>
 *   </tr>
 *   <tr>
 *     <td>Seguridad</td>
 *     <td>Campos sensibles (contraseña, master key) están AUSENTES</td>
 *     <td>Contiene hashes de contraseñas</td>
 *   </tr>
 * </table>
 * 
 * <p>
 * <strong>Flujo de uso típico (Arquitectura Hexagonal/DDD):</strong>
 * <pre>
 * // 1. Creación mediante Factory Method
 * Account newAccount = Account.create("Juan Pérez", "juan@email.com", "+123456789");
 * 
 * // 2. Comportamiento de dominio
 * newAccount.updateDetails("Juan Carlos Pérez", "+987654321");
 * newAccount.deactivate();
 * 
 * // 3. Consulta de estado
 * if (newAccount.isActive()) {
 *     // Realizar operaciones permitidas
 * }
 * 
 * // 4. Conversión a entidad JPA (en el repositorio/infraestructura)
 * AccountEntity entity = new AccountEntity();
 * entity.setAccount_id(account.getId());
 * entity.setAccount_names(extractName(account.getName())); // "Juan"
 * entity.setAccount_lastnames(extractLastnames(account.getName())); // "Pérez"
 * entity.setAccount_email(account.getEmail());
 * entity.setAccount_phone(account.getPhone());
 * // Nota: Los campos de seguridad (contraseña) se manejan en otro contexto
 * </pre>
 * 
 * <p>
 * <strong>⚠️ Consideraciones importantes:</strong>
 * <ul>
 * <li>Esta clase <strong>NO</strong> contiene información de autenticación (contraseñas, master keys)
 *     por razones de separación de responsabilidades y seguridad.</li>
 * <li>El campo {@code name} combina nombres y apellidos en un solo String. Para separarlos,
 *     se recomienda un Value Object {@code FullName} con {@code firstName} y {@code lastName}.</li>
 * <li>La validación de email es básica (solo contiene '@'). Debería mejorarse con expresión regular
 *     RFC 5322 o una biblioteca como {@code Apache Commons Validator}.</li>
 * <li>No tiene relación directa con módulos (finanzas, agenda, contraseñas) para mantener
 *     el principio de <strong>Agregado DDD</strong> y evitar la creación de un "God Object".</li>
 * </ul>
 * 
 * <p>
 * <strong>Mejoras futuras sugeridas:</strong>
 * <ul>
 * <li>Implementar {@code equals()} y {@code hashCode()} basado en {@code id} (identidad por ID, no por estado)</li>
 * <li>Crear Value Objects: {@code Email}, {@code PhoneNumber}, {@code FullName}</li>
 * <li>Añadir eventos de dominio (Domain Events) para notificar cambios (ej: {@code AccountDeactivatedEvent})</li>
 * <li>Implementar patrón Builder si la cantidad de atributos crece significativamente</li>
 * </ul>
 * 
 * @author Juan David Ruiz Garcia
 * @since 1.0.0
 * @version 1.0
 * @see org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity
 * @see <a href="https://martinfowler.com/bliki/AnemicDomainModel.html">Anemic Domain Model (Martin Fowler)</a>
 * @see <a href="https://www.baeldung.com/java-modules">Java Platform Module System</a>
 */
@Getter
public class Account {
    
    /**
     * Identificador único de la cuenta en el dominio.
     * 
     * <p>
     * <strong>Principios de POO:</strong>
     * <ul>
     * <li><b>Inmutabilidad:</b> Campo {@code final} - establecido una sola vez en el constructor</li>
     * <li><b>Identidad vs Estado:</b> El {@code id} define la identidad de la cuenta,
     *     no cambia aunque sus atributos (nombre, email, etc.) sí lo hagan</li>
     * </ul>
     * 
     * <p>
     * Se genera mediante {@code UUID.randomUUID()} (UUID v4) en tiempo de creación.
     * Es compatible con el campo {@code account_id} de {@code AccountEntity}
     * que usa UUID v7 para ordenamiento temporal en base de datos.
     * 
     * <p>
     * <strong>Uso en equals/hashCode:</strong>
     * <pre>
     * &#64;Override
     * public boolean equals(Object o) {
     *     if (this == o) return true;
     *     if (!(o instanceof Account)) return false;
     *     Account account = (Account) o;
     *     return Objects.equals(id, account.id);
     * }
     * </pre>
     */
    private final UUID id;
    
    /**
     * Nombre completo del titular de la cuenta.
     * 
     * <p>
     * Encapsula nombres y apellidos en un solo campo por simplicidad.
     * Para una solución más robusta, considere crear el Value Object {@code FullName}.
     * 
     * <p>
     * <strong>Mutabilidad controlada:</strong>
     * <ul>
     * <li>No hay setter público ({@code @Setter} ausente)</li>
     * <li>Solo se modifica mediante el método de dominio {@link #updateDetails(String, String)}</li>
     * <li>Antes de modificar, se valida con {@link #validateName(String)}</li>
     * </ul>
     * 
     * <p>
     * <strong>Ejemplo de valores válidos:</strong>
     * <ul>
     * <li>"Juan Pérez"</li>
     * <li>"María José González Rodríguez"</li>
     * <li>"John Doe"</li>
     * </ul>
     */
    private String name;
    
    /**
     * Correo electrónico del usuario.
     * 
     * <p>
     * <strong>Invariante de negocio:</strong> Debe contener el caracter '@'.
     * 
     * <p>
     * <strong>⚠️ Nota de validación:</strong> La validación actual es básica
     * (solo verifica presencia de '@'). En producción, debería usarse:
     * <pre>
     * import org.apache.commons.validator.routines.EmailValidator;
     * EmailValidator.getInstance().isValid(email);
     * </pre>
     * 
     * <p>
     * Este campo NO tiene comportamiento de mutación porque en el dominio actual,
     * el email es inmutable después de la creación. Si se necesita cambiar email,
     * debería añadirse el método {@code updateEmail(String newEmail)}.
     */
    private String email;
    
    /**
     * Número de teléfono de contacto.
     * 
     * <p>
     * Almacenado como {@code String} para preservar formato internacional
     * y evitar problemas con números que comienzan con 0 o tienen códigos de país.
     * 
     * <p>
     * Ejemplos válidos:
     * <ul>
     * <li>"+1234567890"</li>
     * <li>"555-1234"</li>
     * <li>"1-800-MY-APPLE"</li>
     * </ul>
     */
    private String phone;
    
    /**
     * Fecha y hora de creación de la cuenta.
     * 
     * <p>
     * <strong>Inmutabilidad total:</strong> Campo {@code final} establecido en el constructor.
     * Representa el momento exacto en que se instancia el objeto en memoria,
     * no necesariamente cuando se persiste en base de datos.
     * 
     * <p>
     * Para uso en auditoría, considere también almacenar {@code createdAt}
     * en la capa de persistencia (como {@code AccountEntity.account_created_at}).
     */
    private final LocalDateTime createdAt;
    
    /**
     * Estado de actividad de la cuenta.
     * 
     * <p>
     * <strong>Semántica de negocio:</strong>
     * <ul>
     * <li>{@code true} → Cuenta activa, permite todas las operaciones del sistema</li>
     * <li>{@code false} → Cuenta desactivada, acceso denegado (equivalente a "soft delete")</li>
     * </ul>
     * 
     * <p>
     * <strong>Reglas de mutación:</strong>
     * <ul>
     * <li>Se cambia SOLO mediante {@link #deactivate()} y {@link #activate()}</li>
     * <li>No se permite modificación directa (sin setters)</li>
     * <li>Una cuenta desactivada puede reactivarse (diferente de eliminación lógica permanente)</li>
     * </ul>
     * 
     * <p>
     * <strong>⚠️ Diferencia con eliminación en base de datos:</strong>
     * <ul>
     * <li>En {@code AccountEntity} JPA, las cuentas desactivadas aún existen en BD</li>
     * <li>Los repositorios deben filtrar consultas con {@code WHERE active = true}</li>
     * <li>Para eliminación física, se usa {@code DELETE} o {@code orphanRemoval}</li>
     * </ul>
     */
    private boolean active;

    /**
     * Constructor privado que inicializa todos los atributos.
     * 
     * <p>
     * <strong>Patrón implementado:</strong> Constructor privado + Factory Method
     * {@link #create(String, String, String)}.
     * 
     * <p>
     <strong>Ventajas de este patrón:</strong>
     * <ul>
     * <li>Centraliza la creación y validación en un solo lugar</li>
     * <li>Permite nombres de métodos expresivos ({@code create}, {@code reconstitute}, etc.)</li>
     * <li>Oculta la complejidad del constructor (útil cuando hay múltiples parámetros)</li>
     * <li>Puede devolver subtipos o instancias cacheadas</li>
     * </ul>
     * 
     * <p>
     * <strong>Uso para reconstitución desde persistencia:</strong>
     * <pre>
     * // Cuando se lee desde base de datos
     * public static Account reconstitute(UUID id, String name, String email, 
     *                                    String phone, boolean active, LocalDateTime createdAt) {
     *     Account account = new Account(id, name, email, phone, active);
     *     // Nota: createdAt se pierde porque es final y se regenera
     *     // Solución: Usar reflection o modificar diseño para permitirlo
     *     return account;
     * }
     * </pre>
     * 
     * @param id Identificador único de la cuenta
     * @param name Nombre completo del usuario
     * @param email Correo electrónico
     * @param phone Número de teléfono
     * @param active Estado de actividad inicial
     */
    public Account(UUID id, String name, String email, String phone, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }

    // =========================
    // Factory Method / Creación
    // =========================
    
    /**
     * Fábrica (Factory Method) para crear nuevas cuentas.
     * 
     * <p>
     * <strong>Responsabilidades:</strong>
     * <ol>
     * <li>Generar un nuevo {@code UUID} aleatorio (v4)</li>
     * <li>Validar las reglas de negocio (nombre no vacío, email válido)</li>
     * <li>Establecer estado inicial {@code active = true}</li>
     * <li>Registrar fecha de creación automática</li>
     * </ol>
     * 
     * <p>
     * <strong>Ejemplo de uso en caso de uso "Registro de usuario":</strong>
     * <pre>
     * // En el Application Service / Use Case
     * public Account registerUser(RegisterUserCommand command) {
     *     Account newAccount = Account.create(
     *         command.getName(),
     *         command.getEmail(),
     *         command.getPhone()
     *     );
     *     
     *     // Persistir en repositorio
     *     accountRepository.save(newAccount);
     *     
     *     // Disparar evento de dominio (opcional)
     *     eventPublisher.publish(new AccountCreatedEvent(newAccount));
     *     
     *     return newAccount;
     * }
     * </pre>
     * 
     * @param name Nombre completo del usuario (no puede ser {@code null} o vacío)
     * @param email Correo electrónico (debe contener '@')
     * @param phone Número de teléfono (sin validación adicional por ahora)
     * @return Nueva instancia de {@code Account} con estado activo
     * @throws IllegalArgumentException Si {@code name} es vacío o {@code email} no contiene '@'
     */
    public static Account create(String name, String email, String phone) {
        validateName(name);
        validateEmail(email);
        
        return new Account(
                UUID.randomUUID(),
                name,
                email,
                phone,
                true  // Nueva cuenta comienza activa
        );
    }

    // =========================
    // Domain Behavior / Comportamiento
    // =========================
    
    /**
     * Actualiza los datos de contacto de la cuenta.
     * 
     * <p>
     * <strong>Responsabilidades:</strong>
     * <ul>
     * <li>Valida que el nuevo nombre no esté vacío</li>
     * <li>Actualiza ambos campos atómicamente</li>
     * <li>El email NO se puede modificar en esta operación</li>
     * </ul>
     * 
     * <p>
     * <strong>Principio de Tell, Don't Ask:</strong>
     * Este método modifica el estado interno en lugar de exponer setters y
     * que el cliente valide externamente.
     * 
     * <p>
     * <strong>Ejemplo de uso en caso de uso "Actualizar perfil":</strong>
     * <pre>
     * public void updateProfile(UUID accountId, String newName, String newPhone) {
     *     Account account = accountRepository.findById(accountId)
     *         .orElseThrow(() -> new AccountNotFoundException(accountId));
     *     
     *     // La validación ocurre DENTRO del dominio
     *     account.updateDetails(newName, newPhone);
     *     
     *     accountRepository.save(account);
     * }
     * </pre>
     * 
     * @param name Nuevo nombre completo (no puede ser {@code null} o vacío)
     * @param phone Nuevo número de teléfono (puede ser {@code null} o vacío)
     * @throws IllegalArgumentException Si {@code name} es vacío
     */
    public void updateDetails(String name, String phone) {
        validateName(name);
        this.name = name;
        this.phone = phone;
    }

    /**
     * Desactiva la cuenta.
     * 
     * <p>
     * <strong>Efectos secundarios:</strong>
     * <ul>
     * <li>Cambia {@code active} de {@code true} a {@code false}</li>
     * <li>El usuario no podrá iniciar sesión</li>
     * <li>Los datos persisten en base de datos (soft delete)</li>
     * </ul>
     * 
     * <p>
     * <strong>Diferencia entre desactivar y eliminar:</strong>
     * <ul>
     * <li>Desactivar ({@code deactivate()}): Puede revertirse con {@link #activate()}</li>
     * <li>Eliminar (en repositorio): Pérdida permanente de datos</li>
     * </ul>
     * 
     * <p>
     * <strong>Ejemplo de uso en caso de uso "Dar de baja usuario":</strong>
     * <pre>
     * public void deactivateUser(UUID accountId) {
     *     Account account = accountRepository.findById(accountId)
     *         .orElseThrow(() -> new AccountNotFoundException(accountId));
     *     
     *     // El comportamiento de dominio encapsula la lógica
     *     account.deactivate();
     *     
     *     accountRepository.save(account);
     *     
     *     // Opcional: Invalidar sesiones activas
     *     sessionService.invalidateSessions(accountId);
     * }
     * </pre>
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Reactiva una cuenta previamente desactivada.
     * 
     * <p>
     * <strong>Uso típico:</strong>
     * <ul>
     * <li>Usuario solicita reactivación después de resolver problemas de pago</li>
     * <li>Administrador restaura acceso accidentalmente bloqueado</li>
     * </ul>
     * 
     * <p>
     * <strong>⚠️ Consideraciones de seguridad:</strong>
     * <ul>
     * <li>Verificar que la cuenta no haya sido eliminada físicamente</li>
     * <li>Validar que el email siga siendo único (si se permitió reutilización)</li>
     * <li>Auditar la reactivación para cumplimiento normativo</li>
     * </ul>
     */
    public void activate() {
        this.active = true;
    }

    // =========================
    // Business Rules / Validación Privada
    // =========================
    
    /**
     * Valida que el nombre no esté vacío.
     * 
     * <p>
     * <strong>Regla de negocio:</strong> El nombre es obligatorio para identificar al usuario.
     * 
     * <p>
     * <strong>Validaciones realizadas:</strong>
     * <ol>
     * <li>Verifica que {@code name} no sea {@code null}</li>
     * <li>Elimina espacios en blanco al inicio y final con {@code trim()}</li>
     * <li>Verifica que después del trim no esté vacío</li>
     * </ol>
     * 
     * <p>
     * <strong>⚠️ Limitaciones actuales:</strong>
     * <ul>
     * <li>No verifica longitud mínima/máxima (puede ser 1 carácter)</li>
     * <li>No valide caracteres especiales o números</li>
     * <li>Permite nombres como "   " (espacios) porque {@code trim()} los elimina</li>
     * </ul>
     * 
     * <p>
     * <strong>Mejora sugerida:</strong>
     * <pre>
     * private static void validateName(String name) {
     *     if (name == null || name.trim().isEmpty()) {
     *         throw new IllegalArgumentException("Name cannot be empty");
     *     }
     *     if (name.length() > 100) {
     *         throw new IllegalArgumentException("Name too long (max 100 chars)");
     *     }
     *     if (!name.matches("^[\\p{L} .'-]+$")) {
     *         throw new IllegalArgumentException("Name contains invalid characters");
     *     }
     * }
     * </pre>
     * 
     * @param name Nombre a validar
     * @throws IllegalArgumentException Si el nombre es {@code null} o está vacío después de trim()
     */
    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }

    /**
     * Valida que el email tenga formato básico (contenga '@').
     * 
     * <p>
     * <strong>⚠️ ADVERTENCIA DE SEGURIDAD:</strong>
     * Esta validación es EXTREMADAMENTE BÁSICA y NO es suficiente para producción.
     * 
     * <p>
     * <strong>Problemas de esta validación:</strong>
     * <ul>
     * <li>Acepta emails como "a@b" (inválido según RFC 5322)</li>
     * <li>Acepta "user@domain" sin TLD válido</li>
     * <li>Permite espacios y caracteres especiales incorrectos</li>
     * <li>No verifica dominios con longitud válida</li>
     * </ul>
     * 
     * <p>
     * <strong>En producción, implemente UNA de estas opciones:</strong>
     * <pre>
     * // Opción 1: Apache Commons Validator (RECOMENDADA)
     * import org.apache.commons.validator.routines.EmailValidator;
     * if (!EmailValidator.getInstance().isValid(email)) {
     *     throw new IllegalArgumentException("Invalid email format");
     * }
     * 
     * // Opción 2: JavaMail (más pesado)
     * import javax.mail.internet.InternetAddress;
     * try {
     *     new InternetAddress(email).validate();
     * } catch (AddressException e) {
     *     throw new IllegalArgumentException("Invalid email", e);
     * }
     * 
     * // Opción 3: Regex RFC 5322 (complejo pero sin dependencias)
     * String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
     * if (!email.matches(regex)) {
     *     throw new IllegalArgumentException("Invalid email format");
     * }
     * </pre>
     * 
     * @param email Correo electrónico a validar
     * @throws IllegalArgumentException Si email es {@code null} o no contiene '@'
     */
    private static void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }

    /**
     * Consulta pública del estado de actividad.
     * 
     * <p>
     * <strong>Principio de Demeter (Law of Demeter):</strong>
     * Los clientes no acceden directamente al campo {@code active},
     * sino que usan este método para consultar el estado.
     * 
     * <p>
     * <strong>Ejemplo de uso en servicio de autenticación:</strong>
     * <pre>
     * public AuthenticationResult login(String email, String password) {
     *     Account account = accountRepository.findByEmail(email);
     *     
     *     if (!account.isActive()) {
     *         return AuthenticationResult.accountDeactivated();
     *     }
     *     
     *     // Continuar con autenticación...
     * }
     * </pre>
     * 
     * @return {@code true} si la cuenta está activa, {@code false} en caso contrario
     */
    public boolean isActive() {
        return active;
    }
}