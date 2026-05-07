package org.adultofuncional.main.finances.infrastructure.controller;

import java.util.UUID;

import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/finances")
@RequiredArgsConstructor
public class FinancesController {

    //TODO: Inyectar los usecases cuando esten disponibles
    
    //private final CreateMovementUseCase createMovementUseCase;
    //private final GetMovementUseCase getMovementUseCase;
    //private final ListMovementsUseCase listMovementUseCase;
    //private final UpdateMovementUseCase updateMovementUseCase;
    //private final DeleteMovementUseCase deleteMovementUseCase;

    //private final CreateCategoryUseCase createCategoryUseCase;
    //private final GetCategoryUseCase getCategoryUseCase;
    //private final ListCategoriesUseCase listCategoriesUseCase;
    //private final UpdateCategoryUseCase updateCategoryUseCase;
    //private final DeleteCategoryUseCase deleteCategoryUseCase;

    //private final CreateFixedExpenseUseCase createFixedExpenseUseCase;
    //private final GetFixedExpenseUseCase getFixedExpenseUseCase;
    //private final ListFixedExpensesUseCase listFixedExpensesUseCase;
    //private final UpdateFixedExpenseUseCase updateFixedExpenseUseCase;
    //private final DeleteFixedExpenseUseCase deleteFixedExpenseUseCase;


    //Movimientos

    @PostMapping("/movements")
    public ResponseEntity<ApiResponse<Void>> createMovement(@AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con CreateMovementUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Registro de movimiento no implementado")
            .build());
    }

    @GetMapping("/movements/{id}")
    public ResponseEntity<ApiResponse<Void>> getMovement(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {
        
        //TODO: conectar con GetMovementUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Obtener movimiento no implementado")
            .build());
    }


    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<Void>> listMovements(@AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con ListMovementsUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Listar movimientos no implementado")
            .build());
    }


    @PatchMapping("/movements/{id}")
    public ResponseEntity<ApiResponse<Void>> updateMovement(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con UpdateMovementUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Actualizar movimiento no implementado")
            .build());
    }


    @DeleteMapping("/movements/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovement(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con DeleteMovementUseCase 

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Eliminar movimiento no implementado")
            .build());
    }

    //Categorias

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Void>> createCategory(@AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con CreateCategoryUseCase 

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Crear categoria no implementado")
            .build());
    }


    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> getCategory(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con GetCategoryUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Obtener categoria no implementado")
            .build());
    }


    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<Void>> listCategory(@AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con ListCategoriesUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Listar categorias no implementado")
            .build());
    }

    @PatchMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con UpdateCategoryUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Actualizar categoría no implementado")
            .build());
    }


    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con DeleteCategoryUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Eliminar categoria no implementado")
            .build());
    }


    //Gastos fijos

    @PostMapping("/fixed-expenses")
    public ResponseEntity<ApiResponse<Void>> createFixedExpense(@AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con CreateFixedExpenseUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Registrar gasto fijo no implementado")
            .build());
    }


    @GetMapping("/fixed-expenses/{id}")
    public ResponseEntity<ApiResponse<Void>> getFixedExpense(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con GetFixedExpenseUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Obtener gasto fijo no implementado")
            .build());
    }


    @GetMapping("/fixed-expenses")
    public ResponseEntity<ApiResponse<Void>> listFixedExpenses(@AuthenticationPrincipal String loggedEmail) {

        //TODO: conectar con ListFixedExpensesUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Listar gastos fijos no implementado")
            .build());
    }


    @PatchMapping("/fixed-expenses/{id}")
    public ResponseEntity<ApiResponse<Void>> updateFixedExpense(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {
        
        // TODO: conectar con UpdateFixedExpenseUseCase 
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Actualizar gasto fijo no implementado")
            .build());
    }


    @DeleteMapping("/fixed-expenses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFixedExpense(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {
        
        // TODO: conectar con DeleteFixedExpenseUseCase cuando esté implementado
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.<Void>builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .message("Eliminar gasto fijo no implementado")
            .build());
    }
    


}
