package org.adultofuncional.main.account.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
 * <p><strong>Pendiente:</strong> ampliar tests cuando
 * {@code Account.java} y {@code AccountResponse.java} estén disponibles.
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see AccountMapper
 */

@DisplayName("AccountMapper - Tests Unitarios")
class AccountMapperTest {

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
        void TestToDomainReturnsNullWhenEntityIsNull() {

            //Given - entidad null
            AccountEntity entity = null;

            //When - se llama al mapper
            Object result = mapper.toDomain(entity);

            //Then - debe retornar null
            assertNull( result , "toDomain() debe retornar null cuando recibe null");
        }

        @Test
        @DisplayName("Debe retornarl null como placecholder cuando la entidad es válida")
        void TestToDomainReturnsNullPlacecholder() {

            //Given - entidad validada
            AccountEntity entity = new AccountEntity();
            entity.setAccount_names("Juan");
            entity.setAccount_lastnames("Pérez");
            entity.setAccount_email("juan@email.com");
            entity.setAccount_phone("3001234567");
            entity.setAccount_password("hashedPassword");

            Object result = mapper.toDomain(entity);

            //Then - retorna null porque es placecholder temporal
            assertNull(result, "toDomain() retornar null mientras Account.java no esté disponible");


        }
    }

    @Nested
    @DisplayName("toResponse()")
    class ToResponse {

        @Test
        @DisplayName("Debe retornar null cuando el account es null")
        void TestToResponseReturnsNullWhenAccountIsNull() {

            Object account = null;

            Object result = mapper.toResponse(account);

            assertNull(result , "toResponse() debe retornal null cuando recibe null");

        }

        @Test
        @DisplayName("Debe retornar null como placecholder cuando el account es válido")
        void TestToResponseReturnsNullPlacecholder() {
            //Given - un objecto cualquiera simulando un account
            Object account = new Object();

            Object result = mapper.toResponse(account);

            assertNull(result, "toResponse() retona null mientras AccountResponse.java no esté disponible");
        }
    }
}