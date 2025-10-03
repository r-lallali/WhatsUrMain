package com.example.wum.engine

import com.example.wum.domain.recommendation.*
import kotlin.random.Random

/**
 * Gestionnaire de diversité pour éviter les recommandations répétitives
 */
class DiversityManager {
    
    // Stockage en mémoire des recommandations récentes (peut être étendu avec SharedPreferences)
    private val recentRecommendations = mutableListOf<String>()
    private val maxRecentHistory = 10
    
    /**
     * Applique la diversification aux candidats avant le scoring
     * - Boost les champions peu recommandés récemment
     * - Pénalise légèrement les champions souvent recommandés
     */
    fun applyDiversityBoost(
        candidates: List<ChampionCandidate>, 
        scores: List<Double>
    ): List<Pair<ChampionCandidate, Double>> {
        
        return candidates.zip(scores).map { (candidate, baseScore) ->
            val penaltyFactor = when {
                recentRecommendations.contains(candidate.id) -> {
                    val recentIndex = recentRecommendations.indexOf(candidate.id)
                    // Plus c'est récent, plus la pénalité est forte
                    val recencyPenalty = (maxRecentHistory - recentIndex) * 0.1
                    1.0 - recencyPenalty
                }
                else -> 1.0 // Pas de pénalité
            }
            
            val adjustedScore = baseScore * penaltyFactor
            candidate to adjustedScore
        }
    }
    
    /**
     * Sélection diversifiée avec randomisation contrôlée
     * Au lieu de prendre les 3 meilleurs, on prend dans le top 6-8 avec de la randomisation
     */
    fun diverseSelection(
        candidatesWithScores: List<Pair<ChampionCandidate, Double>>,
        topK: Int = 3
    ): List<Recommendation> {
        
        val sortedCandidates = candidatesWithScores
            .sortedByDescending { it.second }
            .filter { it.second > -50.0 } // Filtrer les très mauvais scores
        
        if (sortedCandidates.size <= topK) {
            return sortedCandidates.map { (candidate, score) ->
                createRecommendation(candidate, score)
            }
        }
        
        // Sélection pondérée : on favorise les bons scores mais on laisse de la place au hasard
        val selectedCandidates = mutableListOf<Pair<ChampionCandidate, Double>>()
        val availableCandidates = sortedCandidates.toMutableList()
        
        repeat(topK) {
            if (availableCandidates.isEmpty()) return@repeat
            
            // Sélection pondérée : les 3 premiers ont plus de chances d'être sélectionnés
            val weights = availableCandidates.mapIndexed { index, _ ->
                when (index) {
                    0 -> 40 // 40% de chance pour le premier
                    1 -> 30 // 30% pour le deuxième  
                    2 -> 20 // 20% pour le troisième
                    else -> maxOf(1, 10 - index) // Chances dégressives pour les autres
                }
            }
            
            val selected = weightedRandomSelection(availableCandidates, weights)
            selectedCandidates.add(selected)
            availableCandidates.remove(selected)
        }
        
        return selectedCandidates.map { (candidate, score) ->
            createRecommendation(candidate, score)
        }
    }
    
    /**
     * Sélection aléatoire pondérée
     */
    private fun weightedRandomSelection(
        candidates: List<Pair<ChampionCandidate, Double>>,
        weights: List<Int>
    ): Pair<ChampionCandidate, Double> {
        val totalWeight = weights.sum()
        val randomValue = Random.nextInt(totalWeight)
        
        var currentWeight = 0
        weights.forEachIndexed { index, weight ->
            currentWeight += weight
            if (randomValue < currentWeight) {
                return candidates[index]
            }
        }
        
        return candidates.first() // Fallback
    }
    
    /**
     * Enregistre une recommandation pour la prise en compte future
     */
    fun recordRecommendation(championId: String) {
        recentRecommendations.add(0, championId) // Ajouter en tête
        
        // Limiter la taille de l'historique
        if (recentRecommendations.size > maxRecentHistory) {
            recentRecommendations.removeAt(recentRecommendations.size - 1)
        }
    }
    
    /**
     * Réinitialise l'historique (utile pour les tests ou nouveau compte)
     */
    fun clearHistory() {
        recentRecommendations.clear()
    }
    
    private fun createRecommendation(
        candidate: ChampionCandidate, 
        score: Double
    ): Recommendation {
        return Recommendation(
            champion = candidate,
            score = score,
            explanation = Explanation(
                topUserSignals = emptyList(),
                matchingTraits = listOf("Champion diversifié pour éviter la répétition"),
                penalties = emptyList()
            )
        )
    }
}
