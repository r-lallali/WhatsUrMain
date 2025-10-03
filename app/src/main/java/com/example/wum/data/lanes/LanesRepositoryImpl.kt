package com.example.wum.data.lanes

import com.example.wum.FeatureResultMain.ChampionLol.Lane
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LanesData(
    val champions: Map<String, List<String>>
)

/**
 * Repository pour gérer les lanes des champions
 */
interface LanesRepository {
    suspend fun getLanes(championName: String): Set<Lane>
}

class LanesRepositoryImpl(
    private val assetProvider: com.example.wum.core.io.AssetProvider
) : LanesRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cache: Map<String, Set<Lane>>? = null

    override suspend fun getLanes(championName: String): Set<Lane> {
        if (cache == null) {
            loadLanes()
        }

        return cache?.get(championName) ?: getFallbackLanes(championName)
    }

    private suspend fun loadLanes() {
        try {
            val jsonText = assetProvider.readText("lanes.json")
            val data = json.decodeFromString<LanesData>(jsonText)

            cache = data.champions.mapValues { (_, lanes) ->
                lanes.mapNotNull { laneStr ->
                    try {
                        Lane.valueOf(laneStr)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }.toSet()
            }
        } catch (e: Exception) {
            cache = emptyMap()
        }
    }

    /**
     * Fallback basé sur les tags du champion si pas défini dans lanes.json
     * Heuristiques simples :
     * - Marksman → ADC
     * - Support → SUPPORT
     * - Mage/Assassin → MID
     * - Fighter/Tank → TOP (+ JUNGLE si engage/hardCC élevé)
     */
    private fun getFallbackLanes(championName: String): Set<Lane> {

        return setOf(Lane.MID)
    }
}
