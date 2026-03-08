package zzpj.loldle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

@Composable
fun loldleGameUI() {
    val championBase = championRepository.champions
    val randomChampion = remember { championBase.random() }
    var guesses by remember { mutableStateOf(listOf<Champion>()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Champion", "Species", "Region", "Lane", "Range type", "Resource").forEach { header ->
                Box(modifier = Modifier.size(80.dp, 30.dp), contentAlignment = Alignment.Center) {
                    Text(header, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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

        autocompleteSearch(
                allChampions = championBase,
                alreadyGuessed = guesses,
                onGuess = { guessedChamp -> guesses = guesses + guessedChamp }
            )
        }
}

@Composable
fun autocompleteSearch(
    allChampions: List<Champion>,
    alreadyGuessed: List<Champion>,
    onGuess: (Champion) -> Unit
) {
    var inputName by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val filteredChampions = if (inputName.isBlank()) {
        emptyList()
    } else {
        allChampions.filter {
            it.name.contains(inputName, ignoreCase = true) && !alreadyGuessed.contains(it)
        }.take(5)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            TextField(
                value = inputName,
                onValueChange = {
                    inputName = it
                    isMenuExpanded = true
                },
                label = { Text("Enter champion name...") },
                singleLine = true
            )
            DropdownMenu(
                expanded = isMenuExpanded && filteredChampions.isNotEmpty(),
                onDismissRequest = { isMenuExpanded = false },
                modifier = Modifier.width(200.dp),
                properties = PopupProperties(focusable = false)
            ) {
                filteredChampions.forEach { champ ->
                    DropdownMenuItem(onClick = {
                        inputName = champ.name
                        isMenuExpanded = false
                    }) {
                        Text(text = champ.name)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                val foundChampion = allChampions.find { it.name.equals(inputName, ignoreCase = true) }
                if (foundChampion != null && !alreadyGuessed.contains(foundChampion)) {
                    onGuess(foundChampion)
                    inputName = ""
                    isMenuExpanded = false
                }
            },
            enabled = inputName.isNotBlank()
        ) {
            Text("Guess")
        }
    }
}

@Composable
fun featureBox(value: String, guessed: Boolean) {
    val bgColor = if (guessed) Color(0xFF2E7D32) else Color(0xFFC62828)

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(bgColor)
            .border(2.dp, Color.Black)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun guessingRow(selected: Champion, goal: Champion) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        featureBox(selected.name, selected.name == goal.name)
        featureBox(selected.species, selected.species == goal.species)
        featureBox(selected.region, selected.region == goal.region)
        featureBox(selected.lane, selected.lane == goal.lane)
        featureBox(selected.attackType, selected.attackType == goal.attackType)
        featureBox(selected.partype, selected.partype == goal.partype)
    }
}