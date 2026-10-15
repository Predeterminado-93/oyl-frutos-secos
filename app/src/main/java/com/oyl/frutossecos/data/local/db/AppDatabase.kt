package com.OYL.frutossecos.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.OYL.frutossecos.data.local.dao.ProductoDao
import com.OYL.frutossecos.data.local.dao.UsuarioDao
import com.OYL.frutossecos.data.local.entities.ProductoEntity
import com.OYL.frutossecos.data.local.entities.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos Room principal de OYL Frutos Secos.
 * Versión 1: tablas usuarios y productos.
 */
@Database(
    entities = [UsuarioEntity::class, ProductoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun productoDao(): ProductoDao

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
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback que inserta datos de demostración la primera vez que se crea la BD.
     */
    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            // Usuario Administrador de demo
            db.usuarioDao().insertar(
                UsuarioEntity(
                    nombre = "Admin OYL",
                    email = "admin@oyl.com",
                    password = "admin123",
                    rol = "ADMIN"
                )
            )
            // Usuario Cliente de demo
            db.usuarioDao().insertar(
                UsuarioEntity(
                    nombre = "Cliente Demo",
                    email = "cliente@demo.com",
                    password = "cliente123",
                    rol = "CLIENTE"
                )
            )
            // Productos de demo
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
                    descripcion = "Trozos de mango deshidratado sin azúcar añadida.",
                    fotoUrl = "https://images.unsplash.com/photo-1609480748849-79e5d0dc0ed6?w=400",
                    precio = 1100.0,
                    stock = 4,
                    categoria = "Deshidratados"
                ),
                ProductoEntity(
                    nombre = "Pistachos tostados",
                    descripcion = "Pistachos tostados con sal marina, crujientes y sabrosos.",
                    fotoUrl = "https://images.unsplash.com/photo-1626200924688-e6b3ec6d8a3c?w=400",
                    precio = 2200.0,
                    stock = 20,
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
    }
}
