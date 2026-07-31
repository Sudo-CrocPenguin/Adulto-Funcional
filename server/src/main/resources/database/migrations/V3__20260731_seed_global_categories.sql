INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000001', 'Alimentacion', 'FINANCES'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Alimentacion' AND category_type = 'FINANCES'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000002', 'Transporte', 'FINANCES'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Transporte' AND category_type = 'FINANCES'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000003', 'Vivienda', 'FINANCES'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Vivienda' AND category_type = 'FINANCES'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000004', 'Salud', 'FINANCES'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Salud' AND category_type = 'FINANCES'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000005', 'Educacion', 'FINANCES'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Educacion' AND category_type = 'FINANCES'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000006', 'Servicios', 'FINANCES'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Servicios' AND category_type = 'FINANCES'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000007', 'Ingresos', 'FINANCES'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Ingresos' AND category_type = 'FINANCES'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000008', 'Ahorro', 'FINANCES'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Ahorro' AND category_type = 'FINANCES'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000009', 'Trabajo', 'AGENDA'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Trabajo' AND category_type = 'AGENDA'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000010', 'Familia', 'AGENDA'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Familia' AND category_type = 'AGENDA'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000011', 'Salud', 'AGENDA'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Salud' AND category_type = 'AGENDA'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000012', 'Pagos', 'AGENDA'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Pagos' AND category_type = 'AGENDA'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000013', 'Recordatorios', 'AGENDA'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Recordatorios' AND category_type = 'AGENDA'
);

INSERT INTO categories (category_id, category_name, category_type)
SELECT '01988e6b-0c00-7000-8000-000000000014', 'Personal', 'AGENDA'
WHERE NOT EXISTS (
  SELECT 1 FROM categories WHERE category_name = 'Personal' AND category_type = 'AGENDA'
);
