package com.example.securenotesapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val noteDao = NoteDatabase.getDatabase(application).noteDao()
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            noteDao.insert(Note(title = title, content = content))
        }
    }


    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.delete(note)
        }
    }

    fun getNoteById(id: Int) = noteDao.getNoteById(id)

    fun deleteNoteById(id: Int) {
        viewModelScope.launch {
            noteDao.deleteNoteById(id)
        }
    }

    fun updateNote(id: Int, title: String, content: String) {
        viewModelScope.launch {
            val note = Note(id = id, title = title, content = content)
            noteDao.updateNote(note)
        }
    }
}
