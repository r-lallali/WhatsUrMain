package com.example.wum.FeatureResultMain

import androidx.annotation.DrawableRes

data class ResultMainUiModel(
    val headerText: String = "Ton personnage",
    val name: String = "",
    @DrawableRes val championRes: Int? = null,
    @DrawableRes val frameRes: Int? = null,
    @DrawableRes val positionRes: Int? = null,
    val positionLabel: String? = null
)
