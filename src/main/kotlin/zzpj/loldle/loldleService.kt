package zzpj.loldle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class LoldleService(val project: Project) {

    var randomChampion by mutableStateOf<Champion?>(null)
    var guesses by mutableStateOf(listOf<Champion>())
    var isVictory by mutableStateOf(false)

    init {
        startNewGame()
    }

    fun startNewGame() {
        val allChamps = championRepository.getChampions()
        if (allChamps.isNotEmpty()) {
            randomChampion = allChamps.random()
            guesses = emptyList()
            isVictory = false
            println("Selected champion: ${randomChampion?.name}")
        }
    }

    fun submitGuess(champion: Champion) {
        if (isVictory) return

        guesses = guesses + champion
        if (champion.name.equals(randomChampion?.name, ignoreCase = true)) {
            isVictory = true
        }
    }
}