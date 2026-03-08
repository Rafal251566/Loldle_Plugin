package zzpj.loldle

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

data class Spell(
    val champion: String,
    val key: String,
    val imageId: String,
    val type: String
)

object spellRepository {
    private var spells: List<Spell> = emptyList()

    fun getSpells(): List<Spell> {
        if (spells.isEmpty()) {
            loadSpells()
        }
        return spells
    }

    private fun loadSpells() {
        try {
            val inputStream = this::class.java.getResourceAsStream("/abilities.json")
            if (inputStream != null) {
                val reader = InputStreamReader(inputStream, "UTF-8")
                val listType = object : TypeToken<List<Spell>>() {}.type
                spells = Gson().fromJson(reader, listType)
                println("Sukces: Załadowano ${spells.size} umiejętności!")
            } else {
                println("Błąd: Nie znaleziono pliku spells.json!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}