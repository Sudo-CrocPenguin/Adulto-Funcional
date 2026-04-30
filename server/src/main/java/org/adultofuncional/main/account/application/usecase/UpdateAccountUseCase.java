package org.adultofuncional.main.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * {@code UpdateAccountUseCase} representa un caso de uso que permite modificar los datos
 * de una cuenta de usuario existente.
 *
 * <p><strong>Reglas de negocio implementadas:</strong>
 * <ul>
 *   <li>La cuenta debe existir previamente (de lo contrario, lanza {@link NotFoundException}).</li>
 *   <li>Si se cambia el email, se verifica que el nuevo email no esté ya en uso por otra cuenta diferente.</li>
 *   <li>Los campos actualizables son: nombres, apellidos, teléfono y email.</li>
 *   <li>La contraseña y la master key NO se pueden modificar desde este caso de uso.</li>
 * </ul>
 *
 * <p><strong>Flujo de ejecución:</strong>
 * <ol>
 *   <li>El controlador recibe una solicitud PUT/PATCH con el ID en la URL y el DTO {@link UpdateAccountRequest} en el cuerpo.</li>
 *   <li>El controlador invoca {@code updateAccountUseCase.execute(id, request)}.</li>
 *   <li>El caso de uso busca la entidad actual en el repositorio.</li>
 *   <li>Si el email nuevo es distinto al actual, se comprueba su unicidad.</li>
 *   <li>Se actualizan los campos permitidos y se guarda la entidad modificada.</li>
 *   <li>Se retorna un {@link AccountResponse} con los datos actualizados.</li>
 * </ol>
 *
 * <p><strong>Seguridad:</strong>
 * Este caso de uso no recibe ni modifica campos sensibles. Para cambiar la contraseña
 * o la master key se deben implementar casos de uso separados (ej. {@code ChangePasswordUseCase}).
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
     * @param accountId Identificador de la cuenta a modificar. No puede ser nulo.
     * @param request   Objeto DTO con los nuevos valores (nombres, apellidos, teléfono, email).
     * @return Un {@link AccountResponse} con los datos actualizados de la cuenta.
     * @throws NotFoundException Si no existe una cuenta con el ID proporcionado.
     * @throws BusinessException Si el nuevo email ya está registrado por otra cuenta distinta.
     */
    @Transactional
    public AccountResponse execute(UUID accountId, UpdateAccountRequest request) {
        // 1. Recuperar la entidad existente
        AccountEntity entity = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // 2. Validar unicidad del email si ha cambiado
        if (!entity.getAccount_email().equals(request.getEmail())) {
            boolean emailExists = accountRepository.existsByEmail(request.getEmail());
            if (emailExists) {
                throw new BusinessException("El email " + request.getEmail() + " ya está registrado por otra cuenta");
            }
            // Actualizar el email (solo si es diferente y está disponible)
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
     * <p><strong>Mapeo de campos:</strong>
     * <ul>
     *   <li>{@code account_id} → {@code id}</li>
     *   <li>{@code account_names} → {@code names}</li>
     *   <li>{@code account_lastnames} → {@code lastnames}</li>
     *   <li>{@code account_email} → {@code email}</li>
     *   <li>{@code account_phone} → {@code phone}</li>
     *   <li>{@code account_created_at} → {@code createdAt}</li>
     * </ul>
     *
     * @param entity La entidad actualizada (o la original) a convertir.
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
}