# Manual Técnico del Backend - OYL Frutos Secos

Bienvenido al manual técnico y operativo de la arquitectura de microservicios de **OYL Frutos Secos**. Este backend ha sido construido bajo principios de alta cohesión, bajo acoplamiento (*Bounded Contexts*), tolerancia a fallos y escalabilidad independiente.

---

## 1. Justificación de la Arquitectura y Selección de Versiones

### ¿Por qué no Spring Boot 4.x?
En la propuesta inicial se mencionó Spring Boot `4.2.0`. Técnicamente:
1. **Spring Boot 4.x no existe en el canal estable GA (General Availability)** en los repositorios oficiales de Maven Central.
2. **Spring Cloud**: Las herramientas de Spring Cloud (Eureka y Gateway) dependen de matrices estrictas de compatibilidad (*Release Trains*). No existe ningún Release Train oficial de Spring Cloud compatible con versiones 4.x prelanzamiento.

### Matriz Oficial Seleccionada
Para cumplir con los más altos estándares empresariales y académicos sin depender de artefactos inestables:
* **Java 21 LTS** (Eclipse Adoptium OpenJDK 21): Permite el aprovechamiento de Records inmutables, Pattern Matching y Virtual Threads.
* **Spring Boot 3.3.5**: Versión GA de soporte extendido basada en Spring Framework 6 y Jakarta EE 10.
* **Spring Cloud 2023.0.4 (*Leyton*)**: Matriz oficial 100% compatible con Spring Boot 3.3.x.
* **PostgreSQL JDBC 42.7.4**: Conector de alto rendimiento para bases de datos relacionales.
* **Flyway 10.x**: Motor de control de versiones y migraciones de esquemas SQL.

---

## 2. Mapa de Microservicios y Puertos

Cada componente es un proyecto Maven completamente aislado con su propio ciclo de vida:

| Módulo | Directorio | Puerto | Base de Datos | Rol en la Arquitectura |
| :--- | :--- | :---: | :---: | :--- |
| **`eureka-server`** | `eureka-server/` | `8761` | *Ninguna* | **Service Registry & Discovery**: Monitorea el estado (*heartbeat*) de cada instancia. Panel web de administración. |
| **`api-gateway`** | `api-gateway/` | `8080` | *Ninguna* | **Edge Gateway**: Enrutador no bloqueante (WebFlux) que actúa como cara visible hacia la aplicación Android. Gestiona CORS y balanceo. |
| **`ms-usuarios`** | `ms-usuarios/` | `8081` | `oyl_usuarios` | **Seguridad y Usuarios**: Maneja el ciclo de vida de usuarios, autenticación, roles (`ROLE_CLIENTE`, `ROLE_ADMIN`, `ROLE_TRABAJADOR`) y contraseñas con BCrypt. |
| **`ms-productos`** | `ms-productos/` | `8082` | `oyl_productos` | **Inventario y Catálogo**: Control de categorías, productos, precios, stock disponible y operaciones atómicas de descuento y reposición. |
| **`ms-ventas`** | `ms-ventas/` | `8083` | `oyl_ventas` | **Ventas y Transacciones**: Orquestación de compras, líneas de detalle de pedido, cálculo de totales y máquina de estados del pedido. |

---

## 3. Base de Datos Centralizada con Aislamiento Lógico

Acatando los principios de diseño de microservicios, **ningún microservicio consulta directamente la base de datos de otro**:

```text
PostgreSQL (Puerto 5432)
├── oyl_usuarios   <-- Exclusiva de ms-usuarios (tablas: usuarios)
├── oyl_productos  <-- Exclusiva de ms-productos (tablas: categorias, productos)
└── oyl_ventas     <-- Exclusiva de ms-ventas (tablas: ventas, detalle_ventas)
```

### Inicialización de PostgreSQL
Ejecuta el script SQL incluido en esta carpeta:
```bash
psql -U postgres -f init-databases.sql
```

### Migraciones con Flyway
Cada microservicio incluye sus scripts de migración en `src/main/resources/db/migration/`:
* `ms-usuarios`: `V1__init_usuarios.sql` (tabla `usuarios`, índices y restricciones únicas).
* `ms-productos`: `V1__init_productos.sql` (tablas `categorias`, `productos` y claves foráneas internas).
* `ms-ventas`: `V1__init_ventas.sql` (tablas `ventas` y `detalle_ventas` con cascada controlada).

Al arrancar cada microservicio, Flyway verifica y aplica automáticamente las migraciones pendientes sin necesidad de intervención manual.

---

## 4. Variables de Entorno

Cada servicio dispone de valores predeterminados seguros para desarrollo local, pero pueden sobreescribirse mediante variables de entorno del sistema o archivos `.env`:

| Variable | Descripción | Valor Predeterminado |
| :--- | :--- | :--- |
| `DB_URL` | URL JDBC de conexión al microservicio | `jdbc:postgresql://localhost:5432/<nombre_db>` |
| `DB_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `postgres` |
| `EUREKA_URI` | URL para registrarse en Eureka Server | `http://localhost:8761/eureka/` |

---

## 5. Instrucciones de Compilación y Ejecución

### Compilar y Ejecutar Pruebas
Para validar que todo el ecosistema compila y pasa sus pruebas unitarias:
```bash
mvn -f pom.xml test
```
*Las pruebas unitarias utilizan mocks de Mockito y bases de datos H2 en memoria, por lo que no requieren tener PostgreSQL encendido para compilar.*

### Orden de Inicio de los Servicios en Desarrollo

Para que el descubrimiento dinámico opere correctamente, se recomienda iniciar los servicios en este orden (cada uno en su propia consola):

1. **Eureka Server**:
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```
   *Espera unos segundos y abre en tu navegador: [http://localhost:8761](http://localhost:8761)*.

2. **Microservicio de Usuarios**:
   ```bash
   cd ms-usuarios
   mvn spring-boot:run
   ```

3. **Microservicio de Productos**:
   ```bash
   cd ms-productos
   mvn spring-boot:run
   ```

4. **Microservicio de Ventas**:
   ```bash
   cd ms-ventas
   mvn spring-boot:run
   ```

5. **API Gateway**:
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

Al refrescar el panel de Eureka (`http://localhost:8761`), verás las 4 aplicaciones registradas con estado **UP**.

---

## 6. Ejemplos de Pruebas Rápidas con `curl` (Vía API Gateway :8080)

Todas las solicitudes deben dirigirse al **puerto 8080**:

### 1. Registrar un Usuario
```bash
curl -X POST http://localhost:8080/api/usuarios/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "email": "juan@oyl.cl",
    "password": "miPasswordSeguro123",
    "telefono": "+56912345678",
    "direccion": "Av. Providencia 1234"
  }'
```

### 2. Iniciar Sesión (Login)
```bash
curl -X POST http://localhost:8080/api/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@oyl.cl",
    "password": "miPasswordSeguro123"
  }'
```

### 3. Crear una Categoría
```bash
curl -X POST http://localhost:8080/api/categorias \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Frutos Secos Premium",
    "descripcion": "Nueces, almendras y castañas seleccionadas"
  }'
```

### 4. Crear un Producto
```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Almendras Tostadas 500g",
    "descripcion": "Almendras tostadas sin sal",
    "precio": 6500.00,
    "stock": 50,
    "imagenUrl": "https://oyl.cl/img/almendras.png",
    "categoriaId": 1
  }'
```

### 5. Consultar el Catálogo
```bash
curl -X GET http://localhost:8080/api/productos
```

### 6. Registrar una Compra / Venta
```bash
curl -X POST http://localhost:8080/api/ventas \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": 1,
    "metodoPago": "TARJETA_DEBITO",
    "direccionEntrega": "Av. Providencia 1234",
    "detalles": [
      {
        "productoId": 1,
        "nombreProducto": "Almendras Tostadas 500g",
        "precioUnitario": 6500.00,
        "cantidad": 2
      }
    ]
  }'
```

---

## 7. Preguntas Frecuentes y Diagnóstico

### ¿Por qué el API Gateway me retorna 503 Service Unavailable?
Cuando un microservicio acaba de arrancar, puede tardar entre 10 y 30 segundos en propagar su disponibilidad (*heartbeat*) hacia Eureka y que el Gateway refresque su caché de rutas. Espera unos instantes o consulta el panel `http://localhost:8761` para asegurar que el servicio figure en estado `UP`.

### ¿Cómo conecto la aplicación Android si estoy usando un teléfono físico?
El emulador de Android utiliza la IP virtual `10.0.2.2` para acceder al `localhost` de tu computador. Si ejecutas la app en un teléfono físico conectado a la misma red WiFi que tu computador, reemplaza `10.0.2.2` por tu IP local (por ejemplo `http://192.168.1.50:8080/`) en [ApiClient.kt](file:///c:/Users/juanc/Desktop/PROG/Estudio%20Duoc%20UC/Aplicaciones%20Moviles/oyl-frutos-secos/app/src/main/java/com/oyl/frutossecos/data/remote/ApiClient.kt).
