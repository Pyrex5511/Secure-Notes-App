package com.example.securenotesapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
// Potrebné importy pre LayoutManager a konverziu DP
import androidx.recyclerview.widget.GridLayoutManager
import android.util.TypedValue

class MainActivity : AppCompatActivity() {

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    // Vytvorili ste GridSpacingItemDecoration v samostatnom súbore,
    // takže ju stačí len volať (predpokladám, že je v rovnakom balíku)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔒 Zabezpečenie obsahu – čierna obrazovka v multitaskingu, zakáže screenshoty
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        // ✅ Ak je appka zamknutá, hneď presmeruj na LockActivity
        if (LockManager.isLocked) {
            startActivity(Intent(this, LockActivity::class.java))
            finish()
            return
        }

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

        // --- 🚀 NOVÁ LOGIKA PRE GRID A MEDZERY ---

        val numberOfColumns = 2

        // 1. Nastavenie GridLayoutManager
        recyclerView.layoutManager = GridLayoutManager(this, numberOfColumns)

        // 2. APLIKÁCIA DEKORÁCIE PRE MEDZERY (GAPS)

        // Prevod 12dp na pixely
        val spacingInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            12f,
            resources.displayMetrics
        ).toInt()

        // Odstráňte staré dekorácie (dôležité, aby ste ich nemali násobne)
        while (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }

        // Pridajte novú dekoráciu, ktorá zabezpečí medzery medzi položkami
        recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount = numberOfColumns,
                spacing = spacingInPixels,
                includeEdge = true
            )
        )

        // --- KONIEC NOVEJ LOGIKY ---

        viewModel.allNotes.observe(this) { list ->
            adapter.setNotes(list)
        }

        addButton.setOnClickListener {
            showAddDialog()
        }
    }


    override fun onResume() {
        super.onResume()
        if (LockManager.isLocked) {
            startActivity(Intent(this, LockActivity::class.java))
            finish()
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
        val error_text = dialogView.findViewById<TextView>(R.id.error_text)

        btnSave.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val content = contentInput.text.toString().trim()


            if (title.isEmpty() && content.isEmpty()){
                error_text.isVisible = true
            }
            else if (title.isNotEmpty() || content.isNotEmpty()) {
                viewModel.addNote(title, content)

                error_text.isVisible = false

                dialog.dismiss()
            }
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