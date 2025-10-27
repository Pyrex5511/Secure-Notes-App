package com.example.securenotesapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val addButton = findViewById<FloatingActionButton>(R.id.addButton)

        adapter = NoteAdapter(
            onDeleteClick = { note ->
                viewModel.deleteNote(note)
            },
            onItemClick = { note ->
                val intent = Intent(this, NoteDetailActivity::class.java)
                intent.putExtra("noteId", note.id)
                startActivity(intent)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.allNotes.observe(this) { list ->
            adapter.setNotes(list)

            addButton.setOnClickListener {
                showAddDialog()
            }
        }



    }

    private fun showAddDialog() {
        val dialog = Dialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val titleInput = dialogView.findViewById<EditText>(R.id.titleInput)
        val contentInput = dialogView.findViewById<EditText>(R.id.contentInput)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        btnSave.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val content = contentInput.text.toString().trim()
            if (title.isNotEmpty() || content.isNotEmpty()) {
                viewModel.addNote(title, content)
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        // 👇 Nastavenie plnej šírky (napr. 90 % obrazovky)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }



}
