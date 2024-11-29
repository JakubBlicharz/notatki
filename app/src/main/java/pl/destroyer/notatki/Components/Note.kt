package pl.destroyer.notatki.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.destroyer.notatki.dane.Note


@Composable
fun Note(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color(0xFF574AE2))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)) {
            Text(text = note.title, color = Color.White)
            Text(
                text = note.content.take(25) + if (note.content.length > 25) "..." else "",
                maxLines = 1,
                color = Color.White
            )
            Button(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF452971),
                    contentColor = Color.White

                )
            ) {
                Text("Usuń")
            }
        }
    }
}