package pl.destroyer.notatki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.destroyer.notatki.ui.theme.NotatkiTheme

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
   fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertNote(note: Note): Long

    @Delete
 fun deleteNote(note: Note)
}

@Database(entities = [Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

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
                Column {
                    Naglowek()
                    Notatki(database)
                }
            }
        }
    }
}

@Composable
fun Naglowek() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color = Color(0xFF686869))
            .fillMaxWidth()
            .height(70.dp)

    ) {
        Text(text = "Witaj w notatniku!", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
fun Notatki(database: AppDatabase) {
    val navController = rememberNavController()
    val notatki = remember { mutableStateListOf<Note>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            database.noteDao().getAllNotes().collect { savedNotes ->
                notatki.clear()
                notatki.addAll(savedNotes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    NavHost(navController, startDestination = "noteList") {
        composable("noteList") {
            NoteListScreen(
                notatki = notatki,
                onNoteClick = { noteId ->
                    navController.navigate("noteDetail/$noteId")
                },
                onAddNote = {
                    scope.launch {
                        scope.launch(Dispatchers.IO) {
                        try {
                            val newNote = Note(
                                title = "Nowa notatka",
                                content = ""
                            )
                            val id = database.noteDao().insertNote(newNote)
                        } catch (e: Exception) {
                        }
                    }
                }
                },
                onDeleteNote = { note ->
                    scope.launch {
                        scope.launch(Dispatchers.IO) {
                            try {
                                database.noteDao().deleteNote(note)
                                notatki.remove(note)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            )
        }
        composable("noteDetail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1b1c)))
            if (noteId != null) {
                val note = notatki.find { it.id == noteId }
                if (note != null) {
                    NotatkaSzczegoly(
                        note = note,
                        onSave = { updatedTitle, updatedContent ->
                            scope.launch {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val updatedNote = note.copy(title = updatedTitle, content = updatedContent)
                                        database.noteDao().insertNote(updatedNote)
                                        withContext(Dispatchers.Main) {
                                            val index = notatki.indexOf(note)
                                            notatki[index] = updatedNote
                                            navController.navigate("noteList")
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    )
                }
            } else {
                Text("Notatka nie została znaleziona")
            }
        }
    }
}

@Composable
fun NoteListScreen(
    notatki: List<Note>,
    onNoteClick: (Int) -> Unit,
    onAddNote: () -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            DodajNotatke(onClick = onAddNote)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (notatki.isEmpty()) {
                Text(
                    text = "Brak notatek. Dodaj pierwszą!",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                notatki.forEach { notatka ->
                    Notatka(
                        note = notatka,
                        onClick = { onNoteClick(notatka.id) },
                        onDelete = { onDeleteNote(notatka) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun Notatka(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color(0xFF9eaee8))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)) {
            Text(text = note.title, color = Color.White)
            Text(
                text = note.content.take(25) + if (note.content.length > 25) "..." else "",
                color = Color.White
            )
            Button(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1e2f6b),
                    contentColor = Color.White
                    )
            ) {
                Text("Usuń")
            }
        }
    }
}

@Composable
fun DodajNotatke(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Text("+")
    }
}

@Composable
fun NotatkaSzczegoly(note: Note, onSave: (String, String) -> Unit) {
    val content = remember { mutableStateOf(note.content) }
    val title = remember { mutableStateOf(note.title) }

    Column(modifier = Modifier.padding(16.dp) ) {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Tytuł") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        TextField(
            value = content.value,
            onValueChange = { content.value = it },
            label = { Text("Treść") },
            modifier = Modifier
                .height(400.dp)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Button(onClick = { onSave(title.value, content.value) },colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1e2f6b),
            contentColor = Color.White)
        ) {
            Text("Zapisz", color = Color.White)
        }
    }
}