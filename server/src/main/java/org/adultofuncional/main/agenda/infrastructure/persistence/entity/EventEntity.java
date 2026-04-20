package org.adultofuncional.main.agenda.infrastructure.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
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
 * Entidad JPA que representa un evento en la agenda del usuario.
 * 
 * <p>
 * Esta entidad gestiona compromisos, recordatorios y eventos programados
 * asociados a una cuenta de usuario. Soporta eventos únicos o recurrentes
 * con diferentes niveles de prioridad y estados de seguimiento.
 * 
 * <p>
 * <strong>Características principales:</strong>
 * <ul>
 * <li><b>Identificador único:</b> UUID v7 generado automáticamente</li>
 * <li><b>Eventos recurrentes:</b> Frecuencia configurable en días</li>
 * <li><b>Recordatorios:</b> Fecha y hora específica para notificaciones</li>
 * <li><b>Duración definida:</b> Hora de inicio y fin del evento</li>
 * <li><b>Prioridades:</b> Baja, Media, Alta</li>
 * <li><b>Estados:</b> Pendiente, Completado, Cancelado</li>
 * <li><b>Categoria opcional:</b></li>
 * </ul>
 * 
 * <p>
 * <strong>Valores por defecto:</strong>
 * <ul>
 * <li>Prioridad: "Media"</li>
 * <li>Estado: "Pendiente"</li>
 * </ul>
 * 
 * <p>
 * <strong>Frecuencia de repetición:</strong>
 * <ul>
 * <li>{@code 0} = Evento único (sin repetición)</li>
 * <li>{@code 1} = Diario</li>
 * <li>{@code 7} = Semanal</li>
 * <li>{@code 30} = Mensual (aproximado)</li>
 * <li>{@code 365} = Anual</li>
 * </ul>
 * 
 * <p>
 * <strong>Restricciones de base de datos:</strong>
 * <ul>
 * <li>{@code event_id}: CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())</li>
 * <li>{@code event_title}: VARCHAR(35) NOT NULL</li>
 * <li>{@code event_priority}: VARCHAR(15) DEFAULT 'Media'</li>
 * <li>{@code event_status}: VARCHAR(20) DEFAULT 'Pendiente'</li>
 * <li>{@code event_frequency}: INT NOT NULL (0 = sin repetición)</li>
 * </ul>
 * 
 * <p>
 * <strong>Relaciones:</strong>
 * <ul>
 * <li>{@code @ManyToOne} → {@link AccountEntity} (obligatorio)</li>
 * <li>{@code @ManyToOne} → {@link CategoryEntity} (opcional)</li>
 * </ul>
 * 
 * <p>
 * Esta entidad forma parte del módulo de agenda y permite a los usuarios
 * organizar sus compromisos con recordatorios y seguimiento de estado.
 * 
 * @author juan
 * @since 0.0.1
 * @see AccountEntity
 * @see CategoryEntity
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class EventEntity {

  /**
   * Identificador único del evento.
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
  @Column(name = "event_id", columnDefinition = "CHAR(36)")
  private UUID event_id;

  /**
   * Título descriptivo del evento.
   * 
   * <p>
   * Obligatorio. Máximo 35 caracteres.
   * Debe ser conciso pero informativo sobre el propósito del evento.
   * 
   * <p>
   * Ejemplos: "Reunión equipo", "Cita médico", "Entrega proyecto".
   */
  @Column(name = "event_title", length = 35, nullable = false)
  private String event_title;

  /**
   * Nivel de prioridad del evento.
   * 
   * <p>
   * Opcional. Valor por defecto: "Media".
   * Máximo 15 caracteres.
   * 
   * <p>
   * <strong>Valores típicos:</strong>
   * <ul>
   * <li>"Baja" - Eventos flexibles o poco urgentes</li>
   * <li>"Media" - Prioridad normal (por defecto)</li>
   * <li>"Alta" - Eventos importantes o impostergables</li>
   * </ul>
   * 
   * <p>
   * Útil para ordenar y filtrar eventos por importancia.
   */
  @Column(name = "event_priority", length = 15)
  private String event_priority = "Media";

  /**
   * Fecha en la que ocurre el evento.
   * 
   * <p>
   * Obligatorio. Representa el día calendario del evento,
   * independientemente de la hora de inicio y fin.
   * 
   * <p>
   * Para eventos recurrentes, esta es la fecha de la primera ocurrencia.
   */
  @Column(name = "event_date", nullable = false)
  private LocalDate event_date;

  /**
   * Frecuencia de repetición del evento en días.
   * 
   * <p>
   * Obligatorio. Un valor entero que indica cada cuántos días se repite.
   * 
   * <p>
   * <strong>Valores comunes:</strong>
   * <ul>
   * <li>{@code 0} = Evento único, sin repetición</li>
   * <li>{@code 1} = Diario (cada 1 día)</li>
   * <li>{@code 7} = Semanal (cada 7 días)</li>
   * <li>{@code 14} = Quincenal (cada 14 días)</li>
   * <li>{@code 30} = Mensual aproximado (cada 30 días)</li>
   * <li>{@code 365} = Anual (cada 365 días)</li>
   * </ul>
   * 
   * <p>
   * La lógica de recurrencia se implementa en la capa de aplicación,
   * generando instancias virtuales basadas en esta frecuencia.
   */
  @Column(name = "event_frequency", nullable = false)
  private Integer event_frequency;

  /**
   * Fecha y hora del recordatorio programado.
   * 
   * <p>
   * Obligatorio. Momento exacto en que se debe notificar al usuario
   * sobre el evento. Puede ser anterior a la hora de inicio del evento.
   * 
   * <p>
   * <strong>Ejemplo:</strong> Para un evento a las 15:00, el recordatorio
   * podría configurarse a las 14:30 (30 minutos antes).
   */
  @Column(name = "event_reminder", nullable = false)
  private LocalDateTime event_reminder;

  /**
   * Hora de inicio del evento.
   * 
   * <p>
   * Obligatorio. Momento exacto en que comienza el evento.
   * Incluye fecha y hora para soportar eventos de varios días.
   * 
   * <p>
   * Debe ser anterior a {@link #event_end_hour}.
   */
  @Column(name = "event_start_hour", nullable = false)
  private LocalDateTime event_start_hour;

  /**
   * Hora de finalización del evento.
   * 
   * <p>
   * Obligatorio. Momento exacto en que termina el evento.
   * Incluye fecha y hora para soportar eventos de varios días.
   * 
   * <p>
   * Debe ser posterior a {@link #event_start_hour}.
   */
  @Column(name = "event_end_hour", nullable = false)
  private LocalDateTime event_end_hour;

  /**
   * Descripción detallada del evento.
   * 
   * <p>
   * Opcional. Campo de texto libre sin límite de longitud
   * ({@code TEXT} en base de datos).
   * 
   * <p>
   * Puede incluir notas, agenda, ubicación, participantes, enlaces, etc.
   */
  @Column(name = "event_description", columnDefinition = "TEXT")
  private String event_description;

  /**
   * Estado actual del evento.
   * 
   * <p>
   * Opcional. Valor por defecto: "Pendiente".
   * Máximo 20 caracteres.
   * 
   * <p>
   * <strong>Valores típicos:</strong>
   * <ul>
   * <li>"Pendiente" - Evento programado, aún no realizado</li>
   * <li>"Completado" - Evento ya realizado</li>
   * <li>"Cancelado" - Evento cancelado</li>
   * <li>"Pospuesto" - Evento reprogramado</li>
   * </ul>
   * 
   * <p>
   * Útil para seguimiento y filtrado de eventos.
   */
  @Column(name = "event_status", length = 20)
  private String event_status = "Pendiente";

  /**
   * Categoría asociada al evento.
   * 
   * <p>
   * Relación {@code @ManyToOne} opcional con {@link CategoryEntity}.
   * Carga perezosa ({@code LAZY}) para optimizar rendimiento.
   * 
   * <p>
   * Permite categorizar eventos (ej. "Trabajo", "Personal", "Salud")
   * usando las mismas categorías del módulo financiero.
   * 
   * <p>
   * En base de datos: {@code event_fk_category_id CHAR(36)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_fk_category_id")
  private CategoryEntity category;

  /**
   * Cuenta de usuario propietaria del evento.
   * 
   * <p>
   * Relación {@code @ManyToOne} obligatoria con {@link AccountEntity}.
   * Carga perezosa ({@code LAZY}) para optimizar rendimiento.
   * 
   * <p>
   * Cada evento pertenece a una única cuenta. La eliminación de una cuenta
   * eliminará en cascada todos sus eventos ({@code CascadeType.ALL} +
   * {@code orphanRemoval = true} definido en {@link AccountEntity}).
   * 
   * <p>
   * En base de datos: {@code event_fk_account_id CHAR(36) NOT NULL}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_fk_account_id", nullable = false)
  private AccountEntity account;
}
