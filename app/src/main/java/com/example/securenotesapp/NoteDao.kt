package com.example.securenotesapp

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.securenotesapp.Note

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Query("SELECT * FROM note ORDER BY id DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    @Update
    suspend fun updateNote(note: Note)

    @Query("SELECT * FROM note WHERE id = :id")
    fun getNoteById(id: Int): LiveData<Note>
}
