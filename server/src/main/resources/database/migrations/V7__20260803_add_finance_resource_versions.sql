ALTER TABLE movements
  ADD COLUMN movement_version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE fixed_expenses
  ADD COLUMN fixed_expense_version BIGINT NOT NULL DEFAULT 0;
