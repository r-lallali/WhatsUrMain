package com.example.wum.domain.recommendation

import com.example.wum.data.champions.ChampionRepository
import com.example.wum.data.lanes.LanesRepository
import com.example.wum.data.questions.QuestionBankRepository
import com.example.wum.engine.*

/**
 * Use case principal pour générer des recommandations de champions
 */
interface RecommendUseCase {
    suspend operator fun invoke(input: RecommendationInput): List<Recommendation>
}

class RecommendUseCaseImpl(
    private val championRepository: ChampionRepository,
    private val lanesRepository: LanesRepository,
    private val questionBankRepository: QuestionBankRepository,
    private val featureExtractor: FeatureExtractor,
    private val userVectorBuilder: UserVectorBuilder,
    private val scoringEngine: ScoringEngine
) : RecommendUseCase {

    override suspend fun invoke(input: RecommendationInput): List<Recommendation> {
        // 1. Charger les champions depuis Data_champs.json
        val championSummaries = championRepository.loadAll()

        // 2. Charger les questions pour récupérer les options et leurs poids
        val questionBank = questionBankRepository.loadQuestions(input.mode, input.lane)

        // 3. Créer une map de toutes les options disponibles
        val allOptions = questionBank.questions
            .flatMap { it.options }
            .associateBy { it.id }

        // 4. Construire le vecteur utilisateur à partir des réponses
        val userVector = userVectorBuilder.buildUserVector(input.answers, allOptions)

        // 5. Générer les StrongPrefs dynamiquement à partir des réponses
        val dynamicStrongPrefs = generateStrongPrefsFromAnswers(input.answers, input.strongPrefs)

        // 6. Convertir les champions en candidats avec leurs features
        val candidates = championSummaries.map { summary ->
            val features = featureExtractor.extract(summary)
            val lanes = lanesRepository.getLanes(summary.name)

            ChampionCandidate(
                id = summary.id,
                name = summary.name,
                lanes = lanes,
                features = features
            )
        }

        // 7. Créer le gestionnaire de diversité
        val diversityManager = DiversityManager()
        
        // 8. Générer les recommandations avec le moteur de scoring et la diversité
        return scoringEngine.generateRecommendations(
            userVector = userVector,
            candidates = candidates,
            strongPrefs = dynamicStrongPrefs,
            targetLane = input.lane,
            topK = 3,
            diversityManager = diversityManager
        )
    }

    /**
     * Génère les StrongPrefs dynamiquement à partir des réponses du quiz
     */
    private fun generateStrongPrefsFromAnswers(
        answers: Map<String, List<String>>,
        baseStrongPrefs: StrongPrefs
    ): StrongPrefs {
        var preferRanged: Boolean? = null
        var preferMelee: Boolean? = null
        var requiredDamageType: String? = null
        var requiredResourceType: String? = null
        var dislikeMana = baseStrongPrefs.dislikeMana
        var preferSimple = baseStrongPrefs.preferSimple

        // Analyser les réponses pour détecter les préférences fortes
        answers.forEach { (questionId, selectedOptions) ->
            when (questionId) {
                "q_range" -> {
                    selectedOptions.forEach { optionId ->
                        when (optionId) {
                            "melee" -> {
                                preferMelee = true
                                preferRanged = false
                            }
                            "ranged" -> {
                                preferRanged = true
                                preferMelee = false
                            }
                        }
                    }
                }
                "q_damage_type" -> {
                    selectedOptions.forEach { optionId ->
                        when (optionId) {
                            "physical" -> requiredDamageType = "AD"
                            "magical" -> requiredDamageType = "AP"
                            "hybrid" -> requiredDamageType = "HYBRID"
                        }
                    }
                }
                "q_complexity" -> {
                    selectedOptions.forEach { optionId ->
                        when (optionId) {
                            "simple" -> preferSimple = true
                        }
                    }
                }
                "q_resource_type" -> {
                    selectedOptions.forEach { optionId ->
                        when (optionId) {
                            "mana_user" -> requiredResourceType = "MANA"
                            "energy_user" -> requiredResourceType = "ENERGY"
                            "no_resource" -> requiredResourceType = "NONE"
                        }
                    }
                }
            }
        }

        return StrongPrefs(
            preferRanged = preferRanged ?: baseStrongPrefs.preferRanged,
            preferMelee = preferMelee ?: baseStrongPrefs.preferMelee,
            requiredDamageType = requiredDamageType ?: baseStrongPrefs.requiredDamageType,
            requiredResourceType = requiredResourceType ?: baseStrongPrefs.requiredResourceType,
            dislikeMana = dislikeMana,
            preferSimple = preferSimple
        )
    }
}
