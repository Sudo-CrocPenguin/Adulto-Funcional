package org.adultofuncional.main.account.infrastructure.persistence.mapper;

//por ahora en comentario hasta que estén disponibles
//import org.adultofuncional.main.account.domain.model.Account;
//import org.adultofuncional.main.account.application.dto.AccountResponse;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.springframework.stereotype.Component;

/*Mapper para convertir entre las distintas representaciones de una cuenta

traduce entre:
{@Link AccountEntity} (capa de persistencia) <-> Account (dominio)
Account (dominio) -> AccountResponse (DTO para el frontend)

IMPORTANTE: el mapper no expone nunca account_password ni account_master_key
*/

@Component
public class AccountMapper {

    /*
    convierte una entidad de base de datos al modelo de dominio

    @param entity -> la entidad JPA de la cuenta
    @return -> el modelo de dominio Account

    RECUERDA (para mi): cambiar Object por Account cuando el modelo de dominio esté disponible
    */

    public Object toDomain(AccountEntity entity) {
        if (entity == null) return null;

        /*RECUERDA (para mi): reemplazar este bloque cuando Account.java esté creado:
        
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

    /*
    convierte el modelo de dominio al DTO de respuesta para el frontend

    IMPORTANTE: nunca incluir password ni master key en la respuesta

    @param account   ->  el modelo de dominio
    @return  -> el DTO AccountResponse

    RECUERDA (para mi): reemplazar ambos objetos por Account y AccountResponse cuando los DTO estén listos

    */

    public Object toResponse(Object account) {
        if (account == null) return null;

        /*Recuerda reemplazar este bloque cuando AccountResponse.java esté creado
        
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setNames(account.getNames());
        response.setLastnames(account.getLastnames());
        response.setEmail(account.getEmail());
        response.setPhone(account.getPhone());
        response.setCreatedAt(account.getCreatedAt());

        return response;
        */

        return null; //cuando funcione el bloque de arriba se elimina este return
    }
}