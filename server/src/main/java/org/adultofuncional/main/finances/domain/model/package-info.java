/**
 * Modelos de dominio del módulo de finanzas.
 *
 * <p>
 * Contiene las entidades que representan los conceptos centrales del sistema
 * financiero: categorías, gastos fijos recurrentes y movimientos (ingresos y
 * egresos). Cada entidad encapsula sus propias invariantes de negocio y es
 * responsable de generar su identidad (UUID v7) a través de métodos de fábrica,
 * garantizando que el dominio sea dueño de sus datos antes de la persistencia.
 *
 * <h2>Entidades incluidas</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.domain.model.Category} —
 * Clasifica movimientos y gastos fijos según su ámbito
 * ({@link org.adultofuncional.main.finances.domain.enums.CategoryType}).</li>
 * <li>{@link org.adultofuncional.main.finances.domain.model.FixedExpense} —
 * Gasto recurrente asociado a una cuenta y categoría, con frecuencia definida
 * ({@link org.adultofuncional.main.finances.domain.enums.Frequency}) y estado
 * operativo
 * ({@link org.adultofuncional.main.finances.domain.enums.Status}).</li>
 * <li>{@link org.adultofuncional.main.finances.domain.model.Movement} —
 * Movimiento puntual de ingreso o egreso
 * ({@link org.adultofuncional.main.finances.domain.enums.MovementType})
 * registrado en una cuenta, con fecha de ocurrencia y de registro.</li>
 * </ul>
 *
 * <h2>Responsabilidades comunes</h2>
 * <ul>
 * <li><strong>Métodos de fábrica:</strong> {@code create} para nuevas entidades
 * (genera UUID v7 y asigna valores iniciales) y {@code reconstitute} para
 * reconstruir instancias desde la capa de persistencia sin revalidar
 * invariantes ya garantizadas por la base de datos.</li>
 * <li><strong>Validación de invariantes:</strong> Cada entidad valida que sus
 * campos obligatorios no sean nulos, que los montos sean positivos, que las
 * fechas sean coherentes, etc. Las validaciones de formato y seguridad se
 * delegan a los DTOs de la capa de aplicación.</li>
 * <li><strong>Métodos de actualización:</strong> Exponen métodos específicos
 * para modificar sus atributos de forma controlada, reaplicando las
 * invariantes.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.domain.enums
 * @see org.adultofuncional.main.finances.domain.repository
 * @see org.adultofuncional.main.finances.application.dto
 */
package org.adultofuncional.main.finances.domain.model;
