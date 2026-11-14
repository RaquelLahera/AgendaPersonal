package com.example.agendapersonal.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Declaramos la clase, la versión y NO exportar el esquema
@Database(entities = [Contacto::class], version = 1, exportSchema = false)

// Declaramos como clase abstracta que hereda de RoomDatabase
abstract class AgendaDB : RoomDatabase() {

    // Declaramos contactoDAO
    abstract fun contactoDAO(): ContactoDAO

    companion object { // Equivalente a un método static
        @Volatile // Comunicación para las corrutinas
        private var INSTANCIA: AgendaDB? = null // Creamos inicialmente la base de datos a null

        // Método para obtener la base de datos
        fun obtenerBD(context: Context): AgendaDB {

            // Devolvemos la instancia y, si es null, ejecutamos el siguiente bloque
            return INSTANCIA ?: synchronized(this) {
                // Encargadas de crear la base de datos
                val nuevaInstancia = Room.databaseBuilder(
                    context.applicationContext,
                    AgendaDB::class.java,
                    "agendaPersonal_DB"

                    //Construimos para próximas veces asegurándonos que no se rompa con la migración
                ).fallbackToDestructiveMigration().build()
                INSTANCIA = nuevaInstancia
                nuevaInstancia
            }
        }
    }
}