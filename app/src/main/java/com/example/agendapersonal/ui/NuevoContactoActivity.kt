package com.example.agendapersonal.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.agendaPersonal.R
import com.example.agendaPersonal.databinding.ActivityNuevoContactoBinding
import com.example.agendapersonal.data.room.AgendaDB
import com.example.agendapersonal.data.room.Contacto
import com.example.agendapersonal.data.room.ContactoDAO
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevoContactoActivity : AppCompatActivity() {

    // Declaramos el binding, el dao y el usuario activo
    private lateinit var binding: ActivityNuevoContactoBinding
    private lateinit var dao: ContactoDAO
    private var usuarioActivo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Declaramos el título
        title = "Nuevo Contacto"

        // Inicializamos el binding y conectamos con root de res/layout
        binding = ActivityNuevoContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mostramos la flecha para poder ir a la pantalla anterior
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recogemos los datos de LoginActivity
        usuarioActivo = intent.getStringExtra("CLAVE_SESION_ACTIVA") ?: "Anónimo"

        // Inicializamos dao
        dao = AgendaDB.obtenerBD(this).contactoDAO()

        // Llamamos al botón de crear
        crearContacto()

        // Llamamos a editar contacto por si se necesitara desde AgendaActivity
        editarContacto()
    }

    // Le damos funcionalidad al botón "atrás"/fecla para volver
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // Método para crear un nuevo contacto
    private fun crearContacto() {
        binding.btGuardar.setOnClickListener {
            val id = intent.getIntExtra("ID_CONTACTO", 0)
            val nombre = binding.tfNombre.text.toString().trim()
            val telefono = binding.tfTelefono.text.toString().trim()
            val email = binding.tfEmail.text.toString().trim()

            // Comprobamos que los campos obligatorios no estén vacíos
            if (nombre.isEmpty() || telefono.isEmpty()) {
                mensaje("Debes introducir obligatoriamente el nombre y el teléfono")
                return@setOnClickListener
            }

            // Declaramos el nuevo contacto
            val nuevoContacto = Contacto(
                id = id,
                usuario = usuarioActivo,
                nombre = nombre,
                telefono = telefono.toInt(),
                email = email
            )

            // Lanzamos el nuevo contacto y volvemos a la agenda
            lifecycleScope.launch(Dispatchers.IO) {
                dao.insertarContacto(nuevoContacto)

                withContext(Dispatchers.Main) {
                    vaciar()
                    mensaje("Contacto insertado correctamente.")
                    finish()
                }
            }
        }
    }

    // Declaramos los elementos que cogemos al editar un contacto
    private fun editarContacto() {

        val id = intent.getIntExtra("ID_CONTACTO", 0)

        // Si tenemos un contacto real, no el id=0 por defecto, ejecutamos el cambio
        if (id != 0) {
            title = "Editar Contacto"
            val nom = intent.getStringExtra("NOMBRE_CONTACTO")
            val tel = intent.getIntExtra("TELEFONO_CONTACTO", 0).toString()
            val em = intent.getStringExtra("EMAIL_CONTACTO")
            intent.putExtra("CLAVE_SESION_ACTIVA", usuarioActivo)

            binding.tfNombre.setText(nom)
            binding.tfTelefono.setText(tel)
            binding.tfEmail.setText(em)
        }
    }

    // Método para enviar mensaje
    private fun mensaje(texto: String) {
        Snackbar.make(binding.root, texto, Snackbar.LENGTH_SHORT).setAnchorView(binding.btGuardar)
            .show()
    }

    // Método para vaciar. Ponemos ? por seguridad, por si no fuera un texto
    private fun vaciar() {
        binding.tfNombre.text?.clear()
        binding.tfTelefono.text?.clear()
        binding.tfEmail.text?.clear()
    }
}