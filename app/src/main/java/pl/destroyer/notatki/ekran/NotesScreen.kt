package pl.destroyer.notatki.ekran

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.destroyer.notatki.dane.Note
import pl.destroyer.notatki.data.AppDatabase
import pl.destroyer.notatki.komponenty.NoteDetails
import pl.destroyer.notatki.komponenty.NoteListScreen

import kotlin.collections.addAll
import kotlin.collections.remove
import kotlin.text.clear

@Composable
fun NotesScreen(database: AppDatabase) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1a1b1c))
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
            .background(color = Color(0xFF686869))
            .fillMaxWidth()
            .height(80.dp)
            .statusBarsPadding()
    ) {
        Text(text = "Witaj w notatniku!", color = Color.White, fontSize = 20.sp)
    }
}