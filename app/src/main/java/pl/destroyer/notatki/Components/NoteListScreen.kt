package pl.destroyer.notatki.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.destroyer.notatki.dane.Note


@Composable
fun NoteListScreen(
    notatki: List<Note>,
    onNoteClick: (Int) -> Unit,
    onAddNote: () -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FloatingActionButton(onClick = onAddNote, containerColor = Color(0xFF452971) , modifier = Modifier .size(75.dp)) {
                        Text("+", fontSize = 30.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            if (notatki.isEmpty()) {
                Text(
                    text = "Brak notatek. Dodaj pierwszą!",
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                notatki.forEach { notatka ->
                    Note(
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