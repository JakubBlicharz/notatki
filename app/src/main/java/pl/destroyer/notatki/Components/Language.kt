package pl.destroyer.notatki.Components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import pl.destroyer.notation.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import pl.destroyer.notation.ui.theme.defaultButtonColors

@Composable
fun LanguageDropdownMenu(setAppLanguage: (String) -> Unit, drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var selectedLanguage by remember { mutableStateOf(sharedPreferences.getString("language", "pl")!!) }

    Column {
        Button(onClick = { expanded = true }, colors = MaterialTheme.defaultButtonColors) {
            Text(
                text = "${stringResource(R.string.language_label)}: ${
                    if (selectedLanguage == "pl") stringResource(R.string.polish)
                    else stringResource(R.string.english)
                }"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.polish)) },
                onClick = {
                    selectedLanguage = "pl"
                    setAppLanguage("pl")
                    expanded = false
                    scope.launch { drawerState.close() }
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.english)) },
                onClick = {
                    selectedLanguage = "en"
                    setAppLanguage("en")
                    expanded = false
                    scope.launch { drawerState.close() }
                }
            )
        }
    }
}
