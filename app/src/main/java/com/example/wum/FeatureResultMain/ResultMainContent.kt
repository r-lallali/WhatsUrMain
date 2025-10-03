
package com.example.wum.FeatureResultMain

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wum.R
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultMainContent(
    uiModel: ResultMainUiModel,
    modifier: Modifier = Modifier,
    headerText: String = uiModel.headerText,
    @DrawableRes championRes: Int? = null,
    @DrawableRes frameRes: Int? = null,
    @DrawableRes positionRes: Int? = null,
    positionLabel: String? = null,
    championWidthScale: Float = 0.84f,
    championHeightScale: Float = 0.98f,
    championTopRadius: Dp = 50.dp,
    frameScale: Float = 1.00f,
    showDebugBounds: Boolean = false,
    onNavigateToFeatureHome: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    val championName = uiModel.name.ifBlank { "Champion" }
    val baseWidth = 260.dp
    val baseHeight = 360.dp
    val containerWidthScale = max(frameScale, championWidthScale)
    val containerHeightScale = max(frameScale, championHeightScale)
    val championShape = RoundedCornerShape(
        topStart = championTopRadius,
        topEnd = championTopRadius,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    val context = LocalContext.current


    val championResId = remember(championName) {
        val resName = championName
            .lowercase()
            .replace("é", "e")
            .replace("è", "e")
            .replace("ê", "e")
            .replace("ë", "e")
            .replace("á", "a")
            .replace("à", "a")
            .replace("ä", "a")
            .replace("â", "a")
            .replace("ç", "c")
            .replace("î", "i")
            .replace("ï", "i")
            .replace("ô", "o")
            .replace("ö", "o")
            .replace("û", "u")
            .replace("ü", "u")
            .replace("'", "")
            .replace(" ", "")
        val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
        if (id != 0) id else R.drawable.ahri
    }

    Scaffold { inner ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = championName,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(20.dp))


                    Box(
                        modifier = Modifier
                            .size(
                                width = baseWidth * containerWidthScale,
                                height = baseHeight * containerHeightScale
                            )
                            .aspectRatio(260f / 360f)
                            .then(if (showDebugBounds) Modifier.border(1.dp, Color.Blue) else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(championResId),
                            contentDescription = championName,
                            modifier = Modifier
                            .size(
                                width = baseWidth * championWidthScale,
                                height = baseHeight * championHeightScale
                        )
                                .clip(championShape)
                                .then(if (showDebugBounds) Modifier.border(1.dp, Color.Green, championShape) else Modifier),
                            contentScale = ContentScale.Crop
                        )
                        if (frameRes != null) {
                            Image(
                                painter = painterResource(frameRes),
                                contentDescription = "Cadre",
                                modifier = Modifier
                                    .size(
                                        width = baseWidth * frameScale,
                                        height = baseHeight * frameScale
                                    )
                                    .then(if (showDebugBounds) Modifier.border(1.dp, Color.Red) else Modifier),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))


                    if (positionRes != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(positionRes),
                                contentDescription = "Position",
                                modifier = Modifier.size(48.dp),
                                contentScale = ContentScale.Fit
                            )
                            if (!positionLabel.isNullOrBlank()) {
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = positionLabel,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    // NOTE : Suppression volontaire de l'explication / score / description selon la demande
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = onRetry) { Text("Recommencer") }
                Button(onClick = onNavigateToFeatureHome) { Text("Accueil") }
            }
        }
    }
}

@Preview(name = "Result", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun Preview_Result() {
    ResultMainContent(
        uiModel = ResultMainUiModel(
            headerText = "Tu incarnes...",
            name = "Ahri",
            championRes = R.drawable.ahri,
            frameRes = R.drawable.cadre,
            positionRes = R.drawable.position_mid,
            positionLabel = "Mid"
        ),
        championRes = R.drawable.ahri,
        frameRes = R.drawable.cadre,
        positionRes = R.drawable.position_mid,
        positionLabel = "Mid"
    )
}
