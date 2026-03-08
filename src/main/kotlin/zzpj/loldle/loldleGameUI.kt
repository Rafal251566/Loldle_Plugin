package zzpj.loldle

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.TooltipArea
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
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
                    val headers = listOf("Bohater", "Płeć", "Pozycja", "Gatunek", "Zasoby", "Typ zasięgu", "Region", "Rok wydania")
                    headers.forEach { header ->
                        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                            Text(header, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color.White)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassicHelpTooltip() {
    TooltipArea(
        tooltip = {
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E2328), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFC8AA6E), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Jak grać?", color = Color(0xFFC8AA6E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("1. Wpisz nazwę bohatera z League of Legends.", color = Color.White, fontSize = 12.sp)
                    Text("2. Zielony: Dokładne trafienie.", color = Color.White, fontSize = 12.sp)
                    Text("3. Pomarańczowy: Częściowe trafienie (np. 1 z 2 ról).", color = Color.White, fontSize = 12.sp)
                    Text("4. Czerwony: Pudło.", color = Color.White, fontSize = 12.sp)
                    Text("5. Strzałki: Rok wydania bohatera jest starszy (↓) lub nowszy (↑).", color = Color.White, fontSize = 12.sp)
                }
            }
        },
        delayMillis = 200
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color(0xFF0A323C), shape = CircleShape)
                .border(2.dp, Color(0xFFC8AA6E), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("?", color = Color(0xFFC8AA6E), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
    }
}

@Composable
fun VictoryPanel(name: String, onReset: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF091428), Color(0xFF0A323C))
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(2.dp, Color(0xFFC8AA6E), RoundedCornerShape(8.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("🎉 WYGRANA! To jest $name.", color = Color(0xFFF0E6D2), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Button(
            onClick = onReset,
            modifier = Modifier.padding(top = 12.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF1E2328),
                contentColor = Color(0xFFC8AA6E)
            )
        ) {
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
        ClassicHelpTooltip()
        Spacer(modifier = Modifier.width(12.dp))

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
            enabled = inputName.isNotBlank(),
            modifier = Modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF0A323C),
                contentColor = Color(0xFFF0E6D2),
                disabledBackgroundColor = Color(0xFF333333),
                disabledContentColor = Color.Gray
            )
        ) { Text("Zgadnij") }
    }
}

@Composable
fun championGuessRow(selected: Champion, goal: Champion) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        championImageBox(selected.championName, selected.championName.equals(goal.championName, ignoreCase = true))
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
    val bgColor = if (value.equals(goal, ignoreCase = true)) Color(0xFF188038) else Color(0xFF9E2A2B)
    infoBox(value, bgColor)
}

@Composable
fun colorListBox(selectedList: List<String>, goalList: List<String>) {
    val isExactMatch = selectedList.containsAll(goalList) && goalList.containsAll(selectedList)
    val hasPartialMatch = selectedList.any { it in goalList }

    val bgColor = when {
        isExactMatch -> Color(0xFF188038)
        hasPartialMatch -> Color(0xFFD35400)
        else -> Color(0xFF9E2A2B)
    }
    infoBox(selectedList.joinToString(",\n"), bgColor)
}

@Composable
fun colorYearBox(selectedYearStr: String, goalYearStr: String) {
    val selectedYear = selectedYearStr.toIntOrNull() ?: 0
    val goalYear = goalYearStr.toIntOrNull() ?: 0

    val bgColor = when {
        selectedYear == goalYear -> Color(0xFF188038)
        else -> Color(0xFF9E2A2B)
    }

    val arrow = if (selectedYear < goalYear) " ↑" else if (selectedYear > goalYear) " ↓" else ""
    infoBox(selectedYearStr + arrow, bgColor)
}

@Composable
fun infoBox(text: String, baseColor: Color) {
    val darkerColor = Color(
        red = (baseColor.red * 0.7f).coerceIn(0f, 1f),
        green = (baseColor.green * 0.7f).coerceIn(0f, 1f),
        blue = (baseColor.blue * 0.7f).coerceIn(0f, 1f),
        alpha = baseColor.alpha
    )

    Box(
        modifier = Modifier
            .size(80.dp, 80.dp)
            .background(
                brush = Brush.verticalGradient(listOf(baseColor, darkerColor)),
                shape = RoundedCornerShape(6.dp)
            )
            .border(1.dp, Color(0x60000000), shape = RoundedCornerShape(6.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            style = LocalTextStyle.current.copy(
                shadow = Shadow(color = Color(0x80000000), blurRadius = 3f, offset = Offset(2f, 2f))
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun championImageBox(championName: String, isGoal: Boolean) {
    val borderColor = if (isGoal) Color(0xFF2E7D32) else Color(0xFFC62828)
    val safeName = championName.replace(" ", "").replace("'", "")
    val url = "https://ddragon.leagueoflegends.com/cdn/16.5.1/img/champion/$safeName.png"
    val fallbackUrl = "https://ddragon.leagueoflegends.com/cdn/10.18.1/img/profileicon/588.png"
    var image by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        image = withContext(Dispatchers.IO) {
            try {
                URL(url).openStream().use { loadImageBitmap(it) }
            } catch (e: Exception) {
                try {
                    URL(fallbackUrl).openStream().use { loadImageBitmap(it) }
                } catch (fallbackEx: Exception) {
                    null
                }
            }
        }
    }

    TooltipArea(
        tooltip = {
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(4.dp))
                    .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF555555), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(championName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        delayMillis = 300
    ) {
        Box(
            modifier = Modifier
                .size(80.dp, 80.dp)
                .background(Color.DarkGray, shape = RoundedCornerShape(6.dp))
                .border(3.dp, borderColor, shape = RoundedCornerShape(6.dp))
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                Image(
                    bitmap = image!!,
                    contentDescription = championName,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(championName, color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
            }
        }
    }
}