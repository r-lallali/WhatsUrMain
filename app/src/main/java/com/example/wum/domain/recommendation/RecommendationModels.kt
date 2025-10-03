package com.example.wum.domain.recommendation

import com.example.wum.FeatureResultMain.ChampionLol.Lane

/**
 * Modes de questionnaire disponibles
 */
enum class Mode {
    SHORT,
    LONG,
    LANE
}

/**
 * Vecteur de caractéristiques bornées 0..2 (flags 0/1 pour certaines)
 * Basé sur les données disponibles dans champion.json (tags, info, partype)
 */
data class ChampionFeatures(
    val isRanged: Int,
    val dmgAD: Int,
    val dmgAP: Int,
    val dmgHybrid: Int,
    val complexity: Int,
    val mobility: Int,
    val hardCC: Int,
    val poke: Int,
    val burst: Int,
    val dps: Int,
    val durability: Int,
    val engage: Int,
    val peel: Int,
    val splitpush: Int,
    val teamfight: Int,
    val resourceMana: Int,
    val resourceEnergy: Int
) {
    companion object {
        val EMPTY = ChampionFeatures(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    }

    /**
     * Calcule le produit scalaire avec un autre vecteur de features
     */
    fun dotProduct(other: ChampionFeatures): Double {
        return (isRanged * other.isRanged +
                dmgAD * other.dmgAD +
                dmgAP * other.dmgAP +
                dmgHybrid * other.dmgHybrid +
                complexity * other.complexity +
                mobility * other.mobility +
                hardCC * other.hardCC +
                poke * other.poke +
                burst * other.burst +
                dps * other.dps +
                durability * other.durability +
                engage * other.engage +
                peel * other.peel +
                splitpush * other.splitpush +
                teamfight * other.teamfight +
                resourceMana * other.resourceMana +
                resourceEnergy * other.resourceEnergy).toDouble()
    }
}

/**
 * Données d'un champion candidat pour la recommandation
 */
data class ChampionCandidate(
    val id: String,
    val name: String,
    val lanes: Set<Lane>,
    val features: ChampionFeatures
)

data class StrongPrefs(
    val preferRanged: Boolean = false,
    val preferMelee: Boolean = false,
    val dislikeMana: Boolean = false,
    val preferSimple: Boolean = false,
    val requiredDamageType: String? = null, // "AD", "AP", "HYBRID"
    val requiredResourceType: String? = null // "MANA", "ENERGY", "NONE"
)

/**
 * Résultat d'une recommandation avec explication
 */
data class Recommendation(
    val champion: ChampionCandidate,
    val score: Double,
    val explanation: Explanation
)

/**
 * Explication détaillée d'une recommandation
 */
data class Explanation(
    val topUserSignals: List<String>,
    val matchingTraits: List<String>,
    val penalties: List<String>
)

/**
 * Entrée pour le système de recommandation
 */
data class RecommendationInput(
    val mode: Mode,
    val lane: Lane? = null,
    val answers: Map<String, List<String>>,
    val strongPrefs: StrongPrefs = StrongPrefs()
)
