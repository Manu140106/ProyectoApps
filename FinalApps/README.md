# FinalApps - Document Management System

Aplicación empresarial de gestión de documentos basada en **Spring Boot 4.0.2** con autenticación JWT y almacenamiento centralizado.

## 🎯 Características

✅ **Autenticación Segura**: JWT Bearer tokens con BCrypt  
✅ **Gestión Multi-tenant**: Múltiples organizaciones aisladas  
✅ **Control de Documentos**: Crear, leer, actualizar, archivar  
✅ **Auditoría Completa**: Registro de todas las operaciones  
✅ **APIs RESTful**: Con documentación OpenAPI/Swagger  
✅ **Base de Datos**: MySQL con Hibernate ORM  
✅ **Validación**: Spring Validation en DTOs  

---

## 📋 Requisitos

- **Java 21** ✅
- **MySQL 8.0+** (localhost:3306)
- **Gradle** (Wrapper incluido)

---

## 🚀 Inicio Rápido

### 1. Clonar/Descargar el Proyecto

```powershell
cd FinalApps
```

### 2. Construir

```powershell
.\gradlew.bat clean build
```

### 3. Ejecutar

```powershell
.\gradlew.bat bootRun
```

### 4. Acceder

- 🌐 **App**: http://localhost:8080
- 📚 **Swagger**: http://localhost:8080/swagger-ui.html

---

## 📁 Estructura

```
src/main/java/com/eam/demo/
├── config/              # Spring Security, OpenAPI
├── controller/          # REST endpoints
├── dto/                 # Objetos de transferencia
├── entity/              # Entidades JPA
├── repository/          # Acceso a datos
├── service/             # Lógica de negocio
├── exception/           # Excepciones custom
└── security/            # JWT, autenticación
```

---

## 🔑 Credenciales Locales

| Componente | Usuario | Contraseña |
|------------|---------|-----------|
| MySQL | root | root |
| Base de datos | - | docucloud |

---

## 📚 Documentación

- [**SETUP_LOCAL.md**](SETUP_LOCAL.md) - Guía completa de instalación y troubleshooting
- [**API_REFERENCE.md**](API_REFERENCE.md) - Endpoints y ejemplos de APIs
- [**.env.example**](.env.example) - Variables de entorno disponibles

---

## 🔐 Autenticación

1. **Registrar Organización**
   ```bash
   POST /api/auth/register-organization
   ```

2. **Login**
   ```bash
   POST /api/auth/login
   ```

3. **Usar Token**
   ```
   Authorization: Bearer <TOKEN>
   ```

---

## 📊 Stack Tecnológico

| Layer | Tecnología |
|-------|-----------|
| Framework | Spring Boot 4.0.2 |
| ORM | Hibernate + Spring Data JPA |
| Base de Datos | MySQL 8.0+ |
| Autenticación | JWT (JJWT) + Spring Security |
| Validación | Spring Validation |
| Documentación | OpenAPI 3.0 / Swagger UI |
| Utilidades | Lombok |

---

## 🎮 Comandos Útiles

```powershell
# Compilar
.\gradlew.bat build

# Ejecutar en modo desarrollo
.\gradlew.bat bootRun

# Ejecutar tests
.\gradlew.bat test

# Ver dependencias
.\gradlew.bat dependencies

# Limpiar build
.\gradlew.bat clean
```

---

## 📲 APIs Principales

### Autenticación
- `POST /api/auth/register-organization` - Crear organización
- `POST /api/auth/login` - Iniciar sesión
- `GET /api/auth/organization` - Org actual

### Usuarios
- `GET /api/users` - Listar usuarios
- `POST /api/users` - Crear usuario
- `PUT /api/users/{id}` - Actualizar
- `DELETE /api/users/{id}` - Eliminar

### Documentos
- `GET /api/documents` - Listar
- `POST /api/documents` - Crear
- `PATCH /api/documents/{id}/status` - Cambiar estado

---

## ✅ Checklist de Setup

- [ ] Java 21 verificado: `java -version`
- [ ] MySQL ejecutándose con usuario root/root
- [ ] Base de datos `docucloud` lista
- [ ] `.\gradlew.bat clean build` sin errores
- [ ] App ejecutándose en http://localhost:8080
- [ ] Swagger accesible en http://localhost:8080/swagger-ui.html

---

## 🆘 Problemas Comunes

| Problema | Solución |
|----------|----------|
| **Access denied for user 'root'** | Verificar credenciales MySQL en application.properties |
| **Port 8080 already in use** | Cambiar `server.port` en application.properties |
| **Java not found** | Instalar Java 21 y añadir a PATH |
| **Database not found** | Se crea automáticamente, verificar MySQL está corriendo |

---

## 🔒 Consideraciones de Seguridad

⚠️ **DESARROLLO SOLO**: Las configuraciones incluyen valores por defecto seguros para desarrollo
- JWT Secret debe cambiar en producción
- Contraseña MySQL debe cambiar en producción
- CORS y CSRF están ajustados para desarrollo
- No usar `/actuator/**` sin protección en producción

---

## 📞 Contacto/Soporte

Para configuración adicional, consultar archivos en raíz:
- `SETUP_LOCAL.md` - Guía de instalación
- `API_REFERENCE.md` - Referencia de endpoints

---

**¡Listo para usar! 🎉**
