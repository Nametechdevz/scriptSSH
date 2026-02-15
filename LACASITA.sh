 #!/bin/bash

# ==========================================
# CONFIGURACIÓN DEL VENDEDOR (TÚ)
# ==========================================
# ¡IMPORTANTE! BORRA "PON_TU_IP_AQUI" Y PON LA IP DE TU VPS ADMIN
IP_SERVIDOR_KEYS="PON_TU_IP_AQUI" 
# ==========================================

# --- Validación de Root ---
if [ "$(id -u)" != "0" ]; then
  echo -e "\e[1;31mERROR: Para instalar este script debes ser usuario ROOT.\nEjecuta: sudo -i\e[0m"
  exit 1
fi

# --- Funciones Visuales ---
msg () {
  BRAN='\033[1;37m' && VERMELHO='\e[31m' && VERDE='\e[32m' && AMARELO='\e[33m'
  AZUL='\e[34m' && MAGENTA='\e[35m' && MAG='\033[1;36m' && NEGRITO='\e[1m' && SEMCOR='\e[0m'
  case $1 in
    -ne)cor="${VERMELHO}${NEGRITO}" && echo -ne "${cor}${2}${SEMCOR}";;
    -ama)cor="${AMARELO}${NEGRITO}" && echo -e "${cor}${2}${SEMCOR}";;
    -verm)cor="${AMARELO}${NEGRITO}[!] ${VERMELHO}" && echo -e "${cor}${2}${SEMCOR}";;
    -azu)cor="${MAG}${NEGRITO}" && echo -e "${cor}${2}${SEMCOR}";;
    -verd)cor="${VERDE}${NEGRITO}" && echo -e "${cor}${2}${SEMCOR}";;
    "-bar")cor="${VERMELHO}————————————————————————————————————————————————————" && echo -e "${SEMCOR}${cor}${SEMCOR}";;
  esac
}

# --- Limpieza e Instalación de Dependencias ---
clear
msg -bar
echo -e " \e[97m\033[1;41m    =====>>►►  INSTALADOR LACASITA SSH  ◄◄<<=====       \033[1;37m"
msg -bar
msg -ama "               PREPARANDO SISTEMA..."

# Evitar bloqueos en Ubuntu 22/24
export DEBIAN_FRONTEND=noninteractive

# Instalación silenciosa de herramientas base
apt-get update -y > /dev/null 2>&1
apt-get install -y curl wget net-tools lsof bc unzip zip screen cron > /dev/null 2>&1

# Directorios de Instalación
SC_DIR="/etc/VPS-MX-SCRIPTS"
rm -rf $SC_DIR 
mkdir -p $SC_DIR

# --- Función de Desencriptado (Debe coincidir con tu KeyGen) ---
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

# --- Loop de Solicitud de Key ---
invalid_key () {
  msg -bar && msg -verm "  ¡KEY INVÁLIDA O EXPIRADA!" && msg -bar
  exit 1
}

while [[ ! $Key ]]; do
  echo -e "\033[1;93m          >>> INTRODUCE TU LICENCIA (KEY) <<<\033[0m"
  msg -bar
  echo -ne "\033[1;92m KEY: \033[1;37m" && read Key
done

msg -ne "    # Verificando Licencia... : "

# --- LÓGICA DE CONEXIÓN ---
# 1. Desencriptamos la Key para sacar la ruta
# La Key contiene: IP:81/CARPETA_ALEATORIA
DATOS_KEY=$(ofus "$Key")
IP_KEY=$(echo $DATOS_KEY | cut -d':' -f1)
RUTA_KEY=$(echo $DATOS_KEY | cut -d'/' -f2)

# 2. Definimos a qué IP conectar. 
# Priorizamos la IP puesta manualmente en el script (Variable IP_SERVIDOR_KEYS)
# Si la variable está vacía o es por defecto, usamos la de la Key.
if [[ "$IP_SERVIDOR_KEYS" != "PON_TU_IP_AQUI" ]]; then
    IP_CONEXION="$IP_SERVIDOR_KEYS"
else
    IP_CONEXION="$IP_KEY"
fi

# 3. Descarga lista de archivos
wget -q -O $SC_DIR/lista-arq http://${IP_CONEXION}:81/${RUTA_KEY}/lista-arq

if [[ -e $SC_DIR/lista-arq ]]; then
    echo -e "\033[1;32m ¡Licencia Autorizada!"
    msg -bar
    
    # Descargar archivos
    for arqx in $(cat $SC_DIR/lista-arq); do
        msg -verm "Descargando: $arqx"
        wget -q -O $SC_DIR/${arqx} http://${IP_CONEXION}:81/${RUTA_KEY}/${arqx}
        chmod +x $SC_DIR/${arqx}
    done

    # Finalización
    msg -bar
    
    # Crear acceso directo al menú
    ln -sf $SC_DIR/menu /usr/bin/menu
    ln -sf $SC_DIR/menu /usr/bin/LACASITA
    
    msg -verd "    INSTALACIÓN COMPLETADA EXITOSAMENTE"
    msg -bar
    echo -e "    Escribe \033[1;41m menu \033[0m para iniciar."
    msg -bar
    
    # Limpieza del instalador
    rm -f lista-arq LACASITA.sh
    
    # Entrar al menú
    menu

else
    echo -e "\033[1;91m Fallo de Conexión o Key Vencida"
    echo -e "Intentando conectar a: ${IP_CONEXION}:81"
    invalid_key
fi
