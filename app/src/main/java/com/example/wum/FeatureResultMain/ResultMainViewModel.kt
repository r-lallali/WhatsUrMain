package com.example.wum.FeatureResultMain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wum.FeatureResultMain.ChampionLol.ChampionRepository
import com.example.wum.domain.recommendation.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ResultMainViewModel(
    private val recommendationMapper: RecommendationMapper = RecommendationMapper()
) : ViewModel() {

    private val _ui = MutableStateFlow(ResultMainUiModel())
    val ui: StateFlow<ResultMainUiModel> = _ui

    fun loadChampion(id: String, repository: ChampionRepository = ChampionRepository) {
        viewModelScope.launch {
            val c = repository.get(id) ?: return@launch
            _ui.value = ResultMainUiModel(
                headerText = "Tu incarnes...",
                name = c.displayName,
                championRes = c.portraitRes,
                frameRes = c.frameRes,
                positionRes = c.laneIconRes,
                positionLabel = c.lane.name.lowercase().replaceFirstChar { it.uppercase() }
            )
        }
    }





    fun loadRecommendedChampion(recommendation: Recommendation, targetLane: com.example.wum.FeatureResultMain.ChampionLol.Lane? = null) {
        val championInfo = recommendationMapper.toChampionInfo(recommendation, targetLane)

        _ui.value = ResultMainUiModel(
            headerText = "Recommandé pour toi :",
            name = championInfo.displayName,
            championRes = championInfo.portraitRes,
            frameRes = championInfo.frameRes,
            positionRes = championInfo.laneIconRes,
            positionLabel = championInfo.lane.name.lowercase().replaceFirstChar { it.uppercase() }
        )
    }





    fun loadFromStore() {
        val rec = RecommendationStore.current ?: return
        val targetLane = RecommendationStore.targetLane
        loadRecommendedChampion(rec, targetLane)
        RecommendationStore.clear()
    }
}
