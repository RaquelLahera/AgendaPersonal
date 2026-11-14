package com.example.agendapersonal.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.agendaPersonal.databinding.ItemsContactoBinding
import com.example.agendapersonal.data.room.Contacto

// Creamos el adaptador que una AgendaActivity con Contacto y, además, acepte pulsaciones cortas (pulsar) y largas (mantener)
class ContactoAdapter(private val pulsar:(Contacto) -> Unit, private val mantener:(Contacto) -> Unit) : ListAdapter<Contacto, ContactoAdapter.ContactoViewHolder>(
    ContactoDiffCallback()) {

    // Implementamos automáticamente y rellenamos:
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
        // El binding inflado y lo devolvemos
    ): ContactoViewHolder {
        val binding = ItemsContactoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactoViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ContactoViewHolder,
        position: Int
    ) {
        // El contacto actual dinámico
        val contactoActual = getItem(position)
        holder.pasarDatos(contactoActual)

        // Para controlar las pulsaciones
        holder.itemView.setOnClickListener {
            pulsar(contactoActual)
        }
        holder.itemView.setOnLongClickListener {
            mantener(contactoActual)
            true
        }
    }

    // Hacemos clase del viewHolder
    class ContactoViewHolder(private val binding: ItemsContactoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun pasarDatos(contacto: Contacto) {
            binding.txtNombre.text = contacto.nombre
            binding.txtTelefono.text = contacto.telefono.toString()
            binding.txtEmail.text = contacto.email
        }
    }

    // Hacemos clase con el DiffUtil
    class ContactoDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Contacto>() {
        // Compara si es el mismo elemento de la base de datos
        override fun areItemsTheSame(oldItem: Contacto, newItem: Contacto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contacto, newItem: Contacto): Boolean {
            return oldItem == newItem
        }
    }
}