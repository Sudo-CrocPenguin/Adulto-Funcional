package org.adultofuncional.main.account.infrastructure.controller;

//por ahora en comentarios hasta que esten disponibles
//import org.adultofuncional.main.account.application.usecase.GetAccountUseCase;
// import org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase;
//import org.adultofuncional.main.account.application.dto.AccountResponse;
//import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/*controller expone los endpoints http del modulo account

endpoints disponibles:

GET   /api/account{id}  -> para obtener los datos de una cuenta
PUT  /api/account{id}  -> para actualizar los datos 
DELETE  /api/account{id}  -> para eliminar una cuenta

IMPORTANTE: el controller nunca retorna passwords ni master keys
*/

@RestController 
@RequestMapping("/api/account")
public class AccountController {

    /*por ahora en comentarios hasta que los use cases esten disponibles
    
    private final GetAccountUseCase getAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    
    REEMPLAZAR este constructor por el real cuando los use cases esten listos

    public AccountController(GetAccountUseCase getAccountUseCase, UpdateAccountUseCase updateAccountUseCase) {
        this.getAccountUseCase = getAccountUseCase;
        this.updateAccountUseCase = updateAccountUseCase;
        
    }

    */

    /*obtener los datos de una cuenta por su id
    @param id  -> el UUID de la cuenta
    @return  -> los datos de la cuenta sin informacion sensible
    
    */

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAccount(@PathVariable UUID id) {
        /*REEMPLAZAR cuando GetAccountUseCase este disponible
        
        AccountResponse response = getAccountUseCase.excute(id);
        return ResponseEntity.ok(response);
        
        */

        return ResponseEntity.ok(null); //cuando funcione las lineas de arriba se elimina esta
    }

    /*
    eliminar una cuenta por su id
    @param id  -> el UUID de la cuenta a eliminar
    @return -> respuesta vacia con status 204
    */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        // agregar DeleteAccountUseCase cuando este disponible:
        //quedaria mas o menos asi:
        //deleteAccountUseCase.execute(id);

        return ResponseEntity.noContent().build(); 
    }

}