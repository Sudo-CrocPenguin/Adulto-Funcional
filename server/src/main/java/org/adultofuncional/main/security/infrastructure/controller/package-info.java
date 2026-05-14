/**
 * Controladores REST del módulo de seguridad (gestor de contraseñas).
 *
 * <p>
 * Expone los endpoints HTTP para la gestión de credenciales almacenadas bajo
 * la ruta base {@code /api/security/passwords}. Los controladores delegan la
 * lógica de negocio en los casos de uso correspondientes y retornan respuestas
 * envueltas en {@link org.adultofuncional.main.shared.response.ApiResponse}.
 *
 * <p>
 * El acceso a este módulo requiere que el usuario haya verificado previamente
 * su Master Key. Los endpoints que operan sobre credenciales de una cuenta
 * resuelven el {@code accountId} del usuario autenticado a partir de su correo
 * electrónico.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.security.application.usecase
 * @see org.adultofuncional.main.shared.response.ApiResponse
 */
package org.adultofuncional.main.security.infrastructure.controller;
