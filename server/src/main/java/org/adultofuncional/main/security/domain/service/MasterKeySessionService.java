package org.adultofuncional.main.security.domain.service;

import java.util.UUID;

/**
 * Puerto de dominio para la gestión de la Master Key durante la sesión.
 *
 * <p>
 * Antes de acceder al gestor de contraseñas, el usuario debe ingresar su
 * Master Key. Este servicio mantiene esa clave en memoria durante la sesión
 * activa, evitando que el usuario tenga que reingresarla en cada operación.
 *
 * <h2>Ciclo de vida</h2>
 * <ol>
 * <li>El usuario envía su Master Key al endpoint correspondiente.</li>
 * <li>El controlador (o un caso de uso específico) llama a
 * {@link #verify} después de comprobar que el hash coincide con
 * {@code account_master_key}.</li>
 * <li>Los casos de uso del gestor llaman a {@link #isVerified} antes de
 * operar. Si no está verificada, lanzan
 * {@link org.adultofuncional.main.shared.exception.ForbiddenException}.</li>
 * <li>Cuando el usuario cierra sesión o expira el token, se invoca
 * {@link #clear} para eliminar la Master Key de memoria.</li>
 * </ol>
 *
 * <p>
 * <strong>¿Por qué no guardamos la Master Key en la base de datos?</strong><br>
 * Porque si un atacante obtiene acceso a la BD, podría descifrar todas las
 * contraseñas. Al mantenerla solo en memoria durante la sesión, el riesgo se
 * reduce drásticamente.
 *
 * @author Juan Sebaastian Rios
 * @since 0.0.1
 */
public interface MasterKeySessionService {

  /**
   * Indica si la Master Key ya fue verificada para la cuenta dada.
   *
   * @param accountId identificador de la cuenta
   * @return {@code true} si la Master Key está disponible en la sesión
   */
  boolean isVerified(UUID accountId);

  /**
   * Obtiene la Master Key en texto plano verificada previamente.
   *
   * @param accountId identificador de la cuenta
   * @return Master Key en texto plano
   * @throws IllegalStateException si no ha sido verificada aún
   */
  String getMasterKey(UUID accountId);

  /**
   * Almacena la Master Key en la sesión después de verificarla contra el
   * hash almacenado en {@code accounts.account_master_key}.
   *
   * @param accountId identificador de la cuenta
   * @param masterKey Master Key en texto plano
   */
  void verify(UUID accountId, String masterKey);

  /**
   * Elimina la Master Key de la sesión (logout del gestor o expiración).
   *
   * @param accountId identificador de la cuenta
   */
  void clear(UUID accountId);
}
