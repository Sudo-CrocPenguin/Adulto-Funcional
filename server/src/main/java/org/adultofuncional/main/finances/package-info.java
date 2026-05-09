/**
 * Módulo de gestión financiera personal.
 *
 * <p>
 * Implementa la funcionalidad completa de movimientos financieros, gastos fijos
 * recurrentes y categorías de clasificación bajo los principios de Clean
 * Architecture, distribuyendo las responsabilidades en las capas de dominio,
 * aplicación e infraestructura.
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li>Registro de movimientos de ingreso y egreso asociados a una cuenta.</li>
 * <li>Gestión de gastos fijos recurrentes con frecuencia, estado y
 * categoría.</li>
 * <li>Administración de categorías para clasificar movimientos y gastos
 * fijos.</li>
 * <li>Validación de existencia de cuenta (a través del puerto del módulo
 * {@code account}).</li>
 * <li>Filtrado y búsqueda de movimientos y gastos fijos por tipo, categoría,
 * rango de fechas y término libre.</li>
 * <li>Protección contra Stored XSS mediante la anotación {@code @NoHtml} en los
 * DTOs de entrada.</li>
 * </ul>
 *
 * <h2>Estructura</h2>
 * 
 * <pre>
 * finances/
 * ├── domain/            → Entidades ({@code
 * Category
 * }, {@code
 * FixedExpense
 * },
 * │                         {@code
 * Movement
 * }), enumerados y puertos de repositorio
 * ├── application/       → Casos de uso ({@code
 * CreateMovementUseCase
 * },
 * │                         {@code
 * UpdateFixedExpenseUseCase
 * }, ...) y DTOs
 * │                         de entrada/salida
 * └── infrastructure/    → Controlador REST, entidades JPA, mapeadores y
 *                           adaptadores de repositorio
 * </pre>
 *
 * <h2>Entidades principales</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.domain.model.Movement} —
 * Movimiento puntual de ingreso o egreso.</li>
 * <li>{@link org.adultofuncional.main.finances.domain.model.FixedExpense} —
 * Gasto recurrente con frecuencia definida.</li>
 * <li>{@link org.adultofuncional.main.finances.domain.model.Category} —
 * Categoría de clasificación (ámbito finanzas o agenda).</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li>Todos los endpoints requieren autenticación JWT.</li>
 * <li>La validación de propiedad (ownership) sobre movimientos y gastos fijos
 * se realiza en los casos de uso comparando el {@code accountId}
 * autenticado.</li>
 * <li>Los DTOs de entrada aplican
 * {@link org.adultofuncional.main.shared.security.NoHtml}
 * en campos de texto libre para prevenir inyección de HTML malicioso (Stored
 * XSS).</li>
 * </ul>
 *
 * <h2>Tablas asociadas</h2>
 * {@code movements}, {@code fixed_expenses}, {@code categories}. Todas usan
 * UUID v7 como clave primaria.
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.domain
 * @see org.adultofuncional.main.finances.application
 * @see org.adultofuncional.main.finances.infrastructure
 */
package org.adultofuncional.main.finances;
