package pl.destroyer.notation.extr_data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY `order` ASC")
    fun getAllNotesOrdered(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: Note): Long

    @Delete
    fun deleteNote(note: Note)

    @Query("UPDATE notes SET `order` = :order WHERE id = :noteId")
    fun updateNoteOrder(noteId: Int, order: Int)

    @Update
    fun updateNote(note: Note)

    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<Note>>
}