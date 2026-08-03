-- Ajusta la columna password_iv al tamano real usado por AES-GCM.
-- V1 la creo como BINARY(16), pero AesEncryptionService genera IV de 12 bytes.
-- MariaDB rellenaba BINARY(16) con 0x00; al leerlo, GCM recibia un IV distinto.

ALTER TABLE passwords MODIFY password_iv VARBINARY(16) NOT NULL;

UPDATE passwords
SET password_iv = SUBSTRING(password_iv, 1, 12);

ALTER TABLE passwords MODIFY password_iv BINARY(12) NOT NULL;
