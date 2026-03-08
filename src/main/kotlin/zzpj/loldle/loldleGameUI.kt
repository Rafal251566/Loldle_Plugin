package zzpj.loldle

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import kotlin.math.abs

@Composable
fun loldleGameUI(service: LoldleService) {
    val randomChampion = service.randomChampion ?: return
    val guesses = service.guesses
    val isVictory = service.isVictory

    val horizontalScrollState = rememberScrollState()
    val verticalLazyListState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    val headers = listOf("Name", "Gen", "Pos", "Spec", "Res", "Range", "Reg", "Year")
                    headers.forEach { header ->
                        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                            Text(header, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxHeight()) {
                    LazyColumn(
                        state = verticalLazyListState,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        items(guesses) { guess ->
                            championGuessRow(selected = guess, goal = randomChampion)
                        }
                    }
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(verticalLazyListState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )

            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(horizontalScrollState),
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(bottom = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isVictory) {
            VictoryPanel(randomChampion.championName) { service.startNewGame() }
        } else {
            autocompleteSearchComponent(
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
                .background(Color(0xFF2E7D32), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Wygrana! Champion to $name.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Button(onClick = onReset, modifier = Modifier.padding(top = 8.dp)) {
            Text("Zagraj ponownie")
        }
    }
}

@Composable
fun autocompleteSearchComponent(allChampions: List<Champion>, alreadyGuessed: List<Champion>, onGuess: (Champion) -> Unit) {
    var inputName by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val filtered = if (inputName.isBlank()) emptyList()
    else allChampions.filter {
        it.championName.contains(inputName, ignoreCase = true) &&
                !alreadyGuessed.any { g -> g.championName == it.championName }
    }.take(5)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            TextField(
                value = inputName,
                onValueChange = { inputName = it; isMenuExpanded = true },
                placeholder = { Text("Wpisz imię bohatera...") },
                singleLine = true,
                modifier = Modifier.width(200.dp)
            )
            DropdownMenu(
                expanded = isMenuExpanded && filtered.isNotEmpty(),
                onDismissRequest = { isMenuExpanded = false },
                properties = PopupProperties(focusable = false)
            ) {
                filtered.forEach { champ ->
                    DropdownMenuItem(onClick = {
                        inputName = champ.championName
                        isMenuExpanded = false
                    }) { Text(champ.championName) }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                val found = allChampions.find { it.championName.equals(inputName, ignoreCase = true) }
                if (found != null) {
                    onGuess(found)
                    inputName = ""
                }
            },
            enabled = inputName.isNotBlank()
        ) { Text("Zgadnij") }
    }
}

@Composable
fun championGuessRow(selected: Champion, goal: Champion) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        colorBox(selected.championName, goal.championName)
        colorBox(selected.gender, goal.gender)
        colorListBox(selected.positions, goal.positions)
        colorListBox(selected.species, goal.species)
        colorBox(selected.resource, goal.resource)
        colorListBox(selected.range_type, goal.range_type)
        colorListBox(selected.regions, goal.regions)
        colorYearBox(selected.releaseYear, goal.releaseYear)
    }
}

@Composable
fun colorBox(value: String, goal: String) {
    val bgColor = if (value == goal) Color(0xFF2E7D32) else Color(0xFFC62828)
    infoBox(value, bgColor)
}

@Composable
fun colorListBox(selectedList: List<String>, goalList: List<String>) {
    val bgColor = when {
        selectedList.sorted() == goalList.sorted() -> Color(0xFF2E7D32)
        selectedList.any { it in goalList } -> Color(0xFFFBC02D)
        else -> Color(0xFFC62828)
    }
    infoBox(selectedList.joinToString(", "), bgColor)
}

@Composable
fun colorYearBox(selectedYearStr: String, goalYearStr: String) {
    val selectedYear = selectedYearStr.toIntOrNull() ?: 0
    val goalYear = goalYearStr.toIntOrNull() ?: 0
    val diff = abs(selectedYear - goalYear)

    val bgColor = when {
        diff == 0 -> Color(0xFF2E7D32)
        diff <= 2 -> Color(0xFFFBC02D)
        else -> Color(0xFFC62828)
    }

    val arrow = if (selectedYear < goalYear) " ↑" else if (selectedYear > goalYear) " ↓" else ""
    infoBox(selectedYearStr + arrow, bgColor)
}

@Composable
fun infoBox(text: String, bgColor: Color) {
    Box(
        modifier = Modifier
            .size(80.dp, 60.dp)
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.2f))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}