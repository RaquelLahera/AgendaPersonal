package com.example.agendapersonal.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creamos el archivo de preferencias
private val Context.dataStore by preferencesDataStore(name = "sesion")

// Modelo de datos para agrupar lo de DataStore
data class DatosContacto(val usuario: String, val contraseña: String, val sesionActiva: Boolean)

class UserPreferences(private val context: Context) {

    // Definimos las claves/keys
    companion object { // Equivalente a un método static
        val CLAVE_USUARIO = stringPreferencesKey("clave_usuario")
        val CLAVE_CONTRASEÑA = stringPreferencesKey("clave_contraseña")
        val CLAVE_SESION_ACTIVA = booleanPreferencesKey("clave_sesion_activa")
    }

    // Definimos la lectura reactiva/flow para que se produzca el cambio de dato con el cambio de usuario
    val credenciales: Flow<DatosContacto> = context.dataStore.data.map { preferences ->
        val usuar = preferences[CLAVE_USUARIO] ?: ""
        val contr = preferences[CLAVE_CONTRASEÑA] ?: ""
        val sesionAct =
            preferences[CLAVE_SESION_ACTIVA] ?: false // Si no existe, por defecto vale false
        DatosContacto(usuar, contr, sesionAct)
    }

    // Declaramos el suspend para mantener iniciada la sesión
    suspend fun guardarSesion(usuario: String, contraseña: String, sesionActiva: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[CLAVE_USUARIO] = usuario
            preferences[CLAVE_CONTRASEÑA] = contraseña
            preferences[CLAVE_SESION_ACTIVA] = sesionActiva
        }
    }

    // declaramos el método que cierre la sesión y, como parámetro, decidamos si se mantiene o no
    suspend fun cerrarSesion(sesionActiva: Boolean = false) {
        context.dataStore.edit { preferences ->
            preferences[CLAVE_SESION_ACTIVA] = sesionActiva
        }
    }
}