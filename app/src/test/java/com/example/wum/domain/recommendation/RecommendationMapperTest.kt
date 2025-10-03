package com.example.wum.domain.recommendation

import com.example.wum.FeatureResultMain.ChampionLol.Lane
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour le RecommendationMapper
 */
class RecommendationMapperTest {

    private lateinit var mapper: RecommendationMapper

    @Before
    fun setUp() {
        mapper = RecommendationMapper()
    }

    @Test
    fun `test toChampionInfo with target lane - should use target lane when champion supports it`() {

        val candidate = ChampionCandidate(
            id = "Malphite",
            name = "Malphite",
            lanes = setOf(Lane.TOP, Lane.SUPPORT),
            features = ChampionFeatures.EMPTY
        )

        val recommendation = Recommendation(
            champion = candidate,
            score = 5.0,
            explanation = Explanation(emptyList(), emptyList(), emptyList())
        )


        val championInfoSupport = mapper.toChampionInfo(recommendation, Lane.SUPPORT)
        assertEquals("Should use SUPPORT lane when specified", Lane.SUPPORT, championInfoSupport.lane)


        val championInfoTop = mapper.toChampionInfo(recommendation, Lane.TOP)
        assertEquals("Should use TOP lane when specified", Lane.TOP, championInfoTop.lane)
    }

    @Test
    fun `test toChampionInfo without target lane - should use first available lane`() {
        val candidate = ChampionCandidate(
            id = "Malphite",
            name = "Malphite",
            lanes = setOf(Lane.TOP, Lane.SUPPORT),
            features = ChampionFeatures.EMPTY
        )

        val recommendation = Recommendation(
            champion = candidate,
            score = 5.0,
            explanation = Explanation(emptyList(), emptyList(), emptyList())
        )


        val championInfo = mapper.toChampionInfo(recommendation, null)
        assertEquals("Should use first available lane", Lane.TOP, championInfo.lane)
    }

    @Test
    fun `test toChampionInfo with incompatible target lane - should fallback to first available`() {
        val candidate = ChampionCandidate(
            id = "Malphite",
            name = "Malphite",
            lanes = setOf(Lane.TOP, Lane.SUPPORT),
            features = ChampionFeatures.EMPTY
        )

        val recommendation = Recommendation(
            champion = candidate,
            score = 5.0,
            explanation = Explanation(emptyList(), emptyList(), emptyList())
        )


        val championInfo = mapper.toChampionInfo(recommendation, Lane.ADC)
        assertEquals("Should fallback to first available lane when target is incompatible", 
                    Lane.TOP, championInfo.lane)
    }
}
