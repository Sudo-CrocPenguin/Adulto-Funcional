package org.adultofuncional.main.account.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


/**
 * Tests unitarios para {@link AccountMapper}.
 *
 * <p>Verifica el comportamiento actual del esqueleto:
 * <ul>
 *   <li>Manejo de valores null en {@code toDomain()}</li>
 *   <li>Manejo de valores null en {@code toResponse()}</li>
 * </ul>
 *
 * 
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see AccountMapper
 */

@DisplayName("AccountMapper - Tests Unitarios")
class AccountMapperTest {

    // TODO: ampliar tests cuando Account.java y AccountResponse.java estén disponibles

    private AccountMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AccountMapper();

    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomain {

        @Test
        @DisplayName("Debe retornar null cuando la entidad es null")
        void testToDomainReturnsNullWhenEntityIsNull() {

            //Given - entidad null
            AccountEntity entity = null;

            //When - se llama al mapper
            Object result = mapper.toDomain(entity);

            //Then - debe retornar null
            assertNull( result , "toDomain() debe retornar null cuando recibe null");
        }

        @Test
        @DisplayName("Debe retornar null como placeholder cuando la entidad es válida")
        void testToDomainReturnsNullPlaceholder() {

            //Given - entidad validada
            AccountEntity entity = new AccountEntity();
            entity.setAccount_names("Juan");
            entity.setAccount_lastnames("Pérez");
            entity.setAccount_email("juan@email.com");
            entity.setAccount_phone("3001234567");
            entity.setAccount_password("hashedPassword");

            // When - se llama al mapper
            Object result = mapper.toDomain(entity);

            //Then - retorna null porque es placeholder temporal
            assertNull(result, "toDomain() retornar null mientras Account.java no esté disponible");


        }
    }

    @Nested
    @DisplayName("toResponse()")
    class ToResponse {

        @Test
        @DisplayName("Debe retornar null cuando el account es null")
        void testToResponseReturnsNullWhenAccountIsNull() {

            // Given - account null
            Object account = null;

            // When - se llama al mapper
            Object result = mapper.toResponse(account);

            // Then - debe retornar null
            assertNull(result , "toResponse() debe retornar null cuando recibe null");

        }

        @Test
        @DisplayName("Debe retornar null como placeholder cuando el account es válido")
        void testToResponseReturnsNullPlaceholder() {
            //Given - un objeto cualquiera simulando un account
            Object account = new Object();

            // When - se llama al mapper
            Object result = mapper.toResponse(account);

            // Then - retorna null porque es placeholder temporal
            assertNull(result, "toResponse() retornar null mientras AccountResponse.java no esté disponible");
        }
    }
}