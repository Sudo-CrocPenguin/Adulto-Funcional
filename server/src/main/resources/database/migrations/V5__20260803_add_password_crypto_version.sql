-- Identifica los parámetros criptográficos usados por cada credencial.
-- Las filas históricas se marcan como v1 (PBKDF2 100k, sin AAD).
ALTER TABLE passwords
  ADD COLUMN password_crypto_version SMALLINT NOT NULL DEFAULT 1
  AFTER password_salt;

ALTER TABLE passwords
  ADD CONSTRAINT chk_password_crypto_version
  CHECK (password_crypto_version IN (1, 2));
