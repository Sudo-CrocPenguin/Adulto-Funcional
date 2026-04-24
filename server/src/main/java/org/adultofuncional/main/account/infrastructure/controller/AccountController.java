package org.adultofuncional.main.account.infrastructure.controller;

//por ahora en comentarios hasta que esten disponibles
//import org.adultofuncional.main.account.application.usecase.GetAccountUseCase;
// import org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase;
//import org.adultofuncional.main.account.application.dto.AccountResponse;
//import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

    /**
     * Controller que expone los endpoints HTTP del módulo account.
     *
     * <p>Endpoints disponibles:
     * <ul>
     *   <li>{@code GET    /api/account/{id}} → obtener datos de una cuenta</li>
     *   <li>{@code PUT    /api/account/{id}} → actualizar datos de una cuenta</li>
     *   <li>{@code DELETE /api/account/{id}} → eliminar una cuenta</li>
     * </ul>
     *
     * <p><strong>Seguridad:</strong> este controller nunca retorna
     * {@code password} ni {@code master key} en las respuestas.
     *
     * @author Lidys Jaraba
     * @since 0.0.1
    */

@RestController 
@RequestMapping("/api/account")
public class AccountController {

    /*por ahora en comentarios hasta que los use cases esten disponibles
    
    private final GetAccountUseCase getAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    
    public AccountController(GetAccountUseCase getAccountUseCase, UpdateAccountUseCase updateAccountUseCase) {
        this.getAccountUseCase = getAccountUseCase;
        this.updateAccountUseCase = updateAccountUseCase;
        
    }

    */

    /**
     * Obtiene los datos de una cuenta por su ID.
     *
     * <p>Retorna la información de la cuenta sin exponer
     * datos sensibles como password o master key.
     *
     * <p><strong>Pendiente:</strong> conectar con {@code GetAccountUseCase}
     * cuando esté disponible.
     *
     * @param id el UUID de la cuenta a consultar
     * @return los datos de la cuenta con status 200, o null como placeholder temporal
    */


    @GetMapping("/{id}")
    public ResponseEntity<Object> getAccount(@PathVariable UUID id) {
        /*REEMPLAZAR cuando GetAccountUseCase este disponible
        
        AccountResponse response = getAccountUseCase.excute(id);
        return ResponseEntity.ok(response);
        
        */

        return ResponseEntity.ok(null); //cuando funcione las lineas de arriba se elimina esta
    }

    /**
     * Actualiza los datos de una cuenta.
     *
     * <p>Recibe los nuevos datos en el cuerpo de la petición
     * y retorna la cuenta actualizada.
     *
     * <p><strong>Pendiente:</strong> conectar con {@code UpdateAccountUseCase}
     * cuando esté disponible.
     *
     * @param id el UUID de la cuenta a actualizar
     * @param request los nuevos datos de la cuenta
     * @return los datos actualizados con status 200, o null como placeholder temporal
    */

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAccount ( @PathVariable UUID id, @RequestBody Object request) {

         /*
            AccountResponse response = updateAccountUseCase.excute(id, request);
            return ResponseEntity.ok(response)

        */

        return ResponseEntity.ok(null); 
    }


    /**
     * Elimina una cuenta por su ID.
     *
     * <p>Una vez eliminada, se eliminan en cascada todos los datos
     * asociados a la cuenta como movimientos, eventos y contraseñas.
     *
     * <p><strong>Pendiente:</strong> conectar con {@code DeleteAccountUseCase}
     * cuando esté disponible.
     *
     * @param id el UUID de la cuenta a eliminar
     * @return respuesta vacía con status 204
    */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        // agregar DeleteAccountUseCase cuando este disponible:
        //deleteAccountUseCase.execute(id);

        return ResponseEntity.noContent().build(); 
    }

}