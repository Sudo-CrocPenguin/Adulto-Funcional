/**
 * Capa de dominio del módulo de cuentas.
 *
 * <p>
 * Contiene el modelo de cuenta
 * {@link org.adultofuncional.main.account.domain.model.Account} y
 * el puerto de repositorio
 * {@link org.adultofuncional.main.account.domain.repository.AccountRepository}.
 * Todo el código de esta capa es puro Java, sin anotaciones de Spring ni
 * dependencias de infraestructura,
 * garantizando la independencia de detalles externos.
 * </p>
 *
 * <h2>Modelo de dominio</h2>
 * {@code Account} representa la cuenta de usuario y protege sus invariantes de
 * estado:
 * <ul>
 * <li>Los campos {@code id} y {@code createdAt} nunca pueden ser nulos, y
 * {@code createdAt} no puede
 * estar en el futuro.</li>
 * <li>El ID se genera dentro del dominio mediante
 * {@link com.fasterxml.uuid.Generators#timeBasedEpochGenerator()
 * UUID v7}, asegurando ordenabilidad temporal sin depender de la base de datos.
 * El dominio es dueño
 * de la identidad.</li>
 * <li>Las contraseñas y la clave maestra opcional se almacenan únicamente como
 * hash Argon2; el dominio
 * no tiene conocimiento del texto plano ni del mecanismo de hash.</li>
 * <li>La construcción está controlada por métodos de fábrica:
 * <ul>
 * <li>{@code create(…)} para una cuenta nueva (genera ID y
 * {@code createdAt}).</li>
 * <li>{@code reconstitute(…)} para restaurar una cuenta desde la base de
 * datos.</li>
 * </ul>
 * </li>
 * <li>Métodos de actualización ({@code updateDetails}, {@code updateEmail})
 * encapsulan el cambio de
 * estado, delegando a los casos de uso la verificación de reglas de negocio
 * adicionales (por ejemplo,
 * la unicidad del email).</li>
 * <li>Las validaciones de formato (longitud, estructura del email, caracteres
 * válidos) pertenecen a los
 * DTOs de la capa de aplicación y no se repiten aquí.</li>
 * </ul>
 *
 * <h2>Puerto de repositorio</h2>
 * {@code AccountRepository} define las operaciones de persistencia que necesita
 * el dominio:
 * <ul>
 * <li>{@code save}, {@code findById}, {@code findByEmail}, {@code findAll},
 * {@code deleteById}.</li>
 * <li>Todas trabajan exclusivamente con el modelo de dominio {@code Account},
 * nunca con entidades JPA.</li>
 * <li>La implementación concreta reside en la capa de infraestructura
 * ({@code AccountRepositoryImpl}) y se inyecta mediante el contenedor de
 * Spring.</li>
 * </ul>
 *
 * <p>
 * Esta capa no depende de ningún framework externo (Spring, JPA, etc.) ni de la
 * capa de aplicación,
 * cumpliendo el principio de Clean Architecture.
 * </p>
 *
 * @author Jeronimo Ospina Zapata, Daniel Salazar, Juan Sebastian Rios
 * @since 0.0.1
 */
package org.adultofuncional.main.account.domain;
