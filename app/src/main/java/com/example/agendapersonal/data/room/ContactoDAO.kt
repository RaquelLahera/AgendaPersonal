package com.example.agendapersonal.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Declaramos que es DAO
@Dao
interface ContactoDAO {

    // Método para insertar
    @Insert(onConflict = OnConflictStrategy.REPLACE) // .REPLACE para que reemplace las líneas repetidas (actualizar)
    suspend fun insertarContacto(persona: Contacto) // Suspend para ejecutarse en corutinas y no congelar la pantalla

    // Método para actualizar. Realmente no lo necesitamos pero lo dejamos por si acaso
    @Update
    suspend fun modificarContacto(persona: Contacto)

    // Método para borrar
    @Delete
    suspend fun borrarContacto(persona: Contacto)

    // Método para filtrar solo por el usuario conectado
    @Query("SELECT * FROM contactos WHERE usuario = :conectado ORDER BY nombre ASC")
    fun obtenerContactos(conectado: String): Flow<List<Contacto>>

    // Consulta específica para la edición robusta por ID
    @Query("SELECT * FROM contactos WHERE id = :id LIMIT 1")
    suspend fun obtenerContactoPorId(id: Int): Contacto?
}