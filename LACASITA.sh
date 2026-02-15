#!/bin/bash

# ==========================================
# CONFIGURACIÓN DEL VENDEDOR
# ==========================================
# ⚠️ IMPORTANTE: BORRA LA IP DE EJEMPLO Y PON LA TUYA:
IP_SERVIDOR_KEYS="149.28.114.114"
# ==========================================

# --- Validación de Root ---
if [ "$(id -u)" != "0" ]; then
  echo -e "\033[1;31mERROR: Debes ser ROOT para instalar.\nEjecuta: sudo -i\033[0m"
  exit 1
fi

# --- Configuración Inicial ---
SC_DIR="/etc/VPS-MX-SCRIPTS"
export DEBIAN_FRONTEND=noninteractive

# Función de Diseño
msg () {
  BRAN='\033[1;37m' && VERMELHO='\e[31m' && VERDE='\e[32m' && AMARELO='\e[33m'
  NEGRITO='\e[1m' && SEMCOR='\e[0m'
  case $1 in
    -bar) echo -e "${VERMELHO}————————————————————————————————————————————————————${SEMCOR}";;
    -ama) echo -e "${AMARELO}${NEGRITO}${2}${SEMCOR}";;
    -verd) echo -e "${VERDE}${NEGRITO}${2}${SEMCOR}";;
    -verm) echo -e "${AMARELO}${NEGRITO}[!] ${VERMELHO}${2}${SEMCOR}";;
  esac
}

# --- Limpieza e Instalación de Dependencias ---
clear
msg -bar
echo -e " \033[1;41m    =====>>►►  INSTALADOR LACASITA SSH  ◄◄<<=====       \033[1;37m"
msg -bar
msg -ama "               PREPARANDO SISTEMA..."

# Instalación silenciosa
apt-get update -y > /dev/null 2>&1
apt-get install -y curl wget net-tools lsof bc unzip zip screen cron > /dev/null 2>&1

# Preparar directorios
rm -rf $SC_DIR
mkdir -p $SC_DIR

# --- Función de Desencriptado (Compatible con tu KeyGen) ---
ofus () {
  txtofus=""
  number=${#1}
  for((i=0; i<$number; i++)); do
    char="${1:$i:1}"
    case $char in
      ".")char="C";; "C")char=".";;
      "3")char="@";; "@")char="3";;
      "5")char="9";; "9")char="5";;
      "6")char="P";; "P")char="6";;
      "L")char="O";; "O")char="L";;
    esac
    txtofus+="${char}"
  done
  echo "$txtofus" | rev
}

# --- Solicitar Key ---
while [[ ! $Key ]]; do
  echo -e "\033[1;93m          >>> INTRODUCE TU LICENCIA (KEY) <<<\033[0m"
  msg -bar
  echo -ne "\033[1;92m KEY: \033[1;37m" && read Key
done

msg -ne "    # Verificando Licencia... : "

# --- LÓGICA DE CONEXIÓN ---
DATOS_KEY=$(ofus "$Key")
RUTA_KEY=$(echo $DATOS_KEY | cut -d'/' -f2)
IP_KEY=$(echo $DATOS_KEY | cut -d':' -f1)

# Prioridad: Si pusiste IP en el script, usa esa. Si no, intenta leerla de la Key.
if [[ "$IP_SERVIDOR_KEYS" != "PON_TU_IP_NUEVA_AQUI" ]]; then
    IP_CONEXION="$IP_SERVIDOR_KEYS"
else
    IP_CONEXION="$IP_KEY"
fi

# Descargar lista de archivos
wget -q -O $SC_DIR/lista-arq http://${IP_CONEXION}:81/${RUTA_KEY}/lista-arq

if [[ -e $SC_DIR/lista-arq ]]; then
    echo -e "\033[1;32m ¡Licencia Autorizada!"
    msg -bar
    
    # Descargar archivos del panel
    for arqx in $(cat $SC_DIR/lista-arq); do
        msg -verm "Descargando: $arqx"
        wget -q -O $SC_DIR/${arqx} http://${IP_CONEXION}:81/${RUTA_KEY}/${arqx}
        chmod +x $SC_DIR/${arqx}
    done

    # Crear acceso directo
    ln -sf $SC_DIR/menu /usr/bin/menu
    ln -sf $SC_DIR/menu /usr/bin/LACASITA
    
    msg -bar
    msg -verd "    INSTALACIÓN COMPLETADA EXITOSAMENTE"
    msg -bar
    echo -e "    Escribe \033[1;41m menu \033[0m para iniciar."
    msg -bar
    
    # Limpieza
    rm -f lista-arq LACASITA.sh
    
    # Iniciar Menú
    menu
else
    msg -bar
    echo -e "\033[1;91m FALLO: Key Inválida o Servidor Offline"
    echo -e " Intentando conectar a IP: ${IP_CONEXION}"
    msg -bar
    exit 1
fi
