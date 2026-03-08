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

    var randomSpell by mutableStateOf<Spell?>(null)
    var spellGuesses by mutableStateOf(listOf<String>())
    var isSpellVictory by mutableStateOf(false)

    init {
        startNewGame()
        startNewSpellGame()
    }

    fun startNewGame() {
        val allChamps = championRepository.getChampions()
        if (allChamps.isNotEmpty()) {
            randomChampion = allChamps.random()
            guesses = emptyList()
            isVictory = false
            println("Selected champion (Classic): ${randomChampion?.championName}")
        }
    }

    fun submitGuess(champion: Champion) {
        if (isVictory) return

        if (!guesses.any { it.championName == champion.championName }) {
            guesses = guesses + champion
        }

        if (champion.championName.equals(randomChampion?.championName, ignoreCase = true)) {
            isVictory = true
        }
    }

    fun startNewSpellGame() {
        val allSpells = spellRepository.getSpells()
        if (allSpells.isNotEmpty()) {
            randomSpell = allSpells.random()
            spellGuesses = emptyList()
            isSpellVictory = false
            println("Selected spell for (Spells): ${randomSpell?.champion}")
        }
    }

    fun submitSpellGuess(championName: String) {
        if (isSpellVictory) return

        if (!spellGuesses.contains(championName)) {
            spellGuesses = spellGuesses + championName
        }

        if (championName.equals(randomSpell?.champion, ignoreCase = true)) {
            isSpellVictory = true
        }
    }
}