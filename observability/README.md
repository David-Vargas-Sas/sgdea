# Monitoreo SGDEA — Guía de Observabilidad

## Arquitectura de monitoreo

```
┌─────────────────────────────────────────────────────────────────┐
│  Microservicios Spring Boot                                      │
│                                                                   │
│  ┌──────────────┐  ┌───────────────┐  ┌───────────────────┐    │
│  │  apiGateway  │  │ administracion│  │   multitenancy    │    │
│  │   :8080      │  │    :9090      │  │      :8081        │    │
│  │              │  │               │  │                    │    │
│  │ /actuator/   │  │  /actuator/   │  │  /actuator/       │    │
│  │  prometheus  │  │   prometheus  │  │   prometheus      │    │
│  └──────┬───────┘  └───────┬───────┘  └────────┬──────────┘    │
│         │                  │                    │                │
└─────────┼──────────────────┼────────────────────┼────────────────┘
          │   scrape c/15s   │                    │
          ▼                  ▼                    ▼
    ┌─────────────────────────────────────────────────┐
    │          Prometheus  :9090                       │
    │   Almacena series de tiempo (TSDB 15d)           │
    └─────────────────────────────┬───────────────────┘
                                  │ query PromQL
                                  ▼
                    ┌─────────────────────────┐
                    │    Grafana  :3000        │
                    │  Dashboard precargado    │
                    │  admin / admin           │
                    └─────────────────────────┘
```

---

## Dependencias agregadas a cada microservicio

### ¿Dónde van las dependencias?

| Ubicación           | Qué poner                                         |
|---------------------|---------------------------------------------------|
| **POM padre**       | `micrometer-registry-prometheus` en `<dependencyManagement>` (gestiona versión una sola vez) |
| **Cada microservicio** | `<dependency>` sin `<version>` (la hereda del padre o del `spring-boot-starter-parent`) |

```xml
<!-- En cada microservicio (sin versión — gestionada por spring-boot-starter-parent) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <scope>runtime</scope>  <!-- Solo en runtime, no necesario en compilación -->
</dependency>
```

> **¿Por qué `scope: runtime`?**  
> El registro de Prometheus se autoconfigura con Spring Boot. No necesitas importar ninguna clase de él directamente. `runtime` reduce el classpath de compilación.

---

## Configuración mínima en `application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus   # ← MÍNIMO requerido
  endpoint:
    prometheus:
      enabled: true
  metrics:
    tags:
      application: ${spring.application.name}  # Identifica el servicio en Grafana
```

---

## Levantar el stack de monitoreo

```bash
cd observability
docker compose up -d
```

| Servicio    | URL                          | Credenciales |
|-------------|------------------------------|--------------|
| Prometheus  | http://localhost:9090        | —            |
| Grafana     | http://localhost:3000        | admin / admin |

El dashboard **SGDEA — Microservicios** se carga automáticamente al iniciar Grafana.

---

## Verificar que el monitoreo funciona

### 1. Endpoint Prometheus de cada microservicio

```bash
# apiGateway
curl http://localhost:8080/actuator/prometheus

# administracion
curl http://localhost:9090/actuator/prometheus

# multitenancy
curl http://localhost:8081/actuator/prometheus
```

Deberías ver texto con formato `# HELP` y `# TYPE` seguido de métricas como:
```
# HELP jvm_memory_used_bytes The amount of used memory
jvm_memory_used_bytes{application="administracion",area="heap",...} 5.4321E7
```

### 2. Endpoint de salud

```bash
curl http://localhost:8080/actuator/health
# Respuesta esperada: {"status":"UP"}
```

### 3. Verificar en Prometheus

1. Abre http://localhost:9090/targets
2. Todos los targets deben estar en estado **UP** (fondo verde)
3. Ejecuta una query de prueba: `up{job=~"sgdea-.*"}`

### 4. Verificar en Grafana

1. Abre http://localhost:3000
2. Ve a **Dashboards → SGDEA — Microservicios**
3. Deberías ver gráficas de HTTP, JVM y HikariCP

---

## Métricas clave expuestas automáticamente

| Categoría        | Métrica                                   | Descripción                      |
|------------------|-------------------------------------------|----------------------------------|
| **HTTP**         | `http_server_requests_seconds_*`          | Latencia, tasa, errores HTTP     |
| **JVM Memoria**  | `jvm_memory_used_bytes`                   | Heap y non-heap usados           |
| **JVM GC**       | `jvm_gc_pause_seconds_*`                  | Pausas del garbage collector     |
| **JVM Threads**  | `jvm_threads_live_threads`               | Hilos activos de la JVM          |
| **CPU**          | `process_cpu_usage`                       | % CPU del proceso                |
| **HikariCP**     | `hikaricp_connections_active`             | Conexiones BD en uso             |
| **HikariCP**     | `hikaricp_connections_timeout_total`      | Timeouts del pool (alerta crítica)|
| **Tomcat**       | `tomcat_threads_*`                        | Hilos activos del servidor web   |
| **Uptime**       | `process_uptime_seconds`                  | Tiempo corriendo                 |

---

## Errores comunes y soluciones

### ❌ `/actuator/prometheus` devuelve 404

**Causa:** El endpoint no está incluido en `exposure.include`.  
**Solución:**
```yaml
management.endpoints.web.exposure.include: health,info,prometheus
```

---

### ❌ `/actuator/prometheus` devuelve 401/403

**Causa:** Spring Security está bloqueando el endpoint.  
**Solución:** Permitir el acceso en tu `SecurityConfig`:

```java
// En tu SecurityFilterChain (MVC bloqueante):
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
    .anyRequest().authenticated()
);

// En tu SecurityWebFilterChain (WebFlux/Gateway):
http.authorizeExchange(ex -> ex
    .pathMatchers("/actuator/health", "/actuator/prometheus").permitAll()
    .anyExchange().authenticated()
);
```

---

### ❌ Las métricas no aparecen en Prometheus (target DOWN)

**Causas posibles:**
1. El microservicio no está corriendo → verifica con `curl http://localhost:PORT/actuator/health`
2. Firewall bloqueando el puerto
3. En Linux con Docker, `host.docker.internal` no resuelve → usar la IP del host:
   ```yaml
   # En prometheus.yml — Linux:
   - targets: ["172.17.0.1:8080"]  # IP del bridge de Docker (ifconfig docker0)
   ```

---

### ❌ No veo métricas de HikariCP

**Causa:** `micrometer-registry-prometheus` falta o tiene `scope: provided`.  
**Verificación:** Busca `hikaricp_` en `/actuator/prometheus`.  
**Solución:** Confirma que el JAR está en el classpath de runtime:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <scope>runtime</scope>  <!-- ← CORRECTO -->
</dependency>
```

---

### ❌ Las métricas de diferentes servicios se mezclan en Grafana

**Causa:** Falta el tag `application` en las métricas.  
**Solución:**
```yaml
management.metrics.tags.application: ${spring.application.name}
```
Esto añade el label `application="nombre-servicio"` a TODAS las métricas,
permitiendo filtrar por servicio en Grafana.

---

## Dashboards de Grafana recomendados (ID de Grafana.com)

Puedes importarlos desde **Grafana → Dashboards → Import → Grafana.com ID**:

| Dashboard                          | ID     |
|------------------------------------|--------|
| JVM (Micrometer) — Spring Boot 3   | `4701` |
| Spring Boot Statistics             | `6756` |
| HikariCP                           | `17671`|
| Spring Cloud Gateway               | `11506`|

---

## Configuración para producción con Kubernetes/Docker

En producción, reemplaza `host.docker.internal` en `prometheus.yml` con los nombres
de servicio de Docker/Kubernetes:

```yaml
# Docker Compose (producción):
- targets: ["administracion:9090", "multitenancy:8081", "apigateway:8080"]

# Kubernetes (con service discovery):
- job_name: "sgdea-spring-boot"
  kubernetes_sd_configs:
    - role: pod
  relabel_configs:
    - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
      action: keep
      regex: "true"
```

