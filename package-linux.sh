#!/usr/bin/env bash
# ============================================================
#  DATA LNI - gerar o aplicativo nativo do Linux (jpackage)
#
#  Execute em uma maquina LINUX (jpackage nao faz cross-compile:
#  o pacote e sempre para o SO onde roda).
#
#  Requisitos para GERAR: JDK 21 (o jpackage faz parte do JDK).
#  Nao precisa de Maven (usa o Maven Wrapper). Quem for USAR o
#  aplicativo gerado nao precisa instalar nada (inclui o runtime
#  Java + JavaFX).
#
#  Saida: target/dist/DATALNI/bin/DATALNI  (pasta autocontida)
#
#  Variaveis (opcionais):
#    JPACKAGE_TYPE  APP_IMAGE (padrao) | DEB (dpkg/fakeroot) | RPM (rpm-build)
#    APP_PROFILE    prod (padrao, DB2+LDAP) | dev (usuarios em memoria)
#  Ex.: JPACKAGE_TYPE=DEB APP_PROFILE=dev ./package-linux.sh
# ============================================================
set -e
cd "$(dirname "$0")"

command -v java >/dev/null 2>&1 || { echo "[ERRO] JDK 21 nao encontrado (JAVA_HOME/PATH)."; exit 1; }

JPACKAGE_TYPE="${JPACKAGE_TYPE:-APP_IMAGE}"
APP_PROFILE="${APP_PROFILE:-prod}"

echo "Gerando o aplicativo (jpackage, ${JPACKAGE_TYPE}, perfil ${APP_PROFILE})..."
./mvnw -Pjpackage -DskipTests "-Djpackage.type=${JPACKAGE_TYPE}" "-Dapp.profile=${APP_PROFILE}" package

echo
echo "Pronto! Execute: target/dist/DATALNI/bin/DATALNI"
