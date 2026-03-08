package zzpj.loldle

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun loldleGameUI() {
    var guessedCharacter by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Enter the champion name") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message)

        TextField(
            value = guessedCharacter,
            onValueChange = { guessedCharacter = it },
            label = { Text("Who is that?") }
        )

        Button(onClick = {
            if (guessedCharacter.equals("Teemo", ignoreCase = true)) {
                message = "You got it!"
            } else {
                message = "Wrong, try again!"
            }
        }) {
            Text("Guess")
        }
    }
}