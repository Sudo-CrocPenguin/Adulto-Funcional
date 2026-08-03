ALTER TABLE accounts
  ADD COLUMN account_version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE passwords
  ADD COLUMN password_version BIGINT NOT NULL DEFAULT 0,
  ADD CONSTRAINT uk_passwords_account_application
    UNIQUE (passwords_fk_account_id, password_application_name);
