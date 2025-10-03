package com.example.wum.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wum.domain.recommendation.Mode
import com.example.wum.FeatureResultMain.ChampionLol.Lane
import com.example.wum.data.questions.Question
import com.example.wum.data.questions.QuestionBankRepository
import com.example.wum.di.RecommendationContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val questionBankRepository: QuestionBankRepository =
        RecommendationContainer.getInstance().getQuestionBankRepository()

    private val _selectedLaneId = MutableStateFlow<String?>(null)
    val selectedLaneId: StateFlow<String?> = _selectedLaneId

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions

    /**
     * 🔹 Charger les questions en fonction du mode/lane
     */
    fun loadQuestions(mode: Mode, lane: Lane? = null) {
        viewModelScope.launch {
            try {
                val questionBank = questionBankRepository.loadQuestions(mode, lane)
                _questions.value = questionBank.questions
            } catch (e: Exception) {
                // fallback en SHORT si erreur
                try {
                    val fallback = questionBankRepository.loadQuestions(Mode.SHORT)
                    _questions.value = fallback.questions
                } catch (_: Exception) {
                    _questions.value = emptyList()
                }
            }
        }
    }

    /**
     * 🔹 Lancer un quiz rapide
     */
    fun startQuickQuiz() {
        _selectedLaneId.value = null // réinitialiser lane
        _uiState.value = HomeUiState(
            selectedQuizMode = Mode.SHORT,
            isQuizReady = true,
            selectedLaneId = null
        )
        loadQuestions(Mode.SHORT)
    }

    /**
     * 🔹 Lancer un quiz détaillé
     */
    fun startDetailedQuiz() {
        _selectedLaneId.value = null // réinitialiser lane
        _uiState.value = HomeUiState(
            selectedQuizMode = Mode.LONG,
            isQuizReady = true,
            selectedLaneId = null
        )
        loadQuestions(Mode.LONG)
    }

    /**
     * 🔹 Sélectionner un quiz par rôle (lane)
     */
    fun selectLane(laneId: String) {
        _selectedLaneId.value = laneId
        _uiState.value = HomeUiState(
            selectedQuizMode = Mode.LANE,
            isQuizReady = true,
            selectedLaneId = laneId
        )

        val laneEnum = when (laneId) {
            "top" -> Lane.TOP
            "jungle" -> Lane.JUNGLE
            "mid" -> Lane.MID
            "adc" -> Lane.ADC
            "support" -> Lane.SUPPORT
            else -> null
        }

        loadQuestions(Mode.LANE, laneEnum)
    }

    /**
     * 🔹 Réinitialiser la sélection (utile si tu veux un bouton "Annuler")
     */
    fun resetSelection() {
        _selectedLaneId.value = null
        _uiState.value = HomeUiState()
        _questions.value = emptyList()
    }
}
