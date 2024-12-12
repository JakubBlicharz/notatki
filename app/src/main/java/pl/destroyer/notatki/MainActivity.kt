package pl.destroyer.notatki

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import pl.destroyer.notatki.Screen.NotesScreen
import pl.destroyer.notatki.ui.theme.NotatkiTheme
import pl.destroyer.notatki.data.AppDatabase
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    private val sharedPreferences by lazy {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    private val languageState = mutableStateOf("pl")

    private fun setAppLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        sharedPreferences.edit().putString("language", language).apply()
        languageState.value = language
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedLanguage = sharedPreferences.getString("language", "pl") ?: "pl"
        setAppLanguage(savedLanguage)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notes_database"
        ).fallbackToDestructiveMigration().build()

        enableEdgeToEdge()

        setContent {
            val currentLanguage by languageState

            NotatkiTheme {
                CompositionLocalProvider(

                    LocalContext provides applicationContext.createConfigurationContext(
                        Configuration(resources.configuration).apply {
                            setLocale(Locale(currentLanguage))
                        }
                    )
                ) {
                    AppContent(
                        database = database,
                        setAppLanguage = ::setAppLanguage
                    )
                }
            }
        }
    }
}


@Composable
fun AppContent(database: AppDatabase, setAppLanguage: (String) -> Unit) {
    NotesScreen(database = database, setAppLanguage = setAppLanguage)
}
