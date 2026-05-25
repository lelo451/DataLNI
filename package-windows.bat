@echo off
REM ============================================================
REM  DATA LNI - gerar o aplicativo nativo do Windows (jpackage)
REM
REM  Execute este arquivo EM UMA MAQUINA WINDOWS (jpackage nao faz
REM  cross-compile: o pacote e sempre para o SO onde roda).
REM
REM  Requisitos para GERAR: JDK 21 (JAVA_HOME/PATH) - o jpackage faz
REM  parte do JDK. Nao precisa de Maven (usa o Maven Wrapper).
REM  Quem for USAR o aplicativo gerado nao precisa instalar nada:
REM  o pacote ja inclui o runtime Java + JavaFX.
REM
REM  Saida: target\dist\DATALNI\DATALNI.exe  (pasta autocontida)
REM ============================================================
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
  echo [ERRO] JDK 21 nao encontrado. Instale e configure JAVA_HOME/PATH.
  pause
  exit /b 1
)

REM APP_IMAGE: pasta executavel, sem ferramentas extras.
REM Para um instalador .msi (ou .exe) instale o WiX Toolset e troque por MSI.
set JPACKAGE_TYPE=APP_IMAGE

REM Perfil do Spring embutido no aplicativo: prod (DB2 + LDAP) ou dev
REM (usuarios em memoria: admin/editor/viewer + DB2 de desenvolvimento).
set APP_PROFILE=prod

echo Gerando o aplicativo (jpackage, %JPACKAGE_TYPE%, perfil %APP_PROFILE%)...
call mvnw.cmd -Pjpackage -DskipTests "-Djpackage.type=%JPACKAGE_TYPE%" "-Dapp.profile=%APP_PROFILE%" package
if errorlevel 1 (
  echo.
  echo [ERRO] Falha ao gerar. Veja as mensagens acima.
  pause
  exit /b 1
)

echo.
echo Pronto! Execute: target\dist\DATALNI\DATALNI.exe
pause
endlocal
