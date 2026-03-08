package zzpj.loldle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun spellGameUI(service: LoldleService) {
    val randomSpell = service.randomSpell ?: return
    val guesses = service.spellGuesses
    val isVictory = service.isSpellVictory

    val allChampionNames = remember { championRepository.getChampions().map { it.championName }.sorted() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BigSpellImage(randomSpell)

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth(0.6f)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(guesses.reversed()) { guessName ->
                    val isCorrect = guessName.equals(randomSpell.champion, ignoreCase = true)
                    SpellGuessRow(guessName, isCorrect)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isVictory) {
            VictoryPanel(randomSpell.champion) { service.startNewSpellGame() }
        } else {
            SimpleAutocompleteSearch(
                allNames = allChampionNames,
                alreadyGuessed = guesses,
                onGuess = { service.submitSpellGuess(it) }
            )
        }
    }
}

@Composable
fun BigSpellImage(spell: Spell) {
    val folder = if (spell.type == "passive") "passive" else "spell"
    val url = "https://ddragon.leagueoflegends.com/cdn/16.5.1/img/$folder/${spell.imageId}"

    var image by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        try {
            image = withContext(Dispatchers.IO) {
                URL(url).openStream().use { loadImageBitmap(it) }
            }
        } catch (e: Exception) {
            println("Błąd pobierania skilla z: $url")
        }
    }

    Box(
        modifier = Modifier
            .size(160.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .background(Color(0xFF091428), RoundedCornerShape(12.dp))
            .border(4.dp, Color(0xFFC8AA6E), RoundedCornerShape(12.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(
                bitmap = image!!,
                contentDescription = "Tajemnicza umiejętność",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            CircularProgressIndicator(color = Color(0xFFC8AA6E))
        }
    }
}

@Composable
fun SpellGuessRow(guessName: String, isCorrect: Boolean) {
    val bgColor = if (isCorrect) Color(0xFF188038) else Color(0xFF9E2A2B)
    val darkerColor = Color(
        red = (bgColor.red * 0.7f).coerceIn(0f, 1f),
        green = (bgColor.green * 0.7f).coerceIn(0f, 1f),
        blue = (bgColor.blue * 0.7f).coerceIn(0f, 1f),
        alpha = bgColor.alpha
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Brush.verticalGradient(listOf(bgColor, darkerColor)), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0x60000000), RoundedCornerShape(6.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(guessName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun SimpleAutocompleteSearch(allNames: List<String>, alreadyGuessed: List<String>, onGuess: (String) -> Unit) {
    var inputName by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val filtered = if (inputName.isBlank()) emptyList() else allNames.filter {
        it.contains(inputName, ignoreCase = true) && !alreadyGuessed.contains(it)
    }.take(5)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            OutlinedTextField(
                value = inputName,
                onValueChange = { inputName = it; isMenuExpanded = true },
                placeholder = { Text("Wpisz postać...") },
                singleLine = true,
                modifier = Modifier.width(220.dp)
            )
            DropdownMenu(
                expanded = isMenuExpanded && filtered.isNotEmpty(),
                onDismissRequest = { isMenuExpanded = false },
                properties = androidx.compose.ui.window.PopupProperties(focusable = false)
            ) {
                filtered.forEach { name ->
                    DropdownMenuItem(onClick = {
                        onGuess(name)
                        inputName = ""
                        isMenuExpanded = false
                    }) { Text(name) }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                val found = allNames.find { it.equals(inputName, ignoreCase = true) }
                if (found != null) {
                    onGuess(found)
                    inputName = ""
                }
            },
            enabled = inputName.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF0A323C),
                contentColor = Color(0xFFF0E6D2)
            )
        ) { Text("Zgadnij") }
    }
}