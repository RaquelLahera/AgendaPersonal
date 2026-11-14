package com.example.agendapersonal.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.agendaPersonal.databinding.ActivityLoginBinding
import com.example.agendapersonal.data.datastore.UserPreferences
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // Declaramos el binding y el dataStore
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPrefer: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Declaramos el título vacío para que no aparezca el nombre del proyecto en el bar
        title = " "

        // Inicializamos la binding y conectamos la view al root para poder acceder a res/layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userPrefer = UserPreferences(this)

        // Escuchamos el Flow de dataStore
        recuperarUltimaSesion()

        binding.btInicioSesion.setOnClickListener {
            validarYContinuar()
        }
    } // Cerramos onCreate()

    // Método para recuperar la última sesión al entrar a la app
    private fun recuperarUltimaSesion() {
        lifecycleScope.launch {
            userPrefer.credenciales.collect { datosContacto ->
                if (datosContacto.sesionActiva == true) {
                    val intent = Intent(this@LoginActivity, AgendaActivity::class.java)
                    intent.putExtra("CLAVE_SESION_ACTIVA", datosContacto.usuario)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    // Creamos el método para validar el usuario
    private fun validarYContinuar() {
        val us = binding.tfUsuario.text.toString().trim()
        val cont = binding.tfContraseA.text.toString().trim()

        // Declaramos la obligatoriedad de rellenar los campos
        if (us.isEmpty() || cont.isEmpty()) {
            Snackbar.make(
                binding.root, "Debe introducir el usuario y la contraseña.",
                Snackbar.LENGTH_SHORT
            ).setAnchorView(binding.btInicioSesion).show()
            return
        }

        // Comprobamos los datos del usuario
        lifecycleScope.launch(Dispatchers.IO) {

            val datosGuardados = userPrefer.credenciales.first()

            if (datosGuardados.usuario.isEmpty()) {
                userPrefer.guardarSesion(us, cont)
                irAgenda(us)

            } else if (datosGuardados.usuario == us && datosGuardados.contraseña == cont) {
                userPrefer.guardarSesion(us, cont)
                irAgenda(us)

            } else {
                Snackbar.make(
                    binding.root, "Usuario o contraseña incorrectos.",
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(binding.btInicioSesion).show()
            }
        }
    }

    // Método para ir a la agenda
    private fun irAgenda(usuario: String) {
        val intent = Intent(this@LoginActivity, AgendaActivity::class.java)
        intent.putExtra("CLAVE_SESION_ACTIVA", usuario)
        startActivity(intent)
        finish()
    }
}