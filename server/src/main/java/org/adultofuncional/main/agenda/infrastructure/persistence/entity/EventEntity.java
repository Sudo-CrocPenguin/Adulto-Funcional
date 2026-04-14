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

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class EventEntity {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "event_id", columnDefinition = "CHAR(36)")
  private UUID event_id;

  @Column(name = "event_title", length = 35, nullable = false)
  private String event_title;

  @Column(name = "event_priority", length = 15)
  private String event_priority = "Media";

  @Column(name = "event_date", nullable = false)
  private LocalDate event_date;

  @Column(name = "event_frequency", nullable = false)
  private Integer event_frequency; // 0 = no repetir, 1 = diario, etc.

  @Column(name = "event_reminder", nullable = false)
  private LocalDateTime event_reminder;

  @Column(name = "event_start_hour", nullable = false)
  private LocalDateTime event_start_hour;

  @Column(name = "event_end_hour", nullable = false)
  private LocalDateTime event_end_hour;

  @Column(name = "event_description", columnDefinition = "TEXT")
  private String event_description;

  @Column(name = "event_status", length = 20)
  private String event_status = "Pendiente";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_fk_category_id")
  private CategoryEntity category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_fk_account_id", nullable = false)
  private AccountEntity account;
}
