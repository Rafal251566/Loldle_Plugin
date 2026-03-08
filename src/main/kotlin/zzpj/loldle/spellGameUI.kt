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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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

    val allChampions = remember { championRepository.getChampions() }
    val allChampionNames = remember { allChampions.map { it.championName }.sorted() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BigSpellImage(spell = randomSpell, guessesCount = guesses.size, isVictory = isVictory)

        Spacer(modifier = Modifier.height(16.dp))

        if (!isVictory) {
            if (guesses.size >= 6) {
                val targetChampion = allChampions.find {
                    it.championName.equals(randomSpell.champion, ignoreCase = true)
                }
                val positionsHint = targetChampion?.positions?.joinToString(", ") ?: "Nieznana"

                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E2328), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFC8AA6E), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💡 Podpowiedź: Linia - $positionsHint",
                        color = Color(0xFFC8AA6E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                val triesLeft = 6 - guesses.size
                Text("Podpowiedź za: $triesLeft prób(y)", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            SpellKeyGuessPanel(spell = randomSpell) { service.startNewSpellGame() }
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
fun SpellKeyGuessPanel(spell: Spell, onReset: () -> Unit) {
    var selectedKey by remember(spell) { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .background(Color(0xFF091428), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFFC8AA6E), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎉 To jest ${spell.champion}!", color = Color(0xFFF0E6D2), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val feedbackText = when {
                selectedKey == null -> "Która to umiejętność?"
                selectedKey == spell.key -> "IDEALNIE!"
                else -> "Pudło! To było ${spell.key}."
            }
            val feedbackColor = when {
                selectedKey == null -> Color.Gray
                selectedKey == spell.key -> Color(0xFF188038)
                else -> Color(0xFF9E2A2B)
            }

            Text(feedbackText, color = feedbackColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("P", "Q", "W", "E", "R").forEach { key ->
                    val isSelected = selectedKey == key
                    val isCorrectKey = key == spell.key

                    val bgColor = when {
                        selectedKey != null && isCorrectKey -> Color(0xFF188038)
                        isSelected && !isCorrectKey -> Color(0xFF9E2A2B)
                        else -> Color(0xFF1E2328)
                    }

                    Button(
                        onClick = {
                            if (selectedKey == null) {
                                selectedKey = key
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = bgColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(key, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            if (selectedKey != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF0A323C),
                        contentColor = Color(0xFFF0E6D2)
                    )
                ) {
                    Text("Zagraj ponownie")
                }
            }
        }
    }
}

@Composable
fun BigSpellImage(spell: Spell, guessesCount: Int, isVictory: Boolean) {
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

    val showColor = guessesCount >= 3 || isVictory
    val grayscaleFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
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
                contentScale = ContentScale.Crop,
                colorFilter = if (showColor) null else grayscaleFilter
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