package pl.destroyer.notatki.Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.destroyer.notatki.dane.Note
import pl.destroyer.notatki.data.AppDatabase
import pl.destroyer.notatki.Components.NoteDetails
import pl.destroyer.notatki.Components.NoteListScreen



@Composable
fun NotesScreen(database: AppDatabase) {
    val navController = rememberNavController()
    val notatki = remember { mutableStateListOf<Note>() }
    val scope = rememberCoroutineScope()
    var noteOrder by rememberSaveable { mutableStateOf<List<Int>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            database.noteDao().getAllNotesOrdered().collect { savedNotes: List<Note> ->
                notatki.clear()
                notatki.addAll(savedNotes)
                noteOrder = savedNotes.map { it.id }
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
                    println("Przycisk dodania notatki kliknięty")
                    scope.launch {
                        println("Rozpoczynam dodawanie nowej notatki...")
                        scope.launch(Dispatchers.IO) {
                            try {
                                val maxOrder = database.noteDao().getAllNotesOrdered()
                                    .firstOrNull()?.maxByOrNull { it.order }?.order ?: 0

                                val newNote = Note(
                                    title = "Nowa notatka",
                                    content = "",
                                    order = maxOrder + 1
                                )

                                val id = database.noteDao().insertNote(newNote)
                                println("Nowa notatka dodana z ID: $id")

                                withContext(Dispatchers.Main) {
                                    notatki.add(newNote.copy(id = id.toInt()))
                                    noteOrder = noteOrder + id.toInt()
                                    println("Nowa notatka została dodana do UI")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                println("Błąd przy dodawaniu notatki: ${e.message}")
                            }
                        }
                    }
                }
                ,
                onDeleteNote = { note ->
                    scope.launch {
                        scope.launch(Dispatchers.IO) {
                            try {
                                database.noteDao().deleteNote(note)
                                notatki.remove(note)
                                noteOrder = noteOrder.filter { it != note.id }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                onNoteReorder = { newOrder ->
                    scope.launch(Dispatchers.IO) {
                        newOrder.forEachIndexed { index, noteId ->
                            val note = notatki.find { it.id == noteId }
                            if (note != null) {
                                note.order = index
                                database.noteDao().updateNote(note)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            notatki.sortBy { it.order }
                            noteOrder = newOrder
                        }
                    }
                    println("Zapisano nową kolejność w bazie: $newOrder")
                }
                ,
                noteOrder = noteOrder
            )
        }
        composable("noteDetail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFAB81CD))
            )
            if (noteId != null) {
                val note = notatki.find { it.id == noteId }
                if (note != null) {
                    NoteDetails(
                        note = note,
                        onSave = { updatedTitle, updatedContent ->
                            scope.launch {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val updatedNote =
                                            note.copy(title = updatedTitle, content = updatedContent)
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
fun Naglowek() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color = Color(0xFF222A68))
            .fillMaxWidth()
            .height(80.dp)
            .statusBarsPadding()
    ) {
        Text(text = "Witaj w notatniku!", color = Color.White, fontSize = 20.sp)
    }
}