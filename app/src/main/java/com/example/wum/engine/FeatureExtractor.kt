package com.example.wum.engine

import com.example.wum.data.champions.ChampionSummary
import com.example.wum.domain.recommendation.ChampionFeatures
import kotlin.math.abs

/**
 * Extracteur de caractéristiques pour les champions
 * Convertit les données brutes (tags, info, partype) en vecteur de features bornées 0..2
 */
class FeatureExtractor {

    /**
     * Extrait les caractéristiques d'un champion depuis ses données brutes
     *
     * @param champion Les données du champion depuis Data_champs.json
     * @return Vecteur de caractéristiques normalisées 0..2
     */
    fun extract(champion: ChampionSummary): ChampionFeatures {
        return ChampionFeatures(
            isRanged = extractIsRanged(champion),
            dmgAD = extractDmgAD(champion),
            dmgAP = extractDmgAP(champion),
            dmgHybrid = extractDmgHybrid(champion),
            complexity = extractComplexity(champion),
            mobility = extractMobility(champion),
            hardCC = extractHardCC(champion),
            poke = extractPoke(champion),
            burst = extractBurst(champion),
            dps = extractDPS(champion),
            durability = extractDurability(champion),
            engage = extractEngage(champion),
            peel = extractPeel(champion),
            splitpush = extractSplitpush(champion),
            teamfight = extractTeamfight(champion),
            resourceMana = extractResourceMana(champion),
            resourceEnergy = extractResourceEnergy(champion)
        )
    }

    /**
     * Détermine si le champion est ranged (0=Melee, 1=Ranged)
     * Heuristique : tags contient "Marksman" ou "Mage" → souvent ranged
     */
    private fun extractIsRanged(champion: ChampionSummary): Int {
        val tags = champion.tags.map { it.lowercase() }
        return if (tags.contains("marksman") || tags.contains("mage")) 1 else 0
    }

    /**
     * Extraction des dégâts AD basés sur info.attack (0=Faible, 1=Moyen, 2=Fort)
     */
    private fun extractDmgAD(champion: ChampionSummary): Int {
        return when {
            champion.info.attack >= 7 -> 2
            champion.info.attack >= 4 -> 1
            else -> 0
        }
    }

    /**
     * Extraction des dégâts AP basés sur info.magic (0=Faible, 1=Moyen, 2=Fort)
     */
    private fun extractDmgAP(champion: ChampionSummary): Int {
        return when {
            champion.info.magic >= 7 -> 2
            champion.info.magic >= 4 -> 1
            else -> 0
        }
    }

    /**
     * Détermine si le champion fait des dégâts hybrides
     * Hybride si attack et magic sont proches (|diff| ≤ 1)
     */
    private fun extractDmgHybrid(champion: ChampionSummary): Int {
        val diff = abs(champion.info.attack - champion.info.magic)
        return when {
            diff <= 1 && champion.info.attack >= 4 && champion.info.magic >= 4 -> 2
            diff <= 2 && champion.info.attack >= 3 && champion.info.magic >= 3 -> 1
            else -> 0
        }
    }

    /**
     * Complexité basée sur info.difficulty (0=Simple, 1=Moyen, 2=Complexe)
     */
    private fun extractComplexity(champion: ChampionSummary): Int {
        return when {
            champion.info.difficulty >= 7 -> 2
            champion.info.difficulty >= 4 -> 1
            else -> 0
        }
    }

    /**
     * Mobilité basée sur les tags et heuristiques (0=Statique, 1=Mobile, 2=Très mobile)
     */
    private fun extractMobility(champion: ChampionSummary): Int {
        val tags = champion.tags.map { it.lowercase() }
        return when {
            tags.contains("assassin") -> 2
            tags.contains("fighter") && champion.info.attack >= 6 -> 1
            tags.contains("marksman") -> 1
            else -> 0
        }
    }

    /**
     * Hard CC basé sur les tags (0=Peu, 1=Moyen, 2=Beaucoup)
     */
    private fun extractHardCC(champion: ChampionSummary): Int {
        val tags = champion.tags.map { it.lowercase() }
        return when {
            tags.contains("tank") -> 2
            tags.contains("support") -> 1
            tags.contains("fighter") && champion.info.defense >= 6 -> 1
            else -> 0
        }
    }

    /**
     * Capacité de poke (0=Peu, 1=Moyen, 2=Fort)
     */
    private fun extractPoke(champion: ChampionSummary): Int {
        val isRanged = extractIsRanged(champion)
        val tags = champion.tags.map { it.lowercase() }
        return when {
            tags.contains("mage") && isRanged == 1 -> 2
            tags.contains("marksman") -> 1
            isRanged == 1 -> 1
            else -> 0
        }
    }

    /**
     * Capacité de burst (0=Peu, 1=Moyen, 2=Fort)
     */
    private fun extractBurst(champion: ChampionSummary): Int {
        val tags = champion.tags.map { it.lowercase() }
        return when {
            tags.contains("assassin") -> 2
            tags.contains("mage") -> 1
            else -> 0
        }
    }

    /**
     * DPS (dégâts soutenus) (0=Peu, 1=Moyen, 2=Fort)
     */
    private fun extractDPS(champion: ChampionSummary): Int {
        val tags = champion.tags.map { it.lowercase() }
        return when {
            tags.contains("marksman") -> 2
            tags.contains("fighter") -> 1
            else -> 0
        }
    }

    /**
     * Durabilité basée sur info.defense (0=Fragile, 1=Moyen, 2=Tanky)
     */
    private fun extractDurability(champion: ChampionSummary): Int {
        return when {
            champion.info.defense >= 7 -> 2
            champion.info.defense >= 4 -> 1
            else -> 0
        }
    }

    /**
     * Capacité d'engage (0=Peu, 1=Moyen, 2=Fort)
     */
    private fun extractEngage(champion: ChampionSummary): Int {
        val tags = champion.tags.map { it.lowercase() }
        return when {
            tags.contains("tank") -> 2
            tags.contains("fighter") && champion.info.defense >= 5 -> 1
            else -> 0
        }
    }

    /**
     * Capacité de peel/protection (0=Peu, 1=Moyen, 2=Fort)
     */
    private fun extractPeel(champion: ChampionSummary): Int {
        val tags = champion.tags.map { it.lowercase() }
        return when {
            tags.contains("support") -> 2
            tags.contains("tank") -> 1
            else -> 0
        }
    }

    /**
     * Capacité de splitpush (0=Peu, 1=Moyen, 2=Fort)
     */
    private fun extractSplitpush(champion: ChampionSummary): Int {
        val tags = champion.tags.map { it.lowercase() }
        return when {
            tags.contains("fighter") && champion.info.attack >= 7 -> 2
            tags.contains("assassin") -> 1
            else -> 0
        }
    }

    /**
     * Efficacité en teamfight (0=Peu, 1=Moyen, 2=Fort)
     */
    private fun extractTeamfight(champion: ChampionSummary): Int {
        val tags = champion.tags.map { it.lowercase() }
        return when {
            tags.contains("mage") && champion.info.magic >= 7 -> 2
            tags.contains("marksman") -> 2
            tags.contains("tank") || tags.contains("support") -> 1
            else -> 0
        }
    }

    /**
     * Utilise la mana comme ressource (0=Non, 1=Oui)
     */
    private fun extractResourceMana(champion: ChampionSummary): Int {
        return if (champion.partype.lowercase() == "mana") 1 else 0
    }

    /**
     * Utilise l'énergie comme ressource (0=Non, 1=Oui)
     */
    private fun extractResourceEnergy(champion: ChampionSummary): Int {
        return if (champion.partype.lowercase() == "energy") 1 else 0
    }
}
