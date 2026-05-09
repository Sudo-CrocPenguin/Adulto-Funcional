/**
 * Controladores REST del módulo de cuentas de usuario.
 *
 * <p>
 * Expone los endpoints HTTP para la consulta, actualización y eliminación de
 * cuentas bajo la ruta base {@code /api/account}. Todos los endpoints requieren
 * autenticación JWT y validan que el usuario autenticado sea el propietario de
 * la cuenta mediante
 * {@link org.adultofuncional.main.shared.security.OwnershipValidator}.
 *
 * <p>
 * Las respuestas nunca exponen campos sensibles como el hash de contraseña
 * ({@code account_password}) ni la master key ({@code account_master_key}).
 * El filtrado de datos sensibles se realiza en la capa de aplicación mediante
 * {@link org.adultofuncional.main.account.application.dto.AccountResponse}.
 *
 * <h2>Endpoints</h2>
 * <ul>
 * <li>{@code GET    /api/account/{id}} — Obtener datos de una cuenta</li>
 * <li>{@code PATCH  /api/account/{id}} — Actualizar datos de una cuenta</li>
 * <li>{@code DELETE /api/account/{id}} — Eliminar cuenta y todos sus datos
 * asociados en cascada</li>
 * </ul>
 *
 * @author Lydis Esther Jaraba, Juan Sebastian Rios, Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.account.application.usecase.GetAccountUseCase
 * @see org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase
 * @see org.adultofuncional.main.account.application.usecase.DeleteAccountUseCase
 * @see org.adultofuncional.main.shared.security.OwnershipValidator
 */
package org.adultofuncional.main.account.infrastructure.controller;
