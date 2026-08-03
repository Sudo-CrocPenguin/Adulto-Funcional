-- Aumenta la precisión del instante técnico sin reinterpretar los datos.
-- TIMESTAMP se almacena normalizado por MariaDB y Hibernate opera en UTC.
ALTER TABLE accounts
  MODIFY COLUMN account_created_at TIMESTAMP(6) NOT NULL;
