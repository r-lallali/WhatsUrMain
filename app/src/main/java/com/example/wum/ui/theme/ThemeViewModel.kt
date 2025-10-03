package com.example.wum.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {
    val themeMode = mutableStateOf(ThemeMode.SYSTEM)

    val colorPack = mutableStateOf(ColorPack.BLUE)

    fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
    }

    fun setColorPack(pack: ColorPack) {
        colorPack.value = pack
    }
}