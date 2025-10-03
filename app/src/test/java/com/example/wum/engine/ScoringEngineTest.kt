package com.example.wum.engine

import com.example.wum.domain.recommendation.*
import com.example.wum.FeatureResultMain.ChampionLol.Lane
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour le ScoringEngine
 */
class ScoringEngineTest {

    private lateinit var scoringEngine: ScoringEngine

    @Before
    fun setUp() {
        scoringEngine = ScoringEngine()
    }

    @Test
    fun `test user ranged + poke preferences should favor Ziggs over Malphite`() {
        // Utilisateur qui préfère ranged + poke
        val userVector = ChampionFeatures(
            isRanged = 2, poke = 2, dmgAP = 1, teamfight = 1,
            dmgAD = 0, dmgHybrid = 0, complexity = 0, mobility = 0, hardCC = 0,
            burst = 0, dps = 0, durability = 0, engage = 0, peel = 0,
            splitpush = 0, resourceMana = 0, resourceEnergy = 0
        )

        // Ziggs (ranged mage)
        val ziggs = ChampionCandidate(
            id = "Ziggs",
            name = "Ziggs",
            lanes = setOf(Lane.MID),
            features = ChampionFeatures(
                isRanged = 1, poke = 2, dmgAP = 2, teamfight = 2,
                dmgAD = 0, dmgHybrid = 0, complexity = 1, mobility = 0, hardCC = 0,
                burst = 1, dps = 0, durability = 1, engage = 0, peel = 0,
                splitpush = 0, resourceMana = 1, resourceEnergy = 0
            )
        )

        // Malphite (tank melee)
        val malphite = ChampionCandidate(
            id = "Malphite",
            name = "Malphite",
            lanes = setOf(Lane.TOP),
            features = ChampionFeatures(
                isRanged = 0, poke = 0, dmgAP = 1, teamfight = 1,
                dmgAD = 1, dmgHybrid = 0, complexity = 0, mobility = 0, hardCC = 2,
                burst = 0, dps = 0, durability = 2, engage = 2, peel = 0,
                splitpush = 0, resourceMana = 1, resourceEnergy = 0
            )
        )

        val candidates = listOf(ziggs, malphite)
        val recommendations = scoringEngine.generateRecommendations(
            userVector = userVector,
            candidates = candidates,
            strongPrefs = StrongPrefs(),
            targetLane = null,
            topK = 2
        )

        // Ziggs devrait être mieux classé que Malphite
        assertEquals("Ziggs", recommendations[0].champion.id)
        assertEquals("Malphite", recommendations[1].champion.id)
        assertTrue("Ziggs score should be higher",
            recommendations[0].score > recommendations[1].score)
    }

    @Test
    fun `test user tank + engage preferences should favor Malphite over Ziggs`() {
        // Utilisateur qui préfère tank + engage
        val userVector = ChampionFeatures(
            durability = 2, engage = 2, hardCC = 2, teamfight = 1,
            isRanged = 0, dmgAD = 0, dmgAP = 0, dmgHybrid = 0, complexity = 0,
            mobility = 0, poke = 0, burst = 0, dps = 0, peel = 0,
            splitpush = 0, resourceMana = 0, resourceEnergy = 0
        )

        val ziggs = ChampionCandidate(
            id = "Ziggs", name = "Ziggs", lanes = setOf(Lane.MID),
            features = ChampionFeatures(
                isRanged = 1, poke = 2, dmgAP = 2, teamfight = 2, complexity = 1,
                durability = 1, engage = 0, hardCC = 0, burst = 1,
                dmgAD = 0, dmgHybrid = 0, mobility = 0, dps = 0, peel = 0,
                splitpush = 0, resourceMana = 1, resourceEnergy = 0
            )
        )

        val malphite = ChampionCandidate(
            id = "Malphite", name = "Malphite", lanes = setOf(Lane.TOP),
            features = ChampionFeatures(
                durability = 2, engage = 2, hardCC = 2, teamfight = 1, dmgAP = 1,
                isRanged = 0, dmgAD = 1, dmgHybrid = 0, complexity = 0, mobility = 0,
                poke = 0, burst = 0, dps = 0, peel = 0, splitpush = 0,
                resourceMana = 1, resourceEnergy = 0
            )
        )

        val candidates = listOf(ziggs, malphite)
        val recommendations = scoringEngine.generateRecommendations(
            userVector = userVector,
            candidates = candidates,
            strongPrefs = StrongPrefs()
        )

        // Malphite devrait être mieux classé
        assertEquals("Malphite", recommendations[0].champion.id)
        assertTrue("Malphite score should be higher",
            recommendations[0].score > recommendations[1].score)
    }

    @Test
    fun `test lane filtering - mode LANE ADC excludes Zed and Malphite`() {
        val userVector = ChampionFeatures.EMPTY

        val zed = ChampionCandidate(
            id = "Zed", name = "Zed",
            lanes = setOf(Lane.MID), // Pas ADC
            features = ChampionFeatures.EMPTY
        )

        val malphite = ChampionCandidate(
            id = "Malphite", name = "Malphite",
            lanes = setOf(Lane.TOP, Lane.JUNGLE), // Pas ADC
            features = ChampionFeatures.EMPTY
        )

        val ashe = ChampionCandidate(
            id = "Ashe", name = "Ashe",
            lanes = setOf(Lane.ADC), // ADC !
            features = ChampionFeatures.EMPTY
        )

        val candidates = listOf(zed, malphite, ashe)
        val recommendations = scoringEngine.generateRecommendations(
            userVector = userVector,
            candidates = candidates,
            strongPrefs = StrongPrefs(),
            targetLane = Lane.ADC
        )

        // Seul Ashe devrait être recommandé
        assertEquals(1, recommendations.size)
        assertEquals("Ashe", recommendations[0].champion.id)
    }

    @Test
    fun `test strong preferences penalties`() {
        val userVector = ChampionFeatures(
            isRanged = 2, // User prefers ranged
            dmgAD = 0, dmgAP = 0, dmgHybrid = 0, complexity = 0, mobility = 0,
            hardCC = 0, poke = 0, burst = 0, dps = 0, durability = 0,
            engage = 0, peel = 0, splitpush = 0, teamfight = 0,
            resourceMana = 0, resourceEnergy = 0
        )

        val meleeChampion = ChampionCandidate(
            id = "MeleeTest", name = "Melee Test",
            lanes = setOf(Lane.TOP),
            features = ChampionFeatures(
                isRanged = 0, // Melee champion
                complexity = 2, // Complex champion
                resourceMana = 1, // Uses mana
                dmgAD = 0, dmgAP = 0, dmgHybrid = 0, mobility = 0, hardCC = 0,
                poke = 0, burst = 0, dps = 0, durability = 0, engage = 0,
                peel = 0, splitpush = 0, teamfight = 0, resourceEnergy = 0
            )
        )

        val strongPrefs = StrongPrefs(
            preferRanged = true,
            dislikeMana = true,
            preferSimple = true
        )

        val (score, penalties) = scoringEngine.calculateScore(
            userVector, meleeChampion, strongPrefs
        )

        // Score devrait être négatif à cause des pénalités
        assertTrue("Score should be negative due to penalties", score < 0)
        assertEquals("Should have 3 penalties", 3, penalties.size)
    }
}
