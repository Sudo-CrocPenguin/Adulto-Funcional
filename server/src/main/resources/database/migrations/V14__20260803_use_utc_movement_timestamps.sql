-- Los registros técnicos de movimientos se exponen como Instant ISO 8601.
ALTER TABLE movements
  MODIFY COLUMN movement_register_date TIMESTAMP(6) NOT NULL;
