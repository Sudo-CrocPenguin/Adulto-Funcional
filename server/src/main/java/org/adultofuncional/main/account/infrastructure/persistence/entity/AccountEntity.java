package org.adultofuncional.main.account.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que representa una cuenta de usuario en el sistema.
 * 
 * <p>
 * Esta es la entidad central del dominio, ya que todas las operaciones
 * del sistema están asociadas a una cuenta de usuario. Contiene la información
 * personal del usuario y sus credenciales de acceso.
 * 
 * <p>
 * <strong>Características principales:</strong>
 * <ul>
 * <li><b>Identificador único:</b> UUID v7 generado automáticamente</li>
 * <li><b>Email único:</b> Restricción {@code UNIQUE} en base de datos</li>
 * <li><b>Contraseña hasheada:</b> Longitud de 60 caracteres para Argon2</li>
 * <li><b>Master Key:</b> Clave opcional para encriptación de datos
 * sensibles</li>
 * <li><b>Fecha de creación automática:</b> Establecida en
 * {@code @PrePersist}</li>
 * <li><b>Cascada completa:</b> Al eliminar una cuenta, se eliminan todos sus
 * datos asociados</li>
 * </ul>
 * 
 * <p>
 * <strong>Relaciones:</strong>
 * <ul>
 * <li>Movimientos financieros ({@code @OneToMany} →
 * {@link MovementEntity})</li>
 * <li>Gastos fijos ({@code @OneToMany} → {@link FixedExpensesEntity})</li>
 * <li>Eventos de agenda ({@code @OneToMany} → {@link EventEntity})</li>
 * <li>Contraseñas almacenadas ({@code @OneToMany} →
 * {@link PasswordEntity})</li>
 * </ul>
 * 
 * <p>
 * <strong>⚠️ Consideraciones de seguridad:</strong>
 * <ul>
 * <li>El campo {@code account_password} almacena el hash Argon2, NUNCA texto
 * plano</li>
 * <li>El campo {@code account_master_key} almacena el hash Argon2</li>
 * <li>Esta clase NUNCA debe ser expuesta directamente en respuestas API</li>
 * </ul>
 * 
 * <p>
 * <strong>Restricciones de base de datos:</strong>
 * <ul>
 * <li>{@code account_id}: CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())</li>
 * <li>{@code account_email}: VARCHAR(255) UNIQUE NOT NULL</li>
 * <li>{@code account_password}: VARCHAR(60) NOT NULL (espacio para hash
 * Argon2)</li>
 * <li>{@code account_master_key}: VARCHAR(60) NULL (espacio para hash Argon2)
 * </li>
 * </ul>
 * 
 * <p>
 * Esta entidad es el punto de entrada principal para la autenticación
 * y autorización en el sistema. Todas las demás entidades referencian
 * una cuenta mediante su {@code account_id}.
 * 
 * @author juan
 * @since 0.0.1
 * @see MovementEntity
 * @see FixedExpensesEntity
 * @see EventEntity
 * @see PasswordEntity
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class AccountEntity {

  /**
   * Identificador único de la cuenta.
   * 
   * <p>
   * Se genera automáticamente como UUID v7 (ordenable temporalmente).
   * El estilo {@code UuidGenerator.Style.TIME} garantiza que los primeros
   * bits contengan un timestamp, lo que mejora la indexación en base de datos.
   * 
   * <p>
   * Formato: {@code CHAR(36)} en base de datos.
   */
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "account_id", columnDefinition = "CHAR(36)")
  private UUID account_id;

  /**
   * Nombres del titular de la cuenta.
   * 
   * <p>
   * Obligatorio. Máximo 50 caracteres.
   * Se almacena como {@code VARCHAR(50)}.
   */
  @Column(name = "account_names", length = 50, nullable = false)
  private String account_names;

  /**
   * Apellidos del titular de la cuenta.
   * 
   * <p>
   * Obligatorio. Máximo 50 caracteres.
   * Se almacena como {@code VARCHAR(50)}.
   */
  @Column(name = "account_lastnames", length = 50, nullable = false)
  private String account_lastnames;

  /**
   * Correo electrónico del usuario.
   * 
   * <p>
   * Obligatorio y único en todo el sistema. Se utiliza como
   * identificador para el inicio de sesión (username en Spring Security).
   * Máximo 255 caracteres.
   * 
   * <p>
   * <strong>Restricción BD:</strong> {@code UNIQUE NOT NULL}
   */
  @Column(name = "account_email", length = 255, nullable = false, unique = true)
  private String account_email;

  /**
   * Número de teléfono de contacto.
   * 
   * <p>
   * Obligatorio. Máximo 20 caracteres.
   * Puede incluir código de país y formato internacional.
   */
  @Column(name = "account_phone", length = 20, nullable = false)
  private String account_phone;

  /**
   * Contraseña hasheada del usuario.
   * 
   * <p>
   * Obligatorio. Longitud de 60 caracteres para almacenar el hash
   * generado por Argon2 (formato estándar).
   * 
   * <p>
   * <strong>⚠️ IMPORTANTE:</strong> NUNCA almacenar contraseñas en texto plano.
   * Este campo debe contener SIEMPRE un hash criptográfico.
   * 
   * <p>
   * Formato esperado: {@code $argon2id$v=19$m=4096,t=3,p=1$...}
   * 
   * @see org.springframework.security.crypto.argon2.Argon2PasswordEncoder
   */
  @Column(name = "account_password", length = 60, nullable = false)
  private String account_password;

  /**
   * Hash de la clave maestra para acceder al gestor de contraseñas.
   * 
   * <p>
   * Obligatorio. Longitud de 60 caracteres para almacenar el hash Argon2.
   * 
   * <p>
   * <strong>Propósito:</strong> Actúa como un segundo factor de autenticación
   * específico para proteger el acceso al módulo de gestor de contraseñas.
   * 
   * <p>
   * <strong>Flujo de uso:</strong>
   * <ol>
   * <li>El usuario inicia sesión normalmente con email + contraseña</li>
   * <li>Al intentar acceder al gestor de contraseñas, se solicita la Master
   * Key</li>
   * <li>Se verifica con Argon2:
   * {@code argon2.matches(providedKey, this.account_master_key)}</li>
   * <li>Si es correcta, se concede acceso al gestor de contraseñas</li>
   * </ol>
   * 
   * <p>
   * <strong>⚠️ IMPORTANTE:</strong>
   * <ul>
   * <li>Este campo almacena el <strong>HASH</strong> de la Master Key, NUNCA el
   * valor original</li>
   * <li>Si el usuario olvida su Master Key, pierde acceso a sus contraseñas
   * almacenadas o tendrá que verificar su identidad para la nueva contraseña</li>
   * <li>La Master Key es independiente de la contraseña de login</li>
   * </ul>
   * 
   * <p>
   * Las contraseñas almacenadas en {@link PasswordEntity} están encriptadas
   * por AES-256.
   * 
   * @see PasswordEntity
   */
  @Column(name = "account_master_key", length = 60)
  private String account_master_key;

  /**
   * Fecha y hora de creación de la cuenta.
   * 
   * <p>
   * Se establece automáticamente en {@link #onCreate()} antes de
   * persistir la entidad por primera vez. El campo está marcado como
   * {@code updatable = false}, por lo que nunca se modificará después
   * de la inserción inicial.
   * 
   * <p>
   * Útil para auditoría y reportes de crecimiento de usuarios.
   */
  @Column(name = "account_created_at", updatable = false)
  private LocalDateTime account_created_at;

  /**
   * Lista de movimientos financieros asociados a esta cuenta.
   * 
   * <p>
   * Relación {@code @OneToMany} bidireccional con {@link MovementEntity}.
   * La eliminación de una cuenta eliminará en cascada todos sus movimientos
   * ({@code CascadeType.ALL} + {@code orphanRemoval = true}).
   * 
   * <p>
   * Se inicializa como {@code ArrayList} vacía para evitar
   * {@link NullPointerException} al agregar elementos.
   */
  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MovementEntity> movements = new ArrayList<>();

  /**
   * Lista de gastos fijos asociados a esta cuenta.
   * 
   * <p>
   * Relación {@code @OneToMany} bidireccional con {@link FixedExpensesEntity}.
   * La eliminación de una cuenta eliminará en cascada todos sus gastos fijos.
   * 
   * <p>
   * Se inicializa como {@code ArrayList} vacía para evitar
   * {@link NullPointerException} al agregar elementos.
   */
  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FixedExpensesEntity> fixed_expenses = new ArrayList<>();

  /**
   * Lista de eventos de agenda asociados a esta cuenta.
   * 
   * <p>
   * Relación {@code @OneToMany} bidireccional con {@link EventEntity}.
   * La eliminación de una cuenta eliminará en cascada todos sus eventos.
   * 
   * <p>
   * Se inicializa como {@code ArrayList} vacía para evitar
   * {@link NullPointerException} al agregar elementos.
   */
  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EventEntity> events = new ArrayList<>();

  /**
   * Lista de contraseñas almacenadas asociadas a esta cuenta.
   * 
   * <p>
   * Relación {@code @OneToMany} bidireccional con {@link PasswordEntity}.
   * La eliminación de una cuenta eliminará en cascada todas sus contraseñas
   * almacenadas.
   * 
   * <p>
   * Se inicializa como {@code ArrayList} vacía para evitar
   * {@link NullPointerException} al agregar elementos.
   */
  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PasswordEntity> passwords = new ArrayList<>();

  /**
   * Callback de ciclo de vida JPA ejecutado antes de persistir la entidad.
   * 
   * <p>
   * Establece automáticamente la fecha y hora de creación de la cuenta
   * al momento de ser guardada por primera vez en la base de datos.
   * 
   * <p>
   * Este método es invocado automáticamente por el proveedor JPA
   * (Hibernate) durante la operación {@code persist()}.
   */
  @PrePersist
  public void onCreate() {
    account_created_at = LocalDateTime.now();
  }
}
