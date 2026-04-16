# 🚀 Guía de Configuración Local - FinalApps

## Requisitos Previos

✅ **Java 21** - instalado y en PATH
✅ **MySQL 8.0+** - ejecutándose en `localhost:3306`
✅ **Gradle** - usando el wrapper incluido (gradlew.bat)

---

## 1️⃣ Verificar Java 21

```powershell
java -version
```

Debe mostrar: `openjdk version "21"`

---

## 2️⃣ Verificar MySQL

### Iniciar MySQL (si no está en ejecución)

```powershell
# En Windows, MySQL debería estar como servicio
# O inicia manualmente:
mysql -u root -p
```

### Cambiar contraseña MySQL a "root" (si es necesario)

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';
FLUSH PRIVILEGES;
```

### Crear base de datos (opcional, se crea automáticamente)

```sql
CREATE DATABASE docucloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 3️⃣ Configurar Variables de Entorno (Opcional)

Copiar `.env.example` a `.env`:

```powershell
Copy-Item .env.example .env
```

Editar `.env` si necesitas valores diferentes.

---

## 4️⃣ Compilar el Proyecto

```powershell
# Desde la raíz del proyecto
.\gradlew.bat clean build
```

Esto descargará todas las dependencias e incluye:
- Spring Boot 4.0.2
- MySQL Connector
- JWT (JJWT)
- Lombok
- OpenAPI/Swagger

---

## 5️⃣ Ejecutar la Aplicación

### Opción A: Con Gradle

```powershell
.\gradlew.bat bootRun
```

### Opción B: Ejecutar JAR compilado

```powershell
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
```

### Opción C: Desde VS Code

Instalar extensiones Java:
1. Extension Pack for Java (Microsoft)
2. Spring Boot Extension Pack (VMware)

Luego usar el comando "Run" o presionar `F5`

---

## ✨ Acceder a la Aplicación

| Recurso | URL |
|---------|-----|
| **Aplicación** | http://localhost:8080 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **API Docs** | http://localhost:8080/v3/api-docs |

---

## 📝 Credenciales por Defecto

### MySQL
- **Usuario**: `root`
- **Contraseña**: `root`
- **Base de datos**: `docucloud`

### JWT
- **Secret**: `DocuCloudSuperSecretKeyForDevOnlyChangeInProduction12345`
- **Expiración**: `180 minutos`

---

## 🗂️ Estructura del Proyecto

```
src/main/java/com/eam/demo/
├── DemoApplication.java          # Punto de entrada
├── config/
│   ├── OpenApiConfig.java        # Configuración Swagger
│   └── SecurityConfig.java       # Configuración Spring Security
├── controller/
│   ├── AuthController.java       # Autenticación
│   ├── DocumentController.java   # Gestión de documentos
│   ├── DocumentTypeController.java
│   └── UserController.java       # Gestión de usuarios
├── entity/
│   ├── Document.java
│   ├── DocumentStatus.java
│   ├── DocumentType.java
│   ├── Organization.java
│   ├── Role.java
│   └── UserAccount.java
├── repository/                   # Acceso a datos
├── service/                      # Lógica de negocio
├── dto/                          # Objetos de transferencia
├── exception/                    # Excepciones personalizadas
└── security/                     # Componentes de seguridad
```

---

## 🔐 Seguridad

- **Autenticación**: JWT Bearer tokens
- **Autorización**: Role-based (ROLE_ADMIN, ROLE_USER)
- **Validación**: Spring Validation
- **CORS**: Configurado en SecurityConfig
- **⚠️ IMPORTANTE**: Cambiar JWT_SECRET antes de producción

---

## 📦 Dependencias Principales

- `spring-boot-starter-web` - APIs REST
- `spring-boot-starter-data-jpa` - Acceso a datos
- `spring-boot-starter-security` - Seguridad
- `mysql-connector-j` - Driver MySQL
- `jjwt` - JWT
- `springdoc-openapi` - OpenAPI/Swagger
- `lombok` - Reducir boilerplate

---

## 🐛 Troubleshooting

### Error: "Access denied for user 'root'@'localhost'"
```
→ Verificar credenciales en application.properties
→ Verificar que MySQL está ejecutándose
```

### Error: "No database selected"
```
→ La BD se crea automática, pero verificar: spring.jpa.hibernate.ddl-auto=update
```

### Error: Port 8080 already in use
```
→ Cambiar puerto en application.properties:
  server.port=8081
```

### Error: Java 21 not found
```
→ Verificar: java -version
→ Configurar JAVA_HOME en variables de entorno
```

---

## ✅ Checklist de Setup

- [ ] Java 21 instalado y en PATH
- [ ] MySQL ejecutándose con credenciales root/root
- [ ] Base de datos `docucloud` creada
- [ ] Variables de entorno configuradas (opcional)
- [ ] `gradlew.bat clean build` ejecutado sin errores
- [ ] Application inicia sin excepciones
- [ ] Swagger UI accesible en localhost:8080/swagger-ui.html

---

## 📞 Comandos Útiles

```powershell
# Limpiar y construir
.\gradlew.bat clean build

# Ejecutar tests
.\gradlew.bat test

# Ejecutar con perfiles
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'

# Ver logs detallados
.\gradlew.bat bootRun --debug
```

---

**¡Listo! 🎉 Tu proyecto está configurado y listo para usar.**
