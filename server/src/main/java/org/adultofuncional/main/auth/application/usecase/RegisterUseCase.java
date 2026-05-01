package org.adultofuncional.main.auth.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
//TODO: descomentar cuando JwtService esté implementado
//import org.adultofuncional.main.config.security.JwtService;

/**
 * Caso de uso encargado de registrar un nuevo usuario en el sistema.
 * 
 * <p><strong>¿Qué hace?</strong><br>
 * Orquesta el flujo completo de registro: valida que el correo no esté
 * en uso, hashea las credenciales sensibles, crea la cuenta en dominio,
 * la persiste y genera un token JWT para que el usuario quede autenticado
 * inmediatamente después de registrarse.
 *
 * <p><strong>Flujo:</strong>
 * <ol>
 *   <li>Verifica unicidad del correo electrónico.</li>
 *   <li>Hashea la contraseña (y la Master Key si se proporcionó).</li>
 *   <li>Crea el modelo de dominio {@link Account}.</li>
 *   <li>Persiste la cuenta mediante {@link AccountRepository}.</li>
 *   <li>Genera el token JWT con {@link JwtService}.</li>
 *   <li>Retorna un {@link AuthResponse} con el token y los datos de la cuenta.</li>
 * </ol>
 *
 * <p><strong>Excepciones lanzadas:</strong>
 * <ul>
 *   <li>{@link BusinessException} si el correo ya está registrado.</li>
 * </ul>
 *
 * @author Lidys Jaraba
 * @version 1.0
 * @since 0.0.1
 * @see RegisterRequest
 * @see AuthResponse
 * @see AccountRepository
 * @see JwtService
 */

@Service
public class RegisterUseCase {
    
    /** Repositorio para verificar existencia y persistir la cuenta. */
    private final AccountRepository accountRepository;

    /** Encoder para hashear la contraseña y la Master Key con Argon2. */
    private final PasswordEncoder passwordEncoder;

    /** Servicio para generar y gestionar tokens JWT. */
    //private final JwtService jwtService;
    //agregarlo al contructor 


    /**
     * Constructor que inyecta las dependencias necesarias para el registro.
     *
     * @param accountRepository repositorio de cuentas
     * @param passwordEncoder   encoder de contraseñas (Argon2)
     * @param jwtService        servicio de generación de tokens JWT
     */

    public RegisterUseCase(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        //this.jwtService = jwtService;
    }

    /**
     * Ejecuta el registro de un nuevo usuario.
     *
     * @param request DTO con los datos del nuevo usuario (nombres, apellidos,
     *                teléfono, email, contraseña y Master Key opcional)
     * @return {@link AuthResponse} con el token JWT y los datos de la cuenta creada
     * @throws BusinessException si el correo electrónico ya está registrado
     */

    public AuthResponse execute(RegisterRequest request) {

        //Verifica que el correo no esté registrado
        accountRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new BusinessException("El correo ya está registrado");
        });

        //Hashear la contraseña con Argon2
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        //Hashear la Master Key solo si fue proporcionada
        boolean hasMasterKey = request.getMasterKey() != null && !request.getMasterKey().isBlank();

        //TODO: igual que hashedPassword, masterKey no se persiste aun
        String hashedMasterKey = hasMasterKey ? passwordEncoder.encode(request.getMasterKey()) : null;

        //Crear el modelo de dominio Account
        Account account = Account.reconstitute(
            UUID.randomUUID(),
            request.getNames(),
            request.getLastnames(),
            request.getEmail(),
            request.getPhone(),
            LocalDateTime.now()
        );

        //Persistir la cuenta
        // TODO: AccountRepository.save(Account) no persiste password ni masterKey
        // porque Account (dominio) no tiene esos campos — están en AccountEntity.
        // Debemos definir cómo pasar hashedPassword y hashedMasterKey
        // al repositorio. Opciones:
        //   a) Agregar register(Account, String password, String masterKey) al repositorio
        //   b) Delegar esta responsabilidad a AuthDomainService
        Account savedAccount = accountRepository.save(account);

        // Generar token JWT usando el email como subject
        //TODO: generar token JWT cuando JwtService esté implementado
        //String token = jwtService.generateToken(savedAccount.getEmail());

        // Construir y retornar la respuesta
        return AuthResponse.builder()
            //TODO: asignar token real cuando JwtService esté listo
            .token(null)
            .tokenType("Bearer")
            //TODO: asignar expiresIn desde jwtService.getExpirationTime()
            .expiresIn(null)
            .accountId(savedAccount.getId())
            .names(savedAccount.getNames())
            .lastnames(savedAccount.getLastnames())
            .email(savedAccount.getEmail())
            .phone(savedAccount.getPhone())
            .createdAt(savedAccount.getCreatedAt())
            .hasMasterKey(hasMasterKey)
            .build();

    }

}
