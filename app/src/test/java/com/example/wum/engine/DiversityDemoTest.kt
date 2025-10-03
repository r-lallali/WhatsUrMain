package com.example.wum.engine

import com.example.wum.domain.recommendation.*
import com.example.wum.FeatureResultMain.ChampionLol.Lane
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Test de démonstration de l'amélioration de la diversité
 */
class DiversityDemoTest {

    @Test
    fun `demonstration of diversity improvement`() {
        val diversityManager = DiversityManager()
        
        // Simuler un ensemble de champions avec des scores proches
        val candidates = listOf(
            ChampionCandidate("Zed", "Zed", setOf(Lane.MID), ChampionFeatures.EMPTY) to 10.0,
            ChampionCandidate("Yasuo", "Yasuo", setOf(Lane.MID), ChampionFeatures.EMPTY) to 9.8,
            ChampionCandidate("Ahri", "Ahri", setOf(Lane.MID), ChampionFeatures.EMPTY) to 9.6,
            ChampionCandidate("Syndra", "Syndra", setOf(Lane.MID), ChampionFeatures.EMPTY) to 9.4,
            ChampionCandidate("LeBlanc", "LeBlanc", setOf(Lane.MID), ChampionFeatures.EMPTY) to 9.2,
            ChampionCandidate("Ekko", "Ekko", setOf(Lane.MID), ChampionFeatures.EMPTY) to 9.0
        )
        
        println("=== DÉMONSTRATION AMÉLIORATION DIVERSITÉ ===")
        println()
        
        // Simulation de plusieurs quiz avec les mêmes préférences
        val allRecommendations = mutableSetOf<String>()
        
        repeat(5) { round ->
            val recommendations = diversityManager.diverseSelection(candidates, 3)
            val championNames = recommendations.map { it.champion.name }
            
            println("Quiz #${round + 1}: ${championNames.joinToString(", ")}")
            allRecommendations.addAll(championNames)
        }
        
        println()
        println("Champions recommandés au total: ${allRecommendations.size} différents")
        println("Liste complète: ${allRecommendations.sorted().joinToString(", ")}")
        
        // Vérifier qu'on a bien de la diversité
        assertTrue(
            allRecommendations.size >= 4, 
            "On devrait avoir au moins 4 champions différents recommandés"
        )
        
        println()
        println("✅ Diversité améliorée ! Même profil utilisateur → Recommandations variées")
    }
    
    @Test
    fun `comparison old vs new system`() {
        println("=== COMPARAISON ANCIEN VS NOUVEAU SYSTÈME ===")
        println()
        
        // Simuler l'ancien système (toujours les 3 mêmes)
        val oldSystemResults = listOf(
            listOf("Zed", "Yasuo", "Ahri"),
            listOf("Zed", "Yasuo", "Ahri"), 
            listOf("Zed", "Yasuo", "Ahri"),
            listOf("Zed", "Yasuo", "Ahri"),
            listOf("Zed", "Yasuo", "Ahri")
        )

        println("ANCIEN SYSTÈME:")
        oldSystemResults.forEachIndexed { index, champions ->
            println("Quiz #${index + 1}: ${champions.joinToString(", ")}")
        }
        val oldUnique = oldSystemResults.flatten().toSet()
        println("Diversité: ${oldUnique.size} champions différents")
        
        println()
        println("NOUVEAU SYSTÈME:")
        
        // Simuler le nouveau système
        val diversityManager = DiversityManager()
        val candidates = listOf(
            ChampionCandidate("Zed", "Zed", setOf(Lane.MID), ChampionFeatures.EMPTY) to 10.0,
            ChampionCandidate("Yasuo", "Yasuo", setOf(Lane.MID), ChampionFeatures.EMPTY) to 9.8,
            ChampionCandidate("Ahri", "Ahri", setOf(Lane.MID), ChampionFeatures.EMPTY) to 9.6,
            ChampionCandidate("Syndra", "Syndra", setOf(Lane.MID), ChampionFeatures.EMPTY) to 9.4,
            ChampionCandidate("LeBlanc", "LeBlanc", setOf(Lane.MID), ChampionFeatures.EMPTY) to 9.2,
            ChampionCandidate("Ekko", "Ekko", setOf(Lane.MID), ChampionFeatures.EMPTY) to 9.0,
            ChampionCandidate("Katarina", "Katarina", setOf(Lane.MID), ChampionFeatures.EMPTY) to 8.8
        )
        
        val newSystemResults = mutableListOf<List<String>>()
        val allNewChampions = mutableSetOf<String>()
        
        repeat(5) { round ->
            val recommendations = diversityManager.diverseSelection(candidates, 3)
            val championNames = recommendations.map { it.champion.name }
            newSystemResults.add(championNames)
            allNewChampions.addAll(championNames)
            
            println("Quiz #${round + 1}: ${championNames.joinToString(", ")}")
        }
        
        println("Diversité: ${allNewChampions.size} champions différents")
        
        println()
        println("📈 AMÉLIORATION: ${allNewChampions.size - oldUnique.size} champions de plus découverts !")
    }
}
