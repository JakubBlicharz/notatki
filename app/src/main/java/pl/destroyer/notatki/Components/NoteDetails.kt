package pl.destroyer.notatki.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.destroyer.notatki.R
import pl.destroyer.notatki.dane.Note


@Composable
fun NoteDetails(note: Note, onSave: (String, String) -> Unit) {
    val content = remember { mutableStateOf(note.content) }
    val title = remember { mutableStateOf(note.title) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text(stringResource(id = R.string.title)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(16.dp)),
            singleLine = true
        )
        TextField(
            value = content.value,
            onValueChange = { content.value = it },
            label = { Text(stringResource(id = R.string.content)) },
            modifier = Modifier
                .height(400.dp)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(16.dp)),
            maxLines = Int.MAX_VALUE,
            singleLine = false
        )
        Button(
            onClick = { onSave(title.value, content.value) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF452971),
                contentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(id = R.string.save), color = Color.White)
        }
    }
}