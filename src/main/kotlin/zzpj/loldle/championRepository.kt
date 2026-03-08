package zzpj.loldle

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

data class Champion(
    val championName: String,
    val gender: String,
    val positions: List<String>,
    val species: List<String>,
    val resource: String,
    val range_type: List<String>,
    val regions: List<String>,
    val release_date: String
) {
    val releaseYear: String
        get() = release_date.take(4)
}

object championRepository {
    private var champions: List<Champion> = emptyList()

    fun getChampions(): List<Champion> {
        if (champions.isEmpty()) {
            loadChampions()
        }
        return champions
    }

    private fun loadChampions() {
        try {
            val inputStream = this::class.java.getResourceAsStream("/champions.json")
            if (inputStream != null) {
                val reader = InputStreamReader(inputStream, "UTF-8")
                val listType = object : TypeToken<List<Champion>>() {}.type
                champions = Gson().fromJson(reader, listType)
                println("Success: Loaded ${champions.size} champions!")
            } else {
                println("Error: File champions.json not found!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}