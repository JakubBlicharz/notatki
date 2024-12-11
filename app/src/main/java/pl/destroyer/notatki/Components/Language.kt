package pl.destroyer.notatki.Components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import pl.destroyer.notatki.R

@Composable
fun LanguageDropdownMenu(setAppLanguage: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var selectedLanguage by remember { mutableStateOf(sharedPreferences.getString("language", "pl")!!) }

    Column {
        Button(onClick = { expanded = true }, colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF452971),
            contentColor = Color.White) ) {
            Text(context.getString(R.string.language_label) + ": " +
                    if (selectedLanguage == "pl") context.getString(R.string.polish) else context.getString(R.string.english))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(context.getString(R.string.polish)) },
                onClick = {
                    selectedLanguage = "pl"
                    setAppLanguage("pl")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(context.getString(R.string.english)) },
                onClick = {
                    selectedLanguage = "en"
                    setAppLanguage("en")
                    expanded = false
                }
            )
        }
    }
}
