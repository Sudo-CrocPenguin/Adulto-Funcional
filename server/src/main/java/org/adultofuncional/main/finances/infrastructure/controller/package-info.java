/**
 * Controladores REST del módulo de finanzas personales.
 *
 * <p>
 * Expone los endpoints HTTP para la gestión de movimientos, categorías y
 * gastos fijos bajo la ruta base {@code /api/finances}. Todos los endpoints
 * que operan sobre recursos de una cuenta resuelven el {@code accountId}
 * del usuario autenticado a partir de su correo electrónico mediante
 * {@link org.adultofuncional.main.finances.infrastructure.controller.FinancesController#resolveAccountId(String)},
 * evitando que el cliente manipule identificadores de cuenta en la URL.
 * Las categorías son globales y no requieren esta resolución.
 *
 * <p>
 * Todas las respuestas se envuelven en
 * {@link org.adultofuncional.main.shared.response.ApiResponse} para mantener
 * la consistencia con el resto de la API.
 *
 * <h2>Endpoints expuestos</h2>
 *
 * <h3>Movimientos</h3>
 * <ul>
 * <li>{@code POST   /api/finances/movements} — Registrar un nuevo
 * movimiento</li>
 * <li>{@code GET    /api/finances/movements} — Listar movimientos (filtros
 * opcionales)</li>
 * <li>{@code GET    /api/finances/movements/{id}} — Obtener un movimiento</li>
 * <li>{@code PATCH  /api/finances/movements/{id}} — Actualizar parcialmente un
 * movimiento</li>
 * <li>{@code DELETE /api/finances/movements/{id}} — Eliminar un movimiento</li>
 * </ul>
 *
 * <h3>Categorías</h3>
 * <ul>
 * <li>{@code POST   /api/finances/categories} — Crear una nueva categoría</li>
 * <li>{@code GET    /api/finances/categories} — Listar categorías (filtro por
 * tipo opcional)</li>
 * <li>{@code GET    /api/finances/categories/{id}} — Obtener una categoría</li>
 * <li>{@code PATCH  /api/finances/categories/{id}} — Actualizar parcialmente
 * una categoría</li>
 * <li>{@code DELETE /api/finances/categories/{id}} — Eliminar una
 * categoría</li>
 * </ul>
 *
 * <h3>Gastos fijos</h3>
 * <ul>
 * <li>{@code POST   /api/finances/fixed-expenses} — Registrar un nuevo gasto
 * fijo</li>
 * <li>{@code GET    /api/finances/fixed-expenses} — Listar gastos fijos
 * (filtros opcionales)</li>
 * <li>{@code GET    /api/finances/fixed-expenses/{id}} — Obtener un gasto
 * fijo</li>
 * <li>{@code PATCH  /api/finances/fixed-expenses/{id}} — Actualizar
 * parcialmente un gasto fijo</li>
 * <li>{@code DELETE /api/finances/fixed-expenses/{id}} — Eliminar un gasto
 * fijo</li>
 * </ul>
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase
 * @see org.adultofuncional.main.account.domain.repository.AccountRepository
 * @see org.adultofuncional.main.shared.response.ApiResponse
 */
package org.adultofuncional.main.finances.infrastructure.controller;
