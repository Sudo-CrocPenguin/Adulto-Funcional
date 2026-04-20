package org.adultofuncional.main.security.infrastructure.persistence.entity;

import java.time.LocalDate;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que representa una contraseña almacenada en el gestor de
 * contraseñas.
 * 
 * <p>
 * Esta entidad forma parte del módulo de seguridad y permite al usuario
 * almacenar credenciales de acceso a diferentes servicios y aplicaciones.
 * 
 * <p>
 * <strong>Características principales:</strong>
 * <ul>
 * <li><b>Identificador único:</b> UUID v7 generado automáticamente</li>
 * <li><b>Nombre de aplicación:</b> Identifica el servicio (Gmail, Netflix,
 * etc.)</li>
 * <li><b>Contraseña encriptada:</b> Protegida con AES-256 usando la Master
 * Key</li>
 * <li><b>Fecha de último cambio:</b> para seguimiento de
 * rotación</li>
 * </ul>
 * 
 * <p>
 * <strong>🔐 Seguridad:</strong>
 * <br>
 * La contraseña se almacena <strong>encriptada con AES-256</strong> usando
 * la Master Key del usuario. Esto garantiza que solo el usuario que conoce
 * su Master Key pueda desencriptar y ver sus contraseñas originales.
 * 
 * <p>
 * <strong>Acceso al gestor de contraseñas:</strong>
 * <br>
 * El acceso a este módulo está protegido por la Master Key del usuario
 * (ver {@link AccountEntity#getAccount_master_key()}). El usuario debe
 * proporcionar su Master Key para desbloquear el gestor y poder desencriptar
 * las contraseñas.
 * 
 * <p>
 * <strong>Restricciones de base de datos:</strong>
 * <ul>
 * <li>{@code password_id}: CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())</li>
 * <li>{@code password_application_name}: VARCHAR(35) NOT NULL</li>
 * <li>{@code password_application}: TEXT NOT NULL (Base64 del encriptado)</li>
 * <li>{@code password_last_change_date}: DATE NULL</li>
 * </ul>
 * 
 * <p>
 * <strong>Relaciones:</strong>
 * <ul>
 * <li>{@code @ManyToOne} → {@link AccountEntity} (obligatorio)</li>
 * </ul>
 * 
 * @author juan
 * @since 0.0.1
 * @see AccountEntity
 */
@Entity
@Table(name = "passwords")
@Getter
@Setter
@NoArgsConstructor
public class PasswordEntity {

  /**
   * Identificador único de la contraseña almacenada.
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
  @Column(name = "password_id", columnDefinition = "CHAR(36)")
  private UUID password_id;

  /**
   * Nombre del servicio o aplicación al que pertenece la contraseña.
   * 
   * <p>
   * Obligatorio. Máximo 35 caracteres.
   * 
   * <p>
   * <strong>Ejemplos:</strong>
   * <ul>
   * <li>"Gmail" - Correo electrónico</li>
   * <li>"Netflix" - Streaming</li>
   * <li>"GitHub" - Desarrollo</li>
   * <li>"Banco XYZ" - Banca en línea</li>
   * <li>"Instagram" - Red social</li>
   * </ul>
   */
  @Column(name = "password_application_name", length = 35, nullable = false)
  private String password_application_name;

  /**
   * Contraseña encriptada del servicio.
   * 
   * <p>
   * Obligatorio. Almacenada como {@code TEXT} en base de datos para soportar
   * el resultado de la encriptación AES-256 codificado en Base64.
   * 
   * <p>
   * <strong>🔐 Encriptación:</strong>
   * <ul>
   * <li><b>Algoritmo:</b> AES-256 (simétrico, reversible)</li>
   * <li><b>Clave:</b> Master Key del usuario (proporcionada al desbloquear)</li>
   * <li><b>Formato:</b> Base64 para almacenamiento seguro en BD</li>
   * </ul>
   * 
   * <p>
   * <strong>Flujo de uso:</strong>
   * <ol>
   * <li>Usuario guarda contraseña en texto plano</li>
   * <li>Sistema encripta con AES-256 + Master Key</li>
   * <li>Se almacena el resultado Base64 en este campo</li>
   * <li>Al consultar, se desencripta con la misma Master Key</li>
   * </ol>
   * 
   * <p>
   * <strong>⚠️ Importante:</strong> Sin la Master Key correcta, los datos
   * son irrecuperables.
   * 
   * <p>
   * <strong>Ejemplo de valor almacenado:</strong>
   * 
   * <pre>{@code
   * "a7F3kL9mN2xP5qR8sT1vW4yZ6=="
   * }</pre>
   */
  @Column(name = "password_application", columnDefinition = "TEXT", nullable = false)
  private String password_application;

  /**
   * Fecha del último cambio de la contraseña.
   * 
   * <p>
   * Permite llevar un registro de cuándo se actualizó por última vez.
   * 
   * <p>
   * <strong>Usos:</strong>
   * <ul>
   * <li>Recordatorios de rotación de contraseñas</li>
   * <li>Auditoría de seguridad</li>
   * <li>Identificar contraseñas antiguas</li>
   * </ul>
   * 
   * <p>
   * Al crear una nueva contraseña, este campo puede dejarse {@code null}
   * o establecerse con la fecha actual.
   */
  @Column(name = "password_last_change_date")
  private LocalDate password_last_change_date;

  /**
   * Cuenta de usuario propietaria de esta contraseña.
   * 
   * <p>
   * Relación {@code @ManyToOne} obligatoria con {@link AccountEntity}.
   * Carga perezosa ({@code LAZY}) para optimizar rendimiento.
   * 
   * <p>
   * Cada contraseña pertenece a una única cuenta. La eliminación de una cuenta
   * eliminará en cascada todas sus contraseñas almacenadas
   * ({@code CascadeType.ALL} +
   * {@code orphanRemoval = true} definido en {@link AccountEntity}).
   * 
   * <p>
   * En base de datos: {@code passwords_fk_account_id CHAR(36) NOT NULL}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "passwords_fk_account_id", nullable = false)
  private AccountEntity account;
}
