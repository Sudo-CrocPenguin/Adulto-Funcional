package org.adultofuncional.main.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;
import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Actualizar los datos de una cuenta existente.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un servicio de aplicación que encapsula la lógica de negocio para modificar
 * la información de una cuenta de usuario (nombres, apellidos, teléfono, email).
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para aplicar reglas de negocio antes de persistir los cambios:
 * <ul>
 *   <li>Validar que la cuenta exista</li>
 *   <li>Verificar unicidad del email (si cambia)</li>
 *   <li>Proteger campos sensibles (no se permiten modificar contraseña ni master key)</li>
 * </ul>
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * <ol>
 *   <li>Recibe el ID de la cuenta y el DTO con los nuevos datos.</li>
 *   <li>Busca la cuenta existente en el repositorio.</li>
 *   <li>Si no existe → lanza {@code NotFoundException}.</li>
 *   <li>Si el email cambió y ya existe en otra cuenta → lanza {@code BusinessException}.</li>
 *   <li>Actualiza los campos permitidos en la entidad.</li>
 *   <li>Persiste los cambios mediante el repositorio.</li>
 *   <li>Retorna un {@code AccountResponse} con los datos actualizados.</li>
 * </ol>
 *
 * <p><strong>Reglas de negocio implementadas:</strong>
 * <ul>
 *   <li>El email debe ser único en todo el sistema (no puede haber dos cuentas con el mismo email).</li>
 *   <li>Si el email no cambia, no se verifica unicidad.</li>
 *   <li>Campos que se pueden actualizar: nombres, apellidos, teléfono, email.</li>
 *   <li>Campos que NO se pueden actualizar aquí: contraseña, master key.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see AccountRepository
 * @see AccountEntity
 * @see UpdateAccountRequest
 * @see AccountResponse
 * @see BusinessException
 * @see NotFoundException
 */
@Service
@RequiredArgsConstructor
public class UpdateAccountUseCase {

    private final AccountRepository accountRepository;

    /**
     * Ejecuta la actualización de los datos de una cuenta.
     *
     * @param accountId Identificador de la cuenta a modificar. No puede ser {@code null}.
     * @param request   Objeto DTO con los nuevos valores (nombres, apellidos, teléfono, email).
     * @return Un {@link AccountResponse} con los datos actualizados de la cuenta.
     * @throws NotFoundException Si no existe una cuenta con el ID proporcionado.
     * @throws BusinessException Si el nuevo email ya está registrado por otra cuenta distinta.
     *
     * //TODO: Agregar validación para evitar que el email se cambie a menos que el usuario confirme con contraseña
     * //TODO: Agregar logs de auditoría para cambios en datos de cuenta (usuario que modifica)
     * //TODO: Considerar enviar notificación por email al usuario cuando se cambia el correo electrónico
     */
    @Transactional
    public AccountResponse execute1(UUID accountId, Object request) {
        // 1. Recuperar la entidad existente
        Account entity = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // 2. Validar unicidad del email si ha cambiado
        if (!entity.getEmail().equals(request.getEmail())) {
            boolean emailExists = accountRepository.findByEmail(request.getEmail()) != null;
            if (emailExists) {
                throw new BusinessException("El email " + request.getEmail() + " ya está registrado por otra cuenta");
            }
            entity.setAccount_email(request.getEmail());
        }

        // 3. Actualizar los demás campos permitidos
        entity.setAccount_names(request.getNames());
        entity.setAccount_lastnames(request.getLastnames());
        entity.setAccount_phone(request.getPhone());

        // 4. Persistir los cambios
        AccountEntity updatedEntity = accountRepository.save(entity);

        // 5. Devolver la respuesta mapeada
        return mapToResponse(updatedEntity);
    }

    /**
     * Convierte una entidad {@link AccountEntity} en un DTO de respuesta {@link AccountResponse}.
     *
     * <p><strong>¿Por qué es necesario?</strong><br>
     * Para exponer solo los datos no sensibles al cliente y mantener el principio de
     * separación de capas (la entidad JPA no debe serializarse directamente).
     *
     * @param entity La entidad actualizada a convertir.
     * @return DTO listo para enviar al cliente.
     */
    private AccountResponse mapToResponse(AccountEntity entity) {
        return AccountResponse.builder()
                .id(entity.getAccount_id())
                .names(entity.getAccount_names())
                .lastnames(entity.getAccount_lastnames())
                .email(entity.getAccount_email())
                .phone(entity.getAccount_phone())
                .createdAt(entity.getAccount_created_at())
                .build();
    }

	public AccountResponse execute(UUID id, Object request) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'execute'");
	}
}