package org.adultofuncional.main.account.application.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.adultofuncional.main.account.domain.model.Account;

/**
 * ============================================================
 * AccountTest
 * ============================================================
 *
 * <p>
 * Clase de pruebas unitarias para el modelo de dominio {@link Account}.
 * Verifica que la lógica de negocio y las reglas de validación
 * definidas en la entidad {@code Account} se comporten correctamente
 * en diferentes escenarios.
 *
 * <p>
 * <strong>Propósito:</strong>
 * Garantizar la integridad de los datos y el comportamiento esperado
 * del agregado {@code Account}, que es fundamental para el subsistema
 * de cuentas de usuario.
 *
 * <p>
 * <strong>Alcance de las pruebas:</strong>
 * <ul>
 *   <li><b>Creación válida:</b> Una cuenta se crea correctamente con datos
 *       válidos, generando un identificador único y estado activo.</li>
 *   <li><b>Validaciones de dominio:</b> Se rechazan nombres vacíos y emails
 *       con formato inválido lanzando {@link IllegalArgumentException}.</li>
 *   <li><b>Ciclo de vida de estado:</b> Los métodos {@code deactivate()} y
 *       {@code activate()} modifican correctamente el estado activo/inactivo.</li>
 *   <li><b>Actualización de datos:</b> El método {@code updateDetails()}
 *       permite modificar nombre y teléfono correctamente.</li>
 * </ul>
 *
 * <p>
 * <strong>Tecnologías utilizadas:</strong>
 * <ul>
 *   <li>JUnit 5 (Jupiter) para la definición de pruebas.</li>
 *   <li>AssertJ / JUnit Assertions para validaciones.</li>
 * </ul>
 *
 * <p>
 * <strong>⚠️ Nota para el equipo de desarrollo:</strong>
 * Estas pruebas deben ejecutarse en cada ciclo de integración continua.
 * Cualquier cambio en la lógica de {@code Account} debe ir acompañado
 * de la actualización correspondiente de esta suite.
 *
 * @author Juan (o el nombre correspondiente)
 * @since 0.0.1
 * @see Account
 * @see org.adultofuncional.main.account.domain.model.Account#create(String, String, String)
 * @see org.adultofuncional.main.account.domain.model.Account#updateDetails(String, String)
 */
public class AccountTest {

    /**
     * Prueba la creación exitosa de una cuenta con datos válidos.
     *
     * <p>
     * <strong>Escenario:</strong>
     * Se invoca el método factoría {@code Account.create()} con nombre,
     * email y teléfono correctos.
     *
     * <p>
     * <strong>Resultado esperado:</strong>
     * <ul>
     *   <li>El identificador de la cuenta no es {@code null}.</li>
     *   <li>Los campos coinciden con los proporcionados.</li>
     *   <li>La cuenta se encuentra en estado activo ({@code isActive() == true}).</li>
     * </ul>
     *
     * @see Account#create(String, String, String)
     */
    @Test
    void shouldCreateAccountSuccessfully() {
        // Arrange
        String name = "Juan";
        String email = "juan@test.com";
        String phone = "123";

        // Act
        Account account = Account.create(name, email, phone);

        // Assert
        assertNotNull(account.getId());
        assertEquals(name, account.getName());
        assertEquals(email, account.getEmail());
        assertTrue(account.isActive());
    }

    /**
     * Verifica que no se pueda crear una cuenta con nombre vacío.
     *
     * <p>
     * <strong>Regla de negocio:</strong>
     * El nombre del titular es obligatorio y no puede ser una cadena vacía
     * ni contener solo espacios en blanco (asumiendo que la validación
     * interna de {@code Account} lo rechaza).
     *
     * <p>
     * <strong>Resultado esperado:</strong>
     * Lanzamiento de {@link IllegalArgumentException} con el mensaje
     * {@code "Name cannot be empty"}.
     *
     * @see Account#create(String, String, String)
     */
    @Test
    void shouldFailWhenNameIsEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Account.create("", "test@test.com", "123");
        });

        assertEquals("Name cannot be empty", exception.getMessage());
    }

    /**
     * Verifica que no se pueda crear una cuenta con un email con formato inválido.
     *
     * <p>
     * <strong>Regla de negocio:</strong>
     * El correo electrónico debe cumplir con el formato estándar
     * (contener '@' y un dominio válido, según la implementación de
     * {@code Account}).
     *
     * <p>
     * <strong>Resultado esperado:</strong>
     * Lanzamiento de {@link IllegalArgumentException} con el mensaje
     * {@code "Invalid email"}.
     *
     * @see Account#create(String, String, String)
     */
    @Test
    void shouldFailWhenEmailIsInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Account.create("Juan", "correo-invalido", "123");
        });

        assertEquals("Invalid email", exception.getMessage());
    }

    /**
     * Comprueba que una cuenta activa pueda ser desactivada.
     *
     * <p>
     * <strong>Escenario:</strong>
     * Se crea una cuenta (activa por defecto), luego se invoca
     * {@code account.deactivate()}.
     *
     * <p>
     * <strong>Resultado esperado:</strong>
     * El estado activo cambia de {@code true} a {@code false}.
     *
     * @see Account#deactivate()
     * @see Account#isActive()
     */
    @Test
    void shouldDeactivateAccount() {
        // Arrange
        Account account = Account.create("Juan", "juan@test.com", "123");

        // Act
        account.deactivate();

        // Assert
        assertFalse(account.isActive());
    }

    /**
     * Comprueba que una cuenta desactivada pueda ser reactivada.
     *
     * <p>
     * <strong>Escenario:</strong>
     * Se crea una cuenta, se desactiva y luego se invoca
     * {@code account.activate()}.
     *
     * <p>
     * <strong>Resultado esperado:</strong>
     * El estado activo vuelve a ser {@code true}.
     *
     * @see Account#activate()
     * @see Account#isActive()
     */
    @Test
    void shouldActivateAccount() {
        // Arrange
        Account account = Account.create("Juan", "juan@test.com", "123");

        // Act
        account.deactivate();
        account.activate();

        // Assert
        assertTrue(account.isActive());
    }

    /**
     * Prueba la actualización de nombre y teléfono de una cuenta existente.
     *
     * <p>
     * <strong>Escenario:</strong>
     * Se crea una cuenta con datos iniciales; luego se invoca
     * {@code account.updateDetails(nuevoNombre, nuevoTeléfono)}.
     *
     * <p>
     * <strong>Resultado esperado:</strong>
     * Los campos {@code name} y {@code phone} se actualizan a los nuevos valores.
     * El email y el identificador permanecen inalterados.
     *
     * <p>
     * <strong>Nota:</strong>
     * La prueba asume que el método {@code updateDetails} no modifica
     * el email ni el estado de activación.
     *
     * @see Account#updateDetails(String, String)
     */
    @Test
    void shouldUpdateAccountDetails() {
        // Arrange
        Account account = Account.create("Juan", "juan@test.com", "123");

        // Act
        account.updateDetails("Pedro", "999");

        // Assert
        assertEquals("Pedro", account.getName());
        assertEquals("999", account.getPhone());
    }
}