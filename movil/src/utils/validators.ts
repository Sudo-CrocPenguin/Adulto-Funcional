/**
 * Valida un número de teléfono colombiano.
 * Acepta formatos: 3001234567, +573001234567, 300 123 4567, 300-123-4567
 * @param phone string
 * @returns boolean
 */
export const isValidColombianPhone = (phone: string): boolean => {
  // Limpia caracteres no numéricos excepto el signo '+'
  const cleaned = phone.replace(/[^\d+]/g, '');
  // Patrón: +57 seguido de 10 dígitos, o solo 10 dígitos
  const regex = /^(?:\+57)?(\d{10})$/;
  const match = cleaned.match(regex);
  if (!match) return false;
  // Verificar que el número comience con 3 (móvil) o 6 (fijo, pero normalmente móvil)
  const number = match[1];
  return number.startsWith('3'); // Solo números móviles colombianos
};

/**
 * Valida un correo electrónico con formato estándar.
 */
export const isValidEmail = (email: string): boolean => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(email);
};

/**
 * Valida que la contraseña tenga al menos 8 caracteres.
 */
export const isValidPassword = (password: string): boolean => {
  return password.length >= 8;
};
