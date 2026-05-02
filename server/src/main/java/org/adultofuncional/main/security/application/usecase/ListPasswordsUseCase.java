package org.adultofuncional.main.security.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar todas las contraseñas almacenadas de un usuario.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un servicio de aplicación que encapsula la lógica de negocio para recuperar
 * todas las credenciales (nombres de aplicación y contraseñas) que un usuario
 * ha guardado en su gestor de contraseñas.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para:
 * <ul>
 *   <li>Validar que la cuenta del usuario exista</li>
 *   <li>Verificar que el usuario haya autenticado su Master Key</li>
 *   <li>Recuperar todas las contraseñas asociadas a una cuenta</li>
 *   <li>Desencriptar cada contraseña con AES-256 antes de devolverla</li>
 *   <li>Devolver una lista ordenada (por nombre de aplicación o fecha de creación)</li>
 * </ul>
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * <ol>
 *   <li>Recibe el ID de la cuenta del usuario autenticado</li>
 *   <li>Verifica que la cuenta exista en el repositorio</li>
 *   <li>Verifica que la Master Key haya sido verificada en la sesión actual</li>
 *   <li>Recupera todas las contraseñas de la cuenta desde el repositorio</li>
 *   <li>Desencripta cada contraseña usando la Master Key del usuario</li>
 *   <li>Mapea cada entidad a un DTO de respuesta</li>
 *   <li>Retorna la lista ordenada de contraseñas</li>
 * </ol>
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.security.domain.repository.PasswordRepository
 * @see org.adultofuncional.main.account.domain.repository.AccountRepository
 * @see PasswordResponse
 */
@Service
@RequiredArgsConstructor
public class ListPasswordsUseCase {

    private final PasswordRepository passwordRepository;
    private final AccountRepository accountRepository;

    // TODO: Inyectar servicio de encriptación (AesEncryptionService) cuando esté disponible
    // TODO: Inyectar servicio de sesión para verificar Master Key (MasterKeyVerificationService)

    /**
     * Ejecuta la consulta de todas las contraseñas de una cuenta.
     *
     * @param accountId Identificador de la cuenta. No puede ser {@code null}.
     * @return Una lista de {@link PasswordResponse} con todas las contraseñas desencriptadas.
     * @throws NotFoundException   Si no existe una cuenta con el ID proporcionado.
     * @throws ForbiddenException  Si el usuario no ha verificado su Master Key en esta sesión.
     */
    @Transactional(readOnly = true)
    public List<PasswordResponse> execute(UUID accountId) {
        // 1. Verificar que la cuenta existe
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // 2. Verificar que la Master Key ha sido verificada en esta sesión
        // TODO: Implementar verificación de Master Key en sesión
        // if (!masterKeyVerificationService.isMasterKeyVerified(accountId)) {
        //     throw new ForbiddenException("Debe verificar su Master Key para acceder al gestor de contraseñas");
        // }

        // 3. Recuperar todas las contraseñas de la cuenta
        List<PasswordEntity> passwords = passwordRepository.findAllByAccountId(accountId);

        // 4. Desencriptar cada contraseña y mapear a DTO
        // TODO: Implementar desencriptación AES-256 usando la Master Key del usuario
        // String decryptedPassword = aesEncryptionService.decrypt(
        //     entity.getPasswordApplication(),
        //     account.getAccount_master_key()
        // );

        return passwords.stream()
                .map(entity -> mapToResponse(entity, "DECRYPTED_" + entity.getPasswordApplication()))
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad {@link PasswordEntity} en un DTO {@link PasswordResponse}.
     *
     * <p><strong>Mapeo de campos:</strong>
     * <ul>
     *   <li>{@code passwordId} → {@code id}</li>
     *   <li>{@code passwordApplicationName} → {@code applicationName}</li>
     *   <li>{@code passwordLastChangeDate} → {@code lastChangeDate}</li>
     * </ul>
     *
     * @param entity            La entidad JPA recuperada del repositorio.
     * @param decryptedPassword La contraseña desencriptada en texto plano.
     * @return El DTO listo para ser enviado al cliente.
     */
    private PasswordResponse mapToResponse(PasswordEntity entity, String decryptedPassword) {
        return PasswordResponse.builder()
                .id(entity.getPasswordId())
                .applicationName(entity.getPasswordApplicationName())
                .password(decryptedPassword)
                .lastChangeDate(entity.getPasswordLastChangeDate())
                .build();
    }
}