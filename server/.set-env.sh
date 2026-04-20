#!/usr/bin/env bash

# -------------------------------------------------------------
# .set-env.sh
# Configura las variables de entorno necesarias para la
# aplicación Spring Boot. Permite ingresarlas desde consola,
# guardarlas en .env y cargarlas automáticamente en el entorno.
# -------------------------------------------------------------

# Cambia al directorio del script para que las rutas relativas funcionen
SCRIPT_DIR="$(command cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
command cd "$SCRIPT_DIR"

command clear
printf "\n\n\n"

# Importa funciones auxiliares y variables de entorno comunes
safe_source() {
  source "$1" 2>/dev/null || {
    echo "Error: No se pudo cargar $1"
    sleep 3
    exit 1
  }
}

safe_source "./.scripts/env/variables.sh"
safe_source "./.scripts/env/functions.sh"

prevent_sudo_or_root
execution_warning

# ------------------------------------------------------------------
# Evita que el script se ejecute directamente.
# Debe ser sourceado para que las variables se carguen en el entorno actual.
# Si se ejecuta con ./, muestra un mensaje y termina el script.
# ------------------------------------------------------------------
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  echo "${ROJO}Error, ejecute con: source .set-env.sh${RESET}"
  exit 1
fi

set -e # detiene el script si algun comando falla

# ------------------------------------------------------------------
# Flujo principal del script
# Solicita, guarda y carga las variables, luego muestra un resumen
# ------------------------------------------------------------------
request_variables
export_variables
show_summary

echo "${VERDE}Las variables de entorno están listas. Puede ejecutar su aplicación Spring Boot${RESET}"
echo "${VERDE}con este comando:  ${BLANCO}./mvnw spring-boot:run${RESET}"
echo
echo
echo
echo "${ROJO}NOTA${RESET}"
echo
echo "${ROJO}SI USTED ENCUENTRA PROBLEMAS AL EJECUTAR LA APLICACIÓN SPRING BOOT${RESET}"
echo "${ROJO}POR FAVOR REVISE QUE HAYA ESCRITO CORRECTAMENTE LAS VARIABLES${RESET}"

loading
printf "\n\n"
echo "${BLANCO}Pulsa una tecla para continuar${RESET}"
read -n 1 s
command clear
