-- Los eventos históricos se interpretaban implícitamente en America/Bogota.
-- Se conservan sus horas civiles para compatibilidad y se añaden los instantes
-- UTC que permiten ordenar y ejecutar recordatorios sin depender de la zona
-- horaria del proceso o de la conexión a MariaDB.
ALTER TABLE events
  MODIFY COLUMN event_reminder DATETIME(6) NOT NULL,
  MODIFY COLUMN event_start_hour DATETIME(6) NOT NULL,
  MODIFY COLUMN event_end_hour DATETIME(6) NOT NULL,
  ADD COLUMN event_zone_id VARCHAR(63) NOT NULL DEFAULT 'America/Bogota'
    AFTER event_end_hour,
  ADD COLUMN event_reminder_instant TIMESTAMP(6) NULL
    AFTER event_zone_id,
  ADD COLUMN event_start_instant TIMESTAMP(6) NULL
    AFTER event_reminder_instant,
  ADD COLUMN event_end_instant TIMESTAMP(6) NULL
    AFTER event_start_instant;

UPDATE events
SET event_reminder_instant = CONVERT_TZ(event_reminder, '-05:00', '+00:00'),
    event_start_instant = CONVERT_TZ(event_start_hour, '-05:00', '+00:00'),
    event_end_instant = CONVERT_TZ(event_end_hour, '-05:00', '+00:00');

ALTER TABLE events
  MODIFY COLUMN event_reminder_instant TIMESTAMP(6) NOT NULL,
  MODIFY COLUMN event_start_instant TIMESTAMP(6) NOT NULL,
  MODIFY COLUMN event_end_instant TIMESTAMP(6) NOT NULL,
  ADD CONSTRAINT chk_events_instant_schedule
    CHECK (
      event_reminder_instant < event_start_instant
      AND event_start_instant < event_end_instant
    );

CREATE INDEX idx_events_account_start_instant_id
  ON events(event_fk_account_id, event_start_instant, event_id);
