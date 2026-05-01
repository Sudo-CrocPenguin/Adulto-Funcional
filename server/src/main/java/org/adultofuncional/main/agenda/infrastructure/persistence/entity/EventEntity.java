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
 * Entidad JPA que mapea la tabla {@code events} de MariaDB.
 *
 * <p>
 * Representa un evento en la agenda del usuario, con soporte para
 * recurrencia en días, recordatorios, prioridad y estado.
 *
 * <p>
 * Schema de la tabla {@code events}:
 * 
 * <pre>
 * event_id             CHAR(36)     PRIMARY KEY DEFAULT(UUID_V7())
 * event_title          VARCHAR(35)  NOT NULL
 * event_priority       VARCHAR(15)  NULL DEFAULT 'Media'
 * event_date           DATE         NOT NULL
 * event_frequency      INT          NOT NULL          -- días entre repeticiones (0 = único)
 * event_reminder       DATETIME     NOT NULL
 * event_start_hour     DATETIME     NOT NULL
 * event_end_hour       DATETIME     NOT NULL
 * event_description    TEXT         NULL
 * event_status         VARCHAR(20)  DEFAULT 'Pendiente'
 * event_fk_category_id CHAR(36)     FK → categories(category_id)
 * event_fk_account_id  CHAR(36)     FK → accounts(account_id)
 * </pre>
 *
 * <p>
 * Frecuencia ({@code event_frequency}):
 * <ul>
 * <li>{@code 0} → evento único</li>
 * <li>{@code 1} → diario</li>
 * <li>{@code 7} → semanal</li>
 * <li>{@code 30} → mensual</li>
 * <li>{@code 365} → anual</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
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
   * Columna: {@code event_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())}.
   */
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "event_id", columnDefinition = "CHAR(36)")
  private UUID eventId;

  /**
   * Título del evento.
   *
   * <p>
   * Columna: {@code event_title VARCHAR(35) NOT NULL}.
   */
  @Column(name = "event_title", length = 35, nullable = false)
  private String eventTitle;

  /**
   * Prioridad del evento. Por defecto {@code "Media"}.
   *
   * <p>
   * Columna: {@code event_priority VARCHAR(15) NULL DEFAULT 'Media'}.
   * Valores típicos: {@code "Baja"}, {@code "Media"}, {@code "Alta"}.
   */
  @Column(name = "event_priority", length = 15)
  private String eventPriority = "Media";

  /**
   * Fecha calendario del evento.
   *
   * <p>
   * Columna: {@code event_date DATE NOT NULL}.
   */
  @Column(name = "event_date", nullable = false)
  private LocalDate eventDate;

  /**
   * Frecuencia de repetición en días.
   *
   * <p>
   * Columna: {@code event_frequency INT NOT NULL}.
   * {@code 0} indica evento único sin repetición.
   */
  @Column(name = "event_frequency", nullable = false)
  private Integer eventFrequency;

  /**
   * Fecha y hora del recordatorio.
   *
   * <p>
   * Columna: {@code event_reminder DATETIME NOT NULL}.
   */
  @Column(name = "event_reminder", nullable = false)
  private LocalDateTime eventReminder;

  /**
   * Hora de inicio del evento.
   *
   * <p>
   * Columna: {@code event_start_hour DATETIME NOT NULL}.
   */
  @Column(name = "event_start_hour", nullable = false)
  private LocalDateTime eventStartHour;

  /**
   * Hora de finalización del evento.
   *
   * <p>
   * Columna: {@code event_end_hour DATETIME NOT NULL}.
   */
  @Column(name = "event_end_hour", nullable = false)
  private LocalDateTime eventEndHour;

  /**
   * Descripción detallada del evento.
   *
   * <p>
   * Columna: {@code event_description TEXT NULL}.
   */
  @Column(name = "event_description", columnDefinition = "TEXT")
  private String eventDescription;

  /**
   * Estado del evento. Por defecto {@code "Pendiente"}.
   *
   * <p>
   * Columna: {@code event_status VARCHAR(20) DEFAULT 'Pendiente'}.
   * Valores típicos: {@code "Pendiente"}, {@code "Completado"},
   * {@code "Cancelado"}, {@code "Pospuesto"}.
   */
  @Column(name = "event_status", length = 20)
  private String eventStatus = "Pendiente";

  /**
   * Categoría asociada al evento (opcional).
   *
   * <p>
   * FK: {@code event_fk_category_id CHAR(36)} → {@code categories(category_id)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_fk_category_id")
  private CategoryEntity category;

  /**
   * Cuenta propietaria del evento.
   *
   * <p>
   * FK: {@code event_fk_account_id CHAR(36)} → {@code accounts(account_id)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_fk_account_id", nullable = false)
  private AccountEntity account;
}
