package zzpj.loldle

import com.google.gson.Gson
import java.io.InputStreamReader


data class DataResponse(
    val type: String,
    val format: String,
    val version: String,
    val data: Map<String, Champion>
)

data class Champion(
    val id: String,
    val name: String,
    val species: String,
    val region: String,
    val lane: String,
    val attackType: String,
    val partype: String
)

object championRepository {
    private var champions: List<Champion> = emptyList()

    fun getChampions(): List<Champion> {
        if (champions.isEmpty()) {
            loadChampions()
        }
        return champions
    }

    fun loadChampions() {
        try {
            val inputStream = this::class.java.getResourceAsStream("/champions.json")
            if (inputStream != null) {
                val reader = InputStreamReader(inputStream, "UTF-8")
                val response = Gson().fromJson(reader, DataResponse::class.java)
                champions = response.data.values.toList()
                println("Succes: Loaded ${champions.size} champions!")
            } else {
                println("Error: File champions.json not found!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}