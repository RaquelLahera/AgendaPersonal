package com.example.agendapersonal.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agendaPersonal.R
import com.example.agendaPersonal.databinding.ActivityAgendaBinding
import com.example.agendapersonal.data.datastore.UserPreferences
import com.example.agendapersonal.data.room.AgendaDB
import com.example.agendapersonal.data.room.ContactoDAO
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgendaActivity : AppCompatActivity() {

    // Declaramos el binding, el dao, el adaptador y las preferencias de usuario
    private lateinit var binding: ActivityAgendaBinding
    private lateinit var dao: ContactoDAO
    private lateinit var adaptador: ContactoAdapter
    private lateinit var userPrefer: UserPreferences

    private var usuarioActivo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Declaramos el título en la ToolBar
        title = "Mis Contactos"

        // Inicializamos las preferencias de usuario
        userPrefer = UserPreferences(this)

        // Inicializamos la binding y conectamos la view al root para poder acceder a res/layout
        binding = ActivityAgendaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recogemos los datos de LoginActivity
        usuarioActivo = intent.getStringExtra("CLAVE_SESION_ACTIVA") ?: "Anónimo"

        // Inicializamos dao
        dao = AgendaDB.obtenerBD(this).contactoDAO()

        // Inicializamos el adaptador y declaramos lo que hace cada acción al pulsar
        adaptador = ContactoAdapter(
            pulsar = { contactoSelecionado ->
                val intent = Intent(this@AgendaActivity, NuevoContactoActivity::class.java)
                intent.putExtra("ID_CONTACTO", contactoSelecionado.id)
                intent.putExtra("NOMBRE_CONTACTO", contactoSelecionado.nombre)
                intent.putExtra("TELEFONO_CONTACTO", contactoSelecionado.telefono)
                intent.putExtra("EMAIL_CONTACTO", contactoSelecionado.email)
                intent.putExtra("CLAVE_SESION_ACTIVA", usuarioActivo)
                startActivity(intent)
            },
            mantener = { contactoSeleccionado ->
                val opciones = arrayOf("Eliminar")

                AlertDialog.Builder(this)
                    .setItems(opciones) { _, _ ->
                        AlertDialog.Builder(this)
                            .setTitle("Eliminar contacto")
                            .setMessage("¿Seguro que quieres eliminar a " + contactoSeleccionado.nombre + "?")
                            .setPositiveButton("Eliminar") { _, _ ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    dao.borrarContacto(contactoSeleccionado)
                                    withContext(Dispatchers.Main) {
                                        mensaje("Contacto eliminado correctamente")
                                    }
                                }
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                    .show()

            }
        )
        binding.listaContactos.adapter = adaptador
        binding.listaContactos.layoutManager = LinearLayoutManager(this)

        // Llamamos a los cambios en la base de datos y al botón + para cambiar a NuevoContacto
        mostrarDB()
        botonMas()
    }

    // Lanzamos y conectamos el adaptador
    private fun mostrarDB() {
        lifecycleScope.launch {
            dao.obtenerContactos(usuarioActivo).collect { listado ->
                adaptador.submitList(listado)

                // Declaramos la visibilidad del texto acorde a si hay contactos o no
                if (listado.isEmpty()) {
                    binding.textoSinContactos.visibility = android.view.View.VISIBLE
                } else {
                    binding.textoSinContactos.visibility = android.view.View.GONE
                }
            }

        }
    }

    //Lanzamos y pasamos a la siguiente vista
    private fun botonMas() {
        // Insertar contacto
        binding.btMas.setOnClickListener {
            val intent = Intent(this@AgendaActivity, NuevoContactoActivity::class.java)
            intent.putExtra("CLAVE_SESION_ACTIVA", usuarioActivo)
            startActivity(intent)
        }
    }

    // Método para enviar mensaje
    private fun mensaje(texto: String) {
        Snackbar.make(binding.root, texto, Snackbar.LENGTH_SHORT).setAnchorView(binding.btMas)
            .show()
    }

    // Método del menú
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_agenda, menu)
        return true
    }

    // Método para cerrar la sesión
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.cerrarSesion) {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar la sesión?")
                .setPositiveButton("Sí, cerrar sesión") { _, _ ->
                    lifecycleScope.launch {

                        userPrefer.cerrarSesion(false)
                        val intent = Intent(this@AgendaActivity, LoginActivity::class.java)
                        intent.putExtra("CLAVE_SESION_ACTIVA", usuarioActivo)

                        // start seguido de finish para ir al login pero matar el proceso para no poder volver
                        startActivity(intent)
                        finish()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}