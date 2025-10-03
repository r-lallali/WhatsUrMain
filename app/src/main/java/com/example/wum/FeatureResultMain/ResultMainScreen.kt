package com.example.wum.FeatureResultMain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ResultMainScreen(
    viewModel: ResultMainViewModel = viewModel(),
    onNavigateToFeatureHome: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    LaunchedEffect(Unit) { viewModel.loadFromStore() }
    val uiState by viewModel.ui.collectAsState()

    ResultMainContent(
        uiModel = uiState,
        championRes = uiState.championRes,
        frameRes = uiState.frameRes,
        positionRes = uiState.positionRes,
        positionLabel = uiState.positionLabel,
        onNavigateToFeatureHome = onNavigateToFeatureHome,
        onRetry = onRetry
    )
}