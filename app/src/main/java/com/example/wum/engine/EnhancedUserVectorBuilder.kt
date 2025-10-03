package com.example.wum.engine

import com.example.wum.domain.recommendation.*
import com.example.wum.data.questions.AnswerOption
import kotlin.math.min

/**
 * Constructeur de vecteur utilisateur amélioré avec pondération adaptative
 * pour réduire la dominance de certaines features
 */
class EnhancedUserVectorBuilder {

    private val baseBuilder = UserVectorBuilder()

    /**
     * Construit le vecteur avec pondération adaptative pour éviter 
     * que certaines réponses dominent trop le scoring
     */
    fun buildUserVector(
        answers: Map<String, List<String>>,
        allOptions: Map<String, AnswerOption>
    ): ChampionFeatures {
        
        // Compter d'abord toutes les features comme avant
        val featureCounts = mutableMapOf<String, Int>().apply {
            put("isRanged", 0)
            put("dmgAD", 0)
            put("dmgAP", 0)
            put("dmgHybrid", 0)
            put("complexity", 0)
            put("mobility", 0)
            put("hardCC", 0)
            put("poke", 0)
            put("burst", 0)
            put("dps", 0)
            put("durability", 0)
            put("engage", 0)
            put("peel", 0)
            put("splitpush", 0)
            put("teamfight", 0)
            put("resourceMana", 0)
            put("resourceEnergy", 0)
        }

        answers.values.flatten().forEach { optionId ->
            allOptions[optionId]?.weights?.forEach { (feature, weight) ->
                featureCounts[feature] = (featureCounts[feature] ?: 0) + weight
            }
        }

        // Application de la pondération adaptative
        val adaptiveFeatures = applyAdaptiveWeighting(featureCounts)

        return ChampionFeatures(
            isRanged = min(adaptiveFeatures["isRanged"] ?: 0, 2),
            dmgAD = min(adaptiveFeatures["dmgAD"] ?: 0, 2),
            dmgAP = min(adaptiveFeatures["dmgAP"] ?: 0, 2),
            dmgHybrid = min(adaptiveFeatures["dmgHybrid"] ?: 0, 2),
            complexity = min(adaptiveFeatures["complexity"] ?: 0, 2),
            mobility = min(adaptiveFeatures["mobility"] ?: 0, 2),
            hardCC = min(adaptiveFeatures["hardCC"] ?: 0, 2),
            poke = min(adaptiveFeatures["poke"] ?: 0, 2),
            burst = min(adaptiveFeatures["burst"] ?: 0, 2),
            dps = min(adaptiveFeatures["dps"] ?: 0, 2),
            durability = min(adaptiveFeatures["durability"] ?: 0, 2),
            engage = min(adaptiveFeatures["engage"] ?: 0, 2),
            peel = min(adaptiveFeatures["peel"] ?: 0, 2),
            splitpush = min(adaptiveFeatures["splitpush"] ?: 0, 2),
            teamfight = min(adaptiveFeatures["teamfight"] ?: 0, 2),
            resourceMana = min(adaptiveFeatures["resourceMana"] ?: 0, 2),
            resourceEnergy = min(adaptiveFeatures["resourceEnergy"] ?: 0, 2)
        )
    }

    /**
     * Applique une pondération adaptative pour éviter qu'une seule feature domine
     * Réduit les valeurs très élevées et booste légèrement les valeurs moyennes
     */
    private fun applyAdaptiveWeighting(featureCounts: Map<String, Int>): Map<String, Int> {
        val maxValue = featureCounts.values.maxOrNull() ?: 0
        val avgValue = featureCounts.values.filter { it > 0 }.average()
        
        return featureCounts.mapValues { (_, value) ->
            when {
                // Réduire les valeurs très dominantes
                value >= maxValue && value > 3 -> (value * 0.7).toInt()
                // Booster légèrement les valeurs moyennes pour plus de nuance
                value > 0 && value < avgValue -> value + 1
                else -> value
            }
        }
    }

    /**
     * Délégation vers le builder de base pour les signaux utilisateur
     */
    fun getTopUserSignals(userVector: ChampionFeatures): List<String> {
        return baseBuilder.getTopUserSignals(userVector)
    }
}
