package com.example.wum.engine

import com.example.wum.domain.recommendation.*
import com.example.wum.FeatureResultMain.ChampionLol.Lane
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour le DiversityManager
 */
class DiversityManagerTest {

    private lateinit var diversityManager: DiversityManager

    @Before
    fun setUp() {
        diversityManager = DiversityManager()
    }

    @Test
    fun `test diversity boost penalizes recent recommendations`() {
        // Simuler quelques champions
        val zed = ChampionCandidate("Zed", "Zed", setOf(Lane.MID), ChampionFeatures.EMPTY)
        val ahri = ChampionCandidate("Ahri", "Ahri", setOf(Lane.MID), ChampionFeatures.EMPTY)
        val yasuo = ChampionCandidate("Yasuo", "Yasuo", setOf(Lane.MID), ChampionFeatures.EMPTY)
        
        val candidates = listOf(zed, ahri, yasuo)
        val scores = listOf(10.0, 9.0, 8.0) // Zed a le meilleur score de base
        
        // Enregistrer Zed comme récemment recommandé
        diversityManager.recordRecommendation("Zed")
        
        // Appliquer le boost de diversité
        val boostedCandidates = diversityManager.applyDiversityBoost(candidates, scores)
        
        // Zed devrait avoir un score pénalisé
        val zedBoosted = boostedCandidates.find { it.first.id == "Zed" }
        val ahriOriginal = boostedCandidates.find { it.first.id == "Ahri" }
        
        assertNotNull(zedBoosted)
        assertNotNull(ahriOriginal)
        assertTrue("Zed devrait être pénalisé", zedBoosted!!.second < 10.0)
        assertEquals("Ahri ne devrait pas être pénalisée", 9.0, ahriOriginal!!.second, 0.01)
    }

    @Test
    fun `test diverse selection includes randomness`() {
        // Créer plusieurs candidats avec des scores similaires
        val candidates = (1..10).map { i ->
            ChampionCandidate("Champ$i", "Champion $i", setOf(Lane.MID), ChampionFeatures.EMPTY) to (10.0 - i * 0.1)
        }
        
        // Faire plusieurs sélections
        val selections = mutableSetOf<String>()
        repeat(50) {
            val recommendations = diversityManager.diverseSelection(candidates, 3)
            selections.add(recommendations.map { it.champion.id }.sorted().joinToString(","))
        }
        
        // On devrait avoir plusieurs combinaisons différentes (pas toujours les 3 mêmes)
        assertTrue("La sélection devrait être variée", selections.size > 1)
    }

    @Test
    fun `test clear history resets penalties`() {
        val zed = ChampionCandidate("Zed", "Zed", setOf(Lane.MID), ChampionFeatures.EMPTY)
        val candidates = listOf(zed)
        val scores = listOf(10.0)
        
        // Enregistrer et vérifier la pénalité
        diversityManager.recordRecommendation("Zed")
        val penalized = diversityManager.applyDiversityBoost(candidates, scores)
        assertTrue("Zed devrait être pénalisé", penalized[0].second < 10.0)
        
        // Effacer l'historique
        diversityManager.clearHistory()
        val afterClear = diversityManager.applyDiversityBoost(candidates, scores)
        assertEquals("Après clear, pas de pénalité", 10.0, afterClear[0].second, 0.01)
    }
}
