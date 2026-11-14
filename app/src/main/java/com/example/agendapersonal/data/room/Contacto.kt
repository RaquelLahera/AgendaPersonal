package com.example.agendapersonal.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

// Declaramos el nombre de la tabla
@Entity(tableName = "contactos")

data class Contacto(
    @PrimaryKey(autoGenerate = true) // Autogeneramos el id
    val id: Int = 0,
    val usuario: String, // Declarar el usuario conectado
    val nombre: String,
    val telefono: Int,
    val email: String,
)