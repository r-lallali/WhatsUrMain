package com.example.wum.FeatureResultMain.ChampionLol

import androidx.annotation.DrawableRes

data class ChampionInfo(
    val id: String,              // ex: "ahri"
    val displayName: String,     // ex: "Ahri"
    val lane: Lane,
    @DrawableRes val portraitRes: Int,
    @DrawableRes val frameRes: Int,
    @DrawableRes val laneIconRes: Int
)