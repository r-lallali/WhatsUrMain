package com.example.wum.di

import android.content.Context
import com.example.wum.core.io.*
import com.example.wum.data.champions.*
import com.example.wum.data.lanes.*
import com.example.wum.data.questions.*
import com.example.wum.domain.recommendation.*
import com.example.wum.engine.*

object RecommendationContainer {
    private var _instance: RecommendationDependencies? = null

    fun initialize(context: Context) {
        if (_instance == null) {
            _instance = RecommendationDependencies(context)
        }
    }

    fun getInstance(): RecommendationDependencies {
        return _instance ?: throw IllegalStateException("RecommendationContainer not initialized")
    }
}

class RecommendationDependencies(context: Context) {
    private val assetProvider: AssetProvider = AndroidAssetProvider(context)
    private val championRepository: com.example.wum.data.champions.ChampionRepository =
        LocalChampionRepository(assetProvider)
    private val lanesRepository: LanesRepository = LanesRepositoryImpl(assetProvider)
    private val questionBankRepository: QuestionBankRepository = QuestionBankRepositoryImpl(assetProvider)
    private val featureExtractor = FeatureExtractor()
    private val userVectorBuilder = UserVectorBuilder()
    private val scoringEngine = ScoringEngine()
    private val recommendUseCase: RecommendUseCase = RecommendUseCaseImpl(
        championRepository = championRepository,
        lanesRepository = lanesRepository,
        questionBankRepository = questionBankRepository,
        featureExtractor = featureExtractor,
        userVectorBuilder = userVectorBuilder,
        scoringEngine = scoringEngine
    )

    fun getQuestionBankRepository(): QuestionBankRepository = questionBankRepository
    fun getRecommendUseCase(): RecommendUseCase = recommendUseCase
}
