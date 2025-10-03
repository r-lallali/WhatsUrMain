package com.example.wum.data.questions

import com.example.wum.domain.recommendation.Mode
import com.example.wum.FeatureResultMain.ChampionLol.Lane
import com.example.wum.core.io.AssetProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Tests unitaires pour le QuestionBankRepository
 */
class QuestionBankRepositoryTest {

    @Mock
    private lateinit var assetProvider: AssetProvider

    private lateinit var repository: QuestionBankRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = QuestionBankRepositoryImpl(assetProvider)
    }

    @Test
    fun `test load short questions and map weights correctly`() = runBlocking {
        val mockJson = """
        {
          "mode": "SHORT",
          "lane": null,
          "questions": [
            {
              "id": "q_range",
              "type": "TEXT",
              "prompt": "Test question",
              "answerMode": "SINGLE",
              "options": [
                {
                  "id": "melee",
                  "label": "Corps-à-corps",
                  "weights": {"isRanged": 0, "durability": 1}
                },
                {
                  "id": "ranged",
                  "label": "À distance",
                  "weights": {"isRanged": 1, "poke": 1}
                }
              ]
            }
          ]
        }
        """.trimIndent()

        `when`(assetProvider.readText("questions_short.json")).thenReturn(mockJson)

        val questionBank = repository.loadQuestions(Mode.SHORT)

        assertEquals("SHORT", questionBank.mode)
        assertNull(questionBank.lane)
        assertEquals(1, questionBank.questions.size)

        val question = questionBank.questions[0]
        assertEquals("q_range", question.id)
        assertEquals("Test question", question.prompt)
        assertEquals(2, question.options.size)

        val meleeOption = question.options.find { it.id == "melee" }!!
        assertEquals(0, meleeOption.weights["isRanged"])
        assertEquals(1, meleeOption.weights["durability"])

        val rangedOption = question.options.find { it.id == "ranged" }!!
        assertEquals(1, rangedOption.weights["isRanged"])
        assertEquals(1, rangedOption.weights["poke"])
    }

    @Test
    fun `test load lane-specific questions`() = runBlocking {
        val mockJson = """
        {
          "mode": "LANE",
          "lane": "TOP",
          "questions": [
            {
              "id": "q_top_style",
              "type": "TEXT",
              "prompt": "Top lane style?",
              "answerMode": "SINGLE",
              "options": [
                {
                  "id": "tank",
                  "label": "Tank",
                  "weights": {"durability": 2, "engage": 2}
                }
              ]
            }
          ]
        }
        """.trimIndent()

        `when`(assetProvider.readText("questions_lane_TOP.json")).thenReturn(mockJson)

        val questionBank = repository.loadQuestions(Mode.LANE, Lane.TOP)

        assertEquals("LANE", questionBank.mode)
        assertEquals("TOP", questionBank.lane)
        assertEquals(1, questionBank.questions.size)
        assertEquals("q_top_style", questionBank.questions[0].id)
    }

    @Test
    fun `test fallback to short questions when lane file missing`() = runBlocking {
        val shortJson = """
        {
          "mode": "SHORT",
          "lane": null,
          "questions": [
            {
              "id": "fallback_question",
              "type": "TEXT",
              "prompt": "Fallback",
              "answerMode": "SINGLE",
              "options": []
            }
          ]
        }
        """.trimIndent()

        `when`(assetProvider.readText("questions_lane_JUNGLE.json"))
            .thenThrow(RuntimeException("File not found"))
        `when`(assetProvider.readText("questions_short.json")).thenReturn(shortJson)

        val questionBank = repository.loadQuestions(Mode.LANE, Lane.JUNGLE)

        assertEquals("SHORT", questionBank.mode)
        assertEquals("fallback_question", questionBank.questions[0].id)
    }

    @Test
    fun `test caching mechanism`() = runBlocking {
        val mockJson = """{"mode": "SHORT", "questions": []}"""

        `when`(assetProvider.readText("questions_short.json")).thenReturn(mockJson)

        repository.loadQuestions(Mode.SHORT)
        repository.loadQuestions(Mode.SHORT)
        verify(assetProvider, times(1)).readText("questions_short.json")
    }
}
