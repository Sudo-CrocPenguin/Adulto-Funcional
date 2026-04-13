# -----------------------------------------------------------------------------
# Funciones utilitarias de Bash para la configuración del entorno.
#
# Este archivo es una librería de funciones auxiliares utilizada por
# `.set-env.sh`. NO está diseñado para ejecutarse directamente.
#
# En su lugar, debe cargarse desde otro script usando `source`:
#
#   source ./.lib/functions.sh
#
# Dado que solo define funciones, no requiere shebang (`#!/bin/bash`)
# ni permisos de ejecución.
# -----------------------------------------------------------------------------

# ------------------------------------------------------------------
# Muestra una animación de puntos para indicar que se está
# realizando una operación en progreso.
# ------------------------------------------------------------------
function loading() {
  for i in {1..4}; do
    line=$(printf '%*s' "$i" | tr ' ' '.')
    echo -ne "\r$line"
    sleep 1
  done

}

# ------------------------------------------------------------------
# Evita que el script sea ejecutado como root.
# Ejecutarlo como root puede causar problemas de permisos y seguridad.
# ------------------------------------------------------------------
function prevent_sudo_or_root() {
  if [[ $(whoami) == "root" ]]; then
    printf "\n\n\n"

    echo "${ROJO}Este script no se puede ejecutar como root. Abortando...${RESET}"

    command clear
    printf "\n\n\n"

    exit 1
  fi
}

# --------------------------------------------------------
# Advertencia de como debe de ser ejecutado el script
# para evitar errores al cargar las variables de entorno.
# --------------------------------------------------------
function execution_warning() {
  printf "\n\n\n"

  echo "
  ${AMARILLO}
  NOTA:

  ----------------------------------------------------------------------------------------------------------------
  - RECUERDE QUE ESTE SCRIPT DEBE DE SER EJECUTADO CON ${BLANCO}'source .set-env.sh'${AMARILLO}                                      -
  - TAMBIEN VERIFIQUE QUE USTED ESTA EJECUTANDO ESTE SCRIPT CON 'bash' Y NO OTRA SHELL                           -
  -                                                                                                              -
  - EN CASO TAL DE QUE SE IGNOREN ESTAS ADVERTENCIAS ES PROBABLE QUE ESTE SCRIPT TERMINE CON UN MENSAJE DE ERROR -
  - O VAYA A TENER DIFERENTES PROBLEMAS AL INTENTAR EJECUTARLO                                                   -
  -                                                                                                              -${RESET}${ROJO}
  -                                                                                                              -
  - --------------------------------------------------------------------------------------------------------------
  -                                                                                                              -
  - SI USTED YA EJECUTO ESTE COMANDO Y SOLAMENTE QUIERE CARGAR LAS VARIABLES DE ENTORNO POR FAVOR INGRESE 1      -
  - DE LO CONTRARIO ESCRIBA 0                                                                                    -
  -                                                                                                              -
  ----------------------------------------------------------------------------------------------------------------
${RESET}
"

  loading
  printf "\n\n"
  echo "${BLANCO}Pulsa una tecla para continuar${RESET}"
  read -n 1 s

  echo
  echo
  echo "${AZUL}[0] Configurar las variables de entorno [1] Cargar el archivo .env (responda [0]/[1])${RESET}"
  read -r LOAD_ENV

  while [[ ! $LOAD_ENV =~ ^[01]$ ]]; do
    echo "${ROJO}Debe de responder 0 para continuar o 1 para cargar las variables${RESET}"
    read -r LOAD_ENV
  done

  if [[ $LOAD_ENV == 1 ]]; then
    set -a
    source "$ENV_FILE" 2>/dev/null
    set +a

    if [[ -n "$SPRING_APPLICATION_NAME" ]]; then
      echo "${VERDE}✓ Variables cargadas desde .env${RESET}"

      printf "\n\n"
      echo "${BLANCO}Pulsa una tecla para continuar${RESET}"
      read -n 1 s
    else
      echo "${ROJO}⚠ El archivo .env está vacío o no tiene variables válidas${RESET}"
      echo "${AMARILLO}Procediendo a configurar manualmente...${RESET}"

      printf "\n\n"
      echo "${BLANCO}Pulsa una tecla para continuar${RESET}"
      read -n 1 s
    fi
  fi

  command clear
  printf "\n\n\n"
}

# ------------------------------------------------------------------
# Solicita al usuario los valores de configuración de la aplicación.
# Las variables ingresadas se almacenan temporalmente en el entorno
# del script para luego ser guardadas en el archivo .env.
# ------------------------------------------------------------------
function request_variables() {
  printf "\n\n\n"

  echo -e "${AZUL}Por favor ingrese las variables de entorno:${RESET}"
  echo

  while [[ -z "$SPRING_APPLICATION_NAME" ]]; do
    read -rp "${AZUL}Application name (spring.application.name):${RESET}" SPRING_APPLICATION_NAME
  done

  while [[ -z "$SPRING_DATASOURCE_URL" ]]; do
    read -rp "${AZUL}Datasource URL (example: jdbc:mariadb://localhost:3306/mydb):${RESET}" SPRING_DATASOURCE_URL
  done

  while [[ -z "$SPRING_DATASOURCE_USERNAME" ]]; do
    read -rp "${AZUL}Datasource Username:${RESET}" SPRING_DATASOURCE_USERNAME
  done

  while [[ -z "$SPRING_DATASOURCE_PASSWORD" ]]; do
    read -rsp "${AZUL}Datasource Password:${RESET}" SPRING_DATASOURCE_PASSWORD
    echo
  done

  while [[ ! "$SPRING_JPA_HIBERNATE_DDL_AUTO" =~ ^(update|validate|create)$ ]]; do
    read -rp "${AZUL}JPA Hibernate ddl-auto (update/validate/create):${RESET}" SPRING_JPA_HIBERNATE_DDL_AUTO
  done

  while [[ ! "$SPRING_JPA_SHOW_SQL" =~ ^(true|false)$ ]]; do
    read -rp "${AZUL}Show SQL (true/false):${RESET}" SPRING_JPA_SHOW_SQL
  done

  if [[ -z "$SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT" ]]; then
    read -rp "${AZUL}Hibernate dialect (default: org.hibernate.dialect.MariaDBDialect):${RESET}" SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT

    SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT="org.hibernate.dialect.MariaDBDialect"
  fi

  if [[ -z "$SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_CREATE_SOURCE" ]]; then
    read -rp "${AZUL}Schema generation create source (default: metadata):${RESET}" SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_CREATE_SOURCE

    SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_CREATE_SOURCE="metadata"
  fi

  while [[ -z "$SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_SCRIPTS_ACTION" ]]; do
    read -rp "${AZUL}Scripts action (create/none):${RESET}" SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_SCRIPTS_ACTION
  done

  if [[ -z "$SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET" ]]; then
    read -rp "${AZUL}Scripts create target (.sql path. default: src/main/resources/database/migrations):${RESET}" SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET

    SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET="filesystem:src/main/resources/database/migrations"
  fi

  while [[ ! "$SPRING_FLYWAY_ENABLED" =~ ^(true|false)$ ]]; do
    read -rp "${AZUL}Enable Flyway (true/false):${RESET}" SPRING_FLYWAY_ENABLED

    if [[ "$SPRING_FLYWAY_ENABLED" == "true" ]]; then
      read -rp "${AZUL}Migrations location (default: filesystem:src/main/resources/database/migrations):${RESET}" SPRING_FLYWAY_LOCATIONS

      SPRING_FLYWAY_LOCATIONS="filesystem:src/main/resources/database/migrations"
    fi

    while [[ -z "$SPRING_FLYWAY_BASELINE_ON_MIGRATE" ]]; do
      read -rp "${AZUL}Flyway baseline-on-migrate (true/false):${RESET}" SPRING_FLYWAY_BASELINE_ON_MIGRATE
    done

    while [[ -z "$SPRING_FLYWAY_VALIDATE_ON_MIGRATE" ]]; do
      read -rp "${AZUL}Flyway validate-on-migrate (true/false):${RESET}" SPRING_FLYWAY_VALIDATE_ON_MIGRATE
    done
  done

  if ! [[ "$SERVER_PORT" =~ ^[0-9]+$ ]] || ((SERVER_PORT < 1 || SERVER_PORT > 65535)); then
    read -rp "${AZUL}Server port (1-65535. default: 8080):${RESET}" SERVER_PORT

    SERVER_PORT="8080"
  fi

  if [[ -z "$SERVER_ADDRESS" ]]; then
    read -rp "${AZUL}Server address (default: 127.0.0.1):${RESET}" SERVER_ADDRESS

    SERVER_ADDRESS="127.0.0.1"
  fi

  command clear
  printf "\n\n\n"
}

# ------------------------------------------------------------------
# Guarda todas las variables configuradas en el archivo .env.
# Protege valores con comillas para que se puedan leer correctamente
# aunque contengan espacios o caracteres especiales.
# ------------------------------------------------------------------
function save_variables() {
  # Sobrescribe el archivo para no duplicar
  >"$ENV_FILE"

  # Recorre todas las variables que quieras exportar
  for var in SPRING_APPLICATION_NAME SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD \
    SPRING_JPA_HIBERNATE_DDL_AUTO SPRING_JPA_SHOW_SQL SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_CREATE_SOURCE \
    SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_SCRIPTS_ACTION SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET \
    SPRING_FLYWAY_ENABLED SPRING_FLYWAY_LOCATIONS SPRING_FLYWAY_BASELINE_ON_MIGRATE \
    SPRING_FLYWAY_VALIDATE_ON_MIGRATE \
    SERVER_PORT SERVER_ADDRESS; do

    echo "$var=\"${!var}\"" >>"$ENV_FILE"
  done
}

# ------------------------------------------------------------------
# Actualiza el archivo .env y carga todas las variables en el
# entorno actual. Permite que la aplicación Spring Boot lea
# automáticamente las configuraciones sin ejecutar otro script.
# ------------------------------------------------------------------
function export_variables() {
  save_variables

  set -a
  source "$ENV_FILE"
  set +a
}

# ------------------------------------------------------------------
# Muestra un resumen de las variables cargadas.
# La contraseña se oculta para proteger información sensible.
# ------------------------------------------------------------------
function show_summary() {
  printf "\n\n\n"
  echo "${BLANCO}=====================================${RESET}"
  echo "${VERDE}✓ Variables de entorno cargadas exitosamente:${RESET}"
  echo
  echo "${AZUL}spring.application.name: $SPRING_APPLICATION_NAME${RESET}"
  echo "${AZUL}spring.datasource.url: $SPRING_DATASOURCE_URL${RESET}"
  echo "${AZUL}spring.datasource.username: $SPRING_DATASOURCE_USERNAME${RESET}"
  echo "${AZUL}spring.datasource.password: ********${RESET}"
  echo "${AZUL}spring.jpa.hibernate.ddl-auto: $SPRING_JPA_HIBERNATE_DDL_AUTO${RESET}"
  echo "${AZUL}spring.jpa.show-sql: $SPRING_JPA_SHOW_SQL${RESET}"
  echo "${AZUL}spring.jpa.properties.hibernate.dialect: $SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT${RESET}"
  echo "${AZUL}spring.jpa.properties.jakarta.persistence.schema.generation.create.source: $SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_CREATE_SOURCE${RESET}"
  echo "${AZUL}spring.jpa.properties.jakarta.persistence.schema.generation.scripts.action: $SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_SCRIPTS_ACTION${RESET}"
  echo "${AZUL}spring.jpa_properties.jakarta_persistence.schema.generation.scripts.create.target: $SPRING_JPA_PROPERTIES_JAKARTA_PERSISTENCE_SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET${RESET}"
  echo "${AZUL}spring.flyway.enabled: $SPRING_FLYWAY_ENABLED${RESET}"
  if [[ "$SPRING_FLYWAY_ENABLED" == "true" ]]; then
    echo "${AZUL}spring.flyway.locations: $SPRING_FLYWAY_LOCATIONS${RESET}"
    echo "${AZUL}spring.flyway.baseline-on-migrate: $SPRING_FLYWAY_BASELINE_ON_MIGRATE${RESET}"
    echo "${AZUL}spring.flyway.validate-on-migrate: $SPRING_FLYWAY_VALIDATE_ON_MIGRATE${RESET}"
  fi
  echo "${AZUL}server.port: $SERVER_PORT${RESET}"
  echo "${AZUL}server.address: $SERVER_ADDRESS${RESET}"
  echo "${BLANCO}=====================================${RESET}"

  printf "\n\n"
  echo "${BLANCO}Pulsa una tecla para continuar${RESET}"
  read -n 1 s

  command clear
  printf "\n\n\n"
}
