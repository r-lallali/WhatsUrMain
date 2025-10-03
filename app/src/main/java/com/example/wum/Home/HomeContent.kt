package com.example.wum.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wum.R
import com.example.wum.domain.recommendation.Mode


sealed class HomeTab(val title: String) {
    object Quiz : HomeTab("Quiz")
    object Theme : HomeTab("Thème")
}

data class LaneTheme(val id: String, val title: String, val drawableRes: Int)


data class QuizTypeTheme(val id: String, val title: String, val description: String, val drawableRes: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiModel: HomeUiModel,
    modifier: Modifier = Modifier,
    onNavigateToFeatureSettings: () -> Unit = {},
    onNavigateToFeatureQuiz: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf<HomeTab>(HomeTab.Quiz) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val laneThemes = listOf(
        LaneTheme("top", "Top", R.drawable.lane_top),
        LaneTheme("jungle", "Jungle", R.drawable.lane_jungle),
        LaneTheme("mid", "Mid", R.drawable.lane_mid),
        LaneTheme("adc", "ADC", R.drawable.lane_adc),
        LaneTheme("support", "Support", R.drawable.lane_support)
    )


    val quizTypes = listOf(
        QuizTypeTheme("short", "Quiz Rapide", "6 questions essentielles — 2 minutes", android.R.drawable.ic_media_play),
        QuizTypeTheme("long", "Quiz Détaillé", "12+ questions approfondies — 5 minutes", android.R.drawable.ic_menu_agenda)
    )

    val selectedLaneId by viewModel.selectedLaneId.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val questions by viewModel.questions.collectAsState()

    Scaffold(
        modifier = Modifier
            .then(modifier)
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(onClick = onNavigateToFeatureSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            CustomBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                HomeTab.Quiz -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wum),
                            contentDescription = "Image d'accueil WUM",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showBottomSheet = true }
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        Button(
                          onClick = {
                              if (!uiState.isQuizReady) {
    
                                  viewModel.startQuickQuiz()
                              }
                              showBottomSheet = true
                          },
                        modifier = Modifier.size(width = 300.dp, height = 80.dp)
                        ) {
                            Text("Démarrer un Quiz", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                HomeTab.Theme -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        Text("Types de questionnaire :", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(quizTypes) { quizType ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(96.dp)
                                        .clickable {
                                            when (quizType.id) {
                                                "short" -> viewModel.startQuickQuiz()
                                                "long" -> viewModel.startDetailedQuiz()
                                            }
                                            showBottomSheet = true
                                            selectedTab = HomeTab.Quiz

                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when (quizType.id) {
                                            "short" -> if (uiState.selectedQuizMode?.name == "SHORT" && uiState.selectedLaneId == null)
                                                MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                            "long" -> if (uiState.selectedQuizMode?.name == "LONG" && uiState.selectedLaneId == null)
                                                MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Surface(
                                            modifier = Modifier.size(80.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (quizType.id) {
                                                "short" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                "long" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = if (quizType.id == "short") "⚡" else "📋",
                                                    style = MaterialTheme.typography.headlineLarge
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(quizType.title, style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = quizType.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2
                                            )
                                        }

                                        RadioButton(
                                            selected = when (quizType.id) {
                                                "short" -> uiState.selectedQuizMode?.name == "SHORT" && uiState.selectedLaneId == null
                                                "long" -> uiState.selectedQuizMode?.name == "LONG" && uiState.selectedLaneId == null
                                                else -> false
                                            },
                                            onClick = {
                                                when (quizType.id) {
                                                    "short" -> viewModel.startQuickQuiz()
                                                    "long" -> viewModel.startDetailedQuiz()
                                                }
                                            }
                                        )
                                    }
                                }
                            }


                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Ou choisissez votre lane :", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(12.dp))
                            }


                            items(laneThemes) { lane ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(96.dp)
                                        .clickable { viewModel.selectLane(lane.id)
                                        showBottomSheet = true
                                        selectedTab = HomeTab.Quiz},
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedLaneId == lane.id)
                                            MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = lane.drawableRes),
                                            contentDescription = "${lane.title} image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(lane.title, style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = when (lane.id) {
                                                    "top" -> "Ligne du haut — tanks, fighters..."
                                                    "jungle" -> "Jungle — ganks, contrôle de la carte..."
                                                    "mid" -> "Mid — mages/assassins, pick plays..."
                                                    "adc" -> "Carry ADC — dégâts soutenus à distance..."
                                                    "support" -> "Support — peel, vision, utilité..."
                                                    else -> ""
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2
                                            )
                                        }

                                        RadioButton(
                                            selected = (selectedLaneId == lane.id),
                                            onClick = { viewModel.selectLane(lane.id) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.isQuizReady) {
                            Button(
                                onClick = {
                                    showBottomSheet = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    when {
                                        selectedLaneId != null -> "Lancer Quiz Spécialisé (${selectedLaneId?.uppercase()})"
                                        uiState.selectedQuizMode?.name == "SHORT" -> "Lancer Quiz Rapide"
                                        uiState.selectedQuizMode?.name == "LONG" -> "Lancer Quiz Détaillé"
                                        else -> "Lancer Quiz"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        val selectedLane = laneThemes.firstOrNull { it.id == selectedLaneId }

        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Nombre de questions : ${questions.size}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (selectedLane != null) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = selectedLane.drawableRes),
                                contentDescription = "${selectedLane.title} image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = selectedLane.title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = when (selectedLane.id) {
                                        "top" -> "Quiz spécialisé Toplane ! Tanks, bruisers..."
                                        "jungle" -> "Quiz spécialisé Jungle ! Contrôle de la carte..."
                                        "mid" -> "Quiz spécialisé Midlane ! Mages, assassins..."
                                        "adc" -> "Quiz spécialisé ADC ! Dégâts constants..."
                                        "support" -> "Quiz spécialisé Support ! Vision, peel..."
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                } else {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (uiState.selectedQuizMode == Mode.SHORT) "⚡" else "📋",
                                    style = MaterialTheme.typography.displayLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (uiState.selectedQuizMode) {
                                        Mode.SHORT -> "Quiz Rapide"
                                        Mode.LONG -> "Quiz Détaillé"
                                        else -> "Quiz Général"
                                    },
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (uiState.selectedQuizMode) {
                                        Mode.SHORT -> "6 questions essentielles - 2 minutes"
                                        Mode.LONG -> "12+ questions approfondies - 5 minutes"
                                        else -> "Questions générales"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        showBottomSheet = false
                        onNavigateToFeatureQuiz()
                    },
                    enabled = uiState.isQuizReady,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isQuizReady) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Text(
                        when {
                            selectedLane != null -> "Lancer Quiz Spécialisé"
                            uiState.selectedQuizMode == Mode.SHORT -> "Lancer Quiz Rapide"
                            uiState.selectedQuizMode == Mode.LONG -> "Lancer Quiz Détaillé"
                            else -> "Lancer le Quiz"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(HomeTab.Quiz, HomeTab.Theme).forEach { tab ->
                val isSelected = tab == selectedTab

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tab) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                    shadowElevation = if (isSelected) 8.dp else 2.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
