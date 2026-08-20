package com.example.capstone.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.R
import com.example.capstone.data.EmergencyContact
import com.example.capstone.data.SafeReadyPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.UUID

class EmergencyContactsViewModel(val prefs: SafeReadyPreferences) : ViewModel() {
    val contacts = MutableLiveData<List<EmergencyContact>>()

    init {
        loadContacts()
    }

    fun loadContacts() {
        contacts.value = prefs.getEmergencyContacts()
    }

    fun addContact(name: String, phone: String, relation: String?) {
        val currentList = prefs.getEmergencyContacts().toMutableList()
        currentList.add(EmergencyContact(UUID.randomUUID().toString(), name, phone, relation))
        prefs.saveEmergencyContacts(currentList)
        loadContacts()
    }

    fun deleteContact(id: String) {
        val currentList = prefs.getEmergencyContacts().toMutableList()
        currentList.removeAll { it.id == id }
        prefs.saveEmergencyContacts(currentList)
        loadContacts()
    }

    fun updateContact(id: String, name: String, phone: String, relation: String?) {
        val currentList = prefs.getEmergencyContacts().toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            currentList[index] = EmergencyContact(id, name, phone, relation)
            prefs.saveEmergencyContacts(currentList)
            loadContacts()
        }
    }
}

class EmergencyContactsViewModelFactory(private val prefs: SafeReadyPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EmergencyContactsViewModel(prefs) as T
    }
}

class EmergencyContactsFragment : Fragment() {
    private lateinit var viewModel: EmergencyContactsViewModel
    private lateinit var adapter: EmergencyContactsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_emergency_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = SafeReadyPreferences(requireContext())
        viewModel = ViewModelProvider(this, EmergencyContactsViewModelFactory(prefs))[EmergencyContactsViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.contactsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = EmergencyContactsAdapter(
            onEditClick = { contact -> showEditContactDialog(contact) },
            onDeleteClick = { contactId -> viewModel.deleteContact(contactId) }
        )
        recyclerView.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.addContactFab).setOnClickListener {
            showAddContactDialog()
        }

        viewModel.contacts.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_contact, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val phoneInput = dialogView.findViewById<EditText>(R.id.phoneInput)
        val relationInput = dialogView.findViewById<EditText>(R.id.relationInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                val relation = relationInput.text.toString().trim()

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    viewModel.addContact(name, phone, relation.ifEmpty { null })
                } else {
                    Toast.makeText(requireContext(), "Name and phone are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditContactDialog(contact: EmergencyContact) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_contact, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val phoneInput = dialogView.findViewById<EditText>(R.id.phoneInput)
        val relationInput = dialogView.findViewById<EditText>(R.id.relationInput)

        nameInput.setText(contact.name)
        phoneInput.setText(contact.phone)
        relationInput.setText(contact.relation)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                val relation = relationInput.text.toString().trim()

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    viewModel.updateContact(contact.id, name, phone, relation.ifEmpty { null })
                } else {
                    Toast.makeText(requireContext(), "Name and phone are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class EmergencyContactsAdapter(
    private val onEditClick: (EmergencyContact) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<EmergencyContactsAdapter.ViewHolder>() {
    private var contacts = emptyList<EmergencyContact>()

    fun submitList(newList: List<EmergencyContact>) {
        contacts = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_emergency_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name
        holder.relation.text = contact.relation ?: "No relation specified"
        holder.phone.text = contact.phone
        holder.itemView.setOnClickListener { onEditClick(contact) }
        holder.deleteBtn.setOnClickListener { onDeleteClick(contact.id) }
    }

    override fun getItemCount() = contacts.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.contactName)
        val relation: TextView = view.findViewById(R.id.contactRelation)
        val phone: TextView = view.findViewById(R.id.contactPhone)
        val deleteBtn: ImageButton = view.findViewById(R.id.deleteContactButton)
    }
}
