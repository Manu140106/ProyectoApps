@echo off
REM Script de conveniencia para FinalApps - Windows PowerShell
REM Simplifica comandos Gradle comunes

setlocal enabledelayedexpansion

:menu
cls
echo.
echo ============================================
echo   FinalApps - Document Management System
echo ============================================
echo.
echo 1. Compilar proyecto (clean build)
echo 2. Ejecutar aplicacion (bootRun)
echo 3. Ejecutar tests
echo 4. Compilar JAR
echo 5. Ver dependencias
echo 6. Limpiar build
echo 7. Compilar + Ejecutar (combo)
echo 8. Ver configuracion properties
echo 9. Salir
echo.
set /p choice="Selecciona una opcion (1-9): "

if "%choice%"=="1" goto build
if "%choice%"=="2" goto run
if "%choice%"=="3" goto test
if "%choice%"=="4" goto jar
if "%choice%"=="5" goto deps
if "%choice%"=="6" goto clean
if "%choice%"=="7" goto buildn run
if "%choice%"=="8" goto props
if "%choice%"=="9" exit /b 0

echo Opcion invalida. Presiona Enter para volver al menu.
pause
goto menu

:build
cls
echo.
echo [*] Compilando proyecto (gradle clean build)...
echo.
call .\gradlew.bat clean build
pause
goto menu

:run
cls
echo.
echo [*] Ejecutando aplicacion (spring boot)...
echo [*] Abre: http://localhost:8080/swagger-ui.html
echo [*] Presiona Ctrl+C para detener
echo.
call .\gradlew.bat bootRun
pause
goto menu

:test
cls
echo.
echo [*] Ejecutando tests (gradle test)...
echo.
call .\gradlew.bat test
pause
goto menu

:jar
cls
echo.
echo [*] Compilando JAR (gradle build JAR)...
echo.
call .\gradlew.bat build
echo.
echo [+] JAR generado en: build\libs\demo-0.0.1-SNAPSHOT.jar
echo [*] Para ejecutarlo: java -jar build\libs\demo-0.0.1-SNAPSHOT.jar
echo.
pause
goto menu

:deps
cls
echo.
echo [*] Mostrando arbol de dependencias (gradle dependencies)...
echo.
call .\gradlew.bat dependencies
pause
goto menu

:clean
cls
echo.
echo [*] Limpiando build (gradle clean)...
echo.
call .\gradlew.bat clean
echo [+] Build limpiado exitosamente
echo.
pause
goto menu

:buildn run
cls
echo.
echo [*] Compilando y ejecutando en combo...
echo.
call .\gradlew.bat clean build bootRun
pause
goto menu

:props
cls
echo.
echo ============================================
echo   Configuracion Actual (application.properties)
echo ============================================
echo.
type src\main\resources\application.properties
echo.
pause
goto menu
