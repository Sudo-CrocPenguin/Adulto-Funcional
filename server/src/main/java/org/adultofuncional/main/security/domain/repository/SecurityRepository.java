package org.adultofuncional.main.security.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para operaciones de seguridad sobre cuentas de usuario.
 * <p>
 * Este repositorio proporciona métodos para gestionar identificadores de cuentas,
 * permitiendo búsquedas por ID o email, listado completo y eliminación.
 * 
 * <p><strong>Nota arquitectónica:</strong>
 * Actualmente este repositorio se encuentra en el módulo {@code security},
 * aunque sus operaciones corresponden típicamente al agregado {@code Account}
 * del módulo {@code account}. En futuras iteraciones se evaluará su ubicación
 * definitiva.
 * </p>
 *
 * @author Daniel Salazar
 * @since 1.0
 */
public interface SecurityRepository {
    

    /**
     * Busca el identificador de una cuenta a partir de su UUID.
     *
     * @param id el UUID de la cuenta (no puede ser {@code null})
     * @return un {@code Optional} que contiene el UUID si la cuenta existe,
     *         o {@code Optional.empty()} si no se encuentra.
     * @throws IllegalArgumentException si el parámetro {@code id} es {@code null}
     */
    Optional<UUID> findById(UUID id);

    /**
     * Busca el identificador de una cuenta a partir de su dirección de correo electrónico.
     *
     * @param email el correo electrónico de la cuenta (no puede ser {@code null} o vacío)
     * @return un {@code Optional} que contiene el UUID de la cuenta si existe,
     *         o {@code Optional.empty()} si no hay ninguna cuenta con ese email.
     * @throws IllegalArgumentException si el parámetro {@code email} es {@code null} o está vacío
     */
    Optional<UUID> findByEmail(String email);

    /**
     * Recupera los identificadores de todas las cuentas registradas en el sistema.
     * <p>
     * <strong>Precaución:</strong> Este método puede ser costoso si hay muchas cuentas.
     * Se recomienda usar solo para propósitos administrativos o de auditoría.
     * </p>
     *
     * @return una lista (posiblemente vacía) con los UUID de todas las cuentas.
     *         Nunca retorna {@code null}.
     */
    List<UUID> findAll();

    /**
     * Elimina permanentemente una cuenta por su identificador único.
     * <p>
     * La implementación debe asegurar el borrado en cascada de todas las entidades
     * relacionadas (movimientos, eventos, contraseñas, etc.), según las reglas
     * definidas en la base de datos.
     * </p>
     *
     * @param id el UUID de la cuenta a eliminar (no puede ser {@code null})
     * @throws IllegalArgumentException si el parámetro {@code id} es {@code null}
     * @throws org.adultofuncional.main.shared.exception.NotFoundException
     *         si no existe ninguna cuenta con el ID proporcionado (opcional, según implementación)
     */
    void deleteById(UUID id);

    /**
     * Elimina permanentemente una cuenta por su dirección de correo electrónico.
     * <p>
     * Método alternativo a {@link #deleteById(UUID)} para borrar cuentas usando
     * el email como criterio.
     * </p>
     *
     * @param email el correo electrónico de la cuenta a eliminar (no puede ser {@code null} o vacío)
     * @throws IllegalArgumentException si el parámetro {@code email} es {@code null} o está vacío
     * @throws org.adultofuncional.main.shared.exception.NotFoundException
     *         si no existe ninguna cuenta con ese email
     */
    void deleteByEmail(String email);
}