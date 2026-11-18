package com.example.agendapersonal.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.agendaPersonal.R
import com.example.agendaPersonal.databinding.ActivitySplashBinding
import com.example.agendapersonal.data.datastore.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Limpia la vista de advertencias
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var userPrefer: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Uso de View Binding en lugar de findViewById
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Declaramos el título vacío para que no aparezca el nombre del proyecto en el bar
        title = " "
        userPrefer = UserPreferences(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Declaramos el temporizador para que aparezca y desaparezca
        lifecycleScope.launch {
            // Comprobación de sesión antes de redirigir para evitar parpadeos
            val prefs = userPrefer.credenciales.first()
            delay(1500) // Tiempo de splash visual

            // Si hay sesión activa y usuario, a agenda. Sino, a login
            val intent = if (prefs.sesionActiva && prefs.usuario.isNotEmpty()) {
                Intent(this@SplashActivity, AgendaActivity::class.java).apply {
                    putExtra("CLAVE_SESION_ACTIVA", prefs.usuario)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            } else {
                Intent(this@SplashActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            }
            startActivity(intent)
            finish()
        }
    }
}