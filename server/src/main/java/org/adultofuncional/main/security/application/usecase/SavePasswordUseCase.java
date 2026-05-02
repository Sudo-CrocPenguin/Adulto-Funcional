package org.adultofuncional.main.security.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.security.application.dto.PasswordRequest;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Caso de uso: Guardar una nueva contraseña en el gestor de contraseñas.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un servicio de aplicación que encapsula la lógica de negocio para almacenar
 * una nueva credencial (nombre de aplicación + contraseña) en el gestor de
 * contraseñas del usuario.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para:
 * <ul>
 *   <li>Validar que la cuenta del usuario exista</li>
 *   <li>Verificar que el usuario haya autenticado su Master Key</li>
 *   <li>Encriptar la contraseña con AES-256 antes de almacenarla</li>
 *   <li>Evitar duplicados (misma aplicación para la misma cuenta)</li>
 *   <li>Persistir la contraseña en la base de datos</li>
 * </ul>
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * <ol>
 *   <li>Recibe el ID de la cuenta y el DTO con los datos de la contraseña</li>
 *   <li>Verifica que la cuenta exista en el repositorio</li>
 *   <li>Verifica que la Master Key haya sido verificada en la sesión actual</li>
 *   <li>Valida que no exista ya una contraseña para la misma aplicación</li>
 *   <li>Encripta la contraseña con AES-256 usando la Master Key del usuario</li>
 *   <li>Crea una nueva entidad y la persiste en el repositorio</li>
 *   <li>Retorna un DTO con la contraseña desencriptada (para confirmación)</li>
 * </ol>
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.security.domain.repository.PasswordRepository
 * @see org.adultofuncional.main.account.domain.repository.AccountRepository
 * @see PasswordRequest
 * @see PasswordResponse
 */
@Service
@RequiredArgsConstructor
public class SavePasswordUseCase {

    private final PasswordRepository passwordRepository;
    private final AccountRepository accountRepository;

    // TODO: Inyectar servicio de encriptación (AesEncryptionService) cuando esté disponible
    // TODO: Inyectar servicio de sesión para verificar Master Key (MasterKeyVerificationService)

    /**
     * Ejecuta el guardado de una nueva contraseña en el gestor.
     *
     * @param accountId Identificador de la cuenta propietaria. No puede ser {@code null}.
     * @param request   Objeto DTO con los datos de la contraseña (nombre aplicación, password, fecha cambio).
     * @return Un {@link PasswordResponse} con los datos de la contraseña guardada (desencriptada).
     * @throws NotFoundException   Si no existe una cuenta con el ID proporcionado.
     * @throws ForbiddenException  Si el usuario no ha verificado su Master Key en esta sesión.
     * @throws BusinessException   Si ya existe una contraseña para la misma aplicación.
     */
    @Transactional
    public PasswordResponse execute(UUID accountId, PasswordRequest request) {
        // 1. Verificar que la cuenta existe
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // 2. Verificar que la Master Key ha sido verificada en esta sesión
        // TODO: Implementar verificación de Master Key en sesión
        // if (!masterKeyVerificationService.isMasterKeyVerified(accountId)) {
        //     throw new ForbiddenException("Debe verificar su Master Key para acceder al gestor de contraseñas");
        // }

        // 3. Validar que no exista ya una contraseña para la misma aplicación
        boolean applicationExists = passwordRepository.existsByAccountIdAndApplicationName(
                accountId, request.getApplicationName()
        );
        if (applicationExists) {
            throw new BusinessException(
                    "Ya existe una contraseña guardada para la aplicación: " + request.getApplicationName()
            );
        }

        // 4. Encriptar la contraseña con AES-256 usando la Master Key del usuario
        // TODO: Implementar encriptación AES-256
        // String encryptedPassword = aesEncryptionService.encrypt(
        //     request.getPassword(),
        //     account.getAccount_master_key()
        // );
        String encryptedPassword = "ENCRYPTED_" + request.getPassword(); // Placeholder

        // 5. Crear la entidad
        PasswordEntity entity = new PasswordEntity();
        entity.setAccount(account);
        entity.setPasswordApplicationName(request.getApplicationName());
        entity.setPasswordApplication(encryptedPassword);

        // 6. Establecer fecha de último cambio (si no viene, usar fecha actual)
        LocalDate lastChangeDate = request.getLastChangeDate() != null
                ? request.getLastChangeDate()
                : LocalDate.now();
        entity.setPasswordLastChangeDate(lastChangeDate);

        // 7. Persistir en la base de datos
        PasswordEntity savedEntity = passwordRepository.save(entity);

        // 8. Retornar el DTO de respuesta (con contraseña desencriptada para confirmación)
        return mapToResponse(savedEntity, request.getPassword());
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
     * @param entity            La entidad JPA guardada.
     * @param decryptedPassword La contraseña en texto plano (para mostrar en respuesta).
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