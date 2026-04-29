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
 *     y se establecen solo una vez en el constructor privado.</li>
 * <li><b>Comportamiento rico:</b> Los métodos de dominio encapsulan la lógica de negocio
 *     (activación, desactivación, actualización de datos) en lugar de tener un "anemia model".</li>
 * <li><b>Constructor privado + Factory Methods:</b> Patrón que centraliza la creación (nuevas cuentas)
 *     y la reconstitución (cuentas existentes desde persistencia).</li>
 * <li><b>Validación de invariantes:</b> Los métodos privados {@code validateName()},
 *     {@code validateEmail()} y la validación de {@code createdAt} en el constructor aseguran
 *     que el objeto siempre esté en un estado válido.</li>
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
 *    <tr>
 *     <th>Aspecto</th>
 *     <th>Account (Domain Model)</th>
 *     <th>AccountEntity (JPA Entity)</th>
 *    </tr>
 *    <tr>
 *     <td>Propósito</td>
 *     <td>Lógica de negocio pura</td>
 *     <td>Persistencia en base de datos</td>
 *    </tr>
 *    <tr>
 *     <td>Anotaciones</td>
 *     <td>Ninguna (Lombok {@code @Getter} solo para boilerplate)</td>
 *     <td>{@code @Entity}, {@code @Table}, {@code @Column}, {@code @Id}</td>
 *    </tr>
 *    <tr>
 *     <td>Estado mutable</td>
 *     <td>Controlado por métodos de dominio</td>
 *     <td>Set/Get libres (Lombok {@code @Setter})</td>
 *    </tr>
 *    <tr>
 *     <td>Constructor</td>
 *     <td>Privado + Factory Methods ({@code create}, {@code reconstitute})</td>
 *     <td>{@code @NoArgsConstructor} + setters</td>
 *    </tr>
 *    <tr>
 *     <td>Relaciones</td>
 *     <td>No tiene (solo atributos primitivos)</td>
 *     <td>{@code @OneToMany} con otras entidades</td>
 *    </tr>
 *    <tr>
 *     <td>Seguridad</td>
 *     <td>Campos sensibles (contraseña, master key) están AUSENTES</td>
 *     <td>Contiene hashes de contraseñas</td>
 *    </tr>
 *  </table>
 * 
 * <p>
 * <strong>Flujo de uso típico (Arquitectura Hexagonal/DDD):</strong>
 * <pre>
 * // 1. Creación de NUEVA cuenta mediante Factory Method
 * Account newAccount = Account.create("Juan Pérez", "juan@email.com", "+123456789");
 * 
 * // 2. Reconstitución desde base de datos
 * Account existingAccount = Account.reconstitute(
 *     uuidFromDb, "Juan Pérez", "juan@email.com", "+123456789", 
 *     true, createdAtFromDb
 * );
 * 
 * // 3. Comportamiento de dominio
 * existingAccount.updateDetails("Juan Carlos Pérez", "+987654321");
 * existingAccount.deactivate();
 * 
 * // 4. Consulta de estado
 * if (existingAccount.isActive()) {
 *     // Realizar operaciones permitidas
 * }
 * 
 * // 5. Conversión a entidad JPA (en el repositorio/infraestructura)
 * AccountEntity entity = new AccountEntity();
 * entity.setAccount_id(account.getId());
 * entity.setAccount_names(extractName(account.getName()));
 * entity.setAccount_lastnames(extractLastnames(account.getName()));
 * entity.setAccount_email(account.getEmail());
 * entity.setAccount_phone(account.getPhone());
 * entity.setAccount_created_at(account.getCreatedAt());
 * entity.setAccount_active(account.isActive());
 * </pre>
 * 
 * <p>
 * <strong> Consideraciones importantes:</strong>
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
 * @version 1.1
 * @see org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity
 * @see <a href="https://martinfowler.com/bliki/AnemicDomainModel.html">Anemic Domain Model (Martin Fowler)</a>
 */
@Getter
public class Account {
    
    private final UUID id;
    private String name;
    private final String email;      // Inmutable después de creación
    private String phone;
    private final LocalDateTime createdAt;
    private boolean active;

     /*
     Comparación por identidad (DDD correcto)
      Evita bugs en listas, sets, caché */
    @Override
    public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Account)) return false;
    Account account = (Account) o;
    return id != null && id.equals(account.id);
}

@Override
public int hashCode() {
    return id != null ? id.hashCode() : 0;
}
    /**
     * Constructor privado – el único punto de instanciación.
     * 
     * <p><strong>Invariantes validadas:</strong>
     * <ul>
     *   <li>Nombre válido (no nulo ni vacío)</li>
     *   <li>Email válido (contiene '@')</li>
     *   <li>Fecha de creación no puede ser futura (máximo el momento actual)</li>
     * </ul>
     * 
     * @param id        Identificador único de la cuenta
     * @param name      Nombre completo del titular
     * @param email     Correo electrónico
     * @param phone     Número de teléfono
     * @param active    Estado de actividad
     * @param createdAt Fecha y hora de creación (no puede ser futura)
     * @throws IllegalArgumentException si alguna validación falla
     */
    private Account(UUID id, String name, String email, String phone, boolean active, LocalDateTime createdAt) {
        validateName(name);
        validateEmail(email);
        validateCreatedAt(createdAt);
        
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.active = active;
        this.createdAt = createdAt;
    }

    // =========================
    // Factory Methods (Creación y Reconstitución)
    // =========================
    
    /**
     * Fábrica para <strong>nuevas cuentas</strong> (registro de usuario).
     * 
     * <p>Genera un nuevo {@code UUID} y establece la fecha de creación actual.
     * La cuenta se crea en estado activo.
     * 
     * @param name  Nombre completo (no vacío)
     * @param email Correo electrónico (debe contener '@')
     * @param phone Teléfono de contacto
     * @return Nueva instancia de {@code Account} lista para persistir
     * @throws IllegalArgumentException si nombre o email son inválidos
     */
    public static Account create(String name, String email, String phone) {
        return new Account(
            UUID.randomUUID(),
            name,
            email,
            phone,
            true,                     // Nueva cuenta activa
            LocalDateTime.now()
        );
    }
    
    /**
     * Fábrica para <strong>reconstituir cuentas existentes</strong> desde la persistencia.
     * 
     * <p>Este método respeta la fecha de creación original y el identificador
     * almacenado en la base de datos.
     * 
     * @param id        Identificador único (tal como está en BD)
     * @param name      Nombre completo
     * @param email     Correo electrónico
     * @param phone     Teléfono
     * @param active    Estado de actividad
     * @param createdAt Fecha y hora de creación original
     * @return Instancia de {@code Account} reconstituida
     * @throws IllegalArgumentException si algún dato es inválido (incluyendo {@code createdAt} futura)
     */
    public static Account reconstitute(UUID id, String name, String email, String phone, boolean active, LocalDateTime createdAt) {
        return new Account(id, name, email, phone, active, createdAt);
    }

    // =========================
    // Domain Behavior / Comportamiento
    // =========================
    
    public void updateDetails(String name, String phone) {
        validateName(name);
        this.name = name;
        this.phone = phone;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
    
    public boolean isActive() {
        return active;
    }

    // =========================
    // Business Rules / Validación Privada
    // =========================
    
    private static void validateName(String name) {
        // TODO: Mejorar validación de email con expresión RFC 5322 o Apache Commons Validator.
        //       La validación actual solo verifica que contenga '@', lo cual es insuficiente para producción.
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }
    
    private static void validateEmail(String email) {
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
        throw new IllegalArgumentException("Invalid email");
        }
    }
    
    /**
     * Valida que la fecha de creación no sea futura.
     * 
     * <p><strong>Invariante temporal:</strong> Una cuenta no puede haber sido creada
     * en un momento que aún no ha ocurrido (protege contra errores en la capa de persistencia
     * o relojes desincronizados).
     * 
     * @param createdAt fecha a validar
     * @throws IllegalArgumentException si {@code createdAt} es posterior al momento actual
     */
    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt != null && createdAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("CreatedAt cannot be in the future");
        }
    }
}