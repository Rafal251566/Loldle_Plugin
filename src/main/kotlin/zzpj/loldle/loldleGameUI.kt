package zzpj.loldle

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

@Composable
fun loldleGameUI(service: LoldleService) {
    val randomChampion = service.randomChampion ?: return
    val guesses = service.guesses
    val isVictory = service.isVictory

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Champion", "Species", "Region", "Lane", "Range type", "Resource").forEach { header ->
                Box(modifier = Modifier.size(80.dp, 30.dp), contentAlignment = Alignment.Center) {
                    Text(header, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(guesses) { guess ->
                guessingRow(selected = guess, goal = randomChampion)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isVictory) {
            VictoryPanel(randomChampion.name) { service.startNewGame() }
        } else {
            autocompleteSearch(
                allChampions = championRepository.getChampions(),
                alreadyGuessed = guesses,
                onGuess = { service.submitGuess(it) }
            )
        }
    }
}

@Composable
fun VictoryPanel(name: String, onReset: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E7D32), shape = RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("You WON!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("It was $name", color = Color.White)
            }
        }
        Button(onClick = onReset, modifier = Modifier.padding(top = 8.dp)) {
            Text("Play Again")
        }
    }
}

@Composable
fun autocompleteSearch(allChampions: List<Champion>, alreadyGuessed: List<Champion>, onGuess: (Champion) -> Unit) {
    var inputName by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val filteredChampions = if (inputName.length < 2) emptyList()
    else allChampions.filter { it.name.contains(inputName, ignoreCase = true) && !alreadyGuessed.contains(it) }.take(5)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            TextField(
                value = inputName,
                onValueChange = { inputName = it; isMenuExpanded = true },
                label = { Text("Champion name...") },
                singleLine = true
            )
            DropdownMenu(
                expanded = isMenuExpanded && filteredChampions.isNotEmpty(),
                onDismissRequest = { isMenuExpanded = false },
                properties = PopupProperties(focusable = false)
            ) {
                filteredChampions.forEach { champ ->
                    DropdownMenuItem(onClick = {
                        inputName = champ.name
                        isMenuExpanded = false
                    }) { Text(champ.name) }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                val found = allChampions.find { it.name.equals(inputName, ignoreCase = true) }
                if (found != null) {
                    onGuess(found)
                    inputName = ""
                }
            },
            enabled = inputName.isNotBlank()
        ) { Text("Guess") }
    }
}

@Composable
fun guessingRow(selected: Champion, goal: Champion) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
        featureBox(selected.name, selected.name == goal.name)
        featureBox(selected.species, selected.species == goal.species)
        featureBox(selected.region, selected.region == goal.region)
        featureBox(selected.lane, selected.lane == goal.lane)
        featureBox(selected.attackType, selected.attackType == goal.attackType)
        featureBox(selected.partype, selected.partype == goal.partype)
    }
}

@Composable
fun featureBox(value: String, isCorrect: Boolean) {
    Box(
        modifier = Modifier.size(80.dp).background(if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828))
            .border(1.dp, Color.Black).padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = value, color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}