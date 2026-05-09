/**
 * Controladores REST del módulo de agenda.
 *
 * <p>
 * Expone los endpoints HTTP para la gestión de eventos bajo la ruta base
 * {@code /api/agenda}. Los controladores delegan la lógica de negocio en los
 * casos de uso correspondientes y retornan respuestas envueltas en
 * {@link org.adultofuncional.main.shared.response.ApiResponse}.
 *
 * <p>
 * Los endpoints que operan sobre eventos de una cuenta resuelven el
 * {@code accountId} del usuario autenticado a partir de su correo electrónico
 * mediante el método {@code resolveAccountId} del controlador.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.application.usecase
 * @see org.adultofuncional.main.shared.response.ApiResponse
 */
package org.adultofuncional.main.agenda.infrastructure.controller;
