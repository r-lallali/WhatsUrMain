package com.example.wum.engine

import com.example.wum.domain.recommendation.*
import com.example.wum.FeatureResultMain.ChampionLol.Lane

/**
 * Moteur de scoring pour calculer les recommandations de champions
 */
class ScoringEngine {

    /**
     * Calcule le score d'un champion pour un utilisateur donné
     * @param userVector Vecteur de préférences utilisateur
     * @param champion Champion candidat
     * @param strongPrefs Préférences fortes de l'utilisateur
     * @param targetLane Lane cible (null si pas de filtre)
     * @return Score calculé (peut être négatif avec les pénalités)
     */
    fun calculateScore(
        userVector: ChampionFeatures,
        champion: ChampionCandidate,
        strongPrefs: StrongPrefs,
        targetLane: Lane? = null
    ): Pair<Double, List<String>> {

        val penalties = mutableListOf<String>()

        if (targetLane != null && !champion.lanes.contains(targetLane)) {
            return -1000.0 to listOf("Champion ne joue pas cette lane")
        }

        // FILTRAGE STRICT pour préférences range/melee
        if (strongPrefs.preferRanged && champion.features.isRanged == 0) {
            return -1000.0 to listOf("Vous avez choisi 'à distance' mais ce champion est corps-à-corps")
        }
        if (strongPrefs.preferMelee && champion.features.isRanged == 1) {
            return -1000.0 to listOf("Vous avez choisi 'corps-à-corps' mais ce champion est à distance")
        }

        // FILTRAGE STRICT pour type de dégâts si spécifié
        strongPrefs.requiredDamageType?.let { required ->
            when (required) {
                "AD" -> if (champion.features.dmgAD == 0) {
                    return -1000.0 to listOf("Vous avez choisi dégâts physiques mais ce champion fait peu de dégâts AD")
                }
                "AP" -> if (champion.features.dmgAP == 0) {
                    return -1000.0 to listOf("Vous avez choisi dégâts magiques mais ce champion fait peu de dégâts AP")
                }
                "HYBRID" -> if (champion.features.dmgHybrid == 0) {
                    return -1000.0 to listOf("Vous avez choisi hybride mais ce champion n'est pas hybride")
                }
            }
        }

        // FILTRAGE STRICT pour type de ressource si spécifié
        strongPrefs.requiredResourceType?.let { required ->
            when (required) {
                "MANA" -> if (champion.features.resourceMana == 0) {
                    return -1000.0 to listOf("Vous avez choisi mana mais ce champion n'utilise pas de mana")
                }
                "ENERGY" -> if (champion.features.resourceEnergy == 0) {
                    return -1000.0 to listOf("Vous avez choisi énergie mais ce champion n'utilise pas d'énergie")
                }
                "NONE" -> if (champion.features.resourceMana == 1 || champion.features.resourceEnergy == 1) {
                    return -1000.0 to listOf("Vous avez choisi 'sans contrainte' mais ce champion utilise des ressources")
                }
            }
        }

        var baseScore = userVector.dotProduct(champion.features)

        if (strongPrefs.dislikeMana && champion.features.resourceMana == 1) {
            baseScore -= 2.0
            penalties.add("Vous n'aimez pas gérer la mana")
        }
        if (strongPrefs.preferSimple && champion.features.complexity == 2) {
            baseScore -= 2.0
            penalties.add("Vous préférez simple, ce champion est complexe")
        }

        return baseScore to penalties
    }

    /**
     * Génère les recommandations finales avec explications
     */
    fun generateRecommendations(
        userVector: ChampionFeatures,
        candidates: List<ChampionCandidate>,
        strongPrefs: StrongPrefs,
        targetLane: Lane? = null,
        topK: Int = 3,
        diversityManager: DiversityManager? = null
    ): List<Recommendation> {

        val userVectorBuilder = UserVectorBuilder()
        val topUserSignals = userVectorBuilder.getTopUserSignals(userVector)

        // Calcul des scores de base
        val candidatesWithScores = candidates.map { candidate ->
            val (score, penalties) = calculateScore(userVector, candidate, strongPrefs, targetLane)
            candidate to score
        }.filter { it.second > -100 } // Filtrer les champions vraiment incompatibles

        // Application de la diversité si le manager est fourni
        val finalRecommendations = if (diversityManager != null) {
            val diversifiedCandidates = diversityManager.applyDiversityBoost(
                candidatesWithScores.map { it.first },
                candidatesWithScores.map { it.second }
            )
            
            val selectedRecommendations = diversityManager.diverseSelection(diversifiedCandidates, topK)
            
            // Enregistrer les recommandations pour la prochaine fois
            selectedRecommendations.forEach { recommendation ->
                diversityManager.recordRecommendation(recommendation.champion.id)
            }
            
            // Générer les explications complètes
            selectedRecommendations.map { recommendation ->
                val explanation = generateExplanation(
                    userVector,
                    recommendation.champion,
                    topUserSignals,
                    emptyList() // Pas de pénalités pour les recommandations diversifiées
                )
                recommendation.copy(explanation = explanation)
            }
        } else {
            // Comportement original sans diversité
            candidatesWithScores
                .sortedByDescending { it.second }
                .take(topK)
                .map { (candidate, score) ->
                    val explanation = generateExplanation(
                        userVector,
                        candidate,
                        topUserSignals,
                        emptyList()
                    )
                    Recommendation(candidate, score, explanation)
                }
        }

        return finalRecommendations
    }

    /**
     * Génère une explication détaillée pour une recommandation
     */
    private fun generateExplanation(
        userVector: ChampionFeatures,
        champion: ChampionCandidate,
        topUserSignals: List<String>,
        penalties: List<String>
    ): Explanation {

        val matchingTraits = mutableListOf<String>()


        if (userVector.isRanged > 0 && champion.features.isRanged > 0) {
            matchingTraits.add("Champion à distance comme vous le souhaitez")
        }

        if (userVector.burst > 1 && champion.features.burst > 1) {
            matchingTraits.add("Fort potentiel de burst")
        }

        if (userVector.dps > 1 && champion.features.dps > 1) {
            matchingTraits.add("Excellent pour les dégâts soutenus")
        }

        if (userVector.durability > 1 && champion.features.durability > 1) {
            matchingTraits.add("Champion résistant")
        }

        if (userVector.hardCC > 1 && champion.features.hardCC > 1) {
            matchingTraits.add("Beaucoup de contrôle d'ennemis")
        }

        if (userVector.mobility > 1 && champion.features.mobility > 1) {
            matchingTraits.add("Champion mobile et agile")
        }

        if (userVector.teamfight > 1 && champion.features.teamfight > 1) {
            matchingTraits.add("Excellent en teamfight")
        }

        if (userVector.poke > 1 && champion.features.poke > 1) {
            matchingTraits.add("Bon pour le harcèlement à distance")
        }

        if (userVector.engage > 1 && champion.features.engage > 1) {
            matchingTraits.add("Capable d'initier les combats")
        }

        if (userVector.peel > 1 && champion.features.peel > 1) {
            matchingTraits.add("Protège bien les alliés")
        }


        val topMatchingTraits = matchingTraits.take(3)

        return Explanation(
            topUserSignals = topUserSignals,
            matchingTraits = topMatchingTraits,
            penalties = penalties
        )
    }
}
