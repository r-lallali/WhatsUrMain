package com.example.wum.engine

import com.example.wum.data.champions.ChampionSummary
import com.example.wum.domain.recommendation.ChampionFeatures
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour le FeatureExtractor
 */
class FeatureExtractorTest {

    private lateinit var extractor: FeatureExtractor

    @Before
    fun setUp() {
        extractor = FeatureExtractor()
    }

    @Test
    fun `test extraction Zed - assassin AD mobile`() {
        val zed = ChampionSummary(
            id = "Zed",
            name = "Zed",
            tags = listOf("Assassin"),
            partype = "Energy",
            info = ChampionSummary.Info(attack = 9, defense = 2, magic = 1, difficulty = 7)
        )

        val features = extractor.extract(zed)

        assertEquals(0, features.isRanged)
        assertEquals(2, features.dmgAD)
        assertEquals(0, features.dmgAP)
        assertEquals(0, features.dmgHybrid)
        assertEquals(2, features.complexity)
        assertEquals(2, features.mobility)
        assertEquals(2, features.burst)
        assertEquals(0, features.dps)
        assertEquals(0, features.durability)
        assertEquals(1, features.resourceEnergy)
        assertEquals(0, features.resourceMana)
    }

    @Test
    fun `test extraction Malphite - tank engage`() {
        val malphite = ChampionSummary(
            id = "Malphite",
            name = "Malphite",
            tags = listOf("Tank", "Mage"),
            partype = "Mana",
            info = ChampionSummary.Info(attack = 5, defense = 9, magic = 7, difficulty = 2)
        )

        val features = extractor.extract(malphite)

        assertEquals(0, features.isRanged)
        assertEquals(1, features.dmgAD)
        assertEquals(2, features.dmgAP)
        assertEquals(0, features.complexity)
        assertEquals(2, features.durability)
        assertEquals(2, features.engage)
        assertEquals(2, features.hardCC)
        assertEquals(1, features.teamfight)
        assertEquals(1, features.resourceMana)
    }

    @Test
    fun `test extraction Ziggs - mage ranged poke`() {
        val ziggs = ChampionSummary(
            id = "Ziggs",
            name = "Ziggs",
            tags = listOf("Mage"),
            partype = "Mana",
            info = ChampionSummary.Info(attack = 2, defense = 4, magic = 9, difficulty = 4)
        )

        val features = extractor.extract(ziggs)

        assertEquals(1, features.isRanged)
        assertEquals(0, features.dmgAD)
        assertEquals(2, features.dmgAP)
        assertEquals(1, features.complexity)
        assertEquals(2, features.poke)
        assertEquals(1, features.burst)
        assertEquals(2, features.teamfight)
        assertEquals(1, features.resourceMana)
    }

    @Test
    fun `test extraction Vi - fighter jungle`() {
        val vi = ChampionSummary(
            id = "Vi",
            name = "Vi",
            tags = listOf("Fighter", "Assassin"),
            partype = "Mana",
            info = ChampionSummary.Info(attack = 8, defense = 5, magic = 3, difficulty = 4)
        )

        val features = extractor.extract(vi)

        assertEquals(0, features.isRanged)
        assertEquals(2, features.dmgAD)
        assertEquals(0, features.dmgAP)
        assertEquals(1, features.complexity)
        assertEquals(2, features.mobility)
        assertEquals(2, features.burst) // Assassin = burst
        assertEquals(1, features.dps) // Fighter = DPS moyen
        assertEquals(1, features.durability) // defense = 5
        assertEquals(1, features.engage) // Fighter avec defense >= 5
    }

    @Test
    fun `test bornes des valeurs - toutes entre 0 et 2`() {
        val champion = ChampionSummary(
            id = "Test",
            name = "Test",
            tags = listOf("Tank", "Mage", "Fighter", "Assassin", "Marksman", "Support"),
            partype = "Mana",
            info = ChampionSummary.Info(attack = 10, defense = 10, magic = 10, difficulty = 10)
        )

        val features = extractor.extract(champion)

        // Vérifier que toutes les valeurs sont bornées entre 0 et 2
        val values = listOf(
            features.isRanged, features.dmgAD, features.dmgAP, features.dmgHybrid,
            features.complexity, features.mobility, features.hardCC, features.poke,
            features.burst, features.dps, features.durability, features.engage,
            features.peel, features.splitpush, features.teamfight,
            features.resourceMana, features.resourceEnergy
        )

        values.forEach { value ->
            assertTrue("Valeur $value doit être entre 0 et 2", value in 0..2)
        }
    }
}
