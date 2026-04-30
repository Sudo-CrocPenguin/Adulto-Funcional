package org.adultofuncional.main.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * {@code GetAccountUseCase} representa un caso de uso de la aplicación que permite
 * recuperar la información de una cuenta de usuario a partir de su identificador único.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Validar que la cuenta exista en el repositorio.</li>
 *   <li>Mapear la entidad {@link AccountEntity} a un DTO {@link AccountResponse}.</li>
 *   <li>Lanzar una excepción si la cuenta no se encuentra.</li>
 * </ul>
 *
 * <p><strong>Flujo de ejecución:</strong>
 * <ol>
 *   <li>El controlador REST recibe una solicitud GET con el ID de la cuenta (por ejemplo, {@code /api/accounts/{id}}).</li>
 *   <li>El controlador invoca {@code getAccountUseCase.execute(id)}.</li>
 *   <li>El caso de uso busca la entidad en {@link AccountRepository}.</li>
 *   <li>Si existe, la convierte en {@code AccountResponse} y la retorna.</li>
 *   <li>Si no existe, lanza {@link NotFoundException} que será manejada globalmente.</li>
 * </ol>
 *
 * <p><strong>Transaccionalidad:</strong>
 * La anotación {@code @Transactional(readOnly = true)} optimiza el rendimiento para
 * operaciones de solo lectura y evita bloqueos innecesarios.
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see AccountRepository
 * @see AccountEntity
 * @see AccountResponse
 */
@Service
@RequiredArgsConstructor
public class GetAccountUseCase {

    private final AccountRepository accountRepository;

    /**
     * Ejecuta la consulta de una cuenta por su ID.
     *
     * @param accountId Identificador único de la cuenta (UUID). No puede ser nulo.
     * @return Un objeto {@link AccountResponse} con los datos no sensibles de la cuenta.
     * @throws NotFoundException Si no existe ninguna cuenta con el ID proporcionado.
     */
    @Transactional(readOnly = true)
    public AccountResponse execute(UUID accountId) {
        // Buscar la entidad en el repositorio de dominio
        AccountEntity entity = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // Mapear entidad -> DTO de respuesta
        return mapToResponse(entity);
    }

    /**
     * Convierte una entidad {@link AccountEntity} en un DTO {@link AccountResponse}.
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
     * @param entity La entidad JPA recuperada del repositorio.
     * @return El DTO listo para ser enviado al cliente.
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