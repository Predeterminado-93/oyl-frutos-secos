# Guía de Interacción Frontend (Android) ↔ Backend (Microservicios)
**Proyecto: OYL Frutos Secos**

Esta guía explica en detalle cómo funciona la comunicación entre la aplicación móvil Android (Frontend) y los microservicios Spring Boot en Docker (Backend), y **cómo puedes monitorear y demostrar esta interacción en tiempo real** (en Android Studio, Docker, DBeaver y Postman) para revisiones o presentaciones académicas.

---

## 1. Diagrama de Arquitectura y Flujo de Datos

```
+---------------------------------------------------------------------------------+
|                              APP ANDROID (EMULADOR)                             |
|                                                                                 |
|   [ Pantallas Compose ] (Login, Catálogo, Carrito, Confirmación, Mis Pedidos)   |
|            │                                                                    |
|            ▼                                                                    |
|   [ ViewModels & Repositorios ] (Estrategia Híbrida: Backend + Caché Room)      |
|            │                                                                    |
|            ├── (Conexión exitosa) ──► [ ApiClient (Retrofit + OkHttp BODY) ]     |
|            │                                            │                       |
|            └── (Offline / Fallback) ──► [ SQLite Room ] │ (HTTP REST)          |
+─────────────────────────────────────────────────────────┼───────────────────────+
                                                          │ http://10.0.2.2:8080
                                                          ▼
+─────────────────────────────────────────────────────────────────────────────────+
|                         BACKEND EN DOCKER (RED oyl-network)                     |
|                                                                                 |
|                      [ API GATEWAY ] (Puerto 8080 en host)                      |
|                                     │                                           |
|            ┌────────────────────────┼────────────────────────┐                  |
|            ▼                        ▼                        ▼                  |
|   [ ms-usuarios:8081 ]    [ ms-productos:8082 ]     [ ms-ventas:8083 ]          |
|      /api/usuarios/**       /api/productos/**         /api/ventas/**            |
|            │                        │                        │                  |
|            └────────────────────────┼────────────────────────┘                  |
|                                     ▼                                           |
|                      [ POSTGRESQL CENTRALIZADO:5432 ]                           |
|                      (Bases de datos independientes)                            |
|                       ├── oyl_usuarios                                          |
|                       ├── oyl_productos                                         |
|                       └── oyl_ventas                                            |
|                                                                                 |
|   [ EUREKA SERVER:8761 ] ── Service Registry (Todos registrados como UP)        |
+---------------------------------------------------------------------------------+
```

> **¿Por qué `10.0.2.2:8080`?**  
> En el emulador estándar de Android, `10.0.2.2` es el alias virtual para apuntar al `localhost` de tu computadora anfitriona (donde Docker expone los puertos).

---

## 2. Solución y Conexión a la Base de Datos PostgreSQL

### ¿Por qué fallaba la creación de las bases de datos?
1. **Volumen persistente anterior**: Docker almacena los datos de Postgres en un volumen (`postgres_data`). Si se levantó por primera vez antes de tener el script de inicialización o con un script incompleto, Postgres **no vuelve a ejecutar** los scripts de `/docker-entrypoint-initdb.d/` porque detecta que la carpeta de datos ya existe.
2. **Postgres de Windows vs Postgres de Docker**: Tu Windows tenía instalado un Postgres con clave `admin`, pero el PostgreSQL de Docker usa las credenciales del `docker-compose.yml` (`postgres` / `postgres`).

### Credenciales oficiales para conectar DBeaver o pgAdmin
Para ver las bases de datos en DBeaver, asegúrate de que Docker esté prendido y usa estos parámetros:

| Parámetro | Valor |
|---|---|
| **Host** | `localhost` |
| **Port** | `5432` |
| **Database** | `oyl_usuarios` (o `oyl_productos`, o `oyl_ventas`) |
| **Username** | `postgres` |
| **Password** | `postgres` *(¡ojo: es `postgres`, no `admin`!)* |

#### Consultas SQL para verificar datos:
```sql
-- En base de datos oyl_usuarios:
SELECT id, nombre, apellido, email, rol, activo FROM usuarios;

-- En base de datos oyl_productos:
SELECT id, nombre, precio, stock, categoria_id FROM productos;
SELECT * FROM categorias;

-- En base de datos oyl_ventas:
SELECT * FROM ventas;
SELECT * FROM detalle_ventas;
```

---

## 3. Formas de Ver la Interacción Frontend ↔ Backend en Tiempo Real

### Forma 1: Logcat en Android Studio (Tráfico HTTP Completo)
En la app Android, `ApiClient.kt` tiene configurado `HttpLoggingInterceptor` en nivel `BODY`. Esto significa que cada solicitud y respuesta HTTP se imprime completa en la consola.

1. Abre **Android Studio**.
2. En la parte inferior, abre la pestaña **Logcat**.
3. En la barra de búsqueda de Logcat, escribe el filtro:
   ```text
   tag:OkHttp
   ```
4. Al interactuar con la app, verás en tiempo real:
   - La URL solicitada (`--> POST http://10.0.2.2:8080/api/usuarios/login`)
   - El JSON enviado (`{"email":"admin@oyl.cl","password":"..."}`)
   - La respuesta del servidor (`<-- 200 OK`)
   - El JSON recibido con el token y datos del usuario.

> **Tip**: Si quieres ver los logs específicos de la lógica de sincronización, filtra por:
> ```text
> tag:UsuarioRepository | tag:ProductoRepository | tag:PedidosViewModel
> ```

---

### Forma 2: Logs de Docker en la Terminal (Procesamiento del Backend)
Puedes ver cómo cada microservicio recibe la petición que envió la app:

Abre una terminal PowerShell y corre:

#### Ver el tráfico pasando por el Gateway:
```powershell
docker logs -f oyl-api-gateway
```

#### Ver el microservicio de Usuarios (Login/Registro):
```powershell
docker logs -f oyl-ms-usuarios
```

#### Ver el microservicio de Productos (Catálogo):
```powershell
docker logs -f oyl-ms-productos
```

#### Ver el microservicio de Ventas (Pedidos):
```powershell
docker logs -f oyl-ms-ventas
```

---

### Forma 3: Eureka Server Dashboard (Estado de los Servicios)
1. Abre tu navegador web en:  
   **[http://localhost:8761](http://localhost:8761)**
2. En la sección **Instances currently registered with Eureka**, verás los 4 servicios registrados en verde (`UP`):
   - `API-GATEWAY`
   - `MS-USUARIOS`
   - `MS-PRODUCTOS`
   - `MS-VENTAS`

---

## 4. Guía de Prueba Paso a Paso (Demostración para Evaluación)

Sigue este flujo para demostrar toda la integración:

### Paso 1: Encender los Contenedores
En la raíz del proyecto:
```powershell
docker compose up -d
```
Verifica que los 6 contenedores estén arriba:
```powershell
docker ps
```

### Paso 2: Probar el Catálogo en la App
1. Abre la app en el emulador.
2. Inicia sesión como cliente o ve al catálogo.
3. **Observa**: El catálogo muestra los 12 productos reales cargados desde PostgreSQL vía `ms-productos:8082` a través del Gateway `8080`.
4. En Logcat verás:
   ```text
   --> GET http://10.0.2.2:8080/api/productos
   <-- 200 OK (12 items)
   ```

### Paso 3: Probar el Login con el Backend
1. En la pantalla de login, ingresa:
   - **Usuario / Email**: `admin@oyl.cl`
   - **Contraseña**: `admin123`
   - **Rol**: Administrador
2. Presiona "Iniciar Sesión".
3. En Logcat verás la llamada a `POST /api/usuarios/login` y la respuesta exitosa.

### Paso 4: Realizar una Compra y Verificar en PostgreSQL
1. Agrega productos al carrito (ej. 2x Almendras).
2. Ve al carrito y presiona "Continuar al Pago".
3. Confirma el pedido.
4. **Verificación en DBeaver / Terminal**:
   Corre este comando para ver la venta recién guardada en PostgreSQL:
   ```powershell
   docker exec -i oyl-postgres psql -U postgres -d oyl_ventas -c "SELECT * FROM ventas;"
   docker exec -i oyl-postgres psql -U postgres -d oyl_ventas -c "SELECT * FROM detalle_ventas;"
   ```
5. En la app, ve a "Mis Pedidos" y verás el pedido listado con los datos sincronizados del backend.

### Paso 5: Demostración de Resiliencia / Modo Offline (Room Fallback)
1. Apaga el contenedor del gateway o de los microservicios:
   ```powershell
   docker stop oyl-api-gateway
   ```
2. Usa la app en el emulador:
   - Podrás iniciar sesión y ver los productos gracias al caché local de **SQLite Room**.
3. Vuelve a encenderlo:
   ```powershell
   docker start oyl-api-gateway
   ```
   - La app vuelve a comunicarse fluidamente con el backend.
