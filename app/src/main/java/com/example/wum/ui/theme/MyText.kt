package com.example.wum.ui.theme


import android.R
import android.R.attr.text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MyText(
    modifier: Modifier = Modifier,
    text: String = "",
    style: MyTextStyle = MyTextStyle.Body
) {
    Text(
        modifier = modifier,
        text = text,
        style = when (style) {
            MyTextStyle.Title -> MaterialTheme.typography.headlineLarge
            MyTextStyle.Subtitle -> MaterialTheme.typography.headlineMedium
            MyTextStyle.Body -> MaterialTheme.typography.bodyLarge
        },
    )
}

enum class MyTextStyle {
    Title,
    Subtitle,
    Body
}