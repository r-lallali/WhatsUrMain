package com.example.wum.domain.recommendation

import com.example.wum.FeatureResultMain.ChampionLol.Lane
import com.example.wum.data.champions.ChampionRepository
import com.example.wum.data.lanes.LanesRepository
import com.example.wum.data.questions.QuestionBankRepository
import com.example.wum.engine.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Tests pour la génération dynamique de StrongPrefs
 */
class StrongPrefsGenerationTest {

    @Mock
    private lateinit var championRepository: ChampionRepository
    @Mock
    private lateinit var lanesRepository: LanesRepository
    @Mock
    private lateinit var questionBankRepository: QuestionBankRepository
    @Mock
    private lateinit var featureExtractor: FeatureExtractor
    @Mock
    private lateinit var userVectorBuilder: UserVectorBuilder
    @Mock
    private lateinit var scoringEngine: ScoringEngine

    private lateinit var recommendUseCase: RecommendUseCaseImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        recommendUseCase = RecommendUseCaseImpl(
            championRepository, lanesRepository, questionBankRepository,
            featureExtractor, userVectorBuilder, scoringEngine
        )
    }

    @Test
    fun `test melee preference generates correct StrongPrefs`() = runBlocking {
        // Préparer les mocks avec des données minimales
        `when`(championRepository.loadAll()).thenReturn(emptyList())
        `when`(questionBankRepository.loadQuestions(any(), any())).thenReturn(
            com.example.wum.data.questions.QuestionBank("SHORT", null, emptyList())
        )
        `when`(userVectorBuilder.buildUserVector(any(), any())).thenReturn(ChampionFeatures.EMPTY)
        `when`(scoringEngine.generateRecommendations(any(), any(), any(), any(), any(), any()))
            .thenReturn(emptyList())

        // Réponses indiquant une préférence pour corps-à-corps + AD
        val answers = mapOf(
            "q_range" to listOf("melee"),
            "q_damage_type" to listOf("physical")
        )

        val input = RecommendationInput(
            mode = Mode.SHORT,
            lane = null,
            answers = answers,
            strongPrefs = StrongPrefs()
        )

        // Exécuter le use case
        recommendUseCase(input)

        // Vérifier que le scoringEngine a été appelé avec les bonnes StrongPrefs
        val argumentCaptor = org.mockito.ArgumentCaptor.forClass(StrongPrefs::class.java)
        verify(scoringEngine).generateRecommendations(
            any(), any(), argumentCaptor.capture(), any(), any(), any()
        )

        val capturedStrongPrefs = argumentCaptor.value
        assertTrue("Should prefer melee", capturedStrongPrefs.preferMelee)
        assertFalse("Should not prefer ranged", capturedStrongPrefs.preferRanged)
        assertEquals("Should require AD damage", "AD", capturedStrongPrefs.requiredDamageType)
    }

    @Test
    fun `test ranged preference generates correct StrongPrefs`() = runBlocking {
        // Préparer les mocks avec des données minimales
        `when`(championRepository.loadAll()).thenReturn(emptyList())
        `when`(questionBankRepository.loadQuestions(any(), any())).thenReturn(
            com.example.wum.data.questions.QuestionBank("SHORT", null, emptyList())
        )
        `when`(userVectorBuilder.buildUserVector(any(), any())).thenReturn(ChampionFeatures.EMPTY)
        `when`(scoringEngine.generateRecommendations(any(), any(), any(), any(), any(), any()))
            .thenReturn(emptyList())

        // Réponses indiquant une préférence pour à distance + AP
        val answers = mapOf(
            "q_range" to listOf("ranged"),
            "q_damage_type" to listOf("magical")
        )

        val input = RecommendationInput(
            mode = Mode.SHORT,
            lane = null,
            answers = answers,
            strongPrefs = StrongPrefs()
        )

        // Exécuter le use case
        recommendUseCase(input)

        // Vérifier que le scoringEngine a été appelé avec les bonnes StrongPrefs
        val argumentCaptor = org.mockito.ArgumentCaptor.forClass(StrongPrefs::class.java)
        verify(scoringEngine).generateRecommendations(
            any(), any(), argumentCaptor.capture(), any(), any(), any()
        )

        val capturedStrongPrefs = argumentCaptor.value
        assertTrue("Should prefer ranged", capturedStrongPrefs.preferRanged)
        assertFalse("Should not prefer melee", capturedStrongPrefs.preferMelee)
        assertEquals("Should require AP damage", "AP", capturedStrongPrefs.requiredDamageType)
    }

    @Test
    fun `test hybrid preference generates correct StrongPrefs`() = runBlocking {
        // Préparer les mocks avec des données minimales
        `when`(championRepository.loadAll()).thenReturn(emptyList())
        `when`(questionBankRepository.loadQuestions(any(), any())).thenReturn(
            com.example.wum.data.questions.QuestionBank("SHORT", null, emptyList())
        )
        `when`(userVectorBuilder.buildUserVector(any(), any())).thenReturn(ChampionFeatures.EMPTY)
        `when`(scoringEngine.generateRecommendations(any(), any(), any(), any(), any(), any()))
            .thenReturn(emptyList())

        // Réponses indiquant une préférence hybride + simple
        val answers = mapOf(
            "q_damage_type" to listOf("hybrid"),
            "q_complexity" to listOf("simple"),
            "q_resource_type" to listOf("no_resource")
        )

        val input = RecommendationInput(
            mode = Mode.SHORT,
            lane = null,
            answers = answers,
            strongPrefs = StrongPrefs()
        )

        // Exécuter le use case
        recommendUseCase(input)

        // Vérifier que le scoringEngine a été appelé avec les bonnes StrongPrefs
        val argumentCaptor = org.mockito.ArgumentCaptor.forClass(StrongPrefs::class.java)
        verify(scoringEngine).generateRecommendations(
            any(), any(), argumentCaptor.capture(), any(), any(), any()
        )

        val capturedStrongPrefs = argumentCaptor.value
        assertEquals("Should require hybrid damage", "HYBRID", capturedStrongPrefs.requiredDamageType)
        assertTrue("Should prefer simple", capturedStrongPrefs.preferSimple)
        assertTrue("Should dislike mana", capturedStrongPrefs.dislikeMana)
    }

    @Test
    fun `test no strong preferences keeps defaults`() = runBlocking {
        // Préparer les mocks avec des données minimales
        `when`(championRepository.loadAll()).thenReturn(emptyList())
        `when`(questionBankRepository.loadQuestions(any(), any())).thenReturn(
            com.example.wum.data.questions.QuestionBank("SHORT", null, emptyList())
        )
        `when`(userVectorBuilder.buildUserVector(any(), any())).thenReturn(ChampionFeatures.EMPTY)
        `when`(scoringEngine.generateRecommendations(any(), any(), any(), any(), any(), any()))
            .thenReturn(emptyList())

        // Réponses neutres qui ne génèrent pas de préférences fortes
        val answers = mapOf(
            "q_complexity" to listOf("moderate"),
            "q_resource_type" to listOf("mana_user")
        )

        val input = RecommendationInput(
            mode = Mode.SHORT,
            lane = null,
            answers = answers,
            strongPrefs = StrongPrefs()
        )

        // Exécuter le use case
        recommendUseCase(input)

        // Vérifier que le scoringEngine a été appelé avec les StrongPrefs par défaut
        val argumentCaptor = org.mockito.ArgumentCaptor.forClass(StrongPrefs::class.java)
        verify(scoringEngine).generateRecommendations(
            any(), any(), argumentCaptor.capture(), any(), any(), any()
        )

        val capturedStrongPrefs = argumentCaptor.value
        assertFalse("Should not prefer ranged", capturedStrongPrefs.preferRanged)
        assertFalse("Should not prefer melee", capturedStrongPrefs.preferMelee)
        assertNull("Should have no required damage type", capturedStrongPrefs.requiredDamageType)
        assertFalse("Should not dislike mana", capturedStrongPrefs.dislikeMana)
        assertFalse("Should not prefer simple", capturedStrongPrefs.preferSimple)
    }

    @Test
    fun `test mana preference generates correct StrongPrefs`() = runBlocking {
        // Préparer les mocks avec des données minimales
        `when`(championRepository.loadAll()).thenReturn(emptyList())
        `when`(questionBankRepository.loadQuestions(any(), any())).thenReturn(
            com.example.wum.data.questions.QuestionBank("SHORT", null, emptyList())
        )
        `when`(userVectorBuilder.buildUserVector(any(), any())).thenReturn(ChampionFeatures.EMPTY)
        `when`(scoringEngine.generateRecommendations(any(), any(), any(), any(), any(), any()))
            .thenReturn(emptyList())

        // Réponses indiquant une préférence pour mana
        val answers = mapOf(
            "q_resource_type" to listOf("mana_user")
        )

        val input = RecommendationInput(
            mode = Mode.SHORT,
            lane = null,
            answers = answers,
            strongPrefs = StrongPrefs()
        )

        // Exécuter le use case
        recommendUseCase(input)

        // Vérifier que le scoringEngine a été appelé avec les bonnes StrongPrefs
        val argumentCaptor = org.mockito.ArgumentCaptor.forClass(StrongPrefs::class.java)
        verify(scoringEngine).generateRecommendations(
            any(), any(), argumentCaptor.capture(), any(), any(), any()
        )

        val capturedStrongPrefs = argumentCaptor.value
        assertEquals("Should require mana", "MANA", capturedStrongPrefs.requiredResourceType)
    }

    @Test
    fun `test energy preference generates correct StrongPrefs`() = runBlocking {
        // Préparer les mocks avec des données minimales
        `when`(championRepository.loadAll()).thenReturn(emptyList())
        `when`(questionBankRepository.loadQuestions(any(), any())).thenReturn(
            com.example.wum.data.questions.QuestionBank("SHORT", null, emptyList())
        )
        `when`(userVectorBuilder.buildUserVector(any(), any())).thenReturn(ChampionFeatures.EMPTY)
        `when`(scoringEngine.generateRecommendations(any(), any(), any(), any(), any(), any()))
            .thenReturn(emptyList())

        // Réponses indiquant une préférence pour énergie
        val answers = mapOf(
            "q_resource_type" to listOf("energy_user")
        )

        val input = RecommendationInput(
            mode = Mode.SHORT,
            lane = null,
            answers = answers,
            strongPrefs = StrongPrefs()
        )

        // Exécuter le use case
        recommendUseCase(input)

        // Vérifier que le scoringEngine a été appelé avec les bonnes StrongPrefs
        val argumentCaptor = org.mockito.ArgumentCaptor.forClass(StrongPrefs::class.java)
        verify(scoringEngine).generateRecommendations(
            any(), any(), argumentCaptor.capture(), any(), any(), any()
        )

        val capturedStrongPrefs = argumentCaptor.value
        assertEquals("Should require energy", "ENERGY", capturedStrongPrefs.requiredResourceType)
    }

    @Test
    fun `test no resource preference generates correct StrongPrefs`() = runBlocking {
        // Préparer les mocks avec des données minimales
        `when`(championRepository.loadAll()).thenReturn(emptyList())
        `when`(questionBankRepository.loadQuestions(any(), any())).thenReturn(
            com.example.wum.data.questions.QuestionBank("SHORT", null, emptyList())
        )
        `when`(userVectorBuilder.buildUserVector(any(), any())).thenReturn(ChampionFeatures.EMPTY)
        `when`(scoringEngine.generateRecommendations(any(), any(), any(), any(), any(), any()))
            .thenReturn(emptyList())

        // Réponses indiquant une préférence pour sans contrainte
        val answers = mapOf(
            "q_resource_type" to listOf("no_resource")
        )

        val input = RecommendationInput(
            mode = Mode.SHORT,
            lane = null,
            answers = answers,
            strongPrefs = StrongPrefs()
        )

        // Exécuter le use case
        recommendUseCase(input)

        // Vérifier que le scoringEngine a été appelé avec les bonnes StrongPrefs
        val argumentCaptor = org.mockito.ArgumentCaptor.forClass(StrongPrefs::class.java)
        verify(scoringEngine).generateRecommendations(
            any(), any(), argumentCaptor.capture(), any(), any(), any()
        )

        val capturedStrongPrefs = argumentCaptor.value
        assertEquals("Should require no resources", "NONE", capturedStrongPrefs.requiredResourceType)
    }
}
