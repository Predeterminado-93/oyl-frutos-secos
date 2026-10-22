# Documentación Técnica y Arquitectura — OYL Frutos Secos (Android)

Esta guía detalla la arquitectura completa de la aplicación móvil **OYL Frutos Secos**, desarrollada en **Kotlin** con **Jetpack Compose (Material 3)** y persistencia local en **SQLite mediante Room Database**. Incluye la explicación funcional y de código para cada una de las pantallas (*Screens*), componentes del sistema, validaciones, animaciones, mapa, escáner QR y base de datos.

---

## 📑 Tabla de Contenidos
1. [Arquitectura General del Sistema](#1-arquitectura-general-del-sistema)
2. [Capa de Persistencia: SQLite & Room](#2-capa-de-persistencia-sqlite--room)
   - [¿Qué es Room y su relación con SQLite?](#a-qué-es-room-y-cómo-se-relaciona-con-sqlite)
   - [Ubicación en el proyecto y árbol de archivos](#b-dónde-se-ubica-en-el-proyecto-estructura-de-directorios)
   - [Pilar 1: La Base de Datos (AppDatabase.kt)](#c-pilar-1-la-base-de-datos-appdatabasekt)
   - [Pilar 2: Las Entidades (@Entity)](#d-pilar-2-las-entidades-entity--tablas-de-sqlite)
   - [Pilar 3: Los DAOs y sus Conceptos (@Dao, Suspend, Flow)](#e-pilar-3-los-daos-data-access-objects--la-capa-de-acceso-a-datos)
   - [Explicación de CADA UNO de los 5 DAOs (con código y métodos)](#f-explicación-detallada-de-cada-uno-de-los-5-daos)
   - [Flujo Completo: Conexión Room con MVVM y Compose](#g-flujo-completo-cómo-se-conecta-room-con-mvvm-y-jetpack-compose)
   - [Precarga de Datos (Seeding)](#h-precarga-de-datos-seeding-y-cuentas-por-defecto)
   - [Manejo de Botones de Retroceso (Back Navigation)](#i-manejo-de-botones-de-retroceso-back-navigation)
3. [Detalle de Cada Pantalla (Screens)](#3-detalle-de-cada-pantalla-screens)
   - [Pantalla 1: Splash / Landing](#pantalla-1-splash--landing)
   - [Pantalla 2: Inicio de Sesión (Login)](#pantalla-2-inicio-de-sesión-login)
   - [Pantalla 3: Registro de Cliente](#pantalla-3-registro-de-cliente)
   - [Pantalla 4: Contenedor Principal & Dashboard](#pantalla-4-contenedor-principal--dashboard)
   - [Pantalla 5: Mapa de Tiendas y Puntos de Retiro](#pantalla-5-mapa-de-tiendas-y-puntos-de-retiro)
   - [Pantalla 6: Catálogo de Productos](#pantalla-6-catálogo-de-productos)
   - [Pantalla 7: Detalle del Producto](#pantalla-7-detalle-del-producto)
   - [Pantalla 8: Carrito de Compras](#pantalla-8-carrito-de-compras)
   - [Pantalla 9: Confirmación del Pedido](#pantalla-9-confirmación-del-pedido)
   - [Pantalla 10: Escáner QR Multi-Formato](#pantalla-10-escáner-qr-multi-formato)
   - [Pantalla 11: Historial de Pedidos / Mis Pedidos](#pantalla-11-historial-de-pedidos--mis-pedidos)
   - [Pantalla 12: Perfil de Usuario y Sesión](#pantalla-12-perfil-de-usuario-y-sesión)
   - [Pantalla 13: Gestión de Productos (Administración)](#pantalla-13-gestión-de-productos-administración)
   - [Pantalla 14: Gestión de Inventario y Alertas de Stock](#pantalla-14-gestión-de-inventario-y-alertas-de-stock)
   - [Pantalla 15: Centro de Notificaciones](#pantalla-15-centro-de-notificaciones)
4. [Componentes Clave y Cómo se Implementan](#4-componentes-clave-y-cómo-se-implementan)
   - [Jetpack Compose & Material 3](#a-jetpack-compose--material-3)
   - [Animaciones](#b-animaciones)
   - [Formularios y Validaciones](#c-formularios-y-validaciones)
   - [Módulo de Mapa y Geolocalización](#d-módulo-de-mapa-y-geolocalización)
   - [Escáner QR Multi-Formato y Cámara](#e-escáner-qr-multi-formato-y-cámara)
   - [Control de Sesión y Aislamiento de Clientes](#f-control-de-sesión-y-aislamiento-de-clientes)
   - [Navegación y Gestión del Botón Atrás (BackHandler)](#g-navegación-con-jetpack-compose-y-gestión-del-botón-atrás-backhandler)

---

## 1. Arquitectura General del Sistema

La aplicación sigue el patrón arquitectónico oficial recomendado por Google: **MVVM (Model - View - ViewModel)** combinado con el principio de flujo unidireccional de datos (*Unidirectional Data Flow - UDF*).

```
┌────────────────────────────────────────────────────────┐
│                   VISTA (UI - Compose)                │
│  Composables observan StateFlow y emiten eventos/acciones│
└───────────────────────────▲────────────────────────────┘
                            │ Estado (StateFlow)
                            │ Eventos (Lambdas)
┌───────────────────────────┴────────────────────────────┐
│                    VIEWMODEL                           │
│  Mantiene UiState, valida entradas y ejecuta corrutinas│
└───────────────────────────▲────────────────────────────┘
                            │ Suspends / Flow
┌───────────────────────────┴────────────────────────────┐
│              REPOSITORIO / DAO (Room SQLite)           │
│  Abstracción de datos locales: Entities, Queries, Seeds │
└────────────────────────────────────────────────────────┘
```

* **UI Layer:** Desarrollada 100% con **Jetpack Compose**, sin archivos XML de layout.
* **State Management:** `StateFlow` y `MutableStateFlow` dentro de `ViewModel` para propagar el estado de manera reactiva e inmutable.
* **Navigation:** `NavHost` de Jetpack Navigation Compose con rutas fuertemente tipadas en la clase sellada `NavRoutes`.
* **Asincronía:** Kotlin Coroutines (`viewModelScope`) y Kotlin `Flow` para datos en tiempo real desde SQLite.

---

## 2. Capa de Persistencia: SQLite & Room

### A. ¿Qué es Room y cómo se relaciona con SQLite?

**SQLite** es el motor de base de datos relacional estándar embebido de forma nativa en el sistema operativo Android desde sus inicios. Sin embargo, utilizar SQLite de forma nativa mediante la API clásica de `SQLiteOpenHelper` presenta severos inconvenientes en proyectos modernos:
1. Las consultas SQL se escriben como cadenas de texto plano (`String`), lo que no permite validar errores de sintaxis o nombres de columnas erróneos en tiempo de compilación.
2. Requiere escribir abundante código repetitivo (*boilerplate*) para transformar cursores de base de datos (`Cursor`) en objetos Kotlin mediante métodos manuales como `cursor.getString(cursor.getColumnIndexOrThrow("nombre"))`.
3. No cuenta con soporte reactivo nativo para sincronizar la base de datos con las pantallas declarativas de Jetpack Compose.
4. Si se ejecuta una consulta en el hilo principal (*Main Thread*), la aplicación se congela y arroja el error fatal ANR (*Application Not Responding*).

Para solucionar esto, Google introdujo **Room**, que es la biblioteca oficial de persistencia de Android Jetpack. **Room es un ORM (*Object-Relational Mapping*) robusto que se monta sobre SQLite**, ofreciendo:
* **Verificación de consultas SQL en tiempo de compilación:** Si una tabla o columna en un `@Query` está mal escrita, el compilador KSP (*Kotlin Symbol Processing*) falla antes de que la app se ejecute, evitando caídas en producción.
* **Mapeo automático de tablas a objetos Kotlin:** Transforma directamente las filas de SQLite en instancias de clases de datos (`@Entity`).
* **Soporte nativo para Kotlin Coroutines:** Las operaciones de escritura y lecturas puntuales se ejecutan de manera asíncrona mediante funciones suspendidas (`suspend fun`), liberando completamente el hilo de la interfaz.
* **Reactividad en tiempo real con Kotlin `Flow`:** Cuando una tabla cambia (mediante `INSERT`, `UPDATE` o `DELETE`), Room notifica automáticamente a los observadores con el nuevo listado, provocando la recomposición instantánea de Jetpack Compose sin intervención manual.

```
┌────────────────────────────────────────────────────────────────────────┐
│                   VISTA / UI (Jetpack Compose)                        │
│            Observa StateFlow emitido por el ViewModel                 │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ Estado Reactivo
┌───────────────────────────────────┴────────────────────────────────────┐
│                            VIEWMODEL                                   │
│           Ejecuta corrutinas en viewModelScope y mapea flujos          │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ suspend fun / Flow<T>
┌───────────────────────────────────┴────────────────────────────────────┐
│                           REPOSITORIO                                  │
│         Orquesta los DAOs y centraliza las reglas de negocio           │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │
┌───────────────────────────────────┴────────────────────────────────────┐
│                    ROOM PERSISTENCE LIBRARY (ORM)                     │
│  ┌───────────────────────┐ ┌───────────────────┐ ┌──────────────────┐  │
│  │   AppDatabase.kt      │ │    DAOs (@Dao)    │ │ Entities (@Entity│  │
│  │  Punto de acceso DB   │ │   Consultas SQL   │ │ Tablas relativas │  │
│  └───────────────────────┘ └───────────────────┘ └──────────────────┘  │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ Sentencias SQL generadas
┌───────────────────────────────────┴────────────────────────────────────┐
│                  MOTOR SQLITE NATIVO DE ANDROID                        │
│          Archivo físico: oyl_frutos_secos.db en el dispositivo         │
└────────────────────────────────────────────────────────────────────────┘
```

---

### B. ¿Dónde se ubica en el proyecto? (Estructura de Directorios)

Todos los componentes de la capa de datos local se encuentran organizados dentro del paquete principal `com.OYL.frutossecos.data.local`:

```
app/src/main/java/com/oyl/frutossecos/data/local/
│
├── db/                                      <-- Configuración del motor Room y ciclo de vida
│   └── AppDatabase.kt                       <-- Clase abstracta RoomDatabase, Singleton y Callback de precarga
│
├── entities/                                <-- Clases de datos que definen las tablas SQLite
│   ├── UsuarioEntity.kt                     <-- Tabla 'usuarios' (login, roles, sesión activa)
│   ├── ProductoEntity.kt                    <-- Tabla 'productos' (catálogo, precios, inventario)
│   ├── PuntoRetiroEntity.kt                 <-- Tabla 'puntos_retiro' (tiendas físicas y puntos metro con GPS)
│   ├── PedidoEntity.kt                      <-- Tabla 'pedidos' (historial y estado de compras por cliente)
│   └── CarritoItemEntity.kt                 <-- Tabla 'carrito_items' (canasta temporal por cliente)
│
├── dao/                                     <-- Interfaces con anotaciones SQL para acceso a datos
│   ├── UsuarioDao.kt                        <-- Métodos de autenticación, sesión y perfiles
│   ├── ProductoDao.kt                       <-- Catálogo, búsqueda, categorías y alerta de stock bajo
│   ├── PuntoRetiroDao.kt                    <-- Puntos georreferenciados para el mapa interactivo
│   ├── PedidoDao.kt                         <-- Gestión de pedidos aislados por usuario y KPIs admin
│   └── CarritoDao.kt                        <-- Canasta, incremento de cantidad, totales y vaciado
│
└── repository/                              <-- Capa intermedia que expone los DAOs al resto de la app
    ├── UsuarioRepository.kt
    ├── ProductoRepository.kt
    ├── PuntoRetiroRepository.kt
    ├── PedidoRepository.kt
    └── CarritoRepository.kt
```

#### Ubicación del archivo físico SQLite en el dispositivo:
Cuando la aplicación se compila e instala en el dispositivo o emulador Android, SQLite crea el archivo de base de datos en el almacenamiento interno privado y protegido de la aplicación (al cual no pueden acceder otras aplicaciones por razones de seguridad):
* **Ruta absoluta en el sistema operativo Android:**  
  `/data/data/com.OYL.frutossecos/databases/oyl_frutos_secos.db`
* **Archivos auxiliares de SQLite WAL (*Write-Ahead Logging*):**  
  - `oyl_frutos_secos.db-wal`: Registro de transacciones previas para lecturas y escrituras concurrentes de alta velocidad.
  - `oyl_frutos_secos.db-shm`: Archivo de memoria compartida para coordinar el acceso a los datos.

---

### C. Pilar 1: La Base de Datos (`AppDatabase.kt`)

El archivo [`AppDatabase.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/db/AppDatabase.kt) es el corazón de la persistencia local. Es una clase abstracta que hereda de `RoomDatabase` y define la configuración general del motor.

#### Código Fuente Completo de `AppDatabase.kt`:
```kotlin
package com.OYL.frutossecos.data.local.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.OYL.frutossecos.data.local.dao.*
import com.OYL.frutossecos.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UsuarioEntity::class,
        ProductoEntity::class,
        PuntoRetiroEntity::class,
        PedidoEntity::class,
        CarritoItemEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Métodos abstractos: Room genera la implementación de cada DAO en compilación
    abstract fun usuarioDao(): UsuarioDao
    abstract fun productoDao(): ProductoDao
    abstract fun puntoRetiroDao(): PuntoRetiroDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun carritoDao(): CarritoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oyl_frutos_secos.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

#### Explicación Detallada de Cada Elemento de `AppDatabase.kt`:
1. **Anotación `@Database(...)`:**
   * `entities = [...]`: Le indica a Room cuáles son las 5 clases que deben convertirse en tablas relacionales SQLite.
   * `version = 4`: Controla la versión del esquema. Al incrementar de 3 a 4 (cuando se agregaron las coordenadas de georreferenciación y el campo de sesión activa), Room detecta cambios estructurales.
   * `exportSchema = false`: Evita exportar esquemas JSON a disco, ideal para desarrollo y agilidad de compilación.
2. **`abstract class AppDatabase : RoomDatabase()`:**
   * Debe ser `abstract` porque nosotros no escribimos el código de bajo nivel; KSP genera en compilación la clase interna `AppDatabase_Impl`.
3. **Funciones Abstractas de DAOs (`abstract fun usuarioDao(): UsuarioDao`):**
   * Sirven como fábricas (*factories*) para que los repositorios soliciten la instancia del DAO correspondiente.
4. **Patrón Singleton (`@Volatile` y `synchronized`):**
   * `@Volatile`: Garantiza que cualquier cambio en la variable `INSTANCE` sea visible inmediatamente para todos los hilos de ejecución de la CPU.
   * `synchronized(this)`: Previene condiciones de carrera (*race conditions*). Si dos hilos intentan solicitar la base de datos simultáneamente al iniciar la aplicación, solo uno puede entrar al bloque `synchronized`, evitando la creación de múltiples instancias en memoria.
5. **`fallbackToDestructiveMigration(dropAllTables = true)`:**
   * Si la estructura de alguna entidad cambia durante el desarrollo y se sube el número de versión, Room borra las tablas y las vuelve a crear limpias en vez de colapsar la app con un error de migración fallida.
6. **`.addCallback(SeedCallback())`:**
   * Intercepta la creación (`onCreate`) y apertura (`onOpen`) de SQLite para insertar los datos de prueba obligatorios del MVP (administrador, cliente, catálogo con stock real y tiendas de retiro).

---

### D. Pilar 2: Las Entidades (`@Entity`) — Tablas de SQLite

Una **Entidad** representa una tabla relacional en SQLite. Se define mediante una `data class` de Kotlin anotada con `@Entity`.

#### Anotaciones clave utilizadas en las entidades:
* `@Entity(tableName = "nombre_tabla")`: Asigna el nombre físico de la tabla en SQLite.
* `@PrimaryKey(autoGenerate = true)`: Define la clave primaria autoincremental de tipo entero.
* `@Index(value = ["columna"], unique = true)`: Crea un índice único en el motor de base de datos para garantizar que no existan valores duplicados (ejemplo: alias de usuario único).

#### Las 5 Tablas del Sistema:

| Entidad / Archivo | Tabla SQLite | Llave Primaria | Campos Clave y Restricciones | Propósito en el Negocio |
|---|---|---|---|---|
| [`UsuarioEntity.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/entities/UsuarioEntity.kt) | `usuarios` | `id: Int` (Auto) | `alias` (Único), `password`, `rol` (`ADMIN`/`CLIENTE`), `sesionActiva` (`Boolean`) | Autenticación, control de perfiles y persistencia de sesión local. |
| [`ProductoEntity.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/entities/ProductoEntity.kt) | `productos` | `id: Int` (Auto) | `nombre`, `precio`, `stock`, `categoria`, `fotoUrl`, `activo` | Catálogo comercial y control de quiebres de stock. |
| [`PuntoRetiroEntity.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/entities/PuntoRetiroEntity.kt) | `puntos_retiro` | `id: Int` (Auto) | `latitud`, `longitud`, `direccion`, `horario`, `esTiendaPrincipal` | Georreferenciación de tiendas y puntos metro en el mapa interactivo. |
| [`PedidoEntity.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/entities/PedidoEntity.kt) | `pedidos` | `id: Int` (Auto) | `usuarioId` (FK lógica), `total`, `estado`, `fecha`, `itemsResumen` | Registro histórico de órdenes y aislamiento por cliente. |
| [`CarritoItemEntity.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/entities/CarritoItemEntity.kt) | `carrito_items` | `id: Int` (Auto) | `usuarioId`, `productoId`, `cantidad`, `productoPrecio` | Canasta de compras local temporal vinculada al cliente conectado. |

---

### E. Pilar 3: Los DAOs (*Data Access Objects*) — La Capa de Acceso a Datos

Un **DAO** es una interfaz de Kotlin anotada con `@Dao` que define el contrato de operaciones sobre una o más tablas. En Room:
1. **No se escribe código de conexión:** Solo se declaran las firmas de las funciones y las anotaciones SQL correspondientes. Room se encarga de abrir cursores, enlazar parámetros y cerrar conexiones de forma segura.
2. **Estrategia ante conflictos (`OnConflictStrategy.REPLACE`):** Utilizada en los `@Insert` para que, si un registro con la misma clave primaria o índice único ya existe, SQLite lo actualice silenciosamente en lugar de arrojar una excepción `SQLiteConstraintException`.
3. **Operaciones Asíncronas con Corrutinas (`suspend fun`):** Cualquier operación de modificación (`INSERT`, `UPDATE`, `DELETE`) o consulta síncrona debe ser declarada como `suspend`. De esta forma se ejecuta dentro de un hilo secundario (`Dispatchers.IO`), evitando congelar la interfaz táctil del usuario.
4. **Flujos Reactivos con Kotlin `Flow<T>`:** Las consultas de selección declaradas con retorno `Flow` se integran automáticamente con el motor de invalidación de Room. Cada vez que una fila de la tabla observada cambia, Room vuelve a disparar la consulta SQL internamente y emite la nueva lista a través del `Flow`.

---

### F. Explicación Detallada de CADA UNO de los 5 DAOs

A continuación se presenta el código fuente real y la explicación método por método de cada DAO implementado en la aplicación:

#### 1. `UsuarioDao.kt` — Gestión de Usuarios y Autenticación
* **Archivo:** [`data/local/dao/UsuarioDao.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/dao/UsuarioDao.kt)

```kotlin
@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE LOWER(alias) = LOWER(:alias) LIMIT 1")
    suspend fun obtenerPorAlias(alias: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun obtenerPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE LOWER(alias) = LOWER(:alias) AND password = :password LIMIT 1")
    suspend fun login(alias: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE sesionActiva = 1 LIMIT 1")
    fun usuarioActivo(): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE sesionActiva = 1 LIMIT 1")
    suspend fun obtenerUsuarioActivo(): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): UsuarioEntity?

    @Query("UPDATE usuarios SET sesionActiva = 0")
    suspend fun cerrarTodasLasSesiones()

    @Query("UPDATE usuarios SET sesionActiva = 1 WHERE id = :id")
    suspend fun activarSesion(id: Int)

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun todosLosUsuarios(): Flow<List<UsuarioEntity>>

    @Delete
    suspend fun eliminar(usuario: UsuarioEntity)
}
```

* **Explicación de cada método:**
  * `insertar(usuario)`: Inserta un nuevo usuario en la base de datos (utilizado en el formulario de registro de cliente y en el seed inicial). Devuelve el ID generado como `Long`.
  * `obtenerPorAlias(alias)`: Busca un usuario por su alias sin distinguir entre mayúsculas y minúsculas gracias a `LOWER()`. Se utiliza en el formulario de registro para evitar que dos usuarios tengan el mismo alias.
  * `obtenerPorEmail(email)`: Valida si un correo electrónico ya fue registrado previamente en SQLite.
  * `login(alias, password)`: Comprueba si existe un registro donde coincidan el alias y la clave. Si coincide, devuelve la entidad del usuario autenticado; si no, retorna `null`.
  * `usuarioActivo(): Flow<UsuarioEntity?>`: Emite continuamente el usuario cuya columna `sesionActiva` sea igual a 1. Permite que toda la aplicación sepa de inmediato quién está usando el teléfono.
  * `obtenerUsuarioActivo()`: Versión suspendida de un solo tiro para lecturas directas en repositorios sin necesidad de suscribirse al flujo.
  * `obtenerPorId(id)`: Busca un usuario por su clave primaria numérica.
  * `cerrarTodasLasSesiones()`: Ejecuta una actualización masiva estableciendo `sesionActiva = 0` en toda la tabla. Es el primer paso al cerrar sesión o antes de iniciar una nueva.
  * `activarSesion(id)`: Marca `sesionActiva = 1` exclusivamente en el usuario con el ID especificado.
  * `todosLosUsuarios()`: Emite la lista completa de usuarios ordenada alfabéticamente para la gestión administrativa.
  * `eliminar(usuario)`: Remueve el registro del usuario de SQLite.

---

#### 2. `ProductoDao.kt` — Catálogo, Inventario y Alertas de Stock
* **Archivo:** [`data/local/dao/ProductoDao.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/dao/ProductoDao.kt)

```kotlin
@Dao
interface ProductoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: ProductoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(productos: List<ProductoEntity>)

    @Update
    suspend fun actualizar(producto: ProductoEntity)

    @Delete
    suspend fun eliminar(producto: ProductoEntity)

    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC")
    fun todosActivos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE categoria = :categoria AND activo = 1 ORDER BY nombre ASC")
    fun porCategoria(categoria: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): ProductoEntity?

    @Query("SELECT * FROM productos WHERE nombre LIKE '%' || :query || '%' AND activo = 1")
    fun buscar(query: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE stock <= :umbral AND activo = 1")
    fun stockBajo(umbral: Int = 5): Flow<List<ProductoEntity>>

    @Query("SELECT DISTINCT categoria FROM productos WHERE activo = 1")
    fun categorias(): Flow<List<String>>

    @Query("UPDATE productos SET stock = :nuevoStock WHERE id = :id")
    suspend fun actualizarStock(id: Int, nuevoStock: Int)
}
```

* **Explicación de cada método:**
  * `insertar(producto)` / `insertarTodos(productos)`: Permite guardar uno o varios productos en lote. La función en lote es utilizada en la precarga de datos iniciales.
  * `actualizar(producto)` / `eliminar(producto)`: Permiten modificar precio/descripción o retirar un producto del catálogo.
  * `todosActivos()`: Consulta todos los productos disponibles (`activo = 1`) ordenados alfabéticamente. Alimenta la pantalla de catálogo y el carrusel de destacados del cliente.
  * `porCategoria(categoria)`: Filtra productos por categoría específica (*Frutos secos*, *Semillas*, *Deshidratados*, *Snacks*).
  * `obtenerPorId(id)`: Obtiene la ficha técnica de un producto individual para desplegarla en `DetalleScreen`.
  * `buscar(query)`: Búsqueda reactiva mediante el operador SQL `LIKE`. Conforme el usuario teclea en el buscador de la app, Room emite los productos coincidentes en tiempo real.
  * `stockBajo(umbral)`: Consulta crítica para el **Dashboard del Administrador**. Busca todos los productos cuyo inventario sea menor o igual al umbral ($\le 5$ unidades), generando tarjetas de alerta inmediata de reposición.
  * `categorias()`: Consulta con `SELECT DISTINCT` que extrae las categorías existentes sin repetición para generar dinámicamente los chips de filtro en la UI.
  * `actualizarStock(id, nuevoStock)`: Descuenta existencias cuando se confirma una compra o cuando el administrador reabastece mercadería.

---

#### 3. `PuntoRetiroDao.kt` — Georreferenciación y Tiendas Físicas
* **Archivo:** [`data/local/dao/PuntoRetiroDao.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/dao/PuntoRetiroDao.kt)

```kotlin
@Dao
interface PuntoRetiroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(puntos: List<PuntoRetiroEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(punto: PuntoRetiroEntity): Long

    @Query("SELECT * FROM puntos_retiro ORDER BY esTiendaPrincipal DESC, nombre ASC")
    fun todosLosPuntos(): Flow<List<PuntoRetiroEntity>>

    @Query("SELECT * FROM puntos_retiro ORDER BY esTiendaPrincipal DESC, nombre ASC")
    suspend fun obtenerTodos(): List<PuntoRetiroEntity>

    @Query("SELECT * FROM puntos_retiro WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): PuntoRetiroEntity?

    @Query("SELECT COUNT(*) FROM puntos_retiro")
    suspend fun contar(): Int
}
```

* **Explicación de cada método:**
  * `insertarTodos(puntos)` / `insertar(punto)`: Registra ubicaciones físicas con sus coordenadas geográficas WGS84 (`latitud` y `longitud`).
  * `todosLosPuntos()`: Emite en tiempo real todas las sucursales y puntos en estaciones de metro de Santiago. Ordena con `esTiendaPrincipal DESC` para colocar la Casa Matriz de Providencia siempre en la primera posición.
  * `obtenerTodos()`: Versión síncrona en corrutina para precargas o selector de puntos de entrega.
  * `obtenerPorId(id)`: Recupera los datos exactos del punto elegido por el cliente para adjuntarlo al ticket de su pedido.
  * `contar()`: Consulta `SELECT COUNT(*)` que permite verificar si la base de datos ya cuenta con registros o si debe ejecutar la precarga automática.

---

#### 4. `PedidoDao.kt` — Órdenes de Compra y Aislamiento por Cliente
* **Archivo:** [`data/local/dao/PedidoDao.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/dao/PedidoDao.kt)

```kotlin
@Dao
interface PedidoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pedido: PedidoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(pedidos: List<PedidoEntity>)

    @Query("SELECT * FROM pedidos WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun pedidosPorUsuario(usuarioId: Int): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM pedidos ORDER BY fecha DESC")
    fun todosLosPedidos(): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM pedidos WHERE estado = 'PENDIENTE' ORDER BY fecha DESC")
    fun pedidosPendientes(): Flow<List<PedidoEntity>>

    @Query("SELECT COUNT(*) FROM pedidos")
    suspend fun contar(): Int
}
```

* **Explicación de cada método:**
  * `insertar(pedido)`: Formaliza una nueva compra al presionar *"Confirmar Pedido"*, guardando monto total, fecha, punto de entrega y resumen de productos.
  * `insertarTodos(pedidos)`: Carga de pedidos de prueba en el seeder.
  * `pedidosPorUsuario(usuarioId)`: **Cumple la regla de negocio de aislamiento estricto:** Retorna únicamente los pedidos pertenecientes al cliente conectado (`usuarioId = :usuarioId`), garantizando que ningún usuario pueda ver compras de terceros.
  * `todosLosPedidos()`: Consulta global utilizada por el **Administrador** para auditar todas las ventas de la tienda.
  * `pedidosPendientes()`: Filtra órdenes con estado `'PENDIENTE'` para mostrar la tarjeta de KPI y despacho urgente en el panel de administración.
  * `contar()`: Retorna el número total histórico de pedidos procesados.

---

#### 5. `CarritoDao.kt` — Canasta de Compras Local
* **Archivo:** [`data/local/dao/CarritoDao.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/data/local/dao/CarritoDao.kt)

```kotlin
@Dao
interface CarritoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(item: CarritoItemEntity): Long

    @Update
    suspend fun actualizar(item: CarritoItemEntity)

    @Delete
    suspend fun eliminar(item: CarritoItemEntity)

    @Query("SELECT * FROM carrito_items WHERE usuarioId = :usuarioId ORDER BY agregadoEn DESC")
    fun itemsPorUsuario(usuarioId: Int): Flow<List<CarritoItemEntity>>

    @Query("SELECT * FROM carrito_items WHERE usuarioId = :usuarioId AND productoId = :productoId LIMIT 1")
    suspend fun obtenerItem(usuarioId: Int, productoId: Int): CarritoItemEntity?

    @Query("UPDATE carrito_items SET cantidad = :cantidad WHERE id = :itemId")
    suspend fun actualizarCantidad(itemId: Int, cantidad: Int)

    @Query("DELETE FROM carrito_items WHERE usuarioId = :usuarioId")
    suspend fun vaciarCarrito(usuarioId: Int)

    @Query("SELECT COUNT(*) FROM carrito_items WHERE usuarioId = :usuarioId")
    fun contarItems(usuarioId: Int): Flow<Int>

    @Query("SELECT SUM(productoPrecio * cantidad) FROM carrito_items WHERE usuarioId = :usuarioId")
    fun totalCarrito(usuarioId: Int): Flow<Double?>
}
```

* **Explicación de cada método:**
  * `insertar(item)`: Agrega un fruto seco a la canasta. Si ya existía, reemplaza la fila.
  * `actualizar(item)` / `eliminar(item)`: Modifica cantidades o borra un ítem del carrito.
  * `itemsPorUsuario(usuarioId)`: Devuelve la canasta de compras exclusiva del cliente autenticado.
  * `obtenerItem(usuarioId, productoId)`: Revisa si el producto ya está en el carrito para sumar a su cantidad existente en vez de crear una fila redundante.
  * `actualizarCantidad(itemId, cantidad)`: Atiende los botones `+` y `-` en la pantalla `CarritoScreen`.
  * `vaciarCarrito(usuarioId)`: Borra todos los ítems del usuario cuando la compra se concreta con éxito.
  * `contarItems(usuarioId)`: Emite el número total de ítems en el carrito para dibujar la insignia roja (*badge*) sobre el ícono del carrito en la barra superior.
  * `totalCarrito(usuarioId)`: Utiliza la función agregada `SUM(productoPrecio * cantidad)` de SQLite para calcular el total a pagar directamente en el motor de base de datos de manera instantánea.

---

### G. Flujo Completo: Cómo se Conecta Room con MVVM y Jetpack Compose

El flujo de información sigue un patrón unidireccional y reactivo:

1. **Definición en DAO:**
   ```kotlin
   @Query("SELECT * FROM productos WHERE stock <= :umbral AND activo = 1")
   fun stockBajo(umbral: Int = 5): Flow<List<ProductoEntity>>
   ```
2. **Exposición en Repositorio (`ProductoRepository.kt`):**
   ```kotlin
   fun obtenerStockBajo(umbral: Int = 5): Flow<List<ProductoEntity>> =
       productoDao.stockBajo(umbral)
   ```
3. **Consumo en ViewModel (`DashboardViewModel.kt`):**
   ```kotlin
   init {
       viewModelScope.launch {
           productoRepository.obtenerStockBajo(5).collect { listaStockCritico ->
               _uiState.update { it.copy(productosStockBajo = listaStockCritico) }
           }
       }
   }
   ```
4. **Renderizado en Pantalla Declarativa (`DashboardScreen.kt`):**
   ```kotlin
   val uiState by viewModel.uiState.collectAsState()

   if (uiState.productosStockBajo.isNotEmpty()) {
       AlertaStockBajoCard(productos = uiState.productosStockBajo)
   }
   ```
*Cuando el administrador actualiza el stock en SQLite, Room re-emite la consulta automáticamente, el ViewModel actualiza su `uiState` y Jetpack Compose se recompone sin recargar la pantalla.*

---

### H. Precarga de Datos (*Seeding*) y Cuentas por Defecto

A través de `RoomDatabase.Callback()`, en `onCreate` y `onOpen`, la aplicación garantiza la existencia de:
* **Administrador preconfigurado:** `admin` / `admin123` (`admin@admin.cl`, `+56 9 1234 1234`, `Santiago`).
* **Cliente de demostración:** `cliente` / `cliente123` (`cliente@demo.cl`, `+56 9 9876 5432`, `Providencia, Santiago`).
* **Catálogo inicial:** 6 productos con stock real (almendras crudas, nueces peladas, semillas de chía, mango deshidratado con stock bajo de 3 unidades para activar alertas, pistachos tostados con 4 unidades y mix de semillas).
* **4 Puntos de retiro y tiendas en Santiago:**
  1. *Tienda Matriz OYL Frutos Secos:* Av. Providencia 1234, Local 5 (Metro Manuel Montt). Coords: `-33.4265, -70.6175`.
  2. *Punto Retiro Metro Los Leones:* Av. Nueva Providencia 2155. Coords: `-33.4225, -70.6085`.
  3. *Punto Retiro Santiago Centro:* Paseo Ahumada 312. Coords: `-33.4410, -70.6515`.
  4. *Punto Retiro Las Condes:* Av. Apoquindo 4400 (SubCentro Escuela Militar). Coords: `-33.4190, -70.5820`.

---

### I. Manejo de Botones de Retroceso (*Back Navigation*)

Para garantizar que el botón de volver de la cabecera (`TopAppBar`) y el botón físico o gesto de retroceso del teléfono funcionen de manera fluida y sin cierres inesperados:
1. **Fallback Seguro en `AppNavGraph.kt`:**
   En las pantallas de **Login** y **Registro**, la acción `onVolver` evalúa `if (!navController.popBackStack()) navController.navigate(NavRoutes.Splash.route)`. Si el BackStack estuviera vacío, la app redirige con seguridad a la pantalla de bienvenida en lugar de cerrarse.
2. **Navegación sin Destrucción Inclusiva:**
   Al pasar de la pantalla de bienvenida (`Splash`) a Login o Registro, ya no se hace `inclusive = true` sobre Splash, permitiendo regresar a ella tocando la flecha superior.
3. **Control del Botón Físico en el Menú Inferior con `BackHandler`:**
   En [`MainContainerScreen.kt`](file:///c:/Users/juanc/AndroidStudioProjects/FrutosSecos/app/src/main/java/com/oyl/frutossecos/ui/screens/main/MainContainerScreen.kt), se implementó `BackHandler(enabled = pestañaSeleccionada != 0) { pestañaSeleccionada = 0 }`. Si el usuario navega a la pestaña de **Mapa** y presiona la tecla Atrás del celular, la aplicación regresa suavemente a la pestaña de **Inicio** antes de considerar salir de la aplicación.

---

## 3. Detalle de Cada Pantalla (Screens)

### Pantalla 1: Splash / Landing
* **Archivo:** `ui/screens/splash/SplashScreen.kt`
* **Función:** Es la pantalla de aterrizaje y presentación de marca.
* **Cómo se implementa:**
  * Utiliza un fondo con degradado vertical en tonos tierra (`BrownDark` a `BrownMedium`).
  * Efecto visual de partículas y círculos difuminados con `blur(30.dp)`.
  * Animaciones coordinadas con `LaunchedEffect` y `animateFloatAsState`:
    * `logoScale`: El ícono del fruto seco rebota suavemente usando `Spring.DampingRatioMediumBouncy`.
    * `contenidoAlpha`: Desvanece el texto y la descripción desde 0% a 100% en 800ms.
    * `desplazamientoY`: Efecto de subida fluida desde 60dp a 0dp.
  * Botones de acción: *"Comenzar"* (lleva al Registro), *"Ya tengo cuenta"* (lleva al Login), *"Ver catálogo"* y *"Cómo llegar"* (acceso directo al Mapa de tiendas).

---

### Pantalla 2: Inicio de Sesión (Login)
* **Archivos:** `ui/screens/login/LoginScreen.kt` y `LoginViewModel.kt`
* **Función:** Autenticar usuarios con validación de credenciales y roles diferenciados (Cliente vs Administrador).
* **Cómo se implementa:**
  * **Selector de Rol:** Dos pestañas tipo *pill* (`Surface` clicable) que actualizan `uiState.rol` entre `"CLIENTE"` y `"ADMIN"`.
  * **Campos con visibilidad protegida:**
    * Alias con ícono de usuario y botón para limpiar texto.
    * Contraseña con `PasswordVisualTransformation` y alternancia a `VisualTransformation.None` mediante el botón de ojo interactivo.
    * Colores fijados con `focusedTextColor = BrownDark` y `unfocusedTextColor = BrownDark` para asegurar contraste en modo oscuro y claro.
  * **Conexión a SQLite:**
    * El `LoginViewModel` invoca `UsuarioRepository.login(alias, password, rol)`.
    * Consulta `usuarioDao.obtenerPorAlias(alias)`.
    * Valida coincidencia de clave y rol.
    * Ejecuta `usuarioDao.cerrarTodasLasSesiones()` y `usuarioDao.activarSesion(usuario.id)` para persistir `sesionActiva = 1`.
  * **Mensajes de Error:** Tarjeta animada con `AnimatedVisibility` que se expande verticalmente si la contraseña es errónea o el rol no coincide.
  * **Acceso Rápido MVP:** Botones de un toque para autocompletar credenciales de prueba (`admin / admin123` y `cliente / cliente123`).
  * **Navegación:** Al autenticarse, navega al `Dashboard` y remueve la pantalla de Login del BackStack.

---

### Pantalla 3: Registro de Cliente
* **Archivos:** `ui/screens/registro/RegistroScreen.kt` y `RegistroViewModel.kt`
* **Función:** Registro exclusivo para nuevos clientes (el administrador no utiliza este formulario).
* **Cómo se implementa:**
  * Formulario vertical desplazable (`verticalScroll(rememberScrollState())`) con campos requeridos:
    1. Nombre completo.
    2. Alias único (utilizado como identificador de inicio de sesión).
    3. Contraseña y Confirmación de contraseña.
    4. Correo electrónico.
    5. Teléfono de contacto.
    6. Dirección de entrega.
  * **Validaciones obligatorias:**
    * Campos no vacíos.
    * Longitud de alias $\ge 3$ caracteres.
    * Longitud de contraseña $\ge 4$ caracteres y coincidencia exacta entre ambas.
    * Validación de formato de correo con `@` y `.`.
    * Verificación asíncrona en SQLite de que ni el alias ni el correo existan previamente en la tabla `usuarios`.
  * **Persistencia en Room:** Inserta un nuevo `UsuarioEntity` con rol `"CLIENTE"` y activa su sesión automáticamente, dirigiendo al usuario al Dashboard.

---

### Pantalla 4: Contenedor Principal & Dashboard
* **Archivos:** `ui/screens/main/MainContainerScreen.kt`, `ui/screens/dashboard/DashboardScreen.kt`, `DashboardViewModel.kt`
* **Función:** Panel principal adaptativo que muestra información personalizada según el rol del usuario (Cliente o Administrador), integrando una **Barra de Navegación Inferior persistente**.
* **Cómo se implementa:**
  * **Barra Inferior (`NavigationBar`):**
    * 5 pestañas: **Inicio**, **Mapa**, **Escanear**, **Pedidos** y **Perfil**.
    * Permite alternar instantáneamente entre el Dashboard y el Mapa de tiendas sin recargar la pantalla.
  * **Hero Banner con Imagen Generada por IA:**
    * Muestra la imagen fotorrealista de alta calidad `R.drawable.banner_frutos_secos` con un degradado semitransparente oscuro y textos promocionales sobre la frescura natural.
  * **Vista Cliente:**
    * Saludo con nombre del cliente y badge `"CLIENTE"`.
    * Tarjeta de acceso directo para consultar las tiendas en el mapa.
    * Estado en vivo de su pedido activo (`PedidoEntity` con estado: *En preparación*, *Listo para retiro*, total y lugar de retiro).
    * Carrusel horizontal (`LazyRow`) de productos destacados con imágenes vía Coil.
    * Tarjetas de promociones y descuentos.
  * **Vista Administrador:**
    * Tarjetas métricas KPI: Pedidos pendientes, productos con stock crítico ($\le 5$ unidades) y puntos de retiro activos.
    * Alertas de inventario crítico: Alerta visual para reponer existencias (detecta automáticamente stock bajo en mango deshidratado y pistachos).
    * Listado de pedidos pendientes por despachar con monto y punto de retiro.
  * **Cerrar Sesión:** Botón en la cabecera que abre un diálogo de confirmación, ejecuta `usuarioDao.cerrarTodasLasSesiones()` en SQLite y retorna al Login.

---

### Pantalla 5: Mapa de Tiendas y Puntos de Retiro
* **Archivos:** `ui/screens/mapa/MapaScreen.kt` y `MapaViewModel.kt`
* **Función:** Visualizar en un mapa interactivo las tiendas físicas y los puntos de retiro para que los clientes retiren sus compras sin costo de envío.
* **Cómo se implementa:**
  * **Lectura reactiva desde Room:** `MapaViewModel` observa `puntoRetiroDao.todosLosPuntos(): Flow<List<PuntoRetiroEntity>>`.
  * **Filtros por categoría:** Chips interactivos para filtrar: *"Todos"*, *"Tienda Matriz"* y *"Puntos Metro"*.
  * **Lienzo de Mapa Visual (`VisualMapCanvas`):**
    * Componente gráfico que dibuja el plano urbano de Santiago con calles y avenidas.
    * Pines táctiles diferenciados: Insignia dorada 🌰 para la tienda matriz y pines 📍 para los puntos en estaciones de metro.
    * Tocar un pin actualiza el punto seleccionado con animaciones de sombra y borde dorado.
  * **Tarjeta de Detalle Interactiva:**
    * Dirección exacta, comuna y horario de atención.
    * Teléfono e indicaciones precisas de retiro (ej: *"A pasos de la salida de Metro Manuel Montt"*).
    * Botón **"Cómo Llegar"**: Ejecuta un `Intent` explícito de Android (`Intent.ACTION_VIEW`) con esquema URI `geo:lat,long?q=...` para abrir la ruta en la app nativa de Google Maps.
    * Botón **"Llamar"**: Ejecuta un `Intent` con `Intent.ACTION_DIAL` hacia el teléfono del local.

---

### Pantalla 6: Catálogo de Productos
* **Archivos:** `ui/screens/catalogo/CaralogoScreen.kt` y `CatalogoViewModel.kt`
* **Función:** Explorar los productos naturales por categoría, buscar por nombre y agregar al carrito.
* **Cómo se implementa:**
  * Campo de búsqueda con filtro reactivo en SQLite (`productoDao.buscar(query)`).
  * Filtro por categorías mediante pestañas dinámicas (Frutos secos, Semillas, Deshidratados, Snacks).
  * Cuadrícula de productos (`LazyVerticalGrid`): Cada ítem muestra imagen cargada asíncronamente con Coil (`AsyncImage`), nombre, precio, indicador de stock y botón rápido para sumar al carrito de SQLite.

---

### Pantalla 7: Detalle del Producto
* **Archivos:** `ui/screens/detalle/DetalleScreen.kt` y `DetalleViewModel.kt`
* **Función:** Ficha técnica y nutricional del producto seleccionado.
* **Cómo se implementa:**
  * Recibe el `productoId` por parámetro de navegación (`detalle/{productoId}`).
  * Carga desde SQLite el producto mediante `productoDao.obtenerPorId(id)`.
  * Selector de cantidad (+ / -) con validación para no sobrepasar el stock real en inventario.
  * Botón *"Agregar al Carrito"* que persiste el ítem en la tabla `carrito_items` para el cliente conectado.

---

### Pantalla 8: Carrito de Compras
* **Archivos:** `ui/screens/carrito/CarritoScreen.kt` y `CarritoViewModel.kt`
* **Función:** Revisar productos seleccionados, modificar cantidades y calcular el total de la compra.
* **Cómo se implementa:**
  * Consulta en SQLite `carritoDao.itemsPorUsuario(usuarioId)`.
  * Actualiza en tiempo real subtotales y total a pagar.
  * Permite eliminar ítems o vaciar el carrito completo.
  * Botón *"Continuar Compra"* que dirige a la Confirmación del Pedido.

---

### Pantalla 9: Confirmación del Pedido
* **Archivos:** `ui/screens/confirmacion/ConfirmacionScreen.kt` y `ConfirmacionViewModel.kt`
* **Función:** Seleccionar punto de retiro o despacho y formalizar la orden de compra.
* **Cómo se implementa:**
  * Selector del punto de entrega (carga los puntos de SQLite).
  * Selección de método de pago de demostración (Efectivo, Débito, Transferencia).
  * Al confirmar:
    1. Descuenta las existencias en la tabla `productos` (`productoDao.actualizarStock(...)`).
    2. Inserta un nuevo registro en la tabla `pedidos` con estado `"PENDIENTE"` o `"EN_PREPARACION"`.
    3. Vacía el carrito del cliente en SQLite.
    4. Genera un ticket visual con código QR simulado para el retiro presencial.

---

### Pantalla 10: Escáner QR Multi-Formato
* **Archivos:** `ui/screens/escanerqr/EscanerQrScreen.kt` y `EscanerQrViewModel.kt`
* **Función:** Lectura de códigos QR mediante la cámara física o emulada del dispositivo para identificar productos instantáneamente y navegar hacia su ficha técnica o validar tickets de retiro.
* **Cómo se implementa:**
  * **Visor de Cámara con CameraX y ZXing:** Renderiza el flujo de video con visor cuadrado centrado, bordes redondeados y efecto de escaneo animado.
  * **Integración con Permisos de Cámara:** Solicita en tiempo de ejecución el permiso `Manifest.permission.CAMERA` mostrando mensajes instructivos si es denegado.
  * **Motor de Parsing Inteligente Multi-Formato (`extraerId`):**
    Admite tres modalidades de códigos QR para máxima flexibilidad:
    1. **Formato JSON:** Procesa objetos estructurados como `{"id": 1}`, `{"productoId": 1}` o `{"codigo": "OYL-PROD-1"}` mediante `org.json.JSONObject`.
    2. **Formato Estándar con Prefijo:** Reconoce etiquetas tipo `OYL-PROD-<id>` (ejemplo: `OYL-PROD-2`).
    3. **Formato Numérico Directo:** Interpreta números enteros directos (ejemplo: `"3"`).
  * **Navegación Reactiva:** Tras decodificar el ID del producto, consulta en SQLite si existe y redirige de inmediato a la pantalla de detalle (`DetalleProducto.createRoute(id)`). Si el formato es desconocido o el producto no existe, despliega un diálogo informativo sin detener la cámara.

---

### Pantalla 11: Historial de Pedidos / Mis Pedidos
* **Archivos:** `ui/screens/pedidos/PedidosScreen.kt` y `PedidosViewModel.kt`
* **Función:** Consultar el historial completo de órdenes de compra realizadas, su desglose y el estado de entrega en tiempo real.
* **Cómo se implementa:**
  * **Filtros por Estado en Tiempo Real:** Fila de chips interactivos para filtrar entre `"TODOS"`, `"PENDIENTE"`, `"EN_PREPARACION"`, `"LISTO_RETIRO"` y `"ENTREGADO"`.
  * **Aislamiento por Rol y Usuario:**
    * Para el **Cliente:** Consulta `pedidoDao.pedidosPorUsuario(usuarioId)`, garantizando que ningún usuario vea transacciones ajenas.
    * Para el **Administrador:** Consulta `pedidoDao.todosLosPedidos()`, permitiendo auditar todas las compras y actualizar el estado logístico de cada una.
  * **Tarjeta de Pedido (`PedidoCard`):**
    * Número de orden e insignia de estado con colores distintivos (Ámbar para *Pendiente*, Azul para *En preparación*, Verde para *Listo para retiro*).
    * Fecha y hora formateada (`dd/MM/yyyy HH:mm`) con `SimpleDateFormat`.
    * Glosa descriptiva de productos comprados (`itemsResumen`).
    * Monto total formateado en pesos chilenos (`$#,###`).
    * Nombre y dirección del punto de retiro o tienda asignada.

---

### Pantalla 12: Perfil de Usuario y Sesión
* **Archivos:** `ui/screens/perfil/PerfilScreen.kt` y `PerfilViewModel.kt`
* **Función:** Gestionar la información del perfil del usuario conectado, actualizar datos de contacto y cerrar sesión de manera segura.
* **Cómo se implementa:**
  * **Lectura Reactiva del Usuario Activo:** Observa `usuarioDao.usuarioActivo(): Flow<UsuarioEntity?>`. Al autenticarse un cliente o admin, los datos se propagan instantáneamente.
  * **Cabecera de Perfil:** Avatar circular con ícono de usuario, nombre completo y badge que indica el rol asignado (`"CLIENTE"` o `"ADMINISTRADOR"`).
  * **Formulario de Actualización de Datos:**
    * Permite modificar el teléfono de contacto y la dirección registrada para despacho o facturación.
    * Los campos cuentan con colores de alto contraste (`BrownDark`) y validación de obligatoriedad antes de guardar.
    * Persiste las modificaciones en SQLite invocando `usuarioDao.insertar(usuario.copy(...))` con estrategia `REPLACE`.
  * **Cierre de Sesión Seguro:**
    * Botón destacado *"Cerrar Sesión"* con ícono `Icons.AutoMirrored.Filled.Logout`.
    * Diálogo de confirmación modal que ejecuta `usuarioDao.cerrarTodasLasSesiones()` en SQLite (`sesionActiva = 0`) y redirige al flujo de Login limpiando la pila de navegación.

---

### Pantalla 13: Gestión de Productos (Administración)
* **Archivos:** `ui/screens/gestionproductos/GestionProductosScreen.kt` y `GestionProductosViewModel.kt`
* **Función:** Mantenimiento integral del catálogo de frutos secos (alta, baja, modificación y control de visibilidad). Exclusivo para el rol Administrador.
* **Cómo se implementa:**
  * **Lista Reactiva de Catálogo:** Consume `productoDao.todosActivos()` y lista todos los ítems ordenados alfabéticamente.
  * **Modal de Creación y Edición de Productos:**
    * Diálogo flotante con campos para: nombre, descripción comercial, categoría (*Frutos secos*, *Semillas*, *Deshidratados*, *Snacks*), precio unitario, stock inicial y URL de imagen representativa.
    * Validaciones numéricas: Precio y stock deben ser valores positivos.
  * **Switch de Visibilidad Rápida:** Interruptor táctil por cada producto que conmuta la bandera booleana `activo` (1 o 0) en SQLite sin tener que borrar el ítem del catálogo.
  * **Confirmación de Eliminación:** Diálogo modal con advertencia antes de ejecutar `productoDao.eliminar(producto)`, previniendo borrados accidentales de mercadería.

---

### Pantalla 14: Gestión de Inventario y Alertas de Stock
* **Archivos:** `ui/screens/gestioninventario/GestionInventarioScreen.kt` y `GestionInventarioViewModel.kt`
* **Función:** Supervisión de bodegas, detección de quiebres de existencias y registro de movimientos de almacén. Exclusivo para el rol Administrador.
* **Cómo se implementa:**
  * **Métricas Globales de Inventario:** Tarjetas KPI que calculan en tiempo real la cantidad total de unidades disponibles y la valorización total del stock en bodega.
  * **Buscador Reactivo y Filtro por Categoría:** Campo de búsqueda por texto que filtra de inmediato los productos coincidentes.
  * **Alertas de Stock Crítico ($\le 5$ unidades):**
    * Consulta especializada en SQLite: `productoDao.stockBajo(umbral = 5)`.
    * Destaca visualmente en color terracota/rojo con ícono de advertencia (`Icons.Default.Warning`) los productos a punto de agotarse (detecta mango deshidratado y pistachos precargados con stock bajo).
  * **Registro de Movimientos de Bodega:**
    * Modal interactivo para registrar tres tipos de movimiento:
      1. **Entrada:** Recepción de nueva mercadería desde proveedores (suma unidades).
      2. **Salida:** Mermas, deterioro o consumo interno (resta unidades).
      3. **Ajuste:** Cuadratura manual tras auditoría física.
    * Actualiza inmediatamente el stock en SQLite con `productoDao.actualizarStock(id, nuevoStock)`.

---

### Pantalla 15: Centro de Notificaciones
* **Archivos:** `ui/screens/notificaciones/NotificacionesScreen.kt` y `NotificacionesViewModel.kt`
* **Función:** Bandeja central de notificaciones locales para informar al usuario sobre el ciclo de vida de sus pedidos y avisar al administrador sobre eventos críticos del negocio.
* **Cómo se implementa:**
  * **Tipos de Notificaciones Soportadas:**
    1. *Confirmación de Pedido:* Notifica al cliente cuando su compra fue registrada exitosamente.
    2. *Estado de Retiro:* Alerta al cliente cuando su pedido pasa a estado `"LISTO_RETIRO"` en la tienda física de Santiago.
    3. *Alerta de Stock Bajo:* Avisa al administrador cuando un producto desciende a 5 o menos unidades.
  * **Diseño y Estados de Lectura:**
    * Cada tarjeta cuenta con ícono circular, título en negrita, mensaje descriptivo, marca de tiempo y punto indicador de lectura.
    * Al pulsar sobre una notificación se marca automáticamente como leída.
  * **Acciones en Lote:** Botones superiores en la barra de herramientas para *"Marcar todas como leídas"* (`Icons.Default.DoneAll`) y *"Eliminar historial"* (`Icons.Default.Delete`).

---

## 4. Componentes Clave y Cómo se Implementan

### A. Jetpack Compose & Material 3
* **Theming Centralizado:** Paleta de diseño basada en tonos cálidos definida en `Color.kt` (`BrownDark`, `BrownMedium`, `AmberGold`, `WarmTan`, `CreamWhite`, `OliveGreen`).
* **Scaffold:** Utilizado como contenedor estructural en cada pantalla para organizar la barra superior (`TopAppBar`), el contenido con márgenes automáticos (`paddingValues`) y la barra inferior (`NavigationBar`).
* **Listas Eficientes:** `LazyColumn` y `LazyRow` con parámetros `key` para optimizar el reciclaje de vistas y evitar recomposiciones innecesarias.
* **Carga de Imágenes:** Uso de la biblioteca moderna **Coil 3** (`AsyncImage`) para cargar imágenes de productos desde la web o recursos locales de forma asíncrona con caché en memoria y disco.

### B. Animaciones
* **`animateFloatAsState`:** Utilizado en el Splash para escalar el logo con un resorte dinámico (`spring(dampingRatio = Spring.DampingRatioMediumBouncy)`).
* **`AnimatedVisibility`:** Utilizado en los formularios de Login y Registro para mostrar/ocultar los mensajes de error con transiciones suaves:
  ```kotlin
  AnimatedVisibility(
      visible = uiState.errorMessage != null,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically()
  )
  ```
* **Transiciones de Pestañas:** Animaciones de elevación y cambio de color en el selector de roles y la barra inferior.

### C. Formularios y Validaciones
* **Control de Texto y Foco:** `LocalFocusManager.current` para cerrar el teclado táctil automáticamente tras presionar el botón de acción (`ImeAction.Done`).
* **Garantía de Contraste y Visibilidad:**
  ```kotlin
  @Composable
  fun textFieldColors() = OutlinedTextFieldDefaults.colors(
      focusedTextColor = BrownDark,       // Marrón oscuro de alto contraste
      unfocusedTextColor = BrownDark,     // Legible en modo claro y oscuro
      focusedBorderColor = AmberGold,
      unfocusedBorderColor = WarmTan,
      cursorColor = AmberGold
  )
  ```
* **Validación en Capas:**
  1. *Capa UI / ViewModel:* Verificación inmediata de formato (correo con `@`, contraseñas coincidentes de $\ge 4$ caracteres, alias no vacío).
  2. *Capa Base de Datos:* Comprobación de no duplicidad de alias/correo antes de ejecutar el `INSERT`.

### D. Módulo de Mapa y Geolocalización
* **Almacenamiento Local:** Las coordenadas latitud/longitud se guardan en tipos primitivos `Double` en la tabla `puntos_retiro`.
* **Interacción Externa mediante Android Intents:**
  ```kotlin
  val uri = Uri.parse("geo:${punto.latitud},${punto.longitud}?q=${Uri.encode(punto.nombre + " " + punto.direccion)}")
  val mapIntent = Intent(Intent.ACTION_VIEW, uri)
  context.startActivity(mapIntent)
  ```
  Esto permite que la aplicación delegue el cálculo de rutas y navegación GPS a la aplicación nativa de Google Maps instalada en el dispositivo móvil.

### E. Escáner QR Multi-Formato y Cámara
* **Lectura Inteligente y Tolerante a Formatos:** El ViewModel (`EscanerQrViewModel.kt`) implementa una función de parsing de tres etapas:
  1. *Parser JSON Nativo:* Si el string escaneado inicia con `{` y finaliza con `}`, intenta decodificarlo como un objeto `JSONObject` extrayendo las claves `"id"`, `"productoId"` o `"codigo"`. Esto permite compatibilidad con sistemas externos y tickets empresariales.
  2. *Prefijo Estándar:* Detecta cadenas con prefijo oficial `"OYL-PROD-"` extrayendo el ID posterior.
  3. *ID Numérico Directo:* Si el código solo contiene un número entero, lo convierte a `Int`.
* **Integración de Hardware:** Emplea **CameraX** (`androidx.camera.view.PreviewView`) para el renderizado del visor y **ZXing** (`com.google.zxing:core`) para la decodificación de la matriz de puntos.
* **Manejo de Errores Amigable:** Si el código no corresponde a ningún producto del catálogo de SQLite, el estado emite una advertencia visual que no interrumpe el ciclo de escaneo.

### F. Control de Sesión y Aislamiento de Clientes
* Para cumplir con la regla de negocio de **no mezclar carritos, pedidos ni historiales**:
  1. Al iniciar sesión, la consulta SQLite `UPDATE usuarios SET sesionActiva = 0` limpia todas las sesiones activas anteriores.
  2. Luego se marca `sesionActiva = 1` exclusivamente en el usuario autenticado.
  3. Las consultas de pedidos y carritos filtran siempre por el `usuarioId` activo:
     ```sql
     SELECT * FROM pedidos WHERE usuarioId = :usuarioId ORDER BY fecha DESC;
     SELECT * FROM carrito_items WHERE usuarioId = :usuarioId;
     ```
  4. De este modo, si un cliente cierra sesión y entra otro diferente en el mismo teléfono, cada uno ve única y exclusivamente sus propios datos.

### G. Navegación con Jetpack Compose y Gestión del Botón Atrás (`BackHandler`)
* **Control de Pilas de Navegación (*Back Stack*):**
  * Se evitan limpiezas destructivas (`inclusive = true`) en la pantalla de bienvenida (`Splash`), permitiendo que el usuario pueda presionar el botón de retroceso desde el Login o Registro y volver al inicio.
  * En `LoginScreen` y `RegistroScreen`, el callback `onVolver` utiliza un fallback seguro:
    ```kotlin
    if (!navController.popBackStack()) {
        navController.navigate(NavRoutes.Splash.route)
    }
    ```
* **Interceptación del Botón Físico / Gesto del Dispositivo con `BackHandler`:**
  * En el contenedor principal con barra de navegación (`MainContainerScreen.kt`):
    ```kotlin
    BackHandler(enabled = pestañaSeleccionada != 0) {
        pestañaSeleccionada = 0
    }
    ```
    Si el usuario se encuentra navegando en el **Mapa** (o en cualquier pestaña distinta a Inicio) y pulsa el botón atrás de Android, el evento es interceptado suavemente para regresarlo a la pestaña de **Inicio (Dashboard)** antes de considerar salir de la aplicación.

---

## 5. Resumen de Tecnologías y Dependencias

| Tecnología | Versión / Biblioteca | Propósito |
|---|---|---|
| **Lenguaje** | Kotlin 2.2.10 | Lenguaje principal del proyecto |
| **UI Framework** | Jetpack Compose (BOM) | Construcción de interfaces declarativas |
| **Diseño** | Material 3 | Componentes visuales y sistema de diseño |
| **Base de Datos** | Room 2.6.1 (SQLite) | Persistencia local de usuarios, productos, puntos y pedidos |
| **Procesador de Anotaciones** | KSP (Kotlin Symbol Processing) | Generación de código para Room |
| **Navegación** | Jetpack Navigation Compose | Flujo entre pantallas y paso de argumentos |
| **Imágenes** | Coil 3 Compose | Carga reactiva de imágenes remotas y locales |
| **Geolocalización** | Google Maps & Android Intents | Visualización de tiendas y rutas de retiro |
| **Escáner** | ZXing Core & Camera | Reconocimiento de códigos QR de pedidos |
