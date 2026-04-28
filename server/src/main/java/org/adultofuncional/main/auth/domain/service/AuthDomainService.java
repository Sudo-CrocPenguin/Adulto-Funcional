package org.adultofuncional.main.auth.domain.service;

import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio de dominio para la autenticación de usuarios.
 * 
 * <p>
 * <strong>⚠️ ADVERTENCIA ARQUITECTÓNICA:</strong>
 * Esta clase actualmente mezcla responsabilidades de dominio y aplicación,
 * lo que viola los principios de Arquitectura Limpia / Hexagonal.
 * 
 * <p>
 * <strong>Problemas identificados en la implementación actual:</strong>
 * <ul>
 * <li><b>Capa incorrecta:</b> Un servicio de dominio NO debería depender de DTOs
 *     ({@link LoginRequest}, {@link RegisterRequest}, {@link AuthResponse}) que
 *     pertenecen a la capa de aplicación.</li>
 * <li><b>Lógica de aplicación en dominio:</b> La validación de datos de entrada,
 *     generación de tokens y manejo de respuestas pertenecen a casos de uso
 *     (use cases), no al dominio.</li>
 * <li><b>Hardcode de credenciales:</b> La contraseña almacenada está fija
 *     ({@code "$2a$10$exampleEncodedPassword"}), lo que impide múltiples usuarios.</li>
 * <li><b>Token falso:</b> {@code generateFakeToken()} no es seguro para producción.</li>
 * <li><b>Ausencia de repositorio:</b> No se persisten los usuarios registrados.</li>
 * </ul>
 * 
 * <p>
 * <strong>Arquitectura propuesta (siguiendo el patrón de los demás módulos):</strong>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │                     Capa de Presentación                     │
 * │  AuthController (REST) ← usa DTOs (LoginRequest, etc.)      │
 * └─────────────────────────────────────────────────────────────┘
 *                                │
 *                                ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    Capa de Aplicación                        │
 * │  LoginUseCase / RegisterUseCase (orquestan repositorios)    │
 * │  - Validan comandos                                          │
 * │  - Manejan transacciones                                     │
 * │  - Producen AuthResponse                                     │
 * └─────────────────────────────────────────────────────────────┘
 *                                │
 *                                ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │                       Capa de Dominio                        │
 * │  • AuthDomainService (lógica pura de negocio)                │
 * │    - Verificar credenciales (delegate al repositorio)        │
 * │    - Generar tokens (mediante un servicio de dominio)        │
 * │  • User (entidad de dominio con email y password hash)       │
 * │  • AuthRepository (puerto para persistir usuarios)           │
 * └─────────────────────────────────────────────────────────────┘
 *                                │
 *                                ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │                   Capa de Infraestructura                    │
 * │  • UserRepositoryImpl (JPA, Redis, etc.)                     │
 * │  • JwtTokenProvider (genera tokens reales)                   │
 * │  • PasswordEncoder (BCrypt/Argon2)                           │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p>
 * <strong>Responsabilidad CORRECTA de este servicio (después de refactorizar):</strong>
 * <ul>
 * <li>Contener reglas de negocio relacionadas con autenticación (ej: validar que
 *     el email tenga formato válido, que la contraseña cumpla política de seguridad).</li>
 * <li>Interactuar con repositorios mediante puertos (no con implementaciones concretas).</li>
 * <li>NO debe depender de DTOs, ni anotaciones Spring como {@code @Service}
 *     (eso pertenece a la capa de aplicación).</li>
 * </ul>
 * 
 * <p>
 * <strong>Ejemplo de refactorización (solo dominio puro):</strong>
 * <pre>
 * package org.adultofuncional.main.auth.domain.service;
 * 
 * public class AuthDomainService {
 *     private final AuthRepository authRepository;
 *     private final PasswordHasher passwordHasher;
 *     
 *     public AuthDomainService(AuthRepository authRepository, PasswordHasher passwordHasher) {
 *         this.authRepository = authRepository;
 *         this.passwordHasher = passwordHasher;
 *     }
 *     
 *     public User register(String email, String rawPassword) {
 *         // Reglas de dominio
 *         if (authRepository.existsByEmail(email)) {
 *             throw new DomainException("Email already registered");
 *         }
 *         validatePasswordStrength(rawPassword);
 *         String hashed = passwordHasher.hash(rawPassword);
 *         User newUser = User.create(email, hashed);
 *         return authRepository.save(newUser);
 *     }
 *     
 *     public User login(String email, String rawPassword) {
 *         User user = authRepository.findByEmail(email)
 *             .orElseThrow(() -> new DomainException("Invalid credentials"));
 *         if (!passwordHasher.matches(rawPassword, user.getPasswordHash())) {
 *             throw new DomainException("Invalid credentials");
 *         }
 *         return user;
 *     }
 * }
 * </pre>
 * 
 * <p>
 * <strong>Mejoras inmediatas que se pueden aplicar sin romper la compatibilidad:</strong>
 * <ol>
 * <li>Mover las validaciones de DTO a los respectivos casos de uso.</li>
 * <li>Inyectar un {@code UserRepository} para persistencia real.</li>
 * <li>Reemplazar {@code generateFakeToken()} por un {@code JwtTokenProvider} real.</li>
 * <li>Eliminar la dependencia directa de Spring (quitar {@code @Service}) y usar un
 *     {@code AuthApplicationService} que orqueste este dominio.</li>
 * <li>Añadir pruebas unitarias del dominio sin infraestructura.</li>
 * </ol>
 * 
 * <p>
 * <strong>Relación con el módulo Account:</strong>
 * La autenticación debe basarse en la entidad {@link Account} ya existente.
 * Por lo tanto, este servicio debería delegar en {@code AccountRepository}
 * en lugar de mantener su propio almacenamiento de usuarios.
 * 
 * @author Juan David Ruiz Garcia
 * @since 1.0.0
 * @version 1.0 (current - requires refactoring)
 * @see org.adultofuncional.main.account.domain.model.Account
 * @see org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity
 * @deprecated Esta implementación actual será reemplazada por una arquitectura limpia
 *             que separe dominio, aplicación e infraestructura. No usar en producción.
 */
@Service
@Deprecated
public class AuthDomainService {

    /**
     * Codificador de contraseñas (inyectado por Spring).
     * En una arquitectura limpia, esto sería parte de la infraestructura,
     * y el dominio dependería de una interfaz {@code PasswordHasher}.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor para inyección de dependencias.
     * 
     * @param passwordEncoder Implementación de Spring Security para BCrypt/Argon2
     */
    public AuthDomainService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * <p>
     * <strong> Comportamiento actual (no productivo):</strong>
     * <ul>
     * <li>Valida que email no esté vacío y contraseña tenga al menos 6 caracteres.</li>
     * <li>Genera un ID de usuario aleatorio (UUID).</li>
     * <li>Encripta la contraseña usando {@code PasswordEncoder}.</li>
     * <li><b>NO almacena el usuario en base de datos.</b></li>
     * <li>Devuelve un token falso (no JWT).</li>
     * </ul>
     * 
     * <p>
     * <strong>Flujo deseado (refactorización):</strong>
     * <ol>
     * <li>El caso de uso {@code RegisterUseCase} recibe un {@code RegisterRequest}.</li>
     * <li>Valida que el email no exista en {@code AccountRepository}.</li>
     * <li>Crea una entidad {@code Account} mediante el factory method
     *     {@code Account.create(name, email, phone)}.</li>
     * <li>Establece la contraseña hasheada (campo {@code account_password}) y
     *     la master key (si aplica).</li>
     * <li>Persiste la cuenta usando {@code AccountRepository.save()}.</li>
     * <li>Genera un JWT real mediante {@code JwtTokenProvider}.</li>
     * <li>Retorna {@code AuthResponse} con el token, ID y email.</li>
     * </ol>
     * 
     * @param request DTO con email y contraseña (debe migrarse a un comando)
     * @return {@link AuthResponse} con ID de usuario simulado y token falso
     * @throws BusinessException Si el email es inválido o la contraseña es muy corta
     * @see <a href="https://www.baeldung.com/spring-security-registration">Registro de usuarios con Spring Security</a>
     */
    public AuthResponse register(RegisterRequest request) {
        validateRegister(request);
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String userId = UUID.randomUUID().toString();
        // FUTURE: Guardar usuario en base de datos usando AccountRepository
        return AuthResponse.builder()
                .userId(userId)
                .email(request.getEmail())
                .token(generateFakeToken(request.getEmail()))
                .build();
    }

    /**
     * Inicia sesión de un usuario existente.
     * 
     * <p>
     * <strong> Comportamiento actual (no productivo):</strong>
     * <ul>
     * <li>Valida que email y contraseña no estén vacíos.</li>
     * <li>Compara la contraseña provista contra una cadena fija hardcodeada.</li>
     * <li>Siempre retorna el mismo userId mock ("usuario-mock-id").</li>
     * <li>Genera token falso.</li>
     * </ul>
     * 
     * <p>
     * <strong>Flujo deseado (refactorización):</strong>
     * <ol>
     * <li>El caso de uso {@code LoginUseCase} recibe un {@code LoginRequest}.</li>
     * <li>Busca la cuenta por email en {@code AccountRepository}.</li>
     * <li>Si no existe o está desactivada, lanza {@code BusinessException}.</li>
     * <li>Verifica la contraseña usando {@code PasswordEncoder.matches()}.</li>
     * <li>Genera JWT con el ID y roles del usuario.</li>
     * <li>Retorna {@code AuthResponse}.</li>
     * </ol>
     * 
     * @param request DTO con credenciales
     * @return Respuesta con token falso e ID simulado
     * @throws BusinessException Si las credenciales son inválidas
     */
    public AuthResponse login(LoginRequest request) {
        validateLogin(request);
        // TODO: Reemplazar por consulta real a AccountRepository
        String storedPassword = "$2a$10$exampleEncodedPassword";
        if (!passwordEncoder.matches(request.getPassword(), storedPassword)) {
            throw new BusinessException("Credenciales inválidas");
        }
        return AuthResponse.builder()
                .userId("usuario-mock-id")
                .email(request.getEmail())
                .token(generateFakeToken(request.getEmail()))
                .build();
    }

    /**
     * Validaciones específicas para el registro (deben pertenecer al caso de uso).
     * 
     * @param request DTO de registro
     * @throws BusinessException si los datos son incorrectos
     */
    private void validateRegister(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException("El correo es obligatorio");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    /**
     * Validaciones específicas para el login (deben pertenecer al caso de uso).
     * 
     * @param request DTO de login
     * @throws BusinessException si los datos son incorrectos
     */
    private void validateLogin(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException("El correo es obligatorio");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria");
        }
    }

    /**
     * Generador de token temporal.
     * 
     * <p>
     * <strong>⚠️ NO USAR EN PRODUCCIÓN.</strong> Este método crea un token
     * sin firma, sin expiración y fácilmente falsificable.
     * 
     * <p>
     * <strong>Implementación recomendada:</strong> Integrar JWT con
     * {@code io.jsonwebtoken.Jwts.builder()} y una clave secreta.
     * 
     * @param email Email del usuario (se incluye en el token falso)
     * @return Token en formato {@code token-{email}-{UUID}}
     */
    private String generateFakeToken(String email) {
        return "token-" + email + "-" + UUID.randomUUID();
    }
}