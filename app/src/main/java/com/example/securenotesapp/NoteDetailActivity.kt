package com.example.securenotesapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import java.util.Stack

class NoteDetailActivity : AppCompatActivity() {

    private val viewModel: NoteViewModel by viewModels()
    private var noteId: Int = 0

    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var undoButton: Button
    private lateinit var redoButton: Button

    // 🧠 Stacky pre oba polia
    private val titleUndoStack = Stack<String>()
    private val titleRedoStack = Stack<String>()
    private val contentUndoStack = Stack<String>()
    private val contentRedoStack = Stack<String>()

    private var isUndoOrRedo = false
    private var isInitializing = true // 🆕 pridáme tento flag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        noteId = intent.getIntExtra("noteId", -1)

        titleInput = findViewById(R.id.titleInput)
        contentInput = findViewById(R.id.contentInput)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
        undoButton = findViewById(R.id.undoButton)
        redoButton = findViewById(R.id.redoButton)

        // 🧠 Načítaj poznámku
        if (noteId != -1) {
            viewModel.getNoteById(noteId).observe(this, Observer { note ->
                note?.let {
                    // 🛑 Zakáž sledovanie zmien počas načítania
                    isInitializing = true

                    titleInput.setText(it.title)
                    contentInput.setText(it.content)

                    // Po načítaní povoľ späť sledovanie zmien
                    isInitializing = false

                    // 🧹 Vyčisti stacky a pridaj počiatočný stav
                    titleUndoStack.clear()
                    contentUndoStack.clear()
                    titleRedoStack.clear()
                    contentRedoStack.clear()
                    titleUndoStack.push(it.title)
                    contentUndoStack.push(it.content)
                }
            })
        }

        // 🪶 Watcher pre title
        titleInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!isUndoOrRedo && !isInitializing) {
                    titleUndoStack.push(s.toString())
                    titleRedoStack.clear()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // 🪶 Watcher pre content
        contentInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!isUndoOrRedo && !isInitializing) {
                    contentUndoStack.push(s.toString())
                    contentRedoStack.clear()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        undoButton.setOnClickListener { undo() }
        redoButton.setOnClickListener { redo() }

        saveButton.setOnClickListener {
            viewModel.updateNote(
                noteId,
                titleInput.text.toString(),
                contentInput.text.toString()
            )
            finish()
        }

        deleteButton.setOnClickListener {
            viewModel.deleteNoteById(noteId)
            finish()
        }

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            // Jednoducho zavrie aktuálne Activity a vráti sa späť
            finish()
        }


    }

    private fun undo() {
        if (titleUndoStack.size > 1 || contentUndoStack.size > 1) { // 🧠 aby sme nevymazali počiatočný stav
            isUndoOrRedo = true

            if (titleUndoStack.size > 1) {
                val currentTitle = titleInput.text.toString()
                titleRedoStack.push(currentTitle)
                titleUndoStack.pop() // zahoď current
                val prevTitle = titleUndoStack.peek()
                titleInput.setText(prevTitle)
                titleInput.setSelection(prevTitle.length)
            }

            if (contentUndoStack.size > 1) {
                val currentContent = contentInput.text.toString()
                contentRedoStack.push(currentContent)
                contentUndoStack.pop()
                val prevContent = contentUndoStack.peek()
                contentInput.setText(prevContent)
                contentInput.setSelection(prevContent.length)
            }

            isUndoOrRedo = false
        }
    }

    private fun redo() {
        if (titleRedoStack.isNotEmpty() || contentRedoStack.isNotEmpty()) {
            isUndoOrRedo = true

            if (titleRedoStack.isNotEmpty()) {
                val currentTitle = titleInput.text.toString()
                titleUndoStack.push(currentTitle)
                val nextTitle = titleRedoStack.pop()
                titleInput.setText(nextTitle)
                titleInput.setSelection(nextTitle.length)
            }

            if (contentRedoStack.isNotEmpty()) {
                val currentContent = contentInput.text.toString()
                contentUndoStack.push(currentContent)
                val nextContent = contentRedoStack.pop()
                contentInput.setText(nextContent)
                contentInput.setSelection(nextContent.length)
            }

            isUndoOrRedo = false
        }
    }
}
