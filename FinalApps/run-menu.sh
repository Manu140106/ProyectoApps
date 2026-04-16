#!/bin/bash
# Script para FinalApps en Linux/Mac

function show_menu() {
    clear
    echo "============================================"
    echo "   FinalApps - Document Management System"
    echo "============================================"
    echo ""
    echo "1. Compilar proyecto (clean build)"
    echo "2. Ejecutar aplicacion (bootRun)"
    echo "3. Ejecutar tests"
    echo "4. Compilar JAR"
    echo "5. Ver dependencias"
    echo "6. Limpiar build"
    echo "7. Compilar + Ejecutar"
    echo "8. Ver configuracion properties"
    echo "9. Salir"
    echo ""
    read -p "Selecciona una opcion (1-9): " choice
}

while true; do
    show_menu
    
    case $choice in
        1)
            echo "[*] Compilando proyecto..."
            ./gradlew clean build
            ;;
        2)
            echo "[*] Ejecutando aplicacion..."
            echo "[*] Abre: http://localhost:8080/swagger-ui.html"
            ./gradlew bootRun
            ;;
        3)
            echo "[*] Ejecutando tests..."
            ./gradlew test
            ;;
        4)
            echo "[*] Compilando JAR..."
            ./gradlew build
            echo "[+] JAR en: build/libs/demo-0.0.1-SNAPSHOT.jar"
            ;;
        5)
            echo "[*] Mostrando dependencias..."
            ./gradlew dependencies
            ;;
        6)
            echo "[*] Limpiando build..."
            ./gradlew clean
            echo "[+] Build limpiado"
            ;;
        7)
            echo "[*] Compilando y ejecutando..."
            ./gradlew clean build bootRun
            ;;
        8)
            echo "[*] Configuracion actual:"
            cat src/main/resources/application.properties
            ;;
        9)
            echo "Saliendo..."
            exit 0
            ;;
        *)
            echo "Opcion invalida"
            ;;
    esac
    
    read -p "Presiona Enter para continuar..."
done
