# 📋 Referencia Rápida - APIs y Configuración

## 🔐 Ciclo de Autenticación

### 1. Registrar Organización

**POST** `/api/auth/register-organization`

```json
{
  "organizationName": "Mi Empresa",
  "adminFirstName": "Juan",
  "adminLastName": "Pérez",
  "adminEmail": "admin@empresa.com",
  "password": "SecurePassword123!"
}
```

**Response (201)**
```json
{
  "id": 1,
  "name": "Mi Empresa",
  "createdAt": "2024-04-08T10:30:00Z"
}
```

---

### 2. Login

**POST** `/api/auth/login`

```json
{
  "email": "admin@empresa.com",
  "password": "SecurePassword123!"
}
```

**Response (200)**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "expiresIn": 10800
}
```

---

### 3. Obtener Organización Actual

**GET** `/api/auth/organization`

**Headers**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (200)**
```json
{
  "id": 1,
  "name": "Mi Empresa",
  "createdAt": "2024-04-08T10:30:00Z"
}
```

---

## 👥 Gestión de Usuarios

### Crear Usuario

**POST** `/api/users`

```json
{
  "email": "usuario@empresa.com",
  "firstName": "Carlos",
  "lastName": "López",
  "password": "UserPassword123!",
  "role": "ROLE_USER"
}
```

### Obtener Usuarios

**GET** `/api/users`

### Obtener Usuario por ID

**GET** `/api/users/{userId}`

### Actualizar Usuario

**PUT** `/api/users/{userId}`

### Eliminar Usuario

**DELETE** `/api/users/{userId}`

---

## 📄 Gestión de Documentos

### Crear Documento

**POST** `/api/documents`

```json
{
  "name": "Documento importante",
  "description": "Descripción del documento",
  "documentTypeId": 1
}
```

### Obtener Documentos

**GET** `/api/documents`

### Obtener Documento por ID

**GET** `/api/documents/{docId}`

### Actualizar Estado de Documento

**PATCH** `/api/documents/{docId}/status`

```json
{
  "status": "ARCHIVED"
}
```

### Eliminar Documento

**DELETE** `/api/documents/{docId}`

---

## 🏷️ Tipos de Documento

### Crear Tipo de Documento

**POST** `/api/document-types`

```json
{
  "name": "Contrato",
  "description": "Documentos de contrato"
}
```

### Obtener Tipos

**GET** `/api/document-types`

---

## 🔍 Logs de Auditoría

### Obtener Auditoría

**GET** `/api/audit-logs`

### Obtener por Documento

**GET** `/api/audit-logs?documentId={docId}`

---

## 📚 Documentación Interactiva

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## 🛡️ Seguridad de Endpoints

| Endpoint | Requiere Autenticación | Roles |
|----------|----------------------|-------|
| `/api/auth/**` | ❌ No | - |
| `/swagger-ui/**` | ❌ No | - |
| `/v3/api-docs/**` | ❌ No | - |
| `/api/users/**` | ✅ Sí | ROLE_ADMIN |
| `/api/documents/**` | ✅ Sí | ROLE_USER, ROLE_ADMIN |
| `/api/document-types/**` | ✅ Sí | ROLE_ADMIN |
| `/api/audit-logs/**` | ✅ Sí | ROLE_ADMIN |

---

## 🔄 Respuestas HTTP Estándar

| Código | Significado | Ejemplo |
|--------|-------------|---------|
| 200 | OK - Operación exitosa | GET /api/users |
| 201 | CREATED - Recurso creado | POST /api/documents |
| 400 | BAD REQUEST - Datos inválidos | Email inválido |
| 401 | UNAUTHORIZED - Sin token | Falta header Authorization |
| 403 | FORBIDDEN - Sin permisos | Token de ROLE_USER en /api/admin |
| 404 | NOT FOUND - Recurso inexistente | GET /api/users/999 |
| 500 | INTERNAL ERROR - Error del servidor | Exception no manejada |

---

## 🗂️ Modelos de Datos

### UserAccount
- `id` (Long)
- `email` (String, unique)
- `firstName` (String)
- `lastName` (String)
- `passwordHash` (String)
- `role` (Enum: ROLE_ADMIN, ROLE_USER)
- `organization` (Organization)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### Document
- `id` (Long)
- `name` (String)
- `description` (String)
- `status` (Enum: ACTIVE, ARCHIVED, DELETED)
- `type` (DocumentType)
- `organization` (Organization)
- `uploadedBy` (UserAccount)
- `filePath` (String)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### Organization
- `id` (Long)
- `name` (String)
- `users` (List<UserAccount>)
- `documents` (List<Document>)
- `createdAt` (LocalDateTime)

---

## 📝 Ejemplo de Flujo Completo

```
1. POST /api/auth/register-organization
   → Crear organización y admin

2. POST /api/auth/login
   → Obtener JWT token

3. POST /api/document-types (con token)
   → Crear tipos de documento

4. POST /api/documents (con token)
   → Subir documento

5. GET /api/documents (con token)
   → Listar documentos

6. PATCH /api/documents/{id}/status (con token)
   → Cambiar estado
```

---

## 🚀 Headers Obligatorios

Todos los endpoints autenticados (excepto `/api/auth/**`) requieren:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

---

## ⚙️ Configuración de Desarrollo

**Archivo**: `application.properties`

```properties
# Cambios locales a considerar
spring.datasource.username=root           # Tu usuario MySQL
spring.datasource.password=root           # Tu contraseña
server.port=8080                          # Puerto (cambiar si está ocupado)
security.jwt.secret=TuSecretoAqui         # Cambiar en producción
storage.base-path=./storage               # Carpeta para archivos
```

---

## 📊 Estadísticas del Proyecto

- **Controladores**: 4 (Auth, Document, DocumentType, User)
- **Servicios**: 5+
- **Repositorios**: 5
- **Entidades**: 7
- **DTOs**: 10+

---

## 💡 Tips

1. **JWT va a expirar**: Guardar el token y refascar antes de 180 minutos
2. **Documento storage**: Los archivos se guardan en `./storage`
3. **Swagger útil**: Probar APIs desde Swagger UI es muy cómodo
4. **Logs en consola**: Ver `spring.jpa.show-sql=true` para debug
5. **BCrypt**: Las contraseñas se hashean con BCrypt
