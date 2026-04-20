# -----------------------------------------------------------------------------
# Variables de configuración del entorno para la aplicación.
#
# Este archivo define las variables utilizadas durante el proceso de
# configuración del proyecto y la generación del archivo `.env`.
# NO está diseñado para ejecutarse directamente.
#
# En su lugar, debe cargarse desde otro script usando `source`:
#
#   source ./.lib/variables.sh
#
# Las variables declaradas aquí se utilizan para almacenar temporalmente
# los valores de configuración antes de exportarlos al entorno y guardarlos
# en el archivo `.env`.
# -----------------------------------------------------------------------------

# ------------------------------------------------------------------
# Directorio de proyecto y archivo .env
# ENV_FILE contendrá los valores configurados para poder cargarlos
# en cualquier sesión sin volver a ejecutar la configuración.
# ------------------------------------------------------------------
PROJECT_DIR=$(pwd)
ENV_FILE="$PROJECT_DIR/.env"
LOAD_ENV

# ------------------------------------------------------------------
# Variables de configuración de la aplicación
# Se usan temporalmente para capturar los valores del usuario
# antes de ser exportadas al entorno y guardadas en .env.
# ------------------------------------------------------------------
SPRING_APPLICATION_NAME
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_JPA_HIBERNATE_DDL_AUTO
SPRING_JPA_SHOW_SQL
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT
SPRING_FLYWAY_ENABLED
SPRING_FLYWAY_LOCATIONS
SPRING_FLYWAY_BASELINE_ON_MIGRATE
SPRING_FLYWAY_VALIDATE_ON_MIGRATE
SERVER_PORT
SERVER_ADDRESS

# ------------------------------------------------------------------
# Paleta ANSI para definir Colores
# dento de la terminal
# ------------------------------------------------------------------
RESET=$'\033[0m'

# Colores regulares
ROJO=$'\033[0;31m'
VERDE=$'\033[0;32m'
AMARILLO=$'\033[0;33m'
BLANCO=$'\033[0;37m'
AZUL=$'\033[0;34m'
