package pl.destroyer.notatki

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.google.accompanist.flowlayout.FlowRow

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp

import pl.destroyer.notatki.ui.theme.NotatkiTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotatkiTheme {
                Column(

                ) {
                    Naglowek()

                    Notatki()
                }



            }
        }
    }
}


@Composable
fun Naglowek(
){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color = Color.Cyan)
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(text = "Witaj w notatniku!")
    }
}



@Composable
fun Notatki() {
    val notatki = remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            DodajNotatke {
                val nowaLista = notatki.value + "Nowa notatka"
                notatki.value = nowaLista
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 10.dp,
                crossAxisSpacing = 10.dp

            ) {
                for (notatka in notatki.value) {
                    Notatka(

                        text = notatka,
                        onClick = {

                            println("Kliknięto notatkę: $notatka")
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun Notatka(text: String, onClick: () -> Unit) {

    Box(

        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color.Red)
            .padding(8.dp)
            .clickable(onClick = onClick)



    ) {
        Text(text = text, color = Color.White, modifier = Modifier.align(Alignment.Center))
    }
}



@Composable
fun DodajNotatke(onClick: () -> Unit){
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier


    ) {
        Text("+")
    }
}