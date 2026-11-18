package com.example.agendapersonal.ui

import android.content.Intent
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

    // Declaramos el binding, el dao, el usuario activo y su id
    private lateinit var binding: ActivityNuevoContactoBinding
    private lateinit var dao: ContactoDAO
    private var usuarioActivo: String = ""
    private var contactoId: Int = 0

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
        contactoId = intent.getIntExtra("ID_CONTACTO", 0)
        // Inicializamos dao
        dao = AgendaDB.obtenerBD(this).contactoDAO()

        // Edición robusta: si hay ID, cargamos los datos directamente de la BD
        if (contactoId != 0) {
            title = "Editar Contacto"
            cargarContactoPorId(contactoId)
        }

        // Llamamos al botón de crear
        crearContacto()
    }

    // Método para cargar por id
    private fun cargarContactoPorId(id: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val contactoBD = dao.obtenerContactoPorId(id)
            contactoBD?.let { c ->
                withContext(Dispatchers.Main) {
                    binding.tfNombre.setText(c.nombre)
                    binding.tfTelefono.setText(c.telefono)
                    binding.tfEmail.setText(c.email)
                }
            }
        }
    }

    // Le damos funcionalidad al botón "atrás"/fecla para volver
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // Método para crear un nuevo contacto
    private fun crearContacto() {
        binding.btGuardar.setOnClickListener {
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
                id = contactoId,
                usuario = usuarioActivo,
                nombre = nombre,
                telefono = telefono, // Almacenamos como String para soportar prefijos
                email = email
            )

            // Lanzamos el nuevo contacto y volvemos a la agenda
            lifecycleScope.launch(Dispatchers.IO) {
                dao.insertarContacto(nuevoContacto)

                withContext(Dispatchers.Main) {
                    // Preparamos el resultado para que la Agenda muestre el Snackbar de éxito de forma limpia
                    val resultadoIntent = Intent().apply {
                        putExtra("MENSAJE_RESULTADO", if (contactoId == 0) "Contacto insertado correctamente." else "Contacto actualizado correctamente.")
                    }
                    setResult(RESULT_OK, resultadoIntent)
                    finish()
                }
            }
        }
    }

    // Método para enviar mensaje
    private fun mensaje(texto: String) {
        Snackbar.make(binding.root, texto, Snackbar.LENGTH_SHORT).setAnchorView(binding.btGuardar)
            .show()
    }
}