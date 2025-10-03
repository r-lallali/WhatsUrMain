package com.example.wum.Home

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToFeatureSettings: () -> Unit = {},
    onNavigateToFeatureQuiz: () -> Unit = {}
) {
    HomeContent(
        uiModel = HomeUiModel(),
        onNavigateToFeatureSettings = onNavigateToFeatureSettings,
        onNavigateToFeatureQuiz = onNavigateToFeatureQuiz,
        viewModel = viewModel
    )
}
