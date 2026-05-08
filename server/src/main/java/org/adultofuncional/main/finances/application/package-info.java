/**
 * Capa de aplicación del módulo de finanzas.
 *
 * <p>
 * Orquesta los casos de uso y define los DTOs de entrada/salida para la
 * gestión de categorías, gastos fijos y movimientos financieros. Los casos
 * de uso coordinan el dominio y los puertos de repositorio sin depender
 * de la infraestructura externa (JPA, REST), manteniendo la lógica de
 * negocio desacoplada de los controladores y de la capa de persistencia.
 *
 * <h2>Componentes</h2>
 * <ul>
 * <li>{@code dto} — Objetos de transferencia de datos, organizados por
 * entidad de dominio:
 * <ul>
 * <li>{@code dto.category} — {@code CategoryResponse},
 * {@code CreateCategoryRequest}, {@code UpdateCategoryRequest},
 * {@code CategoryFilterRequest}.</li>
 * <li>{@code dto.fixedexpense} — {@code FixedExpenseResponse},
 * {@code CreateFixedExpenseRequest}, {@code UpdateFixedExpenseRequest},
 * {@code FixedExpenseFilterRequest}.</li>
 * <li>{@code dto.movement} — {@code MovementResponse},
 * {@code CreateMovementRequest}, {@code UpdateMovementRequest},
 * {@code MovementFilterRequest}.</li>
 * </ul>
 * </li>
 * <li>{@code usecase} — Casos de uso, organizados por entidad de dominio:
 * <ul>
 * <li>{@code usecase.category} — {@code CreateCategoryUseCase},
 * {@code GetCategoryUseCase}, {@code UpdateCategoryUseCase},
 * {@code DeleteCategoryUseCase}, {@code ListCategoriesUseCase}.</li>
 * <li>{@code usecase.fixedexpense} — {@code CreateFixedExpenseUseCase},
 * {@code GetFixedExpenseUseCase}, {@code UpdateFixedExpenseUseCase},
 * {@code DeleteFixedExpenseUseCase}, {@code ListFixedExpensesUseCase}.</li>
 * <li>{@code usecase.movement} — {@code CreateMovementUseCase},
 * {@code GetMovementUseCase}, {@code UpdateMovementUseCase},
 * {@code ListMovementsUseCase}.</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <p>
 * La validación anti-XSS se aplica mediante la anotación personalizada
 * {@link org.adultofuncional.main.shared.security.NoHtml} en todos los
 * campos de texto libre de los DTOs de entrada (nombres, descripciones,
 * términos de búsqueda). Esto rechaza cualquier string que contenga HTML
 * (basado en Jsoup con {@code Safelist.none()}), previniendo el
 * almacenamiento de scripts maliciosos en la base de datos (Stored XSS).
 *
 * <p>
 * Las respuestas nunca exponen datos sensibles ni de infraestructura
 * (marcas de auditoría, borrado lógico, etc.). Algunas respuestas
 * incluyen objetos anidados (ej. {@code CategoryResponse} dentro de
 * {@code FixedExpenseResponse}) para evitar consultas adicionales por
 * parte del cliente.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.shared.security.NoHtml
 * @see org.adultofuncional.main.finances.application.dto
 * @see org.adultofuncional.main.finances.application.usecase
 */
package org.adultofuncional.main.finances.application;
