package com.example.wum.domain.recommendation

import com.example.wum.FeatureResultMain.ChampionLol.Lane
import com.example.wum.engine.ScoringEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests pour vérifier le filtrage strict des préférences
 */
class StrictFilteringTest {

    private lateinit var scoringEngine: ScoringEngine

    @Before
    fun setUp() {
        scoringEngine = ScoringEngine()
    }

    @Test
    fun `test strict melee preference filters out ranged champions`() {
        // Utilisateur qui préfère strictement corps-à-corps
        val userVector = ChampionFeatures.EMPTY
        val strongPrefs = StrongPrefs(preferMelee = true)

        // Champion à distance (comme Ashe)
        val rangedChampion = ChampionCandidate(
            id = "Ashe",
            name = "Ashe",
            lanes = setOf(Lane.ADC),
            features = ChampionFeatures(
                isRanged = 1, // Champion à distance
                dmgAD = 2, dmgAP = 0, dmgHybrid = 0, complexity = 0, mobility = 0,
                hardCC = 0, poke = 1, burst = 0, dps = 2, durability = 0,
                engage = 0, peel = 0, splitpush = 0, teamfight = 1,
                resourceMana = 1, resourceEnergy = 0
            )
        )

        val (score, penalties) = scoringEngine.calculateScore(
            userVector, rangedChampion, strongPrefs
        )

        // Le champion à distance devrait être rejeté avec un score très négatif
        assertEquals(-1000.0, score, 0.001)
        assertTrue("Should have penalty for being ranged", 
            penalties.any { it.contains("corps-à-corps") })
    }

    @Test
    fun `test strict ranged preference filters out melee champions`() {
        // Utilisateur qui préfère strictement à distance
        val userVector = ChampionFeatures.EMPTY
        val strongPrefs = StrongPrefs(preferRanged = true)

        // Champion corps-à-corps (comme Malphite)
        val meleeChampion = ChampionCandidate(
            id = "Malphite",
            name = "Malphite",
            lanes = setOf(Lane.TOP),
            features = ChampionFeatures(
                isRanged = 0, // Champion corps-à-corps
                dmgAD = 1, dmgAP = 1, dmgHybrid = 0, complexity = 0, mobility = 0,
                hardCC = 2, poke = 0, burst = 0, dps = 0, durability = 2,
                engage = 2, peel = 0, splitpush = 0, teamfight = 1,
                resourceMana = 1, resourceEnergy = 0
            )
        )

        val (score, penalties) = scoringEngine.calculateScore(
            userVector, meleeChampion, strongPrefs
        )

        // Le champion corps-à-corps devrait être rejeté avec un score très négatif
        assertEquals(-1000.0, score, 0.001)
        assertTrue("Should have penalty for being melee", 
            penalties.any { it.contains("à distance") })
    }

    @Test
    fun `test strict AD preference filters out AP champions`() {
        // Utilisateur qui préfère strictement dégâts physiques
        val userVector = ChampionFeatures.EMPTY
        val strongPrefs = StrongPrefs(requiredDamageType = "AD")

        // Champion AP (comme Annie)
        val apChampion = ChampionCandidate(
            id = "Annie",
            name = "Annie",
            lanes = setOf(Lane.MID),
            features = ChampionFeatures(
                isRanged = 1, dmgAD = 0, dmgAP = 2, dmgHybrid = 0, // Que de l'AP
                complexity = 1, mobility = 0, hardCC = 1, poke = 1,
                burst = 2, dps = 0, durability = 0, engage = 0,
                peel = 0, splitpush = 0, teamfight = 2,
                resourceMana = 1, resourceEnergy = 0
            )
        )

        val (score, penalties) = scoringEngine.calculateScore(
            userVector, apChampion, strongPrefs
        )

        // Le champion AP devrait être rejeté
        assertEquals(-1000.0, score, 0.001)
        assertTrue("Should have penalty for not being AD", 
            penalties.any { it.contains("dégâts physiques") })
    }

    @Test
    fun `test strict AP preference filters out AD champions`() {
        // Utilisateur qui préfère strictement dégâts magiques
        val userVector = ChampionFeatures.EMPTY
        val strongPrefs = StrongPrefs(requiredDamageType = "AP")

        // Champion AD (comme Draven)
        val adChampion = ChampionCandidate(
            id = "Draven",
            name = "Draven",
            lanes = setOf(Lane.ADC),
            features = ChampionFeatures(
                isRanged = 1, dmgAD = 2, dmgAP = 0, dmgHybrid = 0, // Que de l'AD
                complexity = 2, mobility = 1, hardCC = 0, poke = 0,
                burst = 1, dps = 2, durability = 0, engage = 0,
                peel = 0, splitpush = 1, teamfight = 1,
                resourceMana = 1, resourceEnergy = 0
            )
        )

        val (score, penalties) = scoringEngine.calculateScore(
            userVector, adChampion, strongPrefs
        )

        // Le champion AD devrait être rejeté
        assertEquals(-1000.0, score, 0.001)
        assertTrue("Should have penalty for not being AP", 
            penalties.any { it.contains("dégâts magiques") })
    }

    @Test
    fun `test strict mana preference filters out energy and no-resource champions`() {
        // Utilisateur qui préfère strictement mana
        val userVector = ChampionFeatures.EMPTY
        val strongPrefs = StrongPrefs(requiredResourceType = "MANA")

        // Champion énergie (comme Zed)
        val energyChampion = ChampionCandidate(
            id = "Zed",
            name = "Zed",
            lanes = setOf(Lane.MID),
            features = ChampionFeatures(
                isRanged = 0, dmgAD = 2, dmgAP = 0, dmgHybrid = 0, complexity = 2,
                mobility = 2, hardCC = 0, poke = 0, burst = 2, dps = 0,
                durability = 0, engage = 0, peel = 0, splitpush = 1, teamfight = 0,
                resourceMana = 0, resourceEnergy = 1 // Utilise énergie
            )
        )

        val (score, penalties) = scoringEngine.calculateScore(
            userVector, energyChampion, strongPrefs
        )

        // Le champion énergie devrait être rejeté avec un score très négatif
        assertEquals(-1000.0, score, 0.001)
        assertTrue("Should have penalty for not using mana", 
            penalties.any { it.contains("mana") })
    }

    @Test
    fun `test strict energy preference filters out mana and no-resource champions`() {
        // Utilisateur qui préfère strictement énergie
        val userVector = ChampionFeatures.EMPTY
        val strongPrefs = StrongPrefs(requiredResourceType = "ENERGY")

        // Champion mana (comme Annie)
        val manaChampion = ChampionCandidate(
            id = "Annie",
            name = "Annie",
            lanes = setOf(Lane.MID),
            features = ChampionFeatures(
                isRanged = 1, dmgAD = 0, dmgAP = 2, dmgHybrid = 0, complexity = 1,
                mobility = 0, hardCC = 1, poke = 1, burst = 2, dps = 0,
                durability = 0, engage = 0, peel = 0, splitpush = 0, teamfight = 2,
                resourceMana = 1, resourceEnergy = 0 // Utilise mana
            )
        )

        val (score, penalties) = scoringEngine.calculateScore(
            userVector, manaChampion, strongPrefs
        )

        // Le champion mana devrait être rejeté
        assertEquals(-1000.0, score, 0.001)
        assertTrue("Should have penalty for not using energy", 
            penalties.any { it.contains("énergie") })
    }

    @Test
    fun `test strict no-resource preference filters out mana and energy champions`() {
        // Utilisateur qui préfère strictement sans contrainte
        val userVector = ChampionFeatures.EMPTY
        val strongPrefs = StrongPrefs(requiredResourceType = "NONE")

        // Champion mana (comme Ziggs)
        val manaChampion = ChampionCandidate(
            id = "Ziggs",
            name = "Ziggs",
            lanes = setOf(Lane.MID),
            features = ChampionFeatures(
                isRanged = 1, dmgAD = 0, dmgAP = 2, dmgHybrid = 0, complexity = 1,
                mobility = 0, hardCC = 0, poke = 2, burst = 1, dps = 0,
                durability = 1, engage = 0, peel = 0, splitpush = 0, teamfight = 2,
                resourceMana = 1, resourceEnergy = 0 // Utilise mana
            )
        )

        val (score, penalties) = scoringEngine.calculateScore(
            userVector, manaChampion, strongPrefs
        )

        // Le champion mana devrait être rejeté
        assertEquals(-1000.0, score, 0.001)
        assertTrue("Should have penalty for using resources", 
            penalties.any { it.contains("sans contrainte") })
    }

    @Test
    fun `test matching preferences allow champions through`() {
        // Utilisateur qui préfère à distance + AP
        val userVector = ChampionFeatures(
            isRanged = 2, dmgAP = 2, poke = 2, teamfight = 2,
            dmgAD = 0, dmgHybrid = 0, complexity = 0, mobility = 0,
            hardCC = 0, burst = 0, dps = 0, durability = 0,
            engage = 0, peel = 0, splitpush = 0,
            resourceMana = 0, resourceEnergy = 0
        )
        val strongPrefs = StrongPrefs(preferRanged = true, requiredDamageType = "AP")

        // Champion AP à distance (comme Ziggs)
        val ziggsLike = ChampionCandidate(
            id = "ZiggsLike",
            name = "Ziggs Like",
            lanes = setOf(Lane.MID),
            features = ChampionFeatures(
                isRanged = 1, dmgAD = 0, dmgAP = 2, dmgHybrid = 0, // AP à distance
                complexity = 1, mobility = 0, hardCC = 0, poke = 2,
                burst = 1, dps = 0, durability = 1, engage = 0,
                peel = 0, splitpush = 0, teamfight = 2,
                resourceMana = 1, resourceEnergy = 0
            )
        )

        val (score, penalties) = scoringEngine.calculateScore(
            userVector, ziggsLike, strongPrefs
        )

        // Le champion compatible devrait avoir un score positif
        assertTrue("Compatible champion should have positive score", score > 0)
        assertTrue("Should have no penalties", penalties.isEmpty())
    }
}
