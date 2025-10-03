package com.example.wum

import android.app.Application
import com.example.wum.FeatureResultMain.ChampionLol.ChampionRepository

class WumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ChampionRepository.init(this)
    }
}