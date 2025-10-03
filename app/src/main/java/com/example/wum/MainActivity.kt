package com.example.wum

import android.R.attr.mode
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wum.FeatureResultMain.ResultMainScreen
import com.example.wum.Home.HomeScreen
import com.example.wum.Home.HomeViewModel
import com.example.wum.Ask.QuestionSimpleScreen
import kotlinx.serialization.Serializable
import com.example.wum.Settings.SettingsScreen
import com.example.wum.ui.theme.AppTheme
import com.example.wum.ui.theme.ThemeViewModel
import com.example.wum.di.RecommendationContainer
import com.example.wum.domain.recommendation.RecommendationStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        RecommendationContainer.initialize(this)

        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val themeViewModel: ThemeViewModel = viewModel()
    val sharedHomeViewModel: HomeViewModel = viewModel()
    AppTheme(themeViewModel) {
    NavHost(navController = navController, startDestination = Routes.Home) {
        composable<Routes.Home> {
            HomeScreen(
                viewModel = sharedHomeViewModel,
                onNavigateToFeatureQuiz = { navController.navigate(Routes.Quiz) },
                onNavigateToFeatureSettings = { navController.navigate(Routes.Settings) }
            )
        }
        composable<Routes.Quiz> {
            var currentQuestionIndex by remember { mutableStateOf(0) }
            var answers by remember { mutableStateOf(mutableMapOf<String, List<String>>()) }
            var questions by remember { mutableStateOf<List<com.example.wum.data.questions.Question>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var isFinished by remember { mutableStateOf(false) }
            var recommendations by remember { mutableStateOf<List<com.example.wum.domain.recommendation.Recommendation>>(emptyList()) }
            var quizMode by remember { mutableStateOf(com.example.wum.domain.recommendation.Mode.SHORT) }
            var targetLane by remember { mutableStateOf<com.example.wum.FeatureResultMain.ChampionLol.Lane?>(null) }

            val dependencies = RecommendationContainer.getInstance()

            val homeUiState by sharedHomeViewModel.uiState.collectAsState()
            val selectedLaneId by sharedHomeViewModel.selectedLaneId.collectAsState()

            LaunchedEffect(homeUiState.selectedQuizMode, selectedLaneId) {
                if (questions.isEmpty()) {
                    try {
                        quizMode = when {
                            selectedLaneId != null -> {
                                targetLane = when (selectedLaneId) {
                                    "top" -> com.example.wum.FeatureResultMain.ChampionLol.Lane.TOP
                                    "jungle" -> com.example.wum.FeatureResultMain.ChampionLol.Lane.JUNGLE
                                    "mid" -> com.example.wum.FeatureResultMain.ChampionLol.Lane.MID
                                    "adc" -> com.example.wum.FeatureResultMain.ChampionLol.Lane.ADC
                                    "support" -> com.example.wum.FeatureResultMain.ChampionLol.Lane.SUPPORT
                                    else -> null
                                }
                                com.example.wum.domain.recommendation.Mode.LANE
                            }
                            homeUiState.selectedQuizMode == com.example.wum.domain.recommendation.Mode.LONG -> com.example.wum.domain.recommendation.Mode.LONG
                            else -> com.example.wum.domain.recommendation.Mode.SHORT
                        }

                        val qb = dependencies.getQuestionBankRepository().loadQuestions(quizMode, targetLane)
                        questions = qb.questions

                        // ✅ correction ici
                        sharedHomeViewModel.loadQuestions(quizMode, targetLane)

                    } catch (e: Exception) {
                        try {
                            val fallback = dependencies.getQuestionBankRepository()
                                .loadQuestions(com.example.wum.domain.recommendation.Mode.SHORT)
                            questions = fallback.questions
                            quizMode = com.example.wum.domain.recommendation.Mode.SHORT
                            targetLane = null
                        } catch (_: Exception) { }
                    } finally {
                        isLoading = false
                    }
                }
            }


            when {
                isLoading -> {

                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Chargement des questions...",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                when {
                                    targetLane != null -> "Quiz spécialisé ${targetLane?.name}"
                                    quizMode.name == "LONG" -> "Quiz détaillé (12+ questions)"
                                    else -> "Quiz rapide (6 questions)"
                                },
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                !isFinished && questions.isNotEmpty() && currentQuestionIndex < questions.size -> {

                    val currentQuestion = questions[currentQuestionIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        LinearProgressIndicator(
                            progress = (currentQuestionIndex + 1).toFloat() / questions.size.toFloat(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            "Question ${currentQuestionIndex + 1} sur ${questions.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            when {
                                targetLane != null -> "Quiz ${targetLane?.name} spécialisé"
                                quizMode.name == "LONG" -> "Quiz Détaillé"
                                else -> "Quiz Rapide"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        QuestionSimpleScreen(
                            question = currentQuestion.prompt,
                            options = currentQuestion.options.map { it.label },
                            questionKey = currentQuestion.id, 
                            onValidate = { selectedIndex ->
                                if (selectedIndex >= 0 && selectedIndex < currentQuestion.options.size) {
                                    val selectedOption = currentQuestion.options[selectedIndex]
                                    answers[currentQuestion.id] = listOf(selectedOption.id)
                                    if (currentQuestionIndex < questions.size - 1) {
                                        currentQuestionIndex++
                                    } else {
                                        isFinished = true
                                    }
                                }
                            },
                            onQuit = {
                                navController.navigate(Routes.Home) {
                                    popUpTo(Routes.Home) { inclusive = false }
                                }
                            }
                        )
                    }
                }

                isFinished -> {

                    LaunchedEffect(answers) {
                        try {
                            val input = com.example.wum.domain.recommendation.RecommendationInput(
                                mode = quizMode,
                                lane = targetLane,
                                answers = answers
                            )
                            recommendations = dependencies.getRecommendUseCase()(input)
                        } catch (_: Exception) { }
                        if (recommendations.isNotEmpty()) {
                            RecommendationStore.set(recommendations.first(), targetLane)
                        }

                        navController.navigate(Routes.Result) {
                            popUpTo(Routes.Quiz) { inclusive = true }
                        }
                    }
                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Génération de ta recommandation...",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
        composable<Routes.Result> {
            val resultVm: com.example.wum.FeatureResultMain.ResultMainViewModel = viewModel()
            ResultMainScreen(
                viewModel = resultVm,
                onNavigateToFeatureHome = { navController.navigate(Routes.Home) },
                onRetry = {
                    RecommendationStore.clear()
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = false }
                    }
                }
            )
        }
        composable<Routes.Settings> {
            SettingsScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }
    }
    }
}

object Routes {
    @Serializable
    data object Home

    @Serializable
    data object Settings

    @Serializable
    data object Result

    @Serializable
    data object Quiz
}
