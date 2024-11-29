package pl.destroyer.notatki.data

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.destroyer.notatki.dane.Note
import pl.destroyer.notatki.dane.NoteDao


@Database(entities = [Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}