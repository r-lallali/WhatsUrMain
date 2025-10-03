package com.example.wum.data.champions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Modèles pour parser le fichier Data_champs.json (format Data Dragon)
 */
@Serializable
data class ChampionDataRoot(
    val data: Map<String, ChampionSummary>
)

@Serializable
data class ChampionSummary(
    val id: String,
    val name: String,
    val tags: List<String> = emptyList(),
    val partype: String = "",
    val info: Info
) {
    @Serializable
    data class Info(
        val attack: Int,
        val defense: Int,
        val magic: Int,
        val difficulty: Int
    )
}

/**
 * Repository pour charger les données des champions depuis Data_champs.json
 */
interface ChampionRepository {
    suspend fun loadAll(): List<ChampionSummary>
}

class LocalChampionRepository(
    private val assetProvider: com.example.wum.core.io.AssetProvider
) : ChampionRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cache: List<ChampionSummary>? = null

    override suspend fun loadAll(): List<ChampionSummary> {
        if (cache != null) return cache!!

        val jsonText = assetProvider.readText("Data_champs.json")
        val root = json.decodeFromString<ChampionDataRoot>(jsonText)
        cache = root.data.values.toList()
        return cache!!
    }
}
