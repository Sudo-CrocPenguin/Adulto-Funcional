package org.adultofuncional.main.account.infrastructure.persistence.mapper;

//por ahora en comentario hasta que estén disponibles
//import org.adultofuncional.main.account.domain.model.Account;
//import org.adultofuncional.main.account.application.dto.AccountResponse;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.springframework.stereotype.Component;

    /**
     * Mapper que traduce entre las distintas representaciones de una cuenta.
     *
     * <p>Convierte entre:
     * <ul>
     *   <li>{@link AccountEntity} (capa de persistencia) hacia Account (dominio)</li>
     *   <li>Account (dominio) hacia AccountResponse (DTO para el frontend)</li>
     * </ul>
     *
     * <p><strong> Seguridad:</strong> este mapper nunca expone
     * {@code account_password} ni {@code account_master_key} en las respuestas.
     *
     * @author Lidys Jaraba
     * @since 0.0.1
     * @see AccountEntity
    */

@Component
public class AccountMapper {

    /**
     * Convierte una entidad de base de datos al modelo de dominio.
     *
     * <p>Traduce los campos de {@link AccountEntity} a un objeto Account
     * del dominio, excluyendo información sensible como password y master key.
     *
     * <p><strong> Pendiente:</strong> reemplazar Object por Account
     * cuando {@code Account.java} esté disponible.
     *
     * @param entity la entidad JPA de la cuenta a convertir
     * @return el modelo de dominio Account, o {@code null} si entity es null
    */

    public Object toDomain(AccountEntity entity) {
        if (entity == null) return null;

        /*Descomentar este bloque cuando Account.java esté creado:
        
        Account account = new Account();
        account.setId(entity.getAccount_id());
        account.setNames(entity.getAccount_names());
        account.setLastnames(entity.getAccount_lastnames());
        account.setEmail(entity.getAccount_email());
        account.setPhone(entity.getAccount_phone());
        account.setCreatedAt(entity.getAccount_created_at());

        return account;

        */

        return null;  //cuando funcione el bloque de arriba se elimina este return
    }

    /**
     * Convierte el modelo de dominio al DTO de respuesta para el frontend.
     *
     * <p>Traduce los campos de Account a un objeto AccountResponse
     * listo para ser enviado como respuesta HTTP.
     *
     * <p><strong>Seguridad:</strong> nunca incluir {@code password}
     * ni {@code master key} en la respuesta.
     *
     * <p><strong>Pendiente:</strong> reemplazar ambos Object por Account
     * y AccountResponse cuando los DTOs estén disponibles.
     *
     * @param account el modelo de dominio de la cuenta
     * @return el DTO AccountResponse, o {@code null} si account es null
    */

    public Object toResponse(Object account) {
        if (account == null) return null;

        /*
        
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setNames(account.getNames());
        response.setLastnames(account.getLastnames());
        response.setEmail(account.getEmail());
        response.setPhone(account.getPhone());
        response.setCreatedAt(account.getCreatedAt());

        return response;
        */

        return null; 
    }
}