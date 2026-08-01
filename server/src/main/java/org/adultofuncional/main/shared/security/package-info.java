/**
 * Componentes de seguridad transversales a todos los módulos.
 *
 * <p>
 * Contiene utilidades y anotaciones que refuerzan la seguridad a nivel de
 * aplicación, utilizadas por todos los módulos de negocio sin necesidad de
 * depender de detalles específicos de cada uno.
 *
 * <h2>Componentes incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.shared.security.NoHtml} —
 * Anotación Jakarta Validation que impide que los campos de texto contengan
 * HTML, protegiendo contra ataques de Stored XSS.</li>
 * <li>{@link org.adultofuncional.main.shared.security.NoHtmlValidator} —
 * Validador que implementa la lógica de {@code @NoHtml} usando Jsoup con
 * {@code Safelist.none()}.</li>
 * <li>{@link org.adultofuncional.main.shared.security.OwnedResource} —
 * Contrato legado para DTOs de respuesta con propietario. La validación de
 * ownership actual usa el UUID estable de la cuenta autenticada.</li>
 * <li>{@link org.adultofuncional.main.shared.security.OwnershipValidator} —
 * Componente Spring que centraliza la validación de que el usuario autenticado
 * es el propietario del recurso solicitado.</li>
 * </ul>
 *
 * <h2>Uso en el sistema</h2>
 * <ul>
 * <li><strong>Protección XSS:</strong> Todos los DTOs de entrada que contienen
 * campos de texto libre anotan esos campos con {@code @NoHtml}. El motor de
 * validación de Spring invoca automáticamente {@code NoHtmlValidator} antes de
 * que la petición llegue a los casos de uso, rechazando cualquier intento de
 * inyección HTML con un error 400.</li>
 * <li><strong>Validación de ownership:</strong> Los controladores de recursos
 * de usuario obtienen el recurso mediante el caso de uso correspondiente y
 * luego llaman a {@code OwnershipValidator.validate(resourceAccountId, accountId)}
 * antes de retornarlo o modificarlo. Si el UUID del recurso no coincide con el
 * UUID autenticado, se responde como recurso inexistente mediante
 * {@link org.adultofuncional.main.shared.exception.NotFoundException}, evitando
 * confirmar la existencia de recursos ajenos.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.shared.security.NoHtml
 * @see org.adultofuncional.main.shared.security.NoHtmlValidator
 * @see org.adultofuncional.main.shared.security.OwnedResource
 * @see org.adultofuncional.main.shared.security.OwnershipValidator
 */
package org.adultofuncional.main.shared.security;
