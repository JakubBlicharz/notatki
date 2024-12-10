package pl.destroyer.notatki

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import pl.destroyer.notatki.Components.LanguageDropdownMenu
import pl.destroyer.notatki.Screen.Naglowek
import pl.destroyer.notatki.Screen.NotesScreen
import pl.destroyer.notatki.ui.theme.NotatkiTheme
import androidx.room.Room
import pl.destroyer.notatki.data.AppDatabase
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notes_database"
        ).fallbackToDestructiveMigration().build()

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("language", "pl") ?: "pl"

        var languageState by mutableStateOf(savedLanguage)

        fun setAppLanguage(language: String) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)

            sharedPreferences.edit().putString("language", language).apply()

            languageState = language
        }

        enableEdgeToEdge()

        setContent {
            NotatkiTheme {
                CompositionLocalProvider {
                    key(languageState) {
                        AppContent(setAppLanguage = ::setAppLanguage)
                    }
                }
            }
        }
    }

    @Composable
    fun AppContent(setAppLanguage: (String) -> Unit) {
        Column {
            Naglowek()
            LanguageDropdownMenu(setAppLanguage = setAppLanguage)
            NotesScreen(database = database)
        }
    }
}
