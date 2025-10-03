package com.example.wum.domain.recommendation

import com.example.wum.FeatureResultMain.ChampionLol.ChampionInfo
import com.example.wum.FeatureResultMain.ChampionLol.Lane

/**
 * Mapper pour convertir les résultats de recommandation vers les modèles UI existants
 */
class RecommendationMapper {

    /**
     * Convertit une recommandation en ChampionInfo compatible avec l'UI existante
     */
    fun toChampionInfo(recommendation: Recommendation, targetLane: Lane? = null): ChampionInfo {
        val lane = if (targetLane != null && recommendation.champion.lanes.contains(targetLane)) {
            targetLane
        } else {
            recommendation.champion.lanes.first()
        }
        
        return ChampionInfo(
            id = recommendation.champion.id,
            displayName = recommendation.champion.name,
            lane = lane,
            portraitRes = getPortraitRes(recommendation.champion.id),
            frameRes = com.example.wum.R.drawable.cadre,
            laneIconRes = getLaneIconRes(lane)
        )
    }



    private fun getPortraitRes(championId: String): Int {
        val name = "champion_${championId}"
        val res = com.example.wum.R.drawable::class.java.fields.firstOrNull { it.name == name }
        return res?.getInt(null) ?: com.example.wum.R.drawable.ahri
    }

    private fun getLaneIconRes(lane: Lane): Int {
        return when (lane) {
            Lane.TOP -> com.example.wum.R.drawable.position_top
            Lane.JUNGLE -> com.example.wum.R.drawable.position_jungle
            Lane.MID -> com.example.wum.R.drawable.position_mid
            Lane.ADC -> com.example.wum.R.drawable.position_bot
            Lane.SUPPORT -> com.example.wum.R.drawable.position_support
        }
    }
}
