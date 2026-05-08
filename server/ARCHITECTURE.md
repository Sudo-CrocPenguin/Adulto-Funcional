# ARCHITECTURE.md - Documentación Técnica de Arquitectura

## Visión general

Adulto Funcional Server implementa **Clean Architecture** (Arquitectura Limpia) propuesta por Robert C. Martin, con las siguientes características:

- **Independencia de frameworks**: El dominio no depende de Spring, JPA o cualquier framework externo
- **Testabilidad**: Las reglas de negocio pueden probarse sin UI, base de datos o servidor web
- **Independencia de UI**: La interfaz puede cambiar sin afectar el resto del sistema
- **Independencia de base de datos**: El dominio no sabe qué base de datos se usa (MariaDB, MySQL, etc.)
- **Independencia de agentes externos**: Las reglas de negocio no saben nada sobre el mundo exterior

## Organización de capas

```
┌──────────────────────────────────────────────────────────────────────────────────────────────┐
│                                     INFRASTRUCTURE LAYER                                     │
│                       (Adaptadores que conectan con el mundo exterior)                       │
│  ┌────────────────────┐      ┌────────────────┐      ┌───────────────────────────────────┐   │
│  │ REST Controllers   │      │ JPA Entities   │      │           Spring Repos            │   │
│  │ (AccountController)│      │ (AccountEntity)│      │   (SpringAccountJpaRepository)    │   │
│  └────────────────────┘      └────────────────┘      └───────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                            │
│          (Casos de uso que orquestan el dominio)                 │
│  ┌───────────────────────┐     ┌──────────────────────────────┐  │
│  │         Use Cases     │     │ DTOs (Request/Response)      │  │
│  │   (GetAccountUseCase  │     │      (AccountResponse,       │  │
│  │  UpdateAccountUseCase)│     │    UpdateAccountRequest)     │  │
│  └───────────────────────┘     └──────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                         │
│  (Reglas de negocio puras, sin dependencias externas)       │
│  ┌──────────────────┐  ┌──────────────────────────────┐     │
│  │ Models           │  │ Repository Ports (Interfaces)│     │
│  │ (Account)        │  │ (AccountRepository)          │     │
│  └──────────────────┘  └──────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘

```

```
org.adultofuncional.main
├── account/            # Módulo de cuentas de usuario
│   ├── domain/         # Modelo de dominio y puertos
│   ├── application/    # Casos de uso y DTOs
│   └── infrastructure/ # Adaptadores (JPA, REST)
├── auth/               # Módulo de autenticación
├── config/             # Configuraciones de Spring
│   ├── beans/          # Configuración de beans
│   ├── jackson/        # Configuración de Jackson JSON
│   └── security/       # Configuración de Spring Security
├── finances/           # Módulo financiero (movimientos, gastos, categorías)
├── agenda/             # Módulo de agenda (eventos)
├── security/           # Gestor de contraseñas con Master Key
└── shared/             # Componentes transversales
    ├── constants/      # Constantes globales
    ├── exception/      # Jerarquía de excepciones y GlobalExceptionHandler
    ├── response/       # Formato estándar de respuestas API (ApiResponse)
    ├── security/       # Validación de ownership reutilizable
    └── util/           # Clases de utilidad general
```

```
┌──────────────────────────────────────────────────────────────────┐
│               SHARED (COMPONENTES COMPARTIDOS)                   │
│   (Elementos transversales usados por todos los módulos)         │
│ ┌────────────┐    ┌────────────┐    ┌─────────────────────────┐  │
│ │ constants/ │    │ exception/ │    │       response/         │  │
│ │ (Constantes│    │ (GlobalExc.│    │    (ApiResponse)        │  │
│ │  globales) │    │  Handler)  │    │                         │  │
│ └────────────┘    └────────────┘    └─────────────────────────┘  │
│   ┌────────────────────┐    ┌──────────────────────────────────┐ │
│   │ security/          │    │ util/ (Clases de utilidad gral)  │ │
│   │ (OwnedResource,    │    │                                  │ │
│   │  OwnershipValid.)  │    │                                  │ │
│   └────────────────────┘    └──────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

## Capa de Dominio (`domain`)

Es el núcleo del sistema. Contiene las entidades y reglas de negocio puras.

### Responsabilidades

- Definir el modelo de negocio (`Account`, futuros: `Movement`, `Event`, `Password`)
- Declarar puertos de salida (interfaces de repositorio)
- Encapsular invariantes de negocio
- Validar reglas de integridad (ej. `id` y `createdAt` no nulos en `Account`)

### Ejemplo: `Account.java`

```java
public class Account {
    private final UUID id;
    private String names;
    private String lastnames;
    private String email;
    private String phone;
    private final LocalDateTime createdAt;

    // Constructor privado - usar métodos de fábrica
    private Account(UUID id, String names, ...) { ... }

    // Método de fábrica para reconstituir desde persistencia
    public static Account reconstitute(UUID id, ...) { ... }

    // Comportamiento de dominio
    public void updateDetails(String names, String lastnames, String phone) { ... }
    public void updateEmail(String email) { ... }
    public String getFullName() { return names + " " + lastnames; }
}
```

### Puertos de repositorio (Interfaces)

El dominio define contratos que la infraestructura debe implementar:

```java
public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    Optional<Account> findByEmail(String email);
    List<Account> findAll();
    void deleteById(UUID id);
}
```

## Capa de Aplicación (`application`)

Orquesta los casos de uso del sistema. Depende del dominio pero no de infraestructura.

### Casos de uso (`usecase`)

Contienen la lógica de aplicación y coordinan el dominio:

- **GetAccountUseCase**: Consulta una cuenta por ID, validando su existencia
- **UpdateAccountUseCase**: Actualiza datos de cuenta, verificando unicidad de email

```java
@Service
@RequiredArgsConstructor
public class UpdateAccountUseCase {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse execute(UUID accountId, UpdateAccountRequest request) {
        // 1. Buscar cuenta (regla: debe existir)
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException(...));

        // 2. Validar unicidad de email (regla de negocio)
        if (!account.getEmail().equals(request.getEmail())) {
            accountRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> throw new ConflictException(...));
        }

        // 3. Aplicar cambios en el dominio
        account.updateDetails(request.getNames(), ...);
        account.updateEmail(request.getEmail());

        // 4. Persistir y retornar DTO
        return accountMapper.toResponse(accountRepository.save(account));
    }
}
```

### DTOs (`dto`)

Objetos de transferencia de datos para la capa de aplicación:

- **Request DTOs**: Entrada validada con Jakarta Validation (`@NotBlank`, `@Email`, `@Size`, `@NoHtml`). La anotación `@NoHtml` protege contra Stored XSS usando Jsoup.
- **Response DTOs**: Salida que nunca expone datos sensibles (password, masterKey)

## Capa de Infraestructura (`infrastructure`)

Implementa los adaptadores que conectan el sistema con tecnologías externas.

### Controladores REST (`controller`)

Exponen los endpoints HTTP y validan la entrada:

```java
@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final GetAccountUseCase getAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final OwnershipValidator ownershipValidator;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
        @PathVariable UUID id,
        @AuthenticationPrincipal String loggedEmail) {
        AccountResponse account = getAccountUseCase.execute(id);
        ownershipValidator.validate(account, loggedEmail);
        return ResponseEntity.ok(
            ApiResponse.<AccountResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cuenta encontrada")
                .data(account)
                .build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateAccountRequest request,
        @AuthenticationPrincipal String loggedEmail) {
        AccountResponse account = getAccountUseCase.execute(id);
        ownershipValidator.validate(account, loggedEmail);
        AccountResponse updated = updateAccountUseCase.execute(id, request);
        return ResponseEntity.ok(
            ApiResponse.<AccountResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cuenta actualizada exitosamente")
                .data(updated)
                .build());
    }
}
```

### Entidades JPA (`persistence/entity`)

Mapean las tablas de base de datos a objetos Java:

```java
@Entity
@Table(name = "accounts")
public class AccountEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "account_id", columnDefinition = "CHAR(36)")
    private UUID accountId;

    @Column(name = "account_password", length = 60, nullable = false)
    private String accountPassword; // Hash Argon2

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovementEntity> movements = new ArrayList<>();
    // ...
}
```

### Mapeadores (`persistence/mapper`)

Convierten entre el modelo de dominio y las entidades JPA:

```java
@Component
public class AccountMapper {
    public Account toDomain(AccountEntity entity) {
        return Account.reconstitute(entity.getAccountId(), ...);
    }

    public AccountEntity toEntity(Account account) { ... }
    public AccountResponse toResponse(Account account) { ... }
}
```

### Implementación de repositorios (`repository`)

Implementa los puertos del dominio usando Spring Data JPA:

```java
@Repository
public class AccountRepositoryImpl implements AccountRepository {
    private final SpringAccountJpaRepository jpaRepository;
    private final AccountMapper mapper;

    @Override
    public Account save(Account account) {
        AccountEntity entity = mapper.toEntity(account);
        AccountEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    // ...
}
```

## Módulos pendientes de desarrollo

Los siguientes módulos tienen definidas sus entidades JPA y el esquema de base de datos, pero aún no cuentan con modelos de dominio, casos de uso ni controladores REST.

### `finances/` — Módulo financiero

Contiene las entidades JPA para la gestión financiera:

- **`CategoryEntity`** — Categorías con soft delete (`@SQLRestriction`). Valores de `category_type`: `"Finanzas"` o `"Agenda"`. Ver `src/main/java/org/adultofuncional/main/finances/infrastructure/persistence/entity/CategoryEntity.java`
- **`MovementEntity`** — Movimientos financieros (ingresos/egresos). Ver `src/main/java/org/adultofuncional/main/finances/infrastructure/persistence/entity/MovementEntity.java`
- **`FixedExpensesEntity`** — Gastos fijos recurrentes (mensuales, semanales, etc.). Ver `src/main/java/org/adultofuncional/main/finances/infrastructure/persistence/entity/FixedExpensesEntity.java`

**Pendiente**: domain models, repository ports, use cases, controllers y mappers.

### `agenda/` — Módulo de agenda

Contiene la entidad JPA para eventos:

- **`EventEntity`** — Eventos con prioridad, recordatorios, recurrencia en días y estado. Ver `src/main/java/org/adultofuncional/main/agenda/infrastructure/persistence/entity/EventEntity.java`

**Pendiente**: domain models, repository ports, use cases, controllers y mappers.

### `security/` — Gestor de contraseñas

Contiene la entidad JPA para el almacenamiento seguro de credenciales:

- **`PasswordEntity`** — Credenciales cifradas con AES-256 (salt + IV + ciphertext). Ver `src/main/java/org/adultofuncional/main/security/infrastructure/persistence/entity/PasswordEntity.java`

**Pendiente**: domain models, repository ports, use cases, controllers, mappers y servicio de encriptación AES-256.

## Configuración de seguridad (`config/security/`)

Módulo responsable de toda la infraestructura de autenticación y autorización:

- **`SecurityConfig`**: Cadena de filtros de Spring Security. Configura CSRF (deshabilitado para API stateless), sesiones stateless, CORS con credenciales y headers de seguridad (CSP, X-Frame-Options, X-XSS-Protection, X-Content-Type-Options, HSTS).
- **`JwtService`**: Generación, validación y extracción de claims de tokens JWT.
- **`JwtAuthenticationFilter`**: Filtro que intercepta cada request, extrae el token de la cookie HttpOnly o del header `Authorization`, lo valida y establece el contexto de seguridad de Spring.
- **`DatabaseUserDetailsService`**: Implementación de `UserDetailsService` que carga las credenciales desde `accounts` en la base de datos, integrando Spring Security con el repositorio de cuentas.
- **`CookieUtils`**: Gestión segura de la cookie `token` (crear/eliminar) con atributos HttpOnly, Secure (configurable) y SameSite.
- **`ClientTypeResolver`**: Detecta si un request proviene de una app nativa (móvil/desktop) o de un navegador web mediante señales pasivas (User-Agent, ausencia de Origin) y un header declarativo (`X-Client-Type`). Determina si el token JWT se incluye en el body de la respuesta además de la cookie.

## Componentes compartidos (`shared/`)

Elementos transversales que no pertenecen a un módulo de negocio específico y son utilizados por todos los módulos.

### `constants/` (Pendiente)

Paquete planificado para constantes globales del sistema:

- Códigos de estado HTTP
- Mensajes de error estandarizados
- Configuraciones de tiempo (JWT expiration, formatos de fecha)
- Nombres de roles y permisos

### `exception/`

Jerarquía centralizada de excepciones:

- **`BusinessException`**: Base para errores de negocio (HTTP 400)
- **`NotFoundException`**: Recurso no encontrado (HTTP 404)
- **`UnauthorizedException`**: Credenciales incorrectas (HTTP 401)
- **`ConflictException`**: Conflicto de datos, ej. email duplicado (HTTP 409)
- **`ForbiddenException`**: Acceso denegado, ej. Master Key no verificada (HTTP 403)
- **`GlobalExceptionHandler`**: `@RestControllerAdvice` que intercepta todas las excepciones y retorna `ApiResponse` estandarizado

### `response/`

Formato estándar de respuesta para toda la API:

```java
public class ApiResponse<T> {
    private final int status;      // Código HTTP
    private final String message;   // Mensaje descriptivo
    private final T data;          // Datos de respuesta
}
```

### `util/` (Pendiente)

Paquete planificado para clases de utilidad general:

- Validación de formatos (email, teléfono)
- Generación de UUID v7
- Encriptación/desencriptación AES-256
- Manejo de fechas y zonas horarias

### `security/`

Componentes reutilizables para validación de acceso por ownership y protección anti‑XSS:

- **`OwnedResource`**: Interfaz que deben implementar los DTOs de respuesta cuyos recursos pertenecen a un usuario específico. Expone el email del propietario mediante `getEmail()`, permitiendo que `OwnershipValidator` valide acceso sin acoplarse a ningún módulo concreto.
- **`OwnershipValidator`**: Componente Spring que centraliza la lógica de validación de ownership. Compara el email del recurso (vía `OwnedResource`) con el email del usuario autenticado (extraído del JWT). Si no coinciden, lanza `UnauthorizedException` (HTTP 401) antes de que el caso de uso sea invocado.
- **`NoHtml`**: Anotación Jakarta Validation que restringe campos de texto para que no contengan HTML.
- **`NoHtmlValidator`**: Validador que usa Jsoup con `Safelist.none()` para rechazar cualquier tag o atributo HTML. Si el texto limpio difiere del original, la validación falla.

## Flujo de datos típico

```
HTTP Request → AccountController → UpdateAccountUseCase → AccountRepository (puerto)
                                                        ↓
AccountRepositoryImpl → SpringAccountJpaRepository → MariaDB
                                                        ↓
                              AccountMapper ← AccountEntity
                                    ↓
                          Account (dominio)
                                    ↓
                Account.updateDetails() / updateEmail()
                                    ↓
            AccountRepositoryImpl.save() → SpringAccountJpaRepository
                                    ↓
                          AccountMapper.toResponse()
                                    ↓
                  AccountResponse → ResponseEntity → HTTP Response
```

## Seguridad

### Autenticación

- **JWT (JSON Web Tokens)**: Autenticación stateless
- **Token siempre establecido en HttpOnly Cookie**. Los clientes nativos identificados por `ClientTypeResolver` lo reciben adicionalmente en el body de la respuesta. Nunca en localStorage ni sessionStorage.
- **Argon2**: Hash de contraseñas de login en `account_password`
- **Master Key**: Hash Argon2 opcional en `account_master_key` para proteger el gestor de contraseñas
- **No enumeración de usuarios**: El login no distingue entre email inexistente y contraseña incorrecta; ambos casos devuelven un error 401 genérico.
- **Protección XSS en entrada**: Todos los DTOs de request anotan los campos de texto con `@NoHtml`, que utiliza Jsoup para rechazar cualquier HTML/script antes de que llegue al dominio.
- **Errores uniformes del filtro JWT**: `JwtAuthenticationFilter` escribe errores 401 con `ApiResponse` para mantener la consistencia con el resto de la API.

### Gestor de contraseñas

- Las contraseñas de servicios se encriptan con **AES-256**
- La clave de encriptación se deriva de la Master Key del usuario
- Acceso protegido: Si no se ha verificado la Master Key, se lanza `ForbiddenException` (HTTP 403)

## Base de datos

### Esquema (Flyway V1\_\_20260408_init_schema.sql)

```
accounts (ENTIDAD CENTRAL)
├── account_id CHAR(36) PRIMARY KEY
├── account_names VARCHAR(50) NOT NULL
├── account_lastnames VARCHAR(50) NOT NULL
├── account_email VARCHAR(255) NOT NULL UNIQUE
├── account_phone VARCHAR(20) NOT NULL
├── account_password VARCHAR(255) NOT NULL (Argon2 hash)
├── account_master_key VARCHAR(255) NULL (Argon2 hash)
└── account_created_at TIMESTAMP NOT NULL

categories
├── category_id CHAR(36) PRIMARY KEY
├── category_name VARCHAR(50) NOT NULL
└── category_type VARCHAR(20) NOT NULL ("Finanzas" | "Agenda")

movements
├── movement_id CHAR(36) PRIMARY KEY
├── movement_type VARCHAR(20) NOT NULL ("Ingreso" | "Egreso")
├── movement_amount DECIMAL(10,2) NOT NULL
├── movement_register_date TIMESTAMP NOT NULL
├── movement_description TEXT NULL
├── movement_date DATE NOT NULL
├── movement_fk_account_id CHAR(36) FK → accounts(account_id)
└── movement_fk_category_id CHAR(36) NOT NULL FK → categories(category_id)

fixed_expenses
├── fixed_expense_id CHAR(36) PRIMARY KEY
├── fixed_expense_name VARCHAR(50) NOT NULL
├── fixed_expense_frequency VARCHAR(15) NOT NULL
├── fixed_expense_amount DECIMAL(10,2) NOT NULL
├── fixed_expense_status VARCHAR(15) NOT NULL ("Activo" | "Inactivo")
├── fixed_expense_start_date DATE NOT NULL
├── fixed_expense_next_due_date DATE NOT NULL
├── fixed_expense_reminder_days INT NOT NULL
├── fixed_expense_fk_category_id CHAR(36) NOT NULL FK → categories(category_id)
└── fixed_expense_fk_account_id CHAR(36) FK → accounts(account_id)

events
├── event_id CHAR(36) PRIMARY KEY
├── event_title VARCHAR(35) NOT NULL
├── event_priority VARCHAR(15) NULL DEFAULT 'Media'
├── event_date DATE NOT NULL
├── event_frequency INT NOT NULL (0=único, 1=diario, 7=semanal, etc.)
├── event_reminder DATETIME NOT NULL
├── event_start_hour DATETIME NOT NULL
├── event_end_hour DATETIME NOT NULL
├── event_description TEXT NULL
├── event_status VARCHAR(20) DEFAULT 'Pendiente'
├── event_fk_category_id CHAR(36) NOT NULL FK → categories(category_id)
└── event_fk_account_id CHAR(36) FK → accounts(account_id)

passwords
├── password_id CHAR(36) PRIMARY KEY
├── password_application_name VARCHAR(35) NOT NULL
├── password_salt VARCHAR(255) NOT NULL (salt para derivar clave AES)
├── password_iv BINARY(16) NOT NULL (IV de 16 bytes para cifrado AES)
├── password_ciphertext VARBINARY(2048) NOT NULL (ciphertext + tag AES-GCM)
├── password_last_change_date DATE NULL
└── passwords_fk_account_id CHAR(36) FK → accounts(account_id)
```

### Características del esquema

- **UUID v7**: Identificadores ordenables temporalmente, generados por la aplicación (no por la base de datos).
- **Eliminación en cascada**: Al eliminar una cuenta, se eliminan todos sus datos relacionados. La cascada se implementa a nivel de JPA, **no** en las restricciones SQL.
- **Categorías obligatorias**: `movements.movement_fk_category_id` y `events.event_fk_category_id` son `NOT NULL`; todos los movimientos y eventos deben tener una categoría asignada.
- **Relaciones**: Todas las entidades referencian a `accounts` como propietario.
- **Índices**: Se crean índices sobre las claves foráneas (`movement_fk_account_id`, `fixed_expense_fk_account_id`, etc.) y sobre las columnas de fecha (`movement_date`, `event_date`) para optimizar las consultas más frecuentes.

## Configuración de Docker

### Multi-stage Dockerfile

```
Etapa 1 (builder): maven:3.9-eclipse-temurin-21-alpine
  └── Descarga dependencias y construye el JAR

Etapa 2 (runtime): eclipse-temurin:21-jre-alpine
  └── Ejecuta el JAR con usuario no root (UID 1000)
  └── Healthcheck en /actuator/health
```

### Docker Compose

- **Servicio mariadb**: Imagen oficial 11.8 con healthcheck nativo
- **Servicio app**: Construido desde Dockerfile
- **Red**: `afs-network` (bridge) para comunicación entre contenedores
- **Volumen**: `mariadb_data` para persistencia de datos

### Variables de entorno requeridas

| Variable                | Descripción                                             | Ejemplo                        |
| ----------------------- | ------------------------------------------------------- | ------------------------------ |
| `MARIADB_ROOT_PASSWORD` | Password de root de MariaDB                             | mysecret                       |
| `MARIADB_DATABASE`      | Nombre de la base de datos                              | adulto_funcional               |
| `MARIADB_USER`          | Usuario de la aplicación                                | afs_user                       |
| `MARIADB_PASSWORD`      | Password del usuario                                    | userpass                       |
| `SPRING_DATASOURCE_URL` | JDBC URL                                                | jdbc:mariadb://mariadb:3306/db |
| `JWT_SECRET`            | Clave secreta para firmar JWT                           | my-jwt-secret                  |
| `JWT_EXPIRATION`        | Tiempo de expiración JWT (ms)                           | 3600000                        |
| `CORS_ALLOWED_ORIGINS`  | Orígenes permitidos para CORS                           | <http://localhost:5173>        |
| `APP_COOKIE_SECURE`     | Atributo Secure de la cookie (true en producción HTTPS) | true                           |
| `APP_COOKIE_SAME_SITE`  | Atributo SameSite de la cookie (Lax, Strict o None)     | Lax                            |

## Manejo de excepciones

El `GlobalExceptionHandler` centraliza todas las excepciones:

```
Exception
  └── RuntimeException
        └── BusinessException (base, HTTP 400)
              ├── NotFoundException (HTTP 404)
              ├── UnauthorizedException (HTTP 401)
              ├── ConflictException (HTTP 409)
              └── ForbiddenException (HTTP 403)
```

Todas las excepciones devuelven `ApiResponse<Void>` o `ApiResponse<Map<String, String>>` con el formato estándar.

## Testing

- **Spring Boot Test**: Pruebas unitarias y de integración
- **Testcontainers**: Contenedores efímeros de MariaDB 11.8 para pruebas de integración
- **Spring Security Test**: Soporte para probar endpoints protegidos (usado en @WebMvcTest)

## Dependencias clave en pom.xml

```xml
<!-- Spring Boot Starters -->
<artifactId>spring-boot-starter-data-jpa</artifactId>
<artifactId>spring-boot-starter-security</artifactId>
<artifactId>spring-boot-starter-web</artifactId>
<artifactId>spring-boot-starter-validation</artifactId>
<artifactId>spring-boot-starter-actuator</artifactId>

<!-- Base de datos -->
<artifactId>flyway-core</artifactId>
<artifactId>flyway-mysql</artifactId>
<artifactId>mariadb-java-client</artifactId>

<!-- Utilidades -->
<artifactId>lombok</artifactId>
<artifactId>java-uuid-generator</artifactId>  <!-- UUID v7 -->

<!-- Testing -->
<artifactId>spring-boot-starter-test</artifactId>
<artifactId>testcontainers-junit-jupiter</artifactId>
<artifactId>testcontainers-mariadb</artifactId>
<artifactId>h2</artifactId>

<!-- Validación anti‑XSS -->
<artifactId>jsoup</artifactId>
```

## Convenciones de código

- **Lombok**: Se usa extensivamente para reducir boilerplate (`@Getter`, `@Builder`, `@RequiredArgsConstructor`)
- **Javadoc**: Todos los archivos tienen documentación siguiendo estándares, con `@author` y `@since`
- **Validación**: Jakarta Validation en DTOs de request (`@Valid`, `@NotBlank`, `@Email`)
- **Paquetes**: `package-info.java` documentan el propósito de cada paquete
- **Nombres de tablas**: Snake_case (`account_names`, `movement_type`)
- **Nombres de columnas**: Prefijo con nombre de tabla (`account_`, `movement_`, `event_`)

## Roadmap técnico

- [x] Completar módulo de autenticación (LoginUseCase, RegisterUseCase)
- [x] Autenticación con HttpOnly Cookie (SameSite configurable vía `APP_COOKIE_SAME_SITE`)
- [x] Validación de ownership con OwnershipValidator reutilizable (shared/security/)
- [x] Tests de integración con Testcontainers
- [x] Protección anti‑XSS con `@NoHtml` en todos los DTOs de entrada
- [x] Errores consistentes del `JwtAuthenticationFilter` con `ApiResponse`
- [ ] Implementar DeleteAccountUseCase y conectar en AccountController
- [ ] Módulo financiero: MovementUseCase, FixedExpenseUseCase, CategoryUseCase
- [ ] Módulo agenda: EventUseCase con lógica de recurrencia
- [ ] Gestor de contraseñas: PasswordUseCase con encriptación AES-256
- [ ] Documentación OpenAPI/Swagger
- [ ] Implementar refresh tokens para JWT
