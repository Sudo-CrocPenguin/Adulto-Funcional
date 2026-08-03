ALTER TABLE movements
  DROP FOREIGN KEY movements_ibfk_1,
  ADD CONSTRAINT fk_movements_account
    FOREIGN KEY (movement_fk_account_id) REFERENCES accounts(account_id)
    ON DELETE CASCADE;

ALTER TABLE fixed_expenses
  DROP FOREIGN KEY fixed_expenses_ibfk_1,
  ADD CONSTRAINT fk_fixed_expenses_account
    FOREIGN KEY (fixed_expense_fk_account_id) REFERENCES accounts(account_id)
    ON DELETE CASCADE;

ALTER TABLE events
  DROP FOREIGN KEY events_ibfk_1,
  ADD CONSTRAINT fk_events_account
    FOREIGN KEY (event_fk_account_id) REFERENCES accounts(account_id)
    ON DELETE CASCADE;

ALTER TABLE passwords
  DROP FOREIGN KEY passwords_ibfk_1,
  ADD CONSTRAINT fk_passwords_account
    FOREIGN KEY (passwords_fk_account_id) REFERENCES accounts(account_id)
    ON DELETE CASCADE;
