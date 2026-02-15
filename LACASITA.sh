 #!/bin/bash

# ==========================================
# CONFIGURACIÓN DEL ADMINISTRADOR (TU NEGOCIO)
# ==========================================
# Aquí es donde el script buscará la validación de la Key.
# Si vas a vender, necesitas tu propio KeyGen (Generador) en esta IP.
# Si usas un generador público, pon esa IP.
IP_SERVIDOR_KEYS="IP_DE_TU_VPS_GENERADOR" 
PUERTO_KEYGEN="81"
# ==========================================

# --- Validación de Root Segura ---
if [ "$(id -u)" != "0" ]; then
  echo -e "\e[1;31mERROR: Para instalar este script debes ser usuario ROOT.\nEjecuta: sudo -i\e[0m"
  exit 1
fi

# --- Funciones de Diseño ---
msg () {
  BRAN='\033[1;37m' && VERMELHO='\e[31m' && VERDE='\e[32m' && AMARELO='\e[33m'
  AZUL='\e[34m' && MAGENTA='\e[35m' && MAG='\033[1;36m' && NEGRITO='\e[1m' && SEMCOR='\e[0m'
  case $1 in
    -ne)cor="${VERMELHO}${NEGRITO}" && echo -ne "${cor}${2}${SEMCOR}";;
    -ama)cor="${AMARELO}${NEGRITO}" && echo -e "${cor}${2}${SEMCOR}";;
    -verm)cor="${AMARELO}${NEGRITO}[!] ${VERMELHO}" && echo -e "${cor}${2}${SEMCOR}";;
    -azu)cor="${MAG}${NEGRITO}" && echo -e "${cor}${2}${SEMCOR}";;
    -verd)cor="${VERDE}${NEGRITO}" && echo -e "${cor}${2}${SEMCOR}";;
    -bra)cor="${VERMELHO}" && echo -ne "${cor}${2}${SEMCOR}";;
    "-bar2"|"-bar")cor="${VERMELHO}————————————————————————————————————————————————————" && echo -e "${SEMCOR}${cor}${SEMCOR}";;
  esac
}

fun_bar () {
  comando="$1"
  _=$(
    $comando > /dev/null 2>&1
  ) & > /dev/null
  pid=$!
  while [[ -d /proc/$pid ]]; do
    echo -ne "  \033[1;33m["
    for((i=0; i<40; i++)); do
      echo -ne "\033[1;31m>"
      sleep 0.1
    done
    echo -ne "\033[1;33m]"
    sleep 1s
    echo
    tput cuu1 && tput dl1
  done
  echo -ne "  \033[1;33m[\033[1;31m>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\033[1;33m] - \033[1;32m OK \033[0m\n"
  sleep 1s
}

# --- Limpieza Inicial ---
clear
msg -bar2
echo -e " \e[97m\033[1;41m    =====>>►►  SCRIPT MOD PRO (VENTA)  ◄◄<<=====       \033[1;37m"
msg -bar2
msg -ama "               PREPARANDO INSTALACION..."
msg -bar2

# Directorios
INSTALL_DIR_PARENT="/usr/local/vpsmxup/"
if [ ! -d "$INSTALL_DIR_PARENT" ]; then
  mkdir -p "$INSTALL_DIR_PARENT"
fi

# --- Instalación de Dependencias (Compatible Ubuntu 22/24/25) ---
# Evitamos bloqueos interactivos
export DEBIAN_FRONTEND=noninteractive

echo -e "\033[97m    ◽️ Actualizando repositorios..."
apt-get update -y > /dev/null 2>&1

echo -e "\033[97m    ◽️ Instalando herramientas base..."
apt-get install -y pv curl wget net-tools lsof bc unzip zip screen cron > /dev/null 2>&1

echo -e "\033[97m    ◽️ Instalando Python 3 y entornos..."
apt-get install -y python3 python3-pip software-properties-common > /dev/null 2>&1

# Soporte Legacy para scripts viejos que buscan 'python'
if ! command -v python &> /dev/null; then
    apt-get install -y python-is-python3 > /dev/null 2>&1 || ln -s /usr/bin/python3 /usr/bin/python
fi

echo -e "\033[97m    ◽️ Configuración de Red y Firewall..."
apt-get install -y ufw iptables-persistent > /dev/null 2>&1

# Instalación cosmética
apt-get install -y figlet cowsay lolcat > /dev/null 2>&1
if ! command -v lolcat &> /dev/null; then
    gem install lolcat > /dev/null 2>&1
fi

# Ajuste de Apache (Puerto 81 para liberar el 80 para Websockets/V2ray)
if dpkg -s apache2 >/dev/null 2>&1; then
    sed -i "s;Listen 80;Listen 81;g" /etc/apache2/ports.conf
    service apache2 restart > /dev/null 2>&1
fi

fun_bar 'sleep 0.5s'

# --- Función de Ofuscación (Desencriptar Key) ---
ofus () {
  unset server
  server=$(echo ${txt_ofuscatw}|cut -d':' -f1)
  unset txtofus
  number=$(expr length $1)
  for((i=1; i<$number+1; i++)); do
    txt[$i]=$(echo "$1" | cut -b $i)
    case ${txt[$i]} in
      ".")txt[$i]="C";;
      "C")txt[$i]=".";;
      "3")txt[$i]="@";;
      "@")txt[$i]="3";;
      "5")txt[$i]="9";;
      "9")txt[$i]="5";;
      "6")txt[$i]="P";;
      "P")txt[$i]="6";;
      "L")txt[$i]="O";;
      "O")txt[$i]="L";;
    esac
    txtofus+="${txt[$i]}"
  done
  echo "$txtofus" | rev
}

# --- Verificación de Archivos Descargados ---
SCPdir="/etc/VPS-MX"
SCPinstal="$HOME/install"
SCPidioma="${SCPdir}/idioma"
SCPusr="${SCPdir}/controlador"
SCPfrm="${SCPdir}/herramientas"
SCPinst="${SCPdir}/protocolos"

verificar_arq () {
  [[ ! -d ${SCPdir} ]] && mkdir ${SCPdir}
  [[ ! -d ${SCPusr} ]] && mkdir ${SCPusr}
  [[ ! -d ${SCPfrm} ]] && mkdir ${SCPfrm}
  [[ ! -d ${SCPinst} ]] && mkdir ${SCPinst}
  
  case $1 in
    "menu"|"message.txt"|"ID")ARQ="${SCPdir}/";; 
    "usercodes")ARQ="${SCPusr}/";; 
    "C-SSR.sh"|"openssh.sh"|"squid.sh"|"dropbear.sh"|"proxy.sh"|"openvpn.sh"|"ssl.sh"|"python.py"|"shadowsocks.sh"|"Shadowsocks-libev.sh"|"Shadowsocks-R.sh"|"v2ray.sh"|"slowdns.sh"|"budp.sh"|"sockspy.sh"|"PDirect.py"|"PPub.py"|"PPriv.py"|"POpen.py"|"PGet.py")ARQ="${SCPinst}/";; 
    *)ARQ="${SCPfrm}/";; 
  esac
  
  mv -f ${SCPinstal}/$1 ${ARQ}/$1
  chmod +x ${ARQ}/$1
}

# --- Loop de Solicitud de Key (Sistema de Venta) ---
error_fun () {
  msg -bar2 && msg -verm "ERROR DE CONEXIÓN CON EL SERVIDOR DE ACTIVACIÓN" && msg -bar2
  [[ -d ${SCPinstal} ]] && rm -rf ${SCPinstal}
  exit 1
}

invalid_key () {
  msg -bar2 && msg -verm "  ¡KEY INVÁLIDA O EXPIRADA! COMPRA UNA NUEVA." && msg -bar2
  [[ -e $HOME/lista-arq ]] && rm $HOME/lista-arq
  exit 1
}

while [[ ! $Key ]]; do
  clear
  msg -bar2
  echo -e "\033[1;93m          >>> SISTEMA DE ACTIVACIÓN PRIVADO <<<\033[0m"
  echo -e "\033[1;97m     Introduce tu licencia (Key) comprada para continuar:\033[0m"
  msg -bar2
  echo -ne "\033[1;92m KEY: \033[1;37m" && read Key
  tput cuu1 && tput dl1
done

msg -ne "    # Verificando Licencia con el Servidor... : "

# Prepara directorios
cd $HOME
[[ ! -d ${SCPinstal} ]] && mkdir ${SCPinstal}

# --- LÓGICA DE VERIFICACIÓN ---
# Nota: Aquí usamos la IP definida arriba o intentamos extraerla de la Key si es el metodo clásico.
# Si la Key contiene la IP encriptada, la usamos. Si no, usa IP_SERVIDOR_KEYS.

IP_DETECTADA=$(ofus "$Key" | grep -o -E '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')

if [ "$IP_DETECTADA" != "" ]; then
    IP_CHECK="$IP_DETECTADA"
else
    IP_CHECK="$IP_SERVIDOR_KEYS"
fi

# Descarga lista de archivos
wget -q -O $HOME/lista-arq http://${IP_CHECK}:${PUERTO_KEYGEN}/$(ofus "$Key" | cut -d'/' -f2)/lista-arq > /dev/null 2>&1

if [[ -e $HOME/lista-arq ]] && [[ ! $(cat $HOME/lista-arq | grep "Code de KEY Invalido!") ]]; then
    echo -e "\033[1;32m ¡Licencia Autorizada!"
    msg -bar2
    
    # Procesar descarga de archivos
    REQUEST=$(ofus "$Key"|cut -d'/' -f2)
    pontos="."
    stopping="Descargando Componentes"
    
    for arqx in $(cat $HOME/lista-arq); do
        msg -verm "${stopping}${pontos}"
        wget --no-check-certificate -q -O ${SCPinstal}/${arqx} http://${IP_CHECK}:${PUERTO_KEYGEN}/${REQUEST}/${arqx} > /dev/null 2>&1 && verificar_arq "${arqx}" || error_fun
        tput cuu1 && tput dl1
        pontos+="."
    done

    # Finalización
    msg -bar2
    rm $HOME/lista-arq &>/dev/null
    
    # Creación de comandos de acceso directo
    echo "${SCPdir}/menu" > /usr/bin/menu && chmod +x /usr/bin/menu
    echo "${SCPdir}/menu" > /usr/bin/VPSMX && chmod +x /usr/bin/VPSMX
    echo "$Key" > ${SCPdir}/key.txt

    # Configuración de idioma español por defecto
    echo "es" > ${SCPidioma}

    msg -verd "    INSTALACIÓN COMPLETADA EXITOSAMENTE"
    msg -bar2
    echo -e "    Escribe \033[1;41m menu \033[0m para iniciar el administrador."
    msg -bar2
    
    # Limpieza final
    rm -rf LACASITA.sh lista-arq install
    
    # Entrar al menu directamente
    /usr/bin/menu

else
    echo -e "\033[1;91m Fallo de Autenticación"
    invalid_key
fi
