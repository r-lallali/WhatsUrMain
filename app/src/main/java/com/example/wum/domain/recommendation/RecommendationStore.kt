package com.example.wum.domain.recommendation

import com.example.wum.FeatureResultMain.ChampionLol.Lane

/**
 * Stockage temporaire (in-memory) de la dernière recommandation à afficher
 * Permet de simplifier le passage Quiz -> Résultat sans sérialisation lourde.
 */
object RecommendationStore {
    var current: Recommendation? = null
    var targetLane: Lane? = null
    
    fun set(recommendation: Recommendation, lane: Lane? = null) {
        current = recommendation
        targetLane = lane
    }
    
    fun clear() { 
        current = null 
        targetLane = null
    }
}
