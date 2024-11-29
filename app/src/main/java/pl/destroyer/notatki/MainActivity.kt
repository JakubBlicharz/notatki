package pl.destroyer.notatki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.room.Room
import pl.destroyer.notatki.data.AppDatabase
import pl.destroyer.notatki.Screen.Naglowek
import pl.destroyer.notatki.Screen.NotesScreen
import pl.destroyer.notatki.ui.theme.NotatkiTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notes_database"
        ).fallbackToDestructiveMigration().build()

        enableEdgeToEdge()

        setContent {
            NotatkiTheme {
                Column{
                    Naglowek()
                    NotesScreen(database = database)
                }

            }
        }
    }
}