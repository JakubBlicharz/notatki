package pl.destroyer.notation.data

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.destroyer.notation.extr_data.Note
import pl.destroyer.notation.extr_data.NoteDao


@Database(entities = [Note::class], version = 2,exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}