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
├── auth/               # Módulo de autenticación (JWT)
├── config/             # Configuraciones de Spring
│   ├── beans/          # Configuración de beans (pendiente)
│   ├── jackson/        # Configuración de Jackson JSON (pendiente)
│   └── security/       # Configuración de Spring Security (pendiente)
├── finances/           # Módulo financiero (movimientos, gastos, categorías)
├── agenda/             # Módulo de agenda (eventos)
├── security/           # Gestor de contraseñas con Master Key
└── shared/             # Componentes transversales
    ├── constants/      # Constantes globales (pendiente)
    ├── exception/      # Jerarquía de excepciones y GlobalExceptionHandler
    ├── response/       # Formato estándar de respuestas API (ApiResponse)
    └── util/           # Clases de utilidad general (pendiente)


```

┌─────────────────────────────────────────────────────────────┐
│ SHARED (COMPONENTES COMPARTIDOS) │
│ (Elementos transversales usados por todos los módulos) │
│ ┌────────────┐ ┌────────────┐ ┌─────────────────────┐ │
│ │ constants/ │ │ exception/ │ │ response/ │ │
│ │ (Constantes│ │ (GlobalExc.│ │ (ApiResponse) │ │
│ │ globales) │ │ Handler) │ │ │ │
│ └────────────┘ └────────────┘ └─────────────────────┘ │
│ ┌────────────────────────────────────────────────────┐ │
│ │ util/ (Clases de utilidad general - pendiente) │ │
│ └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘

```

## Capa de Dominio (`domain`)

Es el núcleo del sistema. Contiene las entidades y reglas de negocio puras.

### Responsabilidades
- Definir el modelo de negocio (`Account`, futuros: `Movement`, `Event`, `Password`)
- Declarar puertos de salida (interfaces de repositorio)
- Encapsular invariantes de negocio
- Validar reglas de integridad (ej. `id` y `createdAt` no nulos en `Account`)

### Ejemplo: `Account.java`
```

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
                .ifPresent(existing -> throw new BusinessException(...));
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

- **Request DTOs**: Entrada validada con Jakarta Validation (`@NotBlank`, `@Email`, `@Size`)
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

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(getAccountUseCase.execute(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(updateAccountUseCase.execute(id, request));
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
- **Argon2**: Hash de contraseñas de login en `account_password`
- **Master Key**: Hash Argon2 opcional en `account_master_key` para proteger el gestor de contraseñas

### Gestor de contraseñas

- Las contraseñas de servicios se encriptan con **AES-256**
- La clave de encriptación se deriva de la Master Key del usuario
- Acceso protegido: Si no se ha verificado la Master Key, se lanza `ForbiddenException` (HTTP 403)

## Base de datos

### Esquema (Flyway V1\_\_20260408_init_schema.sql)

```
accounts (ENTIDAD CENTRAL)
├── account_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())
├── account_names VARCHAR(50) NOT NULL
├── account_lastnames VARCHAR(50) NOT NULL
├── account_email VARCHAR(255) NOT NULL UNIQUE
├── account_phone VARCHAR(20) NOT NULL
├── account_password VARCHAR(60) NOT NULL (Argon2 hash)
├── account_master_key VARCHAR(60) NULL (Argon2 hash)
└── account_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

categories
├── category_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())
├── category_name VARCHAR(20) NOT NULL
├── category_type VARCHAR(20) NOT NULL ("Finanzas" | "Agenda")
├── category_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
└── category_deleted_at TIMESTAMP NULL (soft delete)

movements
├── movement_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())
├── movement_type VARCHAR(7) NOT NULL ("Ingreso" | "Egreso")
├── movement_amount DECIMAL(10,2) NOT NULL
├── movement_register_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
├── movement_description TEXT NULL
├── movement_date DATE NOT NULL
├── movement_fk_account_id CHAR(36) FK → accounts(account_id)
└── movement_fk_category_id CHAR(36) FK → categories(category_id)

fixed_expenses
├── fixed_expense_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())
├── fixed_expense_name VARCHAR(20) NOT NULL
├── fixed_expense_frequency VARCHAR(15) NOT NULL
├── fixed_expense_amount DECIMAL(10,2) NOT NULL
├── fixed_expense_status VARCHAR(15) NOT NULL ("Activo" | "Inactivo")
├── fixed_expense_closing_date DATE NOT NULL
├── fixed_expense_fk_category_id CHAR(36) FK → categories(category_id)
└── fixed_expense_fk_account_id CHAR(36) FK → accounts(account_id)

events
├── event_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())
├── event_title VARCHAR(35) NOT NULL
├── event_priority VARCHAR(15) NULL DEFAULT 'Media'
├── event_date DATE NOT NULL
├── event_frequency INT NOT NULL (0=único, 1=diario, 7=semanal, etc.)
├── event_reminder DATETIME NOT NULL
├── event_start_hour DATETIME NOT NULL
├── event_end_hour DATETIME NOT NULL
├── event_description TEXT NULL
├── event_status VARCHAR(20) DEFAULT 'Pendiente'
├── event_fk_category_id CHAR(36) FK → categories(category_id)
└── event_fk_account_id CHAR(36) FK → accounts(account_id)

passwords
├── password_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())
├── password_application_name VARCHAR(35) NOT NULL
├── password_application TEXT NOT NULL (AES-256 en Base64)
├── password_last_change_date DATE NULL
└── passwords_fk_account_id CHAR(36) FK → accounts(account_id)
```

### Características del esquema

- **UUID v7**: Identificadores ordenables temporalmente (mejor rendimiento en índices B-tree)
- **Eliminación en cascada**: Al eliminar una cuenta, se eliminan todos sus datos relacionados
- **Soft delete**: Las categorías soportan borrado lógico mediante `category_deleted_at`
- **Relaciones**: Todas las entidades referencian a `accounts` como propietario

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
- **Servicio app**: Construido desde Dockerfile, expone puerto 8080
- **Red**: `afs-network` (bridge) para comunicación entre contenedores
- **Volumen**: `mariadb_data` para persistencia de datos

### Variables de entorno requeridas

| Variable                | Descripción                   | Ejemplo                        |
| ----------------------- | ----------------------------- | ------------------------------ |
| `MARIADB_ROOT_PASSWORD` | Password de root de MariaDB   | mysecret                       |
| `MARIADB_DATABASE`      | Nombre de la base de datos    | adulto_funcional               |
| `MARIADB_USER`          | Usuario de la aplicación      | afs_user                       |
| `MARIADB_PASSWORD`      | Password del usuario          | userpass                       |
| `SPRING_DATASOURCE_URL` | JDBC URL                      | jdbc:mariadb://mariadb:3306/db |
| `JWT_SECRET`            | Clave secreta para firmar JWT | my-jwt-secret                  |
| `JWT_EXPIRATION`        | Tiempo de expiración JWT (ms) | 86400000                       |

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
- **Testcontainers**: Contenedores efímeros de MariaDB para pruebas de integración
- **H2 Database**: Base de datos en memoria para pruebas rápidas
- **Spring Security Test**: Soporte para probar endpoints protegidos

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
```

## Convenciones de código

- **Lombok**: Se usa extensivamente para reducir boilerplate (`@Getter`, `@Builder`, `@RequiredArgsConstructor`)
- **Javadoc**: Todos los archivos tienen documentación siguiendo estándares, con `@author` y `@since`
- **Validación**: Jakarta Validation en DTOs de request (`@Valid`, `@NotBlank`, `@Email`)
- **Paquetes**: `package-info.java` documentan el propósito de cada paquete
- **Nombres de tablas**: Snake_case (`account_names`, `movement_type`)
- **Nombres de columnas**: Prefijo con nombre de tabla (`account_`, `movement_`, `event_`)

## Roadmap técnico

- [ ] Completar módulo de autenticación (LoginUseCase, RegisterUseCase)
- [ ] Implementar DeleteAccountUseCase y conectar en AccountController
- [ ] Módulo financiero: MovementUseCase, FixedExpenseUseCase, CategoryUseCase
- [ ] Módulo agenda: EventUseCase con lógica de recurrencia
- [ ] Gestor de contraseñas: PasswordUseCase con encriptación AES-256
- [ ] Tests de integración con Testcontainers
- [ ] Documentación OpenAPI/Swagger
- [ ] Implementar refresh tokens para JWT

