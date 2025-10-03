package com.example.wum.Home

import com.example.wum.domain.recommendation.Mode

/**
 * Représente l'état global de la page Home
 */
data class HomeUiState(
    val selectedQuizMode: Mode? = null,
    val isQuizReady: Boolean = false,
    val selectedLaneId: String? = null
)
