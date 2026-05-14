# Documento Técnico — Estado Actual del Sistema SGDEA
## Sistema de Gestión Documental Electrónico Administrativo

**Versión del documento:** 1.0  
**Fecha de elaboración:** Mayo 2026  
**Destinatarios:** Equipo de desarrollo, arquitectos de software, líderes técnicos  
**Versión del sistema:** 0.0.1-SNAPSHOT  

---

## Tabla de Contenidos

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Arquitectura General del Sistema](#2-arquitectura-general-del-sistema)
3. [Estructura de Microservicios](#3-estructura-de-microservicios)
4. [Modelo de Datos](#4-modelo-de-datos)
5. [Seguridad y Autenticación](#5-seguridad-y-autenticación)
6. [Tecnologías Utilizadas](#6-tecnologías-utilizadas)
7. [Flujos Principales del Sistema](#7-flujos-principales-del-sistema)
8. [Observabilidad y Monitoreo](#8-observabilidad-y-monitoreo)
9. [Configuración y Entornos](#9-configuración-y-entornos)
10. [Estado Actual de Implementación](#10-estado-actual-de-implementación)
11. [Limitaciones Actuales](#11-limitaciones-actuales)
12. [Oportunidades de Mejora y Recomendaciones Técnicas](#12-oportunidades-de-mejora-y-recomendaciones-técnicas)

---

## 1. Resumen Ejecutivo

**SGDEA** (Sistema de Gestión Documental Electrónico Administrativo) es una plataforma backend diseñada bajo el paradigma de **microservicios multi-tenant**. Su propósito es centralizar y gestionar documentación electrónica administrativa para múltiples empresas cliente (tenants), ofreciéndoles un espacio documental aislado con sus propias bases de datos.

El sistema se encuentra actualmente en **fase de desarrollo inicial**. El núcleo de autenticación y gestión de tenants (`multitenancy`) está funcional y maduro. El gateway de entrada (`apiGateway`) está completamente implementado. El microservicio de lógica documental (`administracion`) se encuentra en etapa de diseño con el esquema GraphQL definido pero sin implementación de resolvers.

### Módulos del sistema

| Módulo | Puerto | Estado | Descripción |
|--------|--------|--------|-------------|
| `apiGateway` | 8080 | ✅ Funcional | Punto de entrada único; valida JWT y enruta tráfico |
| `multitenancy` | 8081 | ✅ Funcional | Gestión de tenants, usuarios, roles, autenticación |
| `administracion` | 9090 | 🔧 En desarrollo | API GraphQL para gestión documental (esquema definido) |

---

## 2. Arquitectura General del Sistema

### 2.1 Tipo de Arquitectura

SGDEA implementa una arquitectura de **microservicios con gateway centralizado** y modelo de datos **multi-tenant con base de datos por tenant**. Cada empresa cliente almacena su información documental en su propia base de datos (MS SQL Server), mientras que la base de datos de superadmin (PostgreSQL) almacena la metadata de tenants, usuarios y sesiones.

### 2.2 Diagrama Lógico de Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENTES / FRONTEND                            │
│              (Aplicación web, apps móviles, sistemas externos)              │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │ HTTPS :8080
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          API GATEWAY  :8080                                  │
│                     (Spring Cloud Gateway — WebFlux)                        │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  FILTROS GLOBALES (en orden de ejecución)                              │ │
│  │  1. CorrelationFilter     → Genera X-Correlation-ID por request        │ │
│  │  2. LoggingFilter         → Registra path de cada request entrante     │ │
│  │  3. Spring Security       → Valida JWT HMAC-SHA256 (excepto rutas pub) │ │
│  │  4. JwtClaimsRelayFilter  → Propaga claims JWT como headers X-User-*   │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌──────────────────┐   ┌──────────────────────┐   ┌────────────────────┐  │
│  │  Rate Limiter    │   │  JWT Validation      │   │  Route Matching    │  │
│  │  (Redis Token    │   │  (HMAC-SHA256        │   │  (Path Predicates) │  │
│  │   Bucket)        │   │   Shared Secret)     │   │                    │  │
│  └──────────────────┘   └──────────────────────┘   └────────────────────┘  │
└──────────┬───────────────────────┬──────────────────────────────────────────┘
           │                       │
           │ /multitenancy/**      │ /administracion/** o /graphql
           ▼                       ▼
┌─────────────────────┐   ┌────────────────────────┐
│   MULTITENANCY       │   │    ADMINISTRACION       │
│     :8081            │   │       :9090             │
│  (Spring MVC)        │   │  (Spring + Netflix DGS) │
│                      │   │                         │
│  • Auth (JWT)        │   │  • API GraphQL          │
│  • Empresas          │   │  • Gestión Documental   │
│  • Usuarios/Roles    │   │  • Consultas/Mutaciones │
│  • Licencias         │   │    (en desarrollo)      │
│  • Conexiones BD     │   │                         │
│  • Provisionamiento  │   └──────────┬──────────────┘
└──────────┬───────────┘              │
           │                         │
           ▼                         ▼
  ┌─────────────────────┐   ┌──────────────────────────┐
  │  PostgreSQL          │   │  MS SQL Server           │
  │  (Superadmin DB)     │   │  (BD por Tenant)         │
  │  :8013               │   │  (conexión dinámica)     │
  │                      │   │                          │
  │  companies           │   │  Estructura documental   │
  │  users               │   │  propia de cada empresa  │
  │  roles               │   │                          │
  │  auth_sessions       │   └──────────────────────────┘
  │  company_licenses    │
  │  company_db_conns    │
  │  auth_audits         │
  └──────────────────────┘
           │
           ▼
  ┌──────────────────────┐
  │       REDIS           │
  │    (Caché JWT +       │
  │    Rate Limiting)     │
  └──────────────────────┘
```

### 2.3 Patrones Arquitectónicos Utilizados

| Patrón | Dónde se aplica | Descripción |
|--------|-----------------|-------------|
| **API Gateway** | `apiGateway` | Punto de entrada único para todos los clientes; centraliza seguridad, routing y observabilidad |
| **Multi-Tenancy (DB por Tenant)** | `multitenancy` + `administracion` | Cada empresa tiene su propia base de datos para aislamiento de datos |
| **Token Relay** | `JwtClaimsRelayFilter` | El Gateway propaga claims del JWT validado como headers HTTP a los microservicios downstream |
| **Clean Architecture** | `multitenancy` | Separación en capas: `domain` → `application` → `entryPoints` + `infrastructure` |
| **Repository Pattern** | Todos los módulos | Acceso a datos desacoplado mediante interfaces de repositorio JPA |
| **Use Case Pattern** | `multitenancy` | Interfaces de caso de uso que separan la lógica de negocio de la infraestructura |
| **CQRS parcial** | `multitenancy` | Separación entre operaciones de lectura y escritura mediante DTOs específicos |
| **Circuit Breaker** | No implementado | Pendiente para resiliencia entre microservicios |
| **JWT Stateless + Redis Cache** | `multitenancy` | JWT sin estado con caché Redis para evitar consultas a BD en cada request |

---

## 3. Estructura de Microservicios

### 3.1 Microservicio: `apiGateway`

**Artefacto Maven:** `com.sgdea:apiGateway:0.0.1-SNAPSHOT`  
**Puerto:** `8080` (configurable con `SERVER_PORT`)  
**Tecnología:** Spring Cloud Gateway + Spring WebFlux (reactivo)  
**Clase principal:** `com.sgdea.apigateway.ApiGatewayApplication`

#### Responsabilidad
Actúa como el **único punto de entrada** al sistema. Sus responsabilidades son:
- Validar la autenticidad y vigencia de los JWT en cada request entrante
- Aplicar rate limiting por usuario (rutas autenticadas) o por IP (rutas públicas)
- Enrutar las peticiones al microservicio correspondiente según el path
- Propagar la identidad del usuario autenticado como headers HTTP `X-User-*`
- Generar un `X-Correlation-ID` único por request para trazabilidad

#### Estructura de paquetes

```
com.sgdea.apigateway
└── infrastructure
    ├── config
    │   └── RateLimiterConfig.java        # KeyResolver: por usuario o por IP
    ├── filters
    │   ├── CorrelationFilter.java         # Genera X-Correlation-ID (UUID)
    │   ├── JwtClaimsRelayFilter.java      # Propaga claims JWT → headers X-User-*
    │   └── LoggingFilter.java             # Log del path de cada request
    └── security
        ├── GatewayJwtConfig.java          # Decoder JWT HMAC-SHA256 + converter
        └── SecurityConfig.java            # Cadena de seguridad WebFlux
```

#### Rutas configuradas

| Route ID | Predicado (Path) | Destino | Rate Limit (req/s) | Autenticación |
|----------|---------|---------|---------------------|---------------|
| `multitenancy-auth` | `/multitenancy/auth/**` | `http://localhost:8081` | 5 rep / 10 burst | ❌ Pública |
| `multitenancy` | `/multitenancy/**` | `http://localhost:8081` | 20 rep / 40 burst | ✅ JWT requerido |
| `administracion` | `/administracion/**` | `http://localhost:9090` | 20 rep / 40 burst | ✅ JWT requerido |
| `administracion-graphql` | `/graphql` | `http://localhost:9090` | 20 rep / 40 burst | ✅ JWT requerido |

#### Headers propagados a microservicios downstream

Después de validar el JWT, el filtro `JwtClaimsRelayFilter` extrae los claims y los inyecta como headers en la petición hacia el microservicio destino:

| Header | Claim JWT origen | Descripción |
|--------|-----------------|-------------|
| `X-User-Email` | `email` | Correo electrónico del usuario autenticado |
| `X-User-Id` | `userId` | ID numérico del usuario |
| `X-User-Role` | `roleCode` | Código del rol (ej. `ADMIN`, `USER`) |
| `X-Company-Id` | `companyId` | UUID de la empresa del usuario |
| `X-Company-Code` | `companyCode` | Código corto de la empresa |
| `X-Connection-Id` | `connectionId` | UUID de la conexión de BD del tenant |
| `X-Gateway-Source` | — | Valor fijo `sgdea-gateway` (filtro default) |

> **Nota de seguridad:** El filtro elimina cualquier header `X-User-*` / `X-Company-*` / `X-Connection-*` que venga del cliente antes de añadir los validados por el Gateway, previniendo **header injection**.

#### Rate Limiting

Implementado con Redis Token Bucket (`spring-cloud-gateway` + `spring-boot-starter-data-redis-reactive`).

- **Rutas autenticadas:** clave = `user:{email}` → evita penalizar a múltiples usuarios detrás del mismo NAT/proxy
- **Rutas públicas (login/refresh):** clave = `ip:{ip-real}` usando `X-Forwarded-For` → protege contra fuerza bruta

---

### 3.2 Microservicio: `multitenancy`

**Artefacto Maven:** `com.sgdea:multitenancy:0.0.1-SNAPSHOT`  
**Puerto:** `8081` (configurable con `SERVER_PORT`)  
**Tecnología:** Spring Boot MVC (sincrónico/bloqueante)  
**Clase principal:** `com.sgdea.multitenancy.MultitenancyApplication`

#### Responsabilidad
Núcleo del sistema. Gestiona toda la **capa de superadmin** (administración de tenants):
- Autenticación y emisión de JWT con información de tenant
- CRUD completo de empresas, usuarios, roles, licencias y conexiones de BD
- Provisionamiento integral de nuevos tenants (empresa + usuario admin + licencia + conexión de BD en una sola transacción)
- Gestión de sesiones activas con respaldo en Redis
- Auditoría de eventos de autenticación
- Cifrado de credenciales de conexión de BD de cada tenant

#### Estructura de paquetes (Clean Architecture)

Cada dominio de negocio sigue la misma estructura en capas:

```
com.sgdea.multitenancy.multitenancy
│
├── auth/                               # Autenticación y sesiones
│   ├── domain/
│   │   ├── authSession/model/          # AuthSession.java (entidad JPA)
│   │   └── exceptions/                 # AuthException
│   ├── application/
│   │   ├── dto/                        # AuthLoginRequestDto, AuthLoginResponseDto, etc.
│   │   ├── service/
│   │   │   ├── AuthService.java        # Implementación del caso de uso
│   │   │   ├── LoginRateLimiter.java   # Control de intentos fallidos (in-memory)
│   │   │   └── RefreshTokenService.java
│   │   └── usecase/
│   │       └── AuthUseCase.java        # Interfaz del caso de uso
│   └── entryPoints/rest/
│       └── AuthController.java         # POST /multitenancy/auth/{login,logout,refresh}
│
├── company/                            # Gestión de empresas (tenants)
│   ├── domain/ → model/Company.java, repository/, exceptions/
│   ├── application/ → dto/, service/CompanyService.java, usecase/
│   └── entryPoints/rest/CompanyController.java
│
├── user/                               # Gestión de usuarios
│   ├── domain/ → model/User.java, repository/, exceptions/
│   ├── application/ → dto/, service/UserService.java, usecase/
│   └── entryPoints/rest/UserController.java
│
├── role/                               # Gestión de roles
│   ├── domain/ → model/, repository/
│   ├── application/ → dto/, service/, usecase/
│   └── entryPoints/rest/RoleController.java
│
├── companyUser/                        # Relación empresa-usuario
│   ├── domain/ → model/CompanyUser.java, repository/
│   ├── application/ → dto/, service/, usecase/
│   └── entryPoints/rest/CompanyUserController.java
│
├── companyLicense/                     # Licencias de empresa
│   ├── domain/ → model/CompanyLicense.java, repository/
│   ├── application/ → dto/, service/, usecase/
│   └── entryPoints/rest/CompanyLicenseController.java
│
├── companyDatabaseConnection/          # Conexiones BD de cada tenant
│   ├── domain/ → model/, repository/
│   ├── application/ → dto/, service/
│   ├── infrastructure/config/
│   │   └── DatabaseCredentialMigrationRunner.java  # Migra credenciales en claro al cifrado AES-GCM al iniciar
│   └── entryPoints/rest/CompanyDatabaseConnectionController.java
│
├── companyProvisioning/                # Provisionamiento integral de tenant
│   ├── application/service/
│   │   └── CompanyProvisioningService.java  # Crea empresa+usuario+licencia+BD en una transacción
│   └── entryPoints/rest/CompanyProvisioningController.java
│
├── companyType/                        # Catálogo de tipos de empresa
│   └── entryPoints/rest/CompanyTypeController.java
│
├── licenseType/                        # Catálogo de tipos de licencia
│   └── entryPoints/rest/LicenseTypeController.java
│
├── authAudit/                          # Auditoría de eventos de auth
│   ├── domain/ → model/AuthAudit.java, repository/
│   └── entryPoints/rest/AuthAuditController.java
│
└── securityConfig/                     # Configuración transversal de seguridad
    ├── application/dto/
    │   ├── ApiResponseDto.java          # Wrapper genérico de respuesta REST
    │   └── JwtSessionCacheDto.java      # DTO del objeto cacheado en Redis
    └── infrastructure/
        ├── config/
        │   ├── SecurityConfig.java      # FilterChain Spring Security
        │   ├── RedisConfig.java         # Template Redis tipificado con Jackson
        │   └── OpenApiConfig.java       # Configuración Swagger/OpenAPI
        └── security/
            ├── JwtTokenService.java              # Generación/validación JWT HMAC-SHA256
            ├── JwtSessionCacheService.java        # Caché Redis de sesiones
            ├── JwtAuthenticationFilter.java       # Filtro pre-Auth
            ├── DatabaseCredentialEncryptionService.java  # Cifrado AES-GCM credenciales
            ├── JwtAccessDeniedHandler.java
            └── JwtAuthenticationEntryPoint.java
```

#### Endpoints REST expuestos

| Endpoint | Método | Autenticación | Descripción |
|----------|--------|---------------|-------------|
| `/multitenancy/auth/login` | POST | ❌ Pública | Inicio de sesión |
| `/multitenancy/auth/logout` | POST | ❌ Pública* | Cierre de sesión |
| `/multitenancy/auth/refresh` | POST | ❌ Pública | Renovar access token |
| `/multitenancy/empresas/all` | GET | ✅ JWT | Listar todas las empresas |
| `/multitenancy/empresas/{id}` | GET | ✅ JWT | Obtener empresa por ID |
| `/multitenancy/empresas/codigo/{code}` | GET | ✅ JWT | Obtener empresa por código |
| `/multitenancy/empresas/paginado` | GET | ✅ JWT | Listar empresas paginadas |
| `/multitenancy/empresas` | POST | ✅ JWT | Crear empresa |
| `/multitenancy/empresas/{id}` | PUT | ✅ JWT | Actualizar empresa |
| `/multitenancy/empresas/{id}/estado` | PATCH | ✅ JWT | Activar/desactivar empresa |
| `/multitenancy/empresas/{id}` | DELETE | ✅ JWT | Eliminar empresa |
| `/multitenancy/usuarios/**` | CRUD | ✅ JWT | Gestión de usuarios |
| `/multitenancy/roles/**` | CRUD | ✅ JWT | Gestión de roles |
| `/multitenancy/company-users/**` | CRUD | ✅ JWT | Asociación empresa-usuario |
| `/multitenancy/licencias/**` | CRUD | ✅ JWT | Gestión de licencias |
| `/multitenancy/conexiones/**` | CRUD | ✅ JWT | Gestión de conexiones BD |
| `/multitenancy/provisionamiento` | POST | ✅ JWT | Provisionamiento integral de tenant |
| `/multitenancy/auditoria/**` | GET | ✅ JWT | Consulta de auditoría |
| `/v3/api-docs/**`, `/swagger-ui/**` | GET | ❌ Pública | Documentación OpenAPI |
| `/actuator/**` | GET | Configurable | Endpoints de salud y métricas |

> *El logout se permite sin JWT para cubrir el caso de tokens ya expirados; el microservicio valida el token en el cuerpo de la petición.

---

### 3.3 Microservicio: `administracion`

**Artefacto Maven:** `com.sgdea:administracion:0.0.1-SNAPSHOT`  
**Puerto:** `9090` (configurable con `SERVER_PORT`)  
**Tecnología:** Spring Boot + Netflix DGS + Spring GraphQL  
**Clase principal:** `com.sgdea.administracion.AdministracionApplication`

#### Responsabilidad
Microservicio destinado a la lógica de negocio documental. Proveerá:
- API GraphQL para consulta y mutación de datos documentales por tenant
- Operaciones CRUD sobre entidades de tipo Company y User en el contexto documental
- Conexión con la base de datos específica del tenant (MS SQL Server o PostgreSQL) usando los datos de conexión propagados por el Gateway

#### Estado actual
⚠️ **En desarrollo inicial.** Únicamente existe la clase `AdministracionApplication.java` (punto de arranque de Spring Boot). El esquema GraphQL está **completamente definido** en `company.graphqls`, pero **no hay resolvers, servicios ni repositorios implementados**.

#### Esquema GraphQL definido (`company.graphqls`)

```graphql
# Queries disponibles (sin resolvers implementados aún)
type Query {
    companies: [Company!]!
    companyById(id: ID!): Company
    companyByCode(code: String!): Company
    users: [User!]!
    userById(id: ID!): User
    userByUsername(username: String!): User
    userByEmail(email: String!): User
}

# Mutaciones disponibles (sin resolvers implementados aún)
type Mutation {
    createCompany(input: CompanyCreateInput!): Company!
    updateCompany(id: ID!, input: CompanyUpdateInput!): Company!
    deleteCompany(id: ID!): Boolean!
    createUser(input: UserCreateInput!): User!
    updateUser(id: ID!, input: UserUpdateInput!): User!
    deleteUser(id: ID!): Boolean!
}
```

Los tipos `Company` y `User` están definidos con sus respectivos inputs de creación y actualización.

---

## 4. Modelo de Datos

### 4.1 Base de Datos de Superadmin (PostgreSQL)

Alojada en el servidor accesible desde `jdbc:postgresql://[HOST]:8013/SGDEA_SUPERADMIN`. Contiene la metadata de todos los tenants del sistema.

#### Diagrama de entidades (ERD lógico)

```
company_types ──┐
                │ 1:N
                ▼
companies ──────────────┐──────────┐─────────┐
    │ UUID id            │          │          │
    │ code (unique)      │          │          │
    │ name               │          │          │
    │ tax_id (NIT)       │          │          │
    │ verification_digit │          │          │
    │ logo_path          │          │          │
    │ active             │          │          │
    │ created_at/by      │          │          │
    │ updated_at/by      │          │          │
    │                    │          │          │
    │        ┌───────────┘ 1:N      │ 1:N      │ 1:N
    │        ▼                      ▼          ▼
    │  company_users          company_     company_
    │      Long id            licenses     database_
    │      company_id ──┘     company_id    connections
    │      user_id ──────┐    license_     company_id
    │      active         │   type_id      provider(MSSQL/PG)
    │      created_at/by  │   start_date   server
    │      updated_at/by  │   end_date     database_name
    │                     │   max_users    port
    │                     │   active       db_user
    │                     │   notes        encrypted_password
    │                     │               encrypted_conn_str
    │                     │               default_connection
    │                     │               active
    │ 1:N                 │
    ▼                     │
license_types ────────────┘
                     │
users ───────────────┘
    Long id (serial)     ─── 1:N ──▶  roles
    document_number           │          Long id
    email (unique)            │          code (unique)
    password_hash (BCrypt)    │          name
    first_name                │          active
    second_name               │
    first_last_name           │
    second_last_name          │
    phone                     │
    role_id ──────────────────┘
    active
    created_at/by
    updated_at/by
         │
         │ 1:N
         ▼
auth_sessions
    Long id
    token (JWT)
    refresh_token (UUID)
    user_id
    company_id
    connection_id
    expires_at
    refresh_expires_at
    active
    logged_out_at

auth_audits
    UUID id
    event_type (LOGIN_SUCCESS, LOGIN_FAIL, LOGOUT...)
    success (boolean)
    message
    email
    user_id (nullable)
    company_id (nullable)
    ip_address
    user_agent
    created_at
```

#### Descripción de tablas

| Tabla | PK | Descripción |
|-------|----|-------------|
| `companies` | UUID | Tenants del sistema. Cada empresa es un cliente del SGDEA |
| `company_types` | Long | Catálogo de tipos de empresa (pública, privada, etc.) |
| `users` | Long (serial) | Usuarios del sistema (superadmin y por empresa) |
| `roles` | Long | Roles del sistema (ej. `ADMIN`, `USER`, `SUPERADMIN`) |
| `company_users` | Long | Relación N:M empresa-usuario con constraint única por par |
| `company_licenses` | UUID | Licencias activas por empresa (fecha inicio/fin, max usuarios) |
| `license_types` | — | Catálogo de tipos de licencia |
| `company_database_connections` | UUID | Credenciales **cifradas** de la BD de cada tenant |
| `auth_sessions` | Long | Sesiones JWT activas (access token + refresh token) |
| `auth_audits` | UUID | Registro inmutable de eventos de autenticación |

### 4.2 Bases de Datos por Tenant (MS SQL Server / PostgreSQL)

Cada empresa cliente dispone de su propia base de datos, cuya cadena de conexión y credenciales se almacenan cifradas en `company_database_connections`. El microservicio `administracion` (en implementación) usará el `X-Connection-Id` propagado por el Gateway para recuperar la conexión correspondiente al tenant autenticado.

La tabla soporta múltiples proveedores de base de datos:
- **MS SQL Server** (driver `mssql-jdbc 12.10.1.jre11`)
- **PostgreSQL** (driver `postgresql`)

---

## 5. Seguridad y Autenticación

### 5.1 Modelo de Seguridad General

La seguridad del sistema opera en **dos capas**:

1. **Capa de Gateway** (`apiGateway`): Valida el JWT de cada request entrante antes de enrutar. Los microservicios downstream confían en los headers `X-User-*` inyectados por el Gateway.

2. **Capa de Microservicio** (`multitenancy`): Tiene su propia `SecurityFilterChain` con `JwtAuthenticationFilter` para validar tokens en requests que lleguen directamente (sin pasar por el Gateway). Configurable con `security.enabled` (en dev está en `false` por defecto).

### 5.2 Flujo de Autenticación JWT

**Implementación:** JWT artesanal con HMAC-SHA256 (sin dependencia de librerías JWT externas en `multitenancy`; el Gateway usa Spring Security OAuth2 Resource Server con Nimbus).

**Claims del JWT generado por `multitenancy`:**

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
{
  "iss": "sgdea-multitenancy",
  "sub": "usuario@empresa.com",
  "userId": 42,
  "email": "usuario@empresa.com",
  "roleId": 1,
  "roleCode": "ADMIN",
  "companyId": "550e8400-e29b-41d4-a716-446655440000",
  "companyCode": "EMP001",
  "connectionId": "660e8400-e29b-41d4-a716-446655440001",
  "iat": 1715000000,
  "exp": 1715001800
}
```

**Duraciones configurables:**
- **Access token:** 30 minutos (configurable con `SECURITY_JWT_ACCESS_TOKEN_MINUTES`)
- **Refresh token:** 8 horas (configurable con `SECURITY_JWT_REFRESH_TOKEN_HOURS`)

**Secreto compartido:** El secreto HMAC-SHA256 debe ser idéntico entre `multitenancy` (que genera los tokens) y `apiGateway` (que los valida). Se configura con la variable de entorno `SECURITY_JWT_SECRET`.

### 5.3 Caché de Sesiones con Redis

Para evitar consultas a la base de datos en cada request autenticado, las sesiones JWT se almacenan en Redis con el patrón:

```
Clave: jwt:session:{token}
Valor: JwtSessionCacheDto { token, email, roleCode, active, expiresAt, loggedOutAt }
TTL:   Calculado como (expiresAt - ahora)
```

**Flujo de verificación de sesión:**
1. `JwtAuthenticationFilter` recibe el JWT en el header `Authorization: Bearer {token}`
2. Busca en Redis → **Cache HIT:** usa el DTO cacheado (sin query a BD)
3. Cache **MISS:** consulta la tabla `auth_sessions` en PostgreSQL
4. Verifica que la sesión esté activa y no expirada

**Operaciones sobre la caché:**
- `cacheSession()`: Almacena la sesión con TTL calculado al hacer login/refresh
- `evictSession()`: Elimina el token del caché al cerrar sesión
- `markSessionInactive()`: Marca la sesión como inactiva (TTL reducido a 60s) para absorber requests en vuelo durante el logout

### 5.4 Control de Intentos Fallidos de Login

`LoginRateLimiter` mantiene un mapa en memoria (`ConcurrentHashMap`) por clave compuesta `{email}|{ip}`:
- **Máximo de intentos:** 5 (configurable con `SECURITY_LOGIN_MAX_FAILED_ATTEMPTS`)
- **Tiempo de bloqueo:** 15 minutos (configurable con `SECURITY_LOGIN_LOCK_MINUTES`)
- **Limitación:** Al ser in-memory, el contador se resetea con cada reinicio del servicio y no es efectivo en deployments multi-instancia (ver sección de limitaciones)

### 5.5 Cifrado de Credenciales de Base de Datos

Las contraseñas y cadenas de conexión de los tenants se almacenan en la tabla `company_database_connections` cifradas con **AES-256-GCM**:

```
Implementación: DatabaseCredentialEncryptionService.java
Algoritmo:      AES/GCM/NoPadding
IV:             12 bytes aleatorios (SecureRandom)
Tag GCM:        128 bits
Clave:          SHA-256 del secreto SECURITY_DATABASE_CONNECTION_ENCRYPTION_SECRET
Formato:        "v1:" + Base64URL(IV || ciphertext)
```

**Migración automática:** Al arrancar el servicio en producción, `DatabaseCredentialMigrationRunner` detecta y cifra credenciales almacenadas en texto plano (controlado con `security.database-connection.migration.encrypt-plaintext-on-startup`).

### 5.6 CORS

Configurado en `multitenancy` con los siguientes valores por defecto (sobreescribibles con `SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS`):

- **Origins permitidos:** `http://localhost:*`, `https://localhost:*`, `http://127.0.0.1:*`, etc.
- **Métodos:** GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Headers:** Todos (`*`)
- **Exposed headers:** `Authorization`
- **Max age:** 3600 segundos

### 5.7 Codificación de Contraseñas

Las contraseñas de usuarios se almacenan con **BCrypt** (`BCryptPasswordEncoder` con factor de costo por defecto = 10).

---

## 6. Tecnologías Utilizadas

### 6.1 Lenguajes y Versiones

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| **Java** | 17 (LTS) | Lenguaje principal de todos los microservicios |
| **GraphQL SDL** | — | Definición del esquema del módulo `administracion` |
| **YAML** | — | Configuración de todos los servicios |
| **Docker Compose** | — | Orquestación local del stack de observabilidad |

### 6.2 Frameworks y Librerías

| Librería | Versión | Módulo | Propósito |
|---------|---------|--------|-----------|
| **Spring Boot** | 3.5.14 | Todos | Framework base de microservicios |
| **Spring Cloud Gateway** | 4.3.x (via BOM 2025.0.2) | apiGateway | Routing reactivo y filtros |
| **Spring WebFlux** | 6.x | apiGateway | Stack reactivo del Gateway |
| **Spring Security** | 6.x | Todos | Seguridad y autenticación |
| **Spring OAuth2 Resource Server** | 6.x | apiGateway | Validación JWT con Nimbus JOSE |
| **Spring Data JPA** | 3.5.x | multitenancy, administracion | ORM con Hibernate |
| **Spring Data Redis** | 3.5.x | multitenancy, apiGateway | Caché de sesiones y rate limiting |
| **Spring Boot Actuator** | 3.5.14 | Todos | Endpoints de salud y métricas |
| **Netflix DGS** | 10.5.0 | multitenancy, administracion | Framework GraphQL sobre Spring GraphQL |
| **Micrometer** | (via Spring Boot BOM) | Todos | Facade de métricas |
| **Micrometer Prometheus** | (via BOM) | Todos | Exportador de métricas para Prometheus |
| **Micrometer Tracing Bridge Brave** | (via BOM) | apiGateway | Trazabilidad distribuida |
| **Zipkin Reporter Brave** | (via BOM) | apiGateway | Exportación de trazas a Zipkin |
| **Springdoc OpenAPI** | 2.8.16 | multitenancy | Documentación Swagger UI automática |
| **Lombok** | 1.18.32 | Todos | Reducción de boilerplate (getters/setters/constructores) |
| **MapStruct** | 1.5.5.Final | Todos | Mapeo entre entidades y DTOs en tiempo de compilación |
| **HikariCP** | (via Spring Boot BOM) | multitenancy, administracion | Pool de conexiones a PostgreSQL/MSSQL |
| **Lettuce** | (via Spring Boot BOM) | multitenancy, apiGateway | Cliente Redis con pool de conexiones |
| **Commons Pool2** | (via BOM) | multitenancy, apiGateway | Pool de conexiones Lettuce |
| **PostgreSQL JDBC** | (via BOM) | multitenancy, administracion | Driver JDBC PostgreSQL |
| **MSSQL JDBC** | 12.10.1.jre11 | administracion | Driver JDBC MS SQL Server |
| **GraalVM Native Build Tools** | — | apiGateway | Soporte futuro para compilación nativa |

### 6.3 Herramientas de Build y Desarrollo

| Herramienta | Versión | Uso |
|------------|---------|-----|
| **Maven** | (via mvnw wrapper) | Gestor de dependencias y ciclo de build |
| **Maven Wrapper** | — | Build reproducible sin instalación de Maven en el servidor |
| **Spring Boot Maven Plugin** | 3.5.14 | Empaquetado de JARs ejecutables (`*-SNAPSHOT.jar`) |
| **DGS Codegen Maven Plugin** | 1.61.5 | Generación de código Java desde esquemas GraphQL |
| **Build Helper Maven Plugin** | — | Añadir fuentes generadas al classpath de compilación |
| **Spring Boot DevTools** | 3.5.14 | Recarga en caliente durante desarrollo (apiGateway) |
| **Spring Boot Docker Compose** | 3.5.14 | Arranque automático de contenedores al iniciar la app |

### 6.4 Infraestructura y Operaciones

| Herramienta | Versión | Uso |
|------------|---------|-----|
| **Docker** | — | Contenedores para el stack de observabilidad |
| **Docker Compose** | — | Orquestación local (observability/docker-compose.yml + apiGateway/compose.yaml) |
| **Prometheus** | 2.52.0 | Almacenamiento de series de tiempo y scraping de métricas |
| **Grafana** | 11.0.0 | Visualización de métricas con dashboards precargados |
| **Redis** | (imagen base) | Caché de sesiones JWT + rate limiting del Gateway |
| **Zipkin** | (opcional) | Trazabilidad distribuida (configurado en Gateway, no hay contenedor en docker-compose) |

---

## 7. Flujos Principales del Sistema

### 7.1 Flujo de Autenticación (Login)

```
Cliente → Gateway → Multitenancy → PostgreSQL → Redis → Cliente

1. Cliente: POST /multitenancy/auth/login
   { "email": "usuario@empresa.com", "password": "password123" }

2. Gateway (apiGateway):
   - La ruta /multitenancy/auth/** es PÚBLICA → no requiere JWT
   - CorrelationFilter: genera X-Correlation-ID
   - LoggingFilter: registra "/multitenancy/auth/login"
   - Rate limiter: 5 rep/s por IP (protección fuerza bruta)
   - Enruta a → http://localhost:8081/multitenancy/auth/login

3. Multitenancy (AuthController → AuthUseCase → AuthService):
   a. LoginRateLimiter.assertAllowed(email) → verifica intentos previos
   b. UserRepository.findByEmailIgnoreCase(email) → busca usuario en PostgreSQL
   c. PasswordEncoder.matches(password, passwordHash) → valida BCrypt
   d. Verifica usuario.active == true
   e. CompanyUserRepository.findByUserId(userId) → busca empresa activa
   f. Company.active == true → verifica empresa activa
   g. ConnectionRepository.findByCompanyIdAndDefaultConnectionTrue() → conexión BD del tenant
   h. AuthSessionRepository.deactivateSessionsByUserId() → cierra sesiones previas (bulk UPDATE)
   i. JwtTokenService.generateToken(user, company, connection, expiresAt+30min)
      → genera JWT HMAC-SHA256 con claims del usuario y tenant
   j. AuthSessionRepository.save(session) → persiste sesión en PostgreSQL
   k. JwtSessionCacheService.cacheSession(session) → almacena en Redis con TTL=30min
   l. LoginRateLimiter.recordSuccess(email) → limpia intentos fallidos

4. Respuesta al cliente:
   {
     "token": "eyJ...",
     "refreshToken": "uuid-v4",
     "expiresAt": "2026-05-14T10:30:00",
     "refreshExpiresAt": "2026-05-14T18:00:00",
     "userId": 42,
     "email": "usuario@empresa.com",
     "roleCode": "ADMIN",
     "companyId": "550e8400-...",
     "companyCode": "EMP001",
     ...
   }
```

### 7.2 Flujo de Request Autenticado

```
Cliente → Gateway → Microservicio Downstream

1. Cliente: GET /multitenancy/empresas/all
   Header: Authorization: Bearer eyJ...

2. Gateway (apiGateway):
   a. CorrelationFilter → X-Correlation-ID: "abc-123-..."
   b. LoggingFilter → INFO "Incoming request: /multitenancy/empresas/all"
   c. Spring Security → jwtDecoder.decode(token):
      - Verifica firma HMAC-SHA256 con secreto compartido
      - Verifica claim "iss" == "sgdea-multitenancy"
      - Verifica timestamp "exp" no expirado
      → Si falla: 401 Unauthorized (sin llegar al microservicio)
   d. JwtClaimsRelayFilter (order=-50):
      - Extrae claims: email, userId, roleCode, companyId, companyCode, connectionId
      - Elimina cualquier header X-User-* / X-Company-* / X-Connection-* del cliente
      - Inyecta headers validados
   e. Rate Limiter: clave = "user:usuario@empresa.com" (20 rep/s)
      → Si excede: 429 Too Many Requests
   f. Enruta a → http://localhost:8081/multitenancy/empresas/all
      Con headers adicionales:
        X-User-Email: usuario@empresa.com
        X-User-Id: 42
        X-User-Role: ADMIN
        X-Company-Id: 550e8400-...
        X-Company-Code: EMP001
        X-Connection-Id: 660e8400-...
        X-Gateway-Source: sgdea-gateway
        X-Correlation-ID: abc-123-...

3. Multitenancy (CompanyController → CompanyUseCase → CompanyService):
   a. JwtAuthenticationFilter: verifica JWT (si security.enabled=true)
      Primero busca en Redis → Cache HIT (sin consultar BD)
   b. CompanyRepository.findAll() → consulta PostgreSQL
   c. Retorna lista de empresas

4. Respuesta al cliente: HTTP 200 con cuerpo JSON
```

### 7.3 Flujo de Renovación de Token (Refresh)

```
1. Cliente: POST /multitenancy/auth/refresh
   { "refreshToken": "uuid-generado-en-login" }

2. Gateway: ruta pública → no valida JWT
   Rate limiter: por IP

3. Multitenancy (AuthService.refresh):
   a. AuthSessionRepository.findByRefreshToken(refreshToken) → busca sesión
   b. Valida: session.active, refreshExpiresAt > ahora, user.active, company.active, connection.active
   c. JwtTokenService.generateToken(...) → nuevo access token (expiresAt = ahora + 30min)
   d. AuthSessionRepository.save(session) → actualiza token en BD
   e. JwtSessionCacheService.cacheSession(session) → actualiza caché Redis

4. Retorna: nuevo token + mismos datos de usuario/empresa
   NOTA: El refresh token NO se regenera (el mismo UUID es válido hasta su expiración de 8h)
```

### 7.4 Flujo de Provisionamiento de Nuevo Tenant

```
1. Cliente: POST /multitenancy/provisionamiento
   {
     "company": { "code": "NUEVA", "name": "Nueva Empresa S.A.", ... },
     "adminUser": { "email": "admin@nueva.com", "password": "...", "roleId": 1 },
     "license": { "licenseTypeId": "uuid", "startDate": "...", "endDate": "..." },
     "databaseConnection": { "provider": "MSSQL", "server": "10.0.0.1", ... }
   }

2. Gateway: JWT requerido (solo superadmin puede provisionar)

3. Multitenancy (CompanyProvisioningService.create — @Transactional):
   a. CompanyService.create(dto.company) → inserta empresa en PostgreSQL
   b. UserService.create(dto.adminUser) → inserta usuario con password BCrypt
   c. CompanyUserService.create(...) → relaciona empresa con usuario admin
   d. CompanyLicenseService.create(dto.license) → crea licencia para la empresa
   e. CompanyDatabaseConnectionService.create(dto.databaseConnection):
      - DatabaseCredentialEncryptionService.encryptIfNeeded(password) → cifra AES-GCM
      - Almacena credenciales cifradas en company_database_connections
   → Todo en una única transacción @Transactional: si algo falla, todo se revierte

4. Retorna: { company, adminUser, companyUser, license, databaseConnection }
```

### 7.5 Flujo de Logout

```
1. Cliente: POST /multitenancy/auth/logout
   Header: Authorization: Bearer eyJ...  (o body: { "token": "eyJ..." })

2. Gateway: ruta pública, no valida JWT

3. Multitenancy (AuthService.logout):
   a. AuthSessionRepository.findByToken(token) → busca sesión
   b. session.active = false, session.loggedOutAt = ahora
   c. AuthSessionRepository.save(session) → persiste en PostgreSQL
   d. JwtSessionCacheService.markSessionInactive(token):
      → Si está en Redis: activa=false, loggedOutAt=ahora, TTL=60s
      → Si no está: evictSession() → elimina del caché

4. Respuesta: HTTP 200 "Sesión cerrada correctamente"

NOTA: El JWT sigue siendo criptográficamente válido hasta su expiración,
pero el Gateway no verifica el estado de la sesión en Redis —
la verificación de estado activo se hace en el microservicio si se llama directamente.
Ver sección de limitaciones.
```

---

## 8. Observabilidad y Monitoreo

### 8.1 Arquitectura de Observabilidad

```
┌──────────────────────────────────────────────────────────────────────┐
│  Microservicios Spring Boot (exponen /actuator/prometheus)           │
│                                                                       │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────────┐    │
│  │  apiGateway  │  │ administracion│  │     multitenancy        │    │
│  │   :8080      │  │    :9090     │  │        :8081             │    │
│  │              │  │              │  │                           │    │
│  │ /actuator/   │  │  /actuator/  │  │  /actuator/              │    │
│  │  prometheus  │  │   prometheus │  │   prometheus             │    │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬──────────────┘    │
│         │                 │                      │                    │
└─────────┼─────────────────┼──────────────────────┼────────────────────┘
          │   scrape c/10s  │    scrape c/15s       │ scrape c/15s
          ▼                 ▼                       ▼
    ┌──────────────────────────────────────────────────┐
    │             Prometheus  :9090                     │
    │        Almacena TSDB (retención 15 días)          │
    └──────────────────────┬───────────────────────────┘
                           │ PromQL queries
                           ▼
             ┌──────────────────────────┐
             │       Grafana  :3000      │
             │  Dashboard "SGDEA —       │
             │  Microservicios"          │
             │  (precargado por          │
             │   provisioning)           │
             └──────────────────────────┘
```

### 8.2 Endpoints de Actuator Expuestos

#### Entorno de Desarrollo
```
health, info, prometheus, metrics, env, beans, loggers, httptrace
```

#### Entorno de Producción (limitado)
```
health, info, prometheus
```
- `/actuator/health`: muestra detalles siempre en dev, solo a usuarios autorizados en prod
- `/actuator/prometheus`: acceso sin restricción en dev; OAuth2 resource en prod

### 8.3 Configuración de Scraping (Prometheus)

| Job | Target | Scrape Interval | Labels |
|-----|--------|-----------------|--------|
| `sgdea-apigateway` | `host.docker.internal:8080` | 10 segundos | `service=apiGateway, team=sgdea` |
| `sgdea-administracion` | `host.docker.internal:9090` | 15 segundos | `service=administracion, team=sgdea` |
| `sgdea-multitenancy` | `host.docker.internal:8081` | 15 segundos | `service=multitenancy, team=sgdea` |
| `prometheus` | `localhost:9090` | 15 segundos | Auto-monitoreo |

> Retención de datos: **15 días** (`--storage.tsdb.retention.time=15d`)

### 8.4 Métricas Clave

Todas las métricas incluyen el label `application={nombre-servicio}` para filtrado en Grafana.

| Categoría | Métrica | Descripción |
|-----------|---------|-------------|
| **HTTP** | `http_server_requests_seconds_*` | Latencia p50/p95/p99, tasa de requests, errores |
| **JVM Memoria** | `jvm_memory_used_bytes` | Heap y non-heap usados |
| **JVM GC** | `jvm_gc_pause_seconds_*` | Pausas del garbage collector |
| **JVM Threads** | `jvm_threads_live_threads` | Hilos activos de la JVM |
| **CPU** | `process_cpu_usage` | Porcentaje de CPU del proceso |
| **HikariCP** | `hikaricp_connections_active` | Conexiones de BD en uso |
| **HikariCP** | `hikaricp_connections_timeout_total` | Timeouts del pool (alerta crítica) |
| **Tomcat** | `tomcat_threads_*` | Uso del pool de hilos HTTP |
| **Uptime** | `process_uptime_seconds` | Tiempo de actividad del servicio |

Los percentiles de latencia HTTP (p50, p95, p99) están configurados con `percentiles-histogram: true` en todos los servicios.

### 8.5 Trazabilidad Distribuida

Solo el módulo `apiGateway` tiene habilitada la trazabilidad distribuida:

- **Librería:** Micrometer Tracing Bridge Brave + Zipkin Reporter Brave
- **Endpoint Zipkin:** `${ZIPKIN_URL:http://localhost:9411}/api/v2/spans`
- **Estado:** Configurado en código y `pom.xml`, pero **no hay contenedor Zipkin** en el `docker-compose.yml` actual. No está activo en el stack de desarrollo.

Los microservicios `multitenancy` y `administracion` **no tienen trazabilidad distribuida** configurada actualmente.

### 8.6 Logging

| Nivel | Paquete | Descripción |
|-------|---------|-------------|
| `INFO` | `root` | Log general de la aplicación |
| `DEBUG` | `com.sgdea` | Log detallado del código de negocio |
| `WARN` | `com.zaxxer.hikari` | Solo alertas del pool HikariCP (producción) |

Formato de logs: estándar SLF4J/Logback. No hay configuración de log centralizado (ELK, Loki) en el stack actual.

---

## 9. Configuración y Entornos

### 9.1 Perfiles de Spring Boot

Cada microservicio tiene configuración por perfil:

| Archivo | Perfil | Descripción |
|---------|--------|-------------|
| `application.yml` / `application.yaml` | Base | Valores por defecto (desarrollo local) |
| `application-dev.yml` | `dev` | Overrides específicos para desarrollo |
| `application-prod.yml` | `prod` | Configuración productiva (variables de entorno obligatorias) |

El perfil activo se controla con `SPRING_PROFILES_ACTIVE` (default: `dev`).

### 9.2 Variables de Entorno por Microservicio

#### `multitenancy`

| Variable | Default (dev) | Obligatoria en prod | Descripción |
|----------|---------------|---------------------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://181.204.144.6:8013/SGDEA_SUPERADMIN` | ✅ | URL JDBC PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `juliana_solano` | ✅ | Usuario de BD |
| `SPRING_DATASOURCE_PASSWORD` | `Ju1i4n42026**` | ✅ | Contraseña de BD |
| `SPRING_REDIS_HOST` | `localhost` | ✅ | Host de Redis |
| `SPRING_REDIS_PORT` | `6379` | — | Puerto Redis |
| `SECURITY_JWT_SECRET` | `dev-secret-change-me-dev-secret-change-me` | ✅ | Secreto HMAC-SHA256 |
| `SECURITY_JWT_ISSUER` | `sgdea-multitenancy` | — | Issuer del JWT |
| `SECURITY_JWT_ACCESS_TOKEN_MINUTES` | `30` | — | Duración access token |
| `SECURITY_JWT_REFRESH_TOKEN_HOURS` | `8` | — | Duración refresh token |
| `SECURITY_ENABLED` | `false` | ✅ (debe ser `true`) | Habilita seguridad Spring |
| `SECURITY_DATABASE_CONNECTION_ENCRYPTION_SECRET` | — | ✅ | Clave cifrado AES-GCM |
| `SECURITY_DATABASE_CONNECTION_MIGRATE_PLAINTEXT` | `true` | — | Cifra credenciales en claro al iniciar |
| `HIKARI_POOL_SIZE` | `20` | — | Tamaño del pool de BD |
| `TOMCAT_MAX_THREADS` | `200` | — | Max hilos del servidor web |
| `SERVER_PORT` | `8081` | — | Puerto del servicio |

#### `apiGateway`

| Variable | Default (dev) | Obligatoria en prod | Descripción |
|----------|---------------|---------------------|-------------|
| `SECURITY_JWT_SECRET` | `dev-secret-change-me-dev-secret-change-me` | ✅ (mismo que multitenancy) | Secreto HMAC-SHA256 compartido |
| `SECURITY_JWT_ISSUER` | `sgdea-multitenancy` | — | Issuer esperado en el JWT |
| `SPRING_REDIS_HOST` | `localhost` | ✅ | Host de Redis (para rate limiter) |
| `MULTITENANCY_SERVICE_URL` | `http://localhost:8081` | ✅ | URL del microservicio multitenancy |
| `ADMIN_SERVICE_URL` | `http://localhost:9090` | ✅ | URL del microservicio administracion |
| `ZIPKIN_URL` | `http://localhost:9411` | — | URL de Zipkin para trazas |
| `SERVER_PORT` | `8080` | — | Puerto del Gateway |

#### `administracion`

| Variable | Default (dev) | Descripción |
|----------|---------------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://181.204.144.6:8013/SGDEA_SUPERADMIN` | URL JDBC (misma superadmin BD) |
| `SERVER_PORT` | `9090` | Puerto del servicio |

### 9.3 Configuración de HikariCP

| Parámetro | Desarrollo | Producción | Descripción |
|-----------|-----------|------------|-------------|
| `maximum-pool-size` | 20 | 25 | Máximo de conexiones activas |
| `minimum-idle` | 20 (igual a max) | 25 | Pool fijo (evita latencia de creación) |
| `connection-timeout` | 20s | 20s | Tiempo máximo esperando conexión libre |
| `validation-timeout` | 5s | 3s | Timeout del health check de conexión |
| `max-lifetime` | 30 min | 30 min | Vida máxima de una conexión |
| `keepalive-time` | 60s | 60s | Ping periódico para evitar timeout de firewalls cloud |
| `leak-detection-threshold` | 15s | 60s | Umbral para advertir sobre fugas de conexión |

> **Fórmula recomendada:** `pool_size ≈ (vCPUs × 2) + 1` para SSD/NVMe. Con caché Redis activo y 200 hilos Tomcat, 25 conexiones es adecuado para carga media.

---

## 10. Estado Actual de Implementación

### 10.1 Funcionalidades Completamente Implementadas ✅

#### Módulo `apiGateway`
- [x] Routing dinámico a microservicios (multitenancy, administracion)
- [x] Validación JWT HMAC-SHA256 centralizada
- [x] Rate limiting con Redis (por usuario y por IP)
- [x] Propagación de claims JWT como headers HTTP
- [x] Generación de X-Correlation-ID por request
- [x] Logging de requests entrantes
- [x] Configuración de perfiles dev/prod
- [x] Endpoints Actuator (health, prometheus)
- [x] Integración con Prometheus (métricas)

#### Módulo `multitenancy`
- [x] Autenticación completa (login, logout, refresh token)
- [x] Gestión de empresas (CRUD + paginación + activar/desactivar)
- [x] Gestión de usuarios (CRUD)
- [x] Gestión de roles (CRUD)
- [x] Relación empresa-usuario (CRUD)
- [x] Gestión de licencias de empresa (CRUD)
- [x] Gestión de conexiones de BD por tenant (CRUD + cifrado AES-GCM)
- [x] Tipos de empresa y tipos de licencia (catálogos)
- [x] Provisionamiento integral de tenant (empresa + usuario + licencia + BD en una transacción)
- [x] Auditoría de eventos de autenticación (`auth_audits`)
- [x] Caché Redis de sesiones JWT (con TTL)
- [x] Bloqueo de cuenta por intentos fallidos (in-memory)
- [x] Cifrado de credenciales de BD con AES-256-GCM
- [x] Migración automática de credenciales en claro al cifrado al iniciar
- [x] Documentación Swagger/OpenAPI automática
- [x] Optimización N+1 en cierre de sesiones (bulk UPDATE + proyección escalar)
- [x] Endpoints Actuator (health, prometheus, metrics en dev)

### 10.2 Funcionalidades en Progreso 🔧

#### Módulo `administracion`
- [ ] Implementación de resolvers GraphQL (DGS @DgsComponent)
- [ ] Entidades JPA para el modelo documental del tenant
- [ ] Repositorios Spring Data JPA
- [ ] Servicios de negocio (lógica documental)
- [ ] Conexión dinámica al datasource del tenant (usando X-Connection-Id del header)
- [ ] Seguridad (validación de headers X-User-* del Gateway)

#### Pendiente en todos los módulos
- [ ] Configuración de Zipkin/Jaeger (servicio en docker-compose + microservicios restantes)
- [ ] Alertas en Prometheus (archivos `rules/*.yml`)
- [ ] Configuración de Alertmanager
- [ ] Tests unitarios e integración (cobertura mínima)

---

## 11. Limitaciones Actuales

### 11.1 Funcionales

1. **Módulo `administracion` no operativo**: El esquema GraphQL está definido pero no hay lógica implementada. Las rutas `/administracion/**` y `/graphql` están configuradas en el Gateway pero no retornarán datos útiles.

2. **Módulo `administracion` sin conexión dinámica a BD de tenant**: Aún no implementa el mecanismo de inyección de datasource por tenant usando los headers propagados por el Gateway. Esta es la funcionalidad más compleja del sistema (multi-tenancy a nivel de datasource).

3. **Refresh token sin rotación**: El mismo `refreshToken` (UUID) es válido para renovar el access token durante 8 horas. Si es comprometido, puede usarse para obtener nuevos tokens hasta su expiración.

### 11.2 Técnicas

4. **`LoginRateLimiter` in-memory**: El contador de intentos fallidos de login se almacena en `ConcurrentHashMap`. Esto implica:
   - Se pierde al reiniciar el servicio
   - En deployment multi-instancia (múltiples replicas), cada instancia tiene su propio contador → no hay protección efectiva contra fuerza bruta distribuida
   - **Solución recomendada:** Migrar a Redis con TTL

5. **Seguridad desactivada en desarrollo** (`SECURITY_ENABLED=false` por defecto): El microservicio `multitenancy` arranca con seguridad deshabilitada en entornos de desarrollo. Aunque el Gateway la aplica, es un riesgo si el microservicio es accesible directamente.

6. **Credenciales en texto plano en `application.yaml`** (dev): Los archivos de configuración de desarrollo contienen credenciales reales de BD (`Ju1i4n42026**`). Esto es un riesgo si el repositorio es público o accesible a terceros.

7. **Sin Health Check de Redis en `multitenancy`**: Si Redis no está disponible, el servicio continúa funcionando (el código captura la excepción con un `log.warn`), pero las sesiones JWT no se cachean, aumentando la carga en PostgreSQL.

8. **Sin trazabilidad distribuida en `multitenancy` y `administracion`**: Solo `apiGateway` emite trazas Zipkin. No es posible correlacionar una traza end-to-end entre el Gateway y los microservicios downstream.

9. **Sin configuración de Alertmanager**: Los archivos de reglas Prometheus están vacíos (`rule_files: []`). No hay alertas automáticas configuradas.

10. **Zipkin sin infraestructura**: La dependencia de Zipkin está declarada en el `pom.xml` del Gateway pero no hay servidor Zipkin en el `docker-compose.yml` de observabilidad.

### 11.3 De Arquitectura

11. **Acceso directo posible a microservicios**: No hay restricción de red (firewall rules, service mesh) que impida que un cliente acceda directamente a los puertos `8081` o `9090` sin pasar por el Gateway. La seguridad por "confianza en headers" puede ser eludida.

12. **Sin Service Discovery**: Los microservicios están configurados con URLs hardcodeadas (`http://localhost:PORT`). No hay Eureka, Consul ni Kubernetes service discovery implementado.

---

## 12. Oportunidades de Mejora y Recomendaciones Técnicas

### 12.1 Seguridad (Alta Prioridad)

#### 12.1.1 Rotar credenciales en archivos de configuración
**Problema:** Los archivos `application.yaml`/`application-dev.yml` contienen credenciales reales de base de datos.  
**Recomendación:**
```bash
# Usar variables de entorno o un gestor de secretos
export SPRING_DATASOURCE_PASSWORD=<valor-seguro>
# O en IntelliJ: Edit Run Configurations → Environment Variables
```
Alternativamente, usar Spring Cloud Config o HashiCorp Vault.

#### 12.1.2 Habilitar `SECURITY_ENABLED=true` en todos los entornos
**Problema:** El microservicio `multitenancy` puede ser accedido directamente sin autenticación en dev.  
**Recomendación:** Habilitar `security.enabled=true` incluso en desarrollo local para detectar problemas de seguridad tempranamente.

#### 12.1.3 Migrar `LoginRateLimiter` a Redis
**Problema:** El contador in-memory no funciona en deployments multi-instancia.  
**Recomendación:**
```java
// Usar el RedisTemplate existente con TTL y operaciones atómicas INCR/EXPIRE
// Clave: "login:attempts:{email}|{ip}"
```

#### 12.1.4 Implementar rotación de Refresh Token
**Problema:** El mismo refresh token es válido durante 8 horas.  
**Recomendación:** Generar un nuevo UUID por cada refresh (Refresh Token Rotation). Si el token viejo se usa de nuevo, invalidar todas las sesiones del usuario (señal de compromiso).

#### 12.1.5 Restringir acceso directo a microservicios
**Recomendación:** En producción, configurar reglas de firewall o un service mesh (Istio, Linkerd) para que los microservicios solo acepten conexiones entrantes desde el Gateway (validando el header `X-Gateway-Source: sgdea-gateway`).

### 12.2 Observabilidad (Media Prioridad)

#### 12.2.1 Agregar Zipkin al docker-compose
```yaml
# En observability/docker-compose.yml:
zipkin:
  image: openzipkin/zipkin:3
  container_name: sgdea-zipkin
  ports:
    - "9411:9411"
  networks:
    - observability
```

#### 12.2.2 Añadir trazabilidad a `multitenancy` y `administracion`
```xml
<!-- En sus pom.xml: -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

#### 12.2.3 Configurar reglas de alerta en Prometheus
Crear `observability/rules/sgdea-alerts.yml` con alertas críticas:
```yaml
groups:
  - name: sgdea
    rules:
      - alert: ServiceDown
        expr: up{job=~"sgdea-.*"} == 0
        for: 1m
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 5m
      - alert: HikariPoolExhausted
        expr: hikaricp_connections_timeout_total > 0
        for: 0m
```

#### 12.2.4 Centralizar logs con Loki
Añadir Grafana Loki al stack de observabilidad para correlacionar métricas con logs usando el mismo `X-Correlation-ID` generado por el Gateway.

### 12.3 Rendimiento y Escalabilidad (Media Prioridad)

#### 12.3.1 Ajustar el pool HikariCP según carga real
Monitorear `hikaricp_connections_active` en Grafana y ajustar `HIKARI_POOL_SIZE` siguiendo la fórmula documentada en el `application.yaml`.

#### 12.3.2 Considerar programación reactiva en `multitenancy`
**Situación actual:** `multitenancy` usa Spring MVC bloqueante con 200 hilos Tomcat.  
**Para alta concurrencia:** Migrar a Spring WebFlux + R2DBC podría reducir el número de hilos necesarios. Sin embargo, implica reescritura significativa.

#### 12.3.3 Implementar Service Discovery
Para entornos cloud con múltiples instancias, reemplazar las URLs hardcodeadas con Kubernetes Service DNS o Spring Cloud LoadBalancer.

### 12.4 Calidad de Código (Media Prioridad)

#### 12.4.1 Implementar pruebas unitarias
El proyecto actualmente solo tiene `AdministracionApplicationTests.java` y pruebas básicas. Se recomienda:
- Tests unitarios para `AuthService`, `CompanyProvisioningService`, `JwtTokenService`, `DatabaseCredentialEncryptionService`
- Tests de integración para los controllers con `@WebMvcTest`
- Tests de contrato con Spring Cloud Contract para las interacciones Gateway ↔ Microservicio

#### 12.4.2 Completar el módulo `administracion`
Prioridades de implementación:
1. Configuración del datasource dinámico por tenant (usando `X-Connection-Id`)
2. Entidades JPA del modelo documental
3. Implementación de resolvers DGS para las queries y mutaciones del esquema GraphQL

#### 12.4.3 Añadir manejo de excepciones globales
Para `multitenancy`: añadir `@RestControllerAdvice` global que transforme las excepciones de dominio (`AuthException`, `EntityNotFoundException`, etc.) en respuestas `ApiResponseDto` consistentes con código HTTP apropiado.

### 12.5 Mejores Prácticas No Aplicadas

| Práctica | Estado | Recomendación |
|----------|--------|---------------|
| Secretos en variables de entorno (prod) | ⚠️ Parcial | No usar secrets en `application.yaml` de dev si el repo es compartido |
| HTTPS/TLS entre microservicios | ❌ No | Configurar certificados mTLS o usar service mesh para comunicación interna |
| Versionado de API | ❌ No | Añadir `/v1/` al path base de los endpoints REST |
| Validación de entrada en todos los endpoints | ✅ Parcial | `@Valid` en algunos controllers, revisar cobertura completa |
| Paginación por defecto | ✅ Sí | Implementada en `CompanyController` |
| `open-in-view: false` | ✅ Sí | Configurado correctamente en todos los módulos |
| Pool HikariCP fijo (min=max) | ✅ Sí | Implementado con documentación explicativa |
| DDL automático deshabilitado en prod | ✅ Sí | `ddl-auto: none` en producción |
| SQL logging deshabilitado en prod | ✅ Sí | `show-sql: false` en prod |
| Auditoría de eventos de seguridad | ✅ Sí | Tabla `auth_audits` implementada |

---

*Documento generado automáticamente a partir de análisis del código fuente del repositorio `SGDEA-BACK.V1` — Mayo 2026.*

