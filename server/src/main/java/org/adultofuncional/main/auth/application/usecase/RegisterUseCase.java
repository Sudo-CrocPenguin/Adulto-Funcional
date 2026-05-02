package org.adultofuncional.main.auth.application.usecase;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.config.security.JwtService;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para registrar un nuevo usuario en el sistema.
 *
 * <p>
 * Orquesta el flujo completo de registro: valida unicidad del correo,
 * hashea las credenciales con Argon2, crea la cuenta y genera un
 * token JWT para autenticación inmediata.
 *
 * @author Lydis Ester Jaraba
 * @since 0.0.1
 */

@Service
public class RegisterUseCase {
    
    /** Repositorio para verificar existencia y persistir la cuenta. */
    private final AccountRepository accountRepository;

    /** Encoder para hashear la contraseña y la Master Key con Argon2. */
    private final PasswordEncoder passwordEncoder;

    /** Servicio para generar y gestionar tokens JWT. */
    private final JwtService jwtService;
 


    /**
     * Constructor que inyecta las dependencias necesarias para el registro.
     *
     * @param accountRepository repositorio de cuentas
     * @param passwordEncoder   encoder de contraseñas (Argon2)
     * @param jwtService        servicio de generación de tokens JWT
     */

    public RegisterUseCase(AccountRepository accountRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

        //Crear el modelo de dominio Account - Account.create() genera UUIDv7 y createdAt
        Account account = Account.create(
            request.getNames(),
            request.getLastnames(),
            request.getEmail(),
            request.getPhone(),
            hashedPassword
        );

        //Persistir la cuenta
        //TODO: hashedMasterKey no se persiste aún — Account no tiene ese campo
        // Definir cómo almacenarlo (AccountEntity sí puede tenerlo)
        Account savedAccount = accountRepository.save(account);

        // Generar token JWT usando el email como subject
        String token = jwtService.generateToken(savedAccount.getId().toString(), savedAccount.getEmail());

        //Construir y retornar la respuesta
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpiration())
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
