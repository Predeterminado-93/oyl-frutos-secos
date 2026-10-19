
package com.OYL.frutossecos.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.OYL.frutossecos.data.local.dao.CarritoDao
import com.OYL.frutossecos.data.local.dao.MovimientoInventarioDao
import com.OYL.frutossecos.data.local.dao.NotificacionDao
import com.OYL.frutossecos.data.local.dao.PedidoDao
import com.OYL.frutossecos.data.local.dao.ProductoDao
import com.OYL.frutossecos.data.local.dao.PuntoRetiroDao
import com.OYL.frutossecos.data.local.dao.UsuarioDao
import com.OYL.frutossecos.data.local.entities.CarritoItemEntity
import com.OYL.frutossecos.data.local.entities.MovimientoInventarioEntity
import com.OYL.frutossecos.data.local.entities.NotificacionEntity
import com.OYL.frutossecos.data.local.entities.PedidoEntity
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.data.local.entities.PuntoRetiroEntity
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UsuarioEntity::class,
        ProductoEntity::class,
        PuntoRetiroEntity::class,
        PedidoEntity::class,
        CarritoItemEntity::class,
        MovimientoInventarioEntity::class,
        NotificacionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun productoDao(): ProductoDao
    abstract fun puntoRetiroDao(): PuntoRetiroDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun carritoDao(): CarritoDao
    abstract fun movimientoInventarioDao(): MovimientoInventarioDao
    abstract fun notificacionDao(): NotificacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notificaciones (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        usuarioId INTEGER NOT NULL,
                        titulo TEXT NOT NULL,
                        mensaje TEXT NOT NULL,
                        tipo TEXT NOT NULL,
                        fecha INTEGER NOT NULL,
                        leida INTEGER NOT NULL,
                        rutaDestino TEXT
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_notificaciones_usuarioId
                    ON notificaciones(usuarioId)
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oyl_frutos_secos.db"
                )
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    ensureInitialData(database)
                }
            }
        }

        private suspend fun ensureInitialData(db: AppDatabase) {
            if (db.usuarioDao().obtenerPorAlias("admin") == null) {
                db.usuarioDao().insertar(
                    UsuarioEntity(
                        nombre = "Administrador",
                        alias = "admin",
                        email = "admin@admin.cl",
                        password = "admin123",
                        telefono = "+56 9 1234 1234",
                        direccion = "Santiago",
                        rol = "ADMIN"
                    )
                )
            }

            val clienteExistente = db.usuarioDao().obtenerPorAlias("cliente")
            val clienteId = clienteExistente?.id ?: db.usuarioDao().insertar(
                UsuarioEntity(
                    nombre = "Cliente Demo",
                    alias = "cliente",
                    email = "cliente@demo.cl",
                    password = "cliente123",
                    telefono = "+56 9 9876 5432",
                    direccion = "Providencia, Santiago",
                    rol = "CLIENTE"
                )
            ).toInt()

            if (db.productoDao().contar() == 0) {
                seedProductos(db)
            }

            if (db.puntoRetiroDao().contar() == 0) {
                seedPuntosRetiro(db)
            }

            if (db.pedidoDao().contar() == 0) {
                seedPedidos(db, clienteId)
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            if (db.usuarioDao().obtenerPorAlias("admin") == null) {
                db.usuarioDao().insertar(
                    UsuarioEntity(
                        nombre = "Administrador",
                        alias = "admin",
                        email = "admin@admin.cl",
                        password = "admin123",
                        telefono = "+56 9 1234 1234",
                        direccion = "Santiago",
                        rol = "ADMIN"
                    )
                )
            }

            val clienteExistente = db.usuarioDao().obtenerPorAlias("cliente")
            val clienteId = clienteExistente?.id ?: db.usuarioDao().insertar(
                UsuarioEntity(
                    nombre = "Cliente Demo",
                    alias = "cliente",
                    email = "cliente@demo.cl",
                    password = "cliente123",
                    telefono = "+56 9 9876 5432",
                    direccion = "Providencia, Santiago",
                    rol = "CLIENTE"
                )
            ).toInt()

            if (db.productoDao().contar() == 0) {
                seedProductos(db)
            }

            if (db.puntoRetiroDao().contar() == 0) {
                seedPuntosRetiro(db)
            }

            if (db.pedidoDao().contar() == 0) {
                seedPedidos(db, clienteId)
            }
        }

        private suspend fun seedProductos(db: AppDatabase) {
            val productos = listOf(
                ProductoEntity(
                    nombre = "Almendras crudas",
                    descripcion = "Almendras naturales sin sal, ricas en vitamina E y magnesio.",
                    fotoUrl = "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400",
                    precio = 1200.0,
                    stock = 50,
                    categoria = "Frutos secos"
                ),
                ProductoEntity(
                    nombre = "Nueces peladas",
                    descripcion = "Nueces sin cáscara, fuente de omega-3 y antioxidantes.",
                    fotoUrl = "https://images.unsplash.com/photo-1599599810694-b5b37304c041?w=400",
                    precio = 1800.0,
                    stock = 30,
                    categoria = "Frutos secos"
                ),
                ProductoEntity(
                    nombre = "Semillas de chía",
                    descripcion = "Semillas de chía orgánicas, alto contenido en fibra y proteína.",
                    fotoUrl = "https://images.unsplash.com/photo-1638257538838-e8bfe24e0a27?w=400",
                    precio = 900.0,
                    stock = 80,
                    categoria = "Semillas"
                ),
                ProductoEntity(
                    nombre = "Mango deshidratado",
                    descripcion = "Trozos de mango deshidratado sin azúcar añadida. ¡Stock bajo!",
                    fotoUrl = "https://images.unsplash.com/photo-1609480748849-79e5d0dc0ed6?w=400",
                    precio = 1100.0,
                    stock = 3,
                    categoria = "Deshidratados"
                ),
                ProductoEntity(
                    nombre = "Pistachos tostados",
                    descripcion = "Pistachos tostados con sal marina, crujientes y sabrosos.",
                    fotoUrl = "https://images.unsplash.com/photo-1626200924688-e6b3ec6d8a3c?w=400",
                    precio = 2200.0,
                    stock = 4,
                    categoria = "Frutos secos"
                ),
                ProductoEntity(
                    nombre = "Mix de semillas",
                    descripcion = "Combinación de semillas de girasol, zapallo y sésamo.",
                    fotoUrl = "https://images.unsplash.com/photo-1515867560007-93d2a0b6e27b?w=400",
                    precio = 750.0,
                    stock = 60,
                    categoria = "Semillas"
                )
            )

            db.productoDao().insertarTodos(productos)
        }

        private suspend fun seedPuntosRetiro(db: AppDatabase) {
            val puntos = listOf(
                PuntoRetiroEntity(
                    nombre = "Tienda Matriz OYL Frutos Secos",
                    tipo = "TIENDA_FISICA",
                    direccion = "Av. Providencia 1234, Local 5",
                    comuna = "Providencia, Santiago",
                    latitud = -33.42628,
                    longitud = -70.61389,
                    horario = "Lun a Sáb: 09:30 - 20:30 hrs",
                    telefono = "+56 9 1234 1234",
                    indicaciones = "Frente a salida de Metro Manuel Montt. Estacionamiento disponible.",
                    esTiendaPrincipal = true
                ),
                PuntoRetiroEntity(
                    nombre = "Punto Retiro Metro Los Leones",
                    tipo = "PUNTO_RETIRO",
                    direccion = "Av. Nueva Providencia 2155, Nivel -1",
                    comuna = "Providencia, Santiago",
                    latitud = -33.42194,
                    longitud = -70.60833,
                    horario = "Lun a Vie: 10:00 - 19:30 hrs",
                    telefono = "+56 9 9876 5432",
                    indicaciones = "Galería comercial subterránea, junto al tótem de información.",
                    esTiendaPrincipal = false
                ),
                PuntoRetiroEntity(
                    nombre = "Punto Retiro Santiago Centro",
                    tipo = "PUNTO_RETIRO",
                    direccion = "Paseo Ahumada 312, Stand 8",
                    comuna = "Santiago Centro",
                    latitud = -33.44194,
                    longitud = -70.65139,
                    horario = "Lun a Sáb: 10:00 - 19:00 hrs",
                    telefono = "+56 9 5555 4444",
                    indicaciones = "A media cuadra de Plaza de Armas. Retiro express con código.",
                    esTiendaPrincipal = false
                ),
                PuntoRetiroEntity(
                    nombre = "Punto Retiro Las Condes",
                    tipo = "PUNTO_RETIRO",
                    direccion = "Av. Apoquindo 4400, Local 12",
                    comuna = "Las Condes",
                    latitud = -33.41583,
                    longitud = -70.58750,
                    horario = "Lun a Vie: 09:00 - 18:30 hrs",
                    telefono = "+56 9 7777 8888",
                    indicaciones = "SubCentro Metro Escuela Militar, sector poniente.",
                    esTiendaPrincipal = false
                )
            )

            db.puntoRetiroDao().insertarTodos(puntos)
        }

        private suspend fun seedPedidos(db: AppDatabase, clienteId: Int) {
            val pedidos = listOf(
                PedidoEntity(
                    usuarioId = clienteId,
                    usuarioAlias = "cliente",
                    fecha = System.currentTimeMillis() - 3600000L * 2,
                    estado = "EN_PREPARACION",
                    total = 3000.0,
                    itemsResumen = "1kg Almendras crudas, 500g Nueces peladas",
                    puntoRetiroNombre = "Tienda Matriz OYL Frutos Secos"
                ),
                PedidoEntity(
                    usuarioId = clienteId,
                    usuarioAlias = "cliente",
                    fecha = System.currentTimeMillis() - 3600000L * 24,
                    estado = "PENDIENTE",
                    total = 1850.0,
                    itemsResumen = "Mix de semillas (500g), Mango deshidratado (200g)",
                    puntoRetiroNombre = "Punto Retiro Metro Los Leones"
                ),
                PedidoEntity(
                    usuarioId = clienteId,
                    usuarioAlias = "cliente",
                    fecha = System.currentTimeMillis() - 3600000L * 72,
                    estado = "ENTREGADO",
                    total = 2700.0,
                    itemsResumen = "Semillas de chía (1kg), Almendras crudas (500g)",
                    puntoRetiroNombre = "Tienda Matriz OYL Frutos Secos"
                )
            )

            db.pedidoDao().insertarTodos(pedidos)
        }
    }
}
