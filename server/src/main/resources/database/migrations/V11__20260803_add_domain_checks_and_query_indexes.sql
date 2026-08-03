ALTER TABLE movements
  ADD CONSTRAINT chk_movements_type
    CHECK (movement_type IN ('INCOME', 'EXPENSE')),
  ADD CONSTRAINT chk_movements_amount
    CHECK (movement_amount > 0);

ALTER TABLE fixed_expenses
  ADD CONSTRAINT chk_fixed_expenses_frequency
    CHECK (fixed_expense_frequency IN (
      'WEEKLY', 'BIWEEKLY', 'MONTHLY', 'QUARTERLY', 'SEMIANNUAL', 'ANNUAL'
    )),
  ADD CONSTRAINT chk_fixed_expenses_amount
    CHECK (fixed_expense_amount > 0),
  ADD CONSTRAINT chk_fixed_expenses_status
    CHECK (fixed_expense_status IN ('ACTIVE', 'INACTIVE')),
  ADD CONSTRAINT chk_fixed_expenses_dates
    CHECK (fixed_expense_next_due_date > fixed_expense_start_date),
  ADD CONSTRAINT chk_fixed_expenses_reminder
    CHECK (fixed_expense_reminder_days >= 0);

UPDATE events SET event_priority = 'Media' WHERE event_priority IS NULL;
UPDATE events SET event_status = 'Pendiente' WHERE event_status IS NULL;

ALTER TABLE events
  MODIFY COLUMN event_priority VARCHAR(15) NOT NULL DEFAULT 'Media',
  MODIFY COLUMN event_status VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
  ADD CONSTRAINT chk_events_priority
    CHECK (event_priority IN ('Baja', 'Media', 'Alta')),
  ADD CONSTRAINT chk_events_status
    CHECK (event_status IN ('Pendiente', 'Completado', 'Cancelado', 'Pospuesto')),
  ADD CONSTRAINT chk_events_frequency
    CHECK (event_frequency IN (0, 1, 7, 30, 365)),
  ADD CONSTRAINT chk_events_schedule
    CHECK (
      event_reminder < event_start_hour
      AND event_start_hour < event_end_hour
      AND DATE(event_start_hour) = event_date
      AND DATE(event_end_hour) = event_date
    );

CREATE INDEX idx_movements_account_date_id
  ON movements(movement_fk_account_id, movement_date, movement_id);
CREATE INDEX idx_fixed_expenses_account_due_id
  ON fixed_expenses(fixed_expense_fk_account_id, fixed_expense_next_due_date, fixed_expense_id);
CREATE INDEX idx_events_account_date_id
  ON events(event_fk_account_id, event_date, event_id);
CREATE INDEX idx_categories_owner_type_name
  ON categories(owner_account_id, category_type, category_name);
CREATE INDEX idx_passwords_account_change_id
  ON passwords(passwords_fk_account_id, password_last_change_date, password_id);
