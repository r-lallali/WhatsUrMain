package com.example.wum.engine

import com.example.wum.data.champions.ChampionSummary
import com.example.wum.domain.recommendation.ChampionFeatures
import kotlin.math.abs
import kotlin.random.Random

/**
 * Extracteur de features amélioré avec randomisation contrôlée pour plus de diversité
 */
class EnhancedFeatureExtractor {

    private val baseExtractor = FeatureExtractor()
    private val randomVariationRange = 0.3 // 30% de variation aléatoire

    /**
     * Extrait les features avec une légère randomisation pour éviter 
     * des recommendations trop prévisibles
     */
    fun extract(champion: ChampionSummary): ChampionFeatures {
        val baseFeatures = baseExtractor.extract(champion)
        
        // Ajout d'une variation aléatoire légère pour casser les patterns trop rigides
        return if (shouldApplyVariation()) {
            applyRandomVariation(baseFeatures)
        } else {
            baseFeatures
        }
    }

    /**
     * Détermine si on doit appliquer une variation (20% du temps)
     */
    private fun shouldApplyVariation(): Boolean {
        return Random.nextDouble() < 0.2
    }

    /**
     * Applique une variation aléatoire légère aux features
     * Cela permet d'introduire de la diversité sans casser la logique
     */
    private fun applyRandomVariation(features: ChampionFeatures): ChampionFeatures {
        fun varyFeature(value: Int): Int {
            if (value == 0) return 0 // Ne pas modifier les valeurs nulles
            
            val variation = Random.nextDouble(-randomVariationRange, randomVariationRange)
            val newValue = (value + variation).toInt()
            return newValue.coerceIn(0, 2) // Garder dans les bornes 0..2
        }

        return ChampionFeatures(
            isRanged = features.isRanged, // Ne pas varier les features binaires critiques
            dmgAD = varyFeature(features.dmgAD),
            dmgAP = varyFeature(features.dmgAP),
            dmgHybrid = features.dmgHybrid,
            complexity = features.complexity,
            mobility = varyFeature(features.mobility),
            hardCC = varyFeature(features.hardCC),
            poke = varyFeature(features.poke),
            burst = varyFeature(features.burst),
            dps = varyFeature(features.dps),
            durability = varyFeature(features.durability),
            engage = varyFeature(features.engage),
            peel = varyFeature(features.peel),
            splitpush = varyFeature(features.splitpush),
            teamfight = varyFeature(features.teamfight),
            resourceMana = features.resourceMana,
            resourceEnergy = features.resourceEnergy
        )
    }
}
