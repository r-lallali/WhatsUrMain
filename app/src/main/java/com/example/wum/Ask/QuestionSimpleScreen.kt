package com.example.wum.Ask

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wum.R

@Composable
fun QuestionSimpleScreen(
    question: String,
    options: List<String>,
    onValidate: (selectedIndex: Int) -> Unit,
    onQuit: () -> Unit = {},
    questionKey: Any = question
) {
    var selected by remember(questionKey) { mutableStateOf(-1) }
    var showQuitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showQuitDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            question,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEachIndexed { idx, opt ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .heightIn(min = 48.dp)
                        .clickable { 
                            if (idx >= 0 && idx < options.size) {
                                selected = idx
                            }
                        }
                ) {
                    RadioButton(
                        selected = selected == idx,
                        onClick = { 
                            if (idx >= 0 && idx < options.size) {
                                selected = idx
                            }
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(opt, style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        // Message d'aide si aucune réponse sélectionnée
        if (selected == -1) {
            Text(
                "Sélectionnez une réponse pour continuer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = { showQuitDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Quitter")
            }
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = { 
                    // Protection contre le crash : vérifier que l'index est valide
                    if (selected >= 0 && selected < options.size) {
                        onValidate(selected)
                    }
                },
                enabled = selected != -1,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected != -1) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Valider")
            }
        }
    }

    // Dialog de confirmation pour quitter
    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = {
                Text(
                    "Quitter le quiz ?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Êtes-vous sûr de vouloir quitter le quiz ? Votre progression sera perdue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQuitDialog = false
                        onQuit()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Quitter")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showQuitDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Continuer")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

@Composable
fun QuizSimpleScreen(
    questions: List<QuestionUi>,
    onFinish: (answers: List<List<Int>>) -> Unit,
    onQuit: () -> Unit = {}
) {
    var current by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf(List(questions.size) { emptyList<Int>() }) }
    var showQuitDialog by remember { mutableStateOf(false) }
    val isLast = current == questions.lastIndex
    val q = questions[current]

    BackHandler {
        if (current > 0) {
            current--
        } else {
            showQuitDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Question ${current + 1} / ${questions.size}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (q.imageUrl != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = q.imageUrl,
                    contentDescription = "Image question",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                    fallback = painterResource(id = R.drawable.ic_launcher_foreground)
                )
            }
        }

        Text(
            q.question,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            q.options.forEachIndexed { idx, opt ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .heightIn(min = 48.dp)
                        .clickable {
                            answers = answers.toMutableList().also { list ->
                                list[current] = if (q.multi) {
                                    if (idx in list[current]) list[current] - idx else list[current] + idx
                                } else listOf(idx)
                            }
                        }
                ) {
                    if (q.multi) {
                        Checkbox(
                            checked = idx in answers[current],
                            onCheckedChange = {
                                answers = answers.toMutableList().also { list ->
                                    list[current] = if (idx in list[current]) list[current] - idx else list[current] + idx
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    } else {
                        RadioButton(
                            selected = idx in answers[current],
                            onClick = {
                                answers = answers.toMutableList().also { list ->
                                    list[current] = listOf(idx)
                                }
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(opt, style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = { 
                    if (current > 0) {
                        current--
                    } else {
                        showQuitDialog = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (current > 0) "Précédent" else "Quitter")
            }
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = {
                    if (isLast) onFinish(answers)
                    else current++
                },
                enabled = answers[current].isNotEmpty(),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (isLast) "Valider" else "Suivant")
            }
        }
    }

    // Dialog de confirmation pour quitter
    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = {
                Text(
                    "Quitter le quiz ?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Êtes-vous sûr de vouloir quitter le quiz ? Votre progression sera perdue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQuitDialog = false
                        onQuit()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Quitter")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showQuitDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Continuer")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

data class QuestionUi(
    val question: String,
    val options: List<String>,
    val multi: Boolean = false,
    val imageUrl: String? = null
)
