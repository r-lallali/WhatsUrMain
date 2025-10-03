package com.example.wum.data.questions

import com.example.wum.domain.recommendation.Mode
import com.example.wum.FeatureResultMain.ChampionLol.Lane
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class QuestionBank(
    val mode: String,
    val lane: String? = null,
    val questions: List<Question>
)

@Serializable
data class Question(
    val id: String,
    val type: String,
    val prompt: String,
    val answerMode: String,
    val options: List<AnswerOption>
)

@Serializable
data class AnswerOption(
    val id: String,
    val label: String,
    val weights: Map<String, Int> = emptyMap()
)

/**
 * Repository pour charger les banques de questions
 */
interface QuestionBankRepository {
    suspend fun loadQuestions(mode: Mode, lane: Lane? = null): QuestionBank
}

class QuestionBankRepositoryImpl(
    private val assetProvider: com.example.wum.core.io.AssetProvider
) : QuestionBankRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = mutableMapOf<String, QuestionBank>()

    override suspend fun loadQuestions(mode: Mode, lane: Lane?): QuestionBank {
        val filename = when (mode) {
            Mode.SHORT -> "questions_short.json"
            Mode.LONG -> "questions_long.json"
            Mode.LANE -> {
                requireNotNull(lane) { "Lane must be specified for LANE mode" }
                "questions_lane_${lane.name}.json"
            }
        }

        if (cache.containsKey(filename)) {
            return cache[filename]!!
        }

        try {
            val jsonText = assetProvider.readText(filename)
            val questionBank = json.decodeFromString<QuestionBank>(jsonText)
            cache[filename] = questionBank
            return questionBank
        } catch (e: Exception) {

            if (filename != "questions_short.json") {
                return loadQuestions(Mode.SHORT, null)
            }
            throw e
        }
    }
}
