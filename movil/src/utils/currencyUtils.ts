/**
 * Formatea un número como moneda con separadores de miles (puntos) y dos decimales.
 *
 * Ejemplos:
 *   100 -> "$100.00"
 *   1000 -> "$1.000.00"
 *   1000000 -> "$1.000.000.00"
 *
 * @param amount - Número a formatear
 * @param symbol - Símbolo de moneda (por defecto "$")
 * @returns String formateado
 */
export const formatCurrency = (amount: number, symbol: string = '$'): string => {
  if (isNaN(amount)) return `${symbol}0.00`;
  const [integerPart, decimalPart] = amount.toFixed(2).split('.');
  const formattedInteger = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${symbol}${formattedInteger}.${decimalPart}`;
};

/**
 * Retorna partes separadas (entero y decimal) para aplicar estilos distintos.
 *
 * @returns { integer: string, decimal: string }
 */
export const formatCurrencyParts = (amount: number, symbol: string = '$') => {
  if (isNaN(amount)) return { integer: `${symbol}0`, decimal: '00' };
  const [integerPart, decimalPart] = amount.toFixed(2).split('.');
  const formattedInteger = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return {
    integer: `${symbol}${formattedInteger}`,
    decimal: decimalPart,
  };
};
