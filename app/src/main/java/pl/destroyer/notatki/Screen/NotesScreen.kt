package pl.destroyer.notatki.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.destroyer.notatki.Components.LanguageDropdownMenu
import pl.destroyer.notatki.Components.NoteDetails
import pl.destroyer.notatki.Components.NoteListScreen
import pl.destroyer.notatki.R
import pl.destroyer.notatki.data.AppDatabase
import pl.destroyer.notatki.dane.Note

@Composable
fun NotesScreen(database: AppDatabase, setAppLanguage: (String) -> Unit) {
    val navController = rememberNavController()
    val notatki = remember { mutableStateListOf<Note>() }
    val scope = rememberCoroutineScope()
    var noteOrder by remember { mutableStateOf<List<Int>>(emptyList()) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    fun openDrawer() {
        scope.launch { drawerState.open() }
    }

    LaunchedEffect(notatki.size) {
        database.noteDao().getAllNotesOrdered().collect { savedNotes ->
            notatki.clear()
            notatki.addAll(savedNotes)
            noteOrder = savedNotes.map { it.id }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            Column(modifier = Modifier
                .padding(top = 50.dp, bottom = 50.dp)
                .fillMaxHeight()
                .width(200.dp)
                .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(

                    text = stringResource(R.string.choose_language),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(32.dp))
                LanguageDropdownMenu(setAppLanguage = setAppLanguage, drawerState = drawerState)
            }
        }
    ) {
        Scaffold(
            topBar = { Naglowek(onMenuClick = { openDrawer() }) }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "noteList",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("noteList") {
                    NoteListScreen(
                        notatki = notatki,
                        onNoteClick = { noteId -> navController.navigate("noteDetail/$noteId") },
                        onAddNote = {
                            scope.launch(Dispatchers.IO) {
                                val maxOrder = database.noteDao().getAllNotesOrdered()
                                    .firstOrNull()?.maxByOrNull { it.order }?.order ?: 0
                                val newNote = Note(
                                    title = "Nowa notatka",
                                    content = "",
                                    order = maxOrder + 1
                                )
                                val id = database.noteDao().insertNote(newNote)
                                val addedNote = newNote.copy(id = id.toInt())

                                withContext(Dispatchers.Main) {
                                    notatki.add(addedNote)
                                    noteOrder = notatki.map { it.id }

                                    navController.navigate("noteDetail/${addedNote.id}")

                                }
                            }
                        },
                        onDeleteNote = { note ->
                            scope.launch(Dispatchers.IO) {
                                database.noteDao().deleteNote(note)
                                withContext(Dispatchers.Main) {
                                    notatki.remove(note)
                                    noteOrder = noteOrder.filter { it != note.id }
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
                        }
                    )
                }
                composable("noteDetail/{noteId}") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
                    noteId?.let { id ->
                        val note = notatki.find { it.id == id }
                        note?.let {
                            NoteDetails(note = it, onSave = { updatedTitle, updatedContent ->
                                scope.launch(Dispatchers.IO) {
                                    database.noteDao().updateNote(it.copy(title = updatedTitle, content = updatedContent))
                                    val updatedNotes = database.noteDao().getAllNotesOrdered().firstOrNull()

                                    withContext(Dispatchers.Main) {
                                        updatedNotes?.let { notes ->
                                            notatki.clear()
                                            notatki.addAll(notes)
                                            noteOrder = notes.map { it.id }
                                        }
                                        navController.popBackStack()
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Naglowek(onMenuClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(color = Color(0xFF222A68))
            .fillMaxWidth()
            .height(100.dp)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.welcome),
                color = Color.White,
                fontSize = 20.sp
            )
        }
    }
}

