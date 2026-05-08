/**
 * Capa de dominio del módulo de finanzas.
 *
 * <p>
 * Contiene las entidades, los enumerados y los puertos de repositorio que
 * modelan el núcleo del sistema financiero. Esta capa es completamente
 * independiente de la infraestructura externa (JPA, REST, controladores) y
 * encapsula las reglas de negocio fundamentales de categorías, gastos fijos y
 * movimientos financieros.
 *
 * <h2>Componentes</h2>
 * <ul>
 * <li>{@code model} — Entidades del dominio:
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.domain.model.Category} —
 * Clasifica movimientos y gastos fijos según su ámbito.</li>
 * <li>{@link org.adultofuncional.main.finances.domain.model.FixedExpense} —
 * Gasto recurrente asociado a una cuenta y categoría.</li>
 * <li>{@link org.adultofuncional.main.finances.domain.model.Movement} —
 * Movimiento puntual de ingreso o egreso registrado en una cuenta.</li>
 * </ul>
 * </li>
 * <li>{@code enums} — Tipos constantes compartidos en todo el módulo:
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.domain.enums.CategoryType}</li>
 * <li>{@link org.adultofuncional.main.finances.domain.enums.Frequency}</li>
 * <li>{@link org.adultofuncional.main.finances.domain.enums.MovementType}</li>
 * <li>{@link org.adultofuncional.main.finances.domain.enums.Status}</li>
 * </ul>
 * </li>
 * <li>{@code repository} — Puertos de persistencia que desacoplan el dominio
 * de la implementación concreta de base de datos:
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.domain.repository.CategoryRepository}</li>
 * <li>{@link org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository}</li>
 * <li>{@link org.adultofuncional.main.finances.domain.repository.MovementRepository}</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li><strong>Identidad:</strong> Las entidades generan su propio UUID v7 a
 * través del método de fábrica {@code create}, garantizando que el dominio sea
 * dueño de su identidad antes de la persistencia.</li>
 * <li><strong>Invariantes de negocio:</strong> Cada entidad valida
 * estrictamente
 * sus campos obligatorios, montos positivos y coherencia de fechas. Las
 * validaciones de formato, longitud y seguridad (XSS) corresponden a los DTOs
 * de la capa de aplicación.</li>
 * <li><strong>Separación de operaciones:</strong> El método
 * {@code reconstitute}
 * permite reconstruir instancias desde la capa de persistencia sin revalidar
 * invariantes, bajo el supuesto de que los datos almacenados ya fueron
 * validados
 * al ser insertados.</li>
 * <li><strong>Desacoplamiento:</strong> Los puertos de repositorio permiten
 * que los casos de uso operen sobre interfaces sin depender de implementaciones
 * concretas de JPA o de cualquier otro mecanismo de almacenamiento.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata, Daniel Salazar
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.domain.model
 * @see org.adultofuncional.main.finances.domain.enums
 * @see org.adultofuncional.main.finances.domain.repository
 */
package org.adultofuncional.main.finances.domain;
