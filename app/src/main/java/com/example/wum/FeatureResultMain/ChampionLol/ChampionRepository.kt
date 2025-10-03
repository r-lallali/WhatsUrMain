package com.example.wum.FeatureResultMain.ChampionLol

import android.content.Context
import com.example.wum.R
import org.json.JSONArray

object ChampionRepository {

    private var cache: Map<String, ChampionInfo>? = null

    fun init(context: Context) {
        if (cache != null) return
        val json = context.assets.open("champions.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        val map = mutableMapOf<String, ChampionInfo>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getString("id")
            val lane = Lane.valueOf(o.getString("lane"))
            val portraitRes = resIdForPortrait(id)
            val laneIconRes = resIdForLane(lane)
            map[id] = ChampionInfo(
                id = id,
                displayName = o.getString("name"),
                lane = lane,
                portraitRes = portraitRes,
                frameRes = R.drawable.cadre,
                laneIconRes = laneIconRes
            )
        }
        cache = map
    }

    fun get(id: String): ChampionInfo? = cache?.get(id)



    private fun resIdForPortrait(id: String): Int {
        val name = "champion_${id}"
        val res = R.drawable::class.java.fields.firstOrNull { it.name == name }
        return res?.getInt(null) ?: R.drawable.ahri
    }

    private fun resIdForLane(lane: Lane): Int = when (lane) {
        Lane.TOP -> R.drawable.position_top
        Lane.JUNGLE -> R.drawable.position_jungle
        Lane.MID -> R.drawable.position_mid
        Lane.ADC -> R.drawable.position_bot
        Lane.SUPPORT -> R.drawable.position_support
    }
}
