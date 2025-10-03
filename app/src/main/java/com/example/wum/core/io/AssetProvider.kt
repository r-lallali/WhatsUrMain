package com.example.wum.core.io

import android.content.Context

/**
 * Interface d'abstraction pour l'accès aux assets
 * Permet de faciliter les tests et de centraliser l'accès aux fichiers
 */
interface AssetProvider {
    /**
     * Lit le contenu d'un fichier asset en tant que texte
     */
    suspend fun readText(assetPath: String): String
}

/**
 * Implémentation par défaut utilisant le contexte Android
 */
class AndroidAssetProvider(private val context: Context) : AssetProvider {
    override suspend fun readText(assetPath: String): String {
        return context.assets.open(assetPath).bufferedReader().use { it.readText() }
    }
}
