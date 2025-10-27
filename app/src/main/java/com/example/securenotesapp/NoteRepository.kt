package com.example.securenotesapp


class NoteRepository(private val noteDao: NoteDao) {
    val allNotes = noteDao.getAllNotes()

    suspend fun insert(note: Note) = noteDao.insert(note)
    suspend fun delete(note: Note) = noteDao.delete(note)
    suspend fun deleteById(id: Int) = noteDao.deleteNoteById(id)
}
