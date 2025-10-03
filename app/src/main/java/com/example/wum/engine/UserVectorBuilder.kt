package com.example.wum.engine

import com.example.wum.domain.recommendation.*
import com.example.wum.data.questions.AnswerOption
import kotlin.math.min

/**
 * Constructeur de vecteur utilisateur à partir des réponses au questionnaire
 */
class UserVectorBuilder {

    /**
     * Construit le vecteur de préférences utilisateur à partir des réponses
     * @param answers Map des réponses : questionId -> liste des optionIds sélectionnées
     * @param allOptions Map de toutes les options disponibles : optionId -> AnswerOption
     * @return Vecteur de features utilisateur borné 0..3
     */
    fun buildUserVector(
        answers: Map<String, List<String>>,
        allOptions: Map<String, AnswerOption>
    ): ChampionFeatures {

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


        return ChampionFeatures(
            isRanged = min(featureCounts["isRanged"] ?: 0, 3),
            dmgAD = min(featureCounts["dmgAD"] ?: 0, 3),
            dmgAP = min(featureCounts["dmgAP"] ?: 0, 3),
            dmgHybrid = min(featureCounts["dmgHybrid"] ?: 0, 3),
            complexity = min(featureCounts["complexity"] ?: 0, 3),
            mobility = min(featureCounts["mobility"] ?: 0, 3),
            hardCC = min(featureCounts["hardCC"] ?: 0, 3),
            poke = min(featureCounts["poke"] ?: 0, 3),
            burst = min(featureCounts["burst"] ?: 0, 3),
            dps = min(featureCounts["dps"] ?: 0, 3),
            durability = min(featureCounts["durability"] ?: 0, 3),
            engage = min(featureCounts["engage"] ?: 0, 3),
            peel = min(featureCounts["peel"] ?: 0, 3),
            splitpush = min(featureCounts["splitpush"] ?: 0, 3),
            teamfight = min(featureCounts["teamfight"] ?: 0, 3),
            resourceMana = min(featureCounts["resourceMana"] ?: 0, 3),
            resourceEnergy = min(featureCounts["resourceEnergy"] ?: 0, 3)
        )
    }

    /**
     * Identifie les 3 signaux utilisateur les plus forts
     */
    fun getTopUserSignals(userVector: ChampionFeatures): List<String> {
        val signals = listOf(
            "Combat à distance" to userVector.isRanged,
            "Dégâts AD" to userVector.dmgAD,
            "Dégâts AP" to userVector.dmgAP,
            "Dégâts hybrides" to userVector.dmgHybrid,
            "Complexité" to userVector.complexity,
            "Mobilité" to userVector.mobility,
            "Contrôle dur" to userVector.hardCC,
            "Poke" to userVector.poke,
            "Burst" to userVector.burst,
            "DPS soutenu" to userVector.dps,
            "Durabilité" to userVector.durability,
            "Engage" to userVector.engage,
            "Protection" to userVector.peel,
            "Splitpush" to userVector.splitpush,
            "Teamfight" to userVector.teamfight
        )

        return signals
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
}
