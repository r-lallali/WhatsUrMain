package com.example.wum.Ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wum.domain.recommendation.*
import com.example.wum.data.questions.*
import com.example.wum.FeatureResultMain.ChampionLol.Lane
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QuizUiState(
    val currentQuestionIndex: Int = 0,
    val questions: List<Question> = emptyList(),
    val answers: MutableMap<String, List<String>> = mutableMapOf(),
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)

class QuizViewModel(
    private val questionBankRepository: QuestionBankRepository,
    private val recommendUseCase: RecommendUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState

    private var quizMode: Mode = Mode.SHORT
    private var targetLane: Lane? = null

    fun initializeQuiz(mode: Mode, lane: Lane? = null) {
        quizMode = mode
        targetLane = lane

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val questionBank = questionBankRepository.loadQuestions(mode, lane)
                _uiState.value = _uiState.value.copy(
                    questions = questionBank.questions,
                    isLoading = false,
                    currentQuestionIndex = 0,
                    answers = mutableMapOf(),
                    isComplete = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement des questions: ${e.message}"
                )
            }
        }
    }

    fun answerQuestion(questionId: String, selectedOptions: List<String>) {
        val currentAnswers = _uiState.value.answers.toMutableMap()
        currentAnswers[questionId] = selectedOptions

        _uiState.value = _uiState.value.copy(answers = currentAnswers)
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex >= currentState.questions.size) {

            _uiState.value = currentState.copy(isComplete = true)
        } else {
            _uiState.value = currentState.copy(currentQuestionIndex = nextIndex)
        }
    }

    fun previousQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        if (currentIndex > 0) {
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = currentIndex - 1
            )
        }
    }

    suspend fun generateRecommendations(): List<Recommendation> {
        val currentState = _uiState.value

        return try {
            val input = RecommendationInput(
                mode = quizMode,
                lane = targetLane,
                answers = currentState.answers,
                strongPrefs = StrongPrefs()
            )

            recommendUseCase(input)
        } catch (e: Exception) {
            _uiState.value = currentState.copy(
                error = "Erreur lors de la génération des recommandations: ${e.message}"
            )
            emptyList()
        }
    }




    fun onValidate(selectedIndex: Int) {
        val currentState = _uiState.value
        if (currentState.questions.isNotEmpty() &&
            currentState.currentQuestionIndex < currentState.questions.size &&
            selectedIndex >= 0) {

            val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
            val selectedOption = currentQuestion.options[selectedIndex]

            answerQuestion(currentQuestion.id, listOf(selectedOption.id))
            nextQuestion()
        }
    }
}
