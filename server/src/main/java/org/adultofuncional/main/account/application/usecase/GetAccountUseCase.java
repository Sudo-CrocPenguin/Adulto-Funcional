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
 * Caso de uso: Obtener una cuenta por su identificador.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un servicio de aplicación (Application Service) que encapsula la lógica
 * de negocio para consultar una cuenta de usuario a partir de su ID.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve como intermediario entre la capa de presentación (controlador REST)
 * y la capa de persistencia (repositorio), aplicando reglas de negocio
 * como verificar que la cuenta exista antes de retornar sus datos.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * <ol>
 *   <li>Recibe un {@code UUID} con el identificador de la cuenta.</li>
 *   <li>Invoca al {@code AccountRepository} para buscar la entidad.</li>
 *   <li>Si la cuenta existe, mapea la entidad a {@code AccountResponse}.</li>
 *   <li>Si no existe, lanza {@code NotFoundException} (será manejada globalmente).</li>
 *   <li>Retorna el DTO con los datos no sensibles de la cuenta.</li>
 * </ol>
 *
 * <p><strong>Transaccionalidad:</strong><br>
 * {@code @Transactional(readOnly = true)} optimiza el rendimiento para operaciones
 * de solo lectura y evita bloqueos innecesarios en la base de datos.
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see AccountRepository
 * @see AccountEntity
 * @see AccountResponse
 * @see NotFoundException
 */
@Service
@RequiredArgsConstructor
public class GetAccountUseCase {

    private final AccountRepository accountRepository;

    /**
     * Ejecuta la consulta de una cuenta por su ID.
     *
     * @param accountId Identificador único de la cuenta (UUID). No puede ser {@code null}.
     * @return Un objeto {@link AccountResponse} con los datos de la cuenta (excluye campos sensibles).
     * @throws NotFoundException Si no existe ninguna cuenta con el ID proporcionado.
     *
     * //TODO: Agregar logs de auditoría para rastrear consultas de cuentas (quién, cuándo, qué ID)
     */
    @Transactional(readOnly = true)
    public AccountResponse execute(UUID accountId) {
        // Buscar la entidad en el repositorio de dominio
        AccountEntity entity = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // Mapear entidad a DTO de respuesta
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
     * <p><strong>¿Por qué se mapea?</strong><br>
     * Para separar la entidad JPA (que incluye relaciones y campos sensibles)
     * del DTO público (solo datos necesarios, seguros y serializables).
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