package com.example.wum.integration

import com.example.wum.FeatureResultMain.ChampionLol.Lane
import com.example.wum.data.champions.ChampionSummary
import com.example.wum.domain.recommendation.*
import com.example.wum.engine.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test d'intégration pour vérifier le filtrage strict end-to-end
 */
class StrictFilteringIntegrationTest {

    private lateinit var featureExtractor: FeatureExtractor
    private lateinit var userVectorBuilder: UserVectorBuilder
    private lateinit var scoringEngine: ScoringEngine

    @Before
    fun setUp() {
        featureExtractor = FeatureExtractor()
        userVectorBuilder = UserVectorBuilder()
        scoringEngine = ScoringEngine()
    }

    @Test
    fun `test melee + AD preference filters correctly`() {
        // 1. Créer des champions test
        val champions = createTestChampions()

        // 2. Convertir en candidats
        val candidates = champions.map { summary ->
            val features = featureExtractor.extract(summary)
            ChampionCandidate(
                id = summary.id,
                name = summary.name,
                lanes = setOf(Lane.TOP), // Simplifié pour le test
                features = features
            )
        }

        // 3. Simuler des réponses quiz : corps-à-corps + dégâts physiques
        val answers = mapOf(
            "q_range" to listOf("melee"),
            "q_damage_type" to listOf("physical")
        )

        // 4. Créer les options simulées
        val allOptions = mapOf(
            "melee" to com.example.wum.data.questions.AnswerOption(
                id = "melee",
                label = "Corps-à-corps",
                weights = mapOf("isRanged" to 0, "durability" to 1)
            ),
            "physical" to com.example.wum.data.questions.AnswerOption(
                id = "physical",
                label = "Dégâts physiques (AD)",
                weights = mapOf("dmgAD" to 2, "dps" to 1)
            )
        )

        // 5. Construire le vecteur utilisateur
        val userVector = userVectorBuilder.buildUserVector(answers, allOptions)

        // 6. Générer les StrongPrefs (simulation de la logique de RecommendUseCase)
        val strongPrefs = StrongPrefs(
            preferMelee = true,
            requiredDamageType = "AD"
        )

        // 7. Générer les recommandations
        val recommendations = scoringEngine.generateRecommendations(
            userVector = userVector,
            candidates = candidates,
            strongPrefs = strongPrefs,
            targetLane = null,
            topK = 3
        )

        // 8. Vérifications
        assertTrue("Should have recommendations", recommendations.isNotEmpty())
        
        // Tous les champions recommandés doivent être melee + AD
        recommendations.forEach { recommendation ->
            val champion = recommendation.champion
            assertEquals("Champion should be melee", 0, champion.features.isRanged)
            assertTrue("Champion should have AD damage", champion.features.dmgAD > 0)
            
            // Le score doit être positif (pas de rejet)
            assertTrue("Score should be positive for compatible champion", 
                recommendation.score > 0)
        }

        // Aucun champion à distance ne devrait être recommandé
        val rangedIds = listOf("Ashe", "Ziggs", "Annie")
        recommendations.forEach { recommendation ->
            assertFalse("No ranged champion should be recommended",
                rangedIds.contains(recommendation.champion.id))
        }
    }

    @Test
    fun `test ranged + AP preference filters correctly`() {
        // 1. Créer des champions test
        val champions = createTestChampions()

        // 2. Convertir en candidats
        val candidates = champions.map { summary ->
            val features = featureExtractor.extract(summary)
            ChampionCandidate(
                id = summary.id,
                name = summary.name,
                lanes = setOf(Lane.MID), // Simplifié pour le test
                features = features
            )
        }

        // 3. Simuler des réponses quiz : à distance + dégâts magiques
        val answers = mapOf(
            "q_range" to listOf("ranged"),
            "q_damage_type" to listOf("magical")
        )

        // 4. Créer les options simulées
        val allOptions = mapOf(
            "ranged" to com.example.wum.data.questions.AnswerOption(
                id = "ranged",
                label = "À distance",
                weights = mapOf("isRanged" to 1, "poke" to 1)
            ),
            "magical" to com.example.wum.data.questions.AnswerOption(
                id = "magical",
                label = "Dégâts magiques (AP)",
                weights = mapOf("dmgAP" to 2, "burst" to 1)
            )
        )

        // 5. Construire le vecteur utilisateur
        val userVector = userVectorBuilder.buildUserVector(answers, allOptions)

        // 6. Générer les StrongPrefs
        val strongPrefs = StrongPrefs(
            preferRanged = true,
            requiredDamageType = "AP"
        )

        // 7. Générer les recommandations
        val recommendations = scoringEngine.generateRecommendations(
            userVector = userVector,
            candidates = candidates,
            strongPrefs = strongPrefs,
            targetLane = null,
            topK = 3
        )

        // 8. Vérifications
        assertTrue("Should have recommendations", recommendations.isNotEmpty())
        
        // Tous les champions recommandés doivent être ranged + AP
        recommendations.forEach { recommendation ->
            val champion = recommendation.champion
            assertEquals("Champion should be ranged", 1, champion.features.isRanged)
            assertTrue("Champion should have AP damage", champion.features.dmgAP > 0)
            
            // Le score doit être positif (pas de rejet)
            assertTrue("Score should be positive for compatible champion", 
                recommendation.score > 0)
        }

        // Aucun champion corps-à-corps ne devrait être recommandé
        val meleeIds = listOf("Malphite", "Vi", "Garen")
        recommendations.forEach { recommendation ->
            assertFalse("No melee champion should be recommended",
                meleeIds.contains(recommendation.champion.id))
        }
    }

    @Test
    fun `test mana preference filters correctly in integration`() {
        // 1. Créer des champions test avec différents types de ressources
        val champions = createResourceTestChampions()

        // 2. Convertir en candidats
        val candidates = champions.map { summary ->
            val features = featureExtractor.extract(summary)
            ChampionCandidate(
                id = summary.id,
                name = summary.name,
                lanes = setOf(Lane.MID), // Simplifié pour le test
                features = features
            )
        }

        // 3. Simuler des réponses quiz : préférence pour mana
        val answers = mapOf(
            "q_resource_type" to listOf("mana_user")
        )

        // 4. Créer les options simulées
        val allOptions = mapOf(
            "mana_user" to com.example.wum.data.questions.AnswerOption(
                id = "mana_user",
                label = "Mana (gestion requise)",
                weights = mapOf("resourceMana" to 1, "poke" to 1)
            )
        )

        // 5. Construire le vecteur utilisateur
        val userVector = userVectorBuilder.buildUserVector(answers, allOptions)

        // 6. Générer les StrongPrefs
        val strongPrefs = StrongPrefs(requiredResourceType = "MANA")

        // 7. Générer les recommandations
        val recommendations = scoringEngine.generateRecommendations(
            userVector = userVector,
            candidates = candidates,
            strongPrefs = strongPrefs,
            targetLane = null,
            topK = 3
        )

        // 8. Vérifications
        assertTrue("Should have recommendations", recommendations.isNotEmpty())
        
        // Tous les champions recommandés doivent utiliser mana
        recommendations.forEach { recommendation ->
            val champion = recommendation.champion
            assertEquals("Champion should use mana", 1, champion.features.resourceMana)
            assertEquals("Champion should not use energy", 0, champion.features.resourceEnergy)
            
            // Le score doit être positif (pas de rejet)
            assertTrue("Score should be positive for compatible champion", 
                recommendation.score > 0)
        }

        // Aucun champion énergie ou sans ressource ne devrait être recommandé
        val nonManaIds = listOf("Zed", "Garen", "Yasuo") // Champions énergie/sans ressource
        recommendations.forEach { recommendation ->
            assertFalse("No non-mana champion should be recommended",
                nonManaIds.contains(recommendation.champion.id))
        }
    }

    /**
     * Crée des champions test avec des profils variés
     */
    private fun createTestChampions(): List<ChampionSummary> {
        return listOf(
            // Champions corps-à-corps AD
            ChampionSummary(
                id = "Garen",
                name = "Garen",
                tags = listOf("Fighter", "Tank"),
                partype = "No Resource",
                info = ChampionSummary.Info(attack = 7, defense = 8, magic = 1, difficulty = 3)
            ),
            ChampionSummary(
                id = "Vi",
                name = "Vi",
                tags = listOf("Fighter", "Assassin"),
                partype = "Mana",
                info = ChampionSummary.Info(attack = 8, defense = 5, magic = 3, difficulty = 4)
            ),
            
            // Champions à distance AD
            ChampionSummary(
                id = "Ashe",
                name = "Ashe",
                tags = listOf("Marksman", "Support"),
                partype = "Mana",
                info = ChampionSummary.Info(attack = 8, defense = 3, magic = 2, difficulty = 4)
            ),
            
            // Champions à distance AP
            ChampionSummary(
                id = "Ziggs",
                name = "Ziggs",
                tags = listOf("Mage"),
                partype = "Mana",
                info = ChampionSummary.Info(attack = 2, defense = 4, magic = 9, difficulty = 4)
            ),
            ChampionSummary(
                id = "Annie",
                name = "Annie",
                tags = listOf("Mage"),
                partype = "Mana",
                info = ChampionSummary.Info(attack = 2, defense = 3, magic = 8, difficulty = 6)
            ),
            
            // Champions corps-à-corps mixte/tank
            ChampionSummary(
                id = "Malphite",
                name = "Malphite",
                tags = listOf("Tank", "Mage"),
                partype = "Mana",
                info = ChampionSummary.Info(attack = 5, defense = 9, magic = 7, difficulty = 2)
            )
        )
    }

    /**
     * Crée des champions test avec différents types de ressources
     */
    private fun createResourceTestChampions(): List<ChampionSummary> {
        return listOf(
            // Champions mana
            ChampionSummary(
                id = "Annie",
                name = "Annie",
                tags = listOf("Mage"),
                partype = "Mana", // ← Mana
                info = ChampionSummary.Info(attack = 2, defense = 3, magic = 8, difficulty = 6)
            ),
            ChampionSummary(
                id = "Ziggs",
                name = "Ziggs",
                tags = listOf("Mage"),
                partype = "Mana", // ← Mana
                info = ChampionSummary.Info(attack = 2, defense = 4, magic = 9, difficulty = 4)
            ),
            
            // Champions énergie
            ChampionSummary(
                id = "Zed",
                name = "Zed",
                tags = listOf("Assassin"),
                partype = "Energy", // ← Énergie
                info = ChampionSummary.Info(attack = 9, defense = 2, magic = 1, difficulty = 7)
            ),
            ChampionSummary(
                id = "Akali",
                name = "Akali",
                tags = listOf("Assassin"),
                partype = "Energy", // ← Énergie
                info = ChampionSummary.Info(attack = 5, defense = 3, magic = 8, difficulty = 8)
            ),
            
            // Champions sans ressource
            ChampionSummary(
                id = "Garen",
                name = "Garen",
                tags = listOf("Fighter", "Tank"),
                partype = "No Resource", // ← Sans ressource
                info = ChampionSummary.Info(attack = 7, defense = 8, magic = 1, difficulty = 3)
            ),
            ChampionSummary(
                id = "Yasuo",
                name = "Yasuo",
                tags = listOf("Fighter", "Assassin"),
                partype = "Flow", // ← Système spécial (considéré comme sans ressource)
                info = ChampionSummary.Info(attack = 8, defense = 4, magic = 4, difficulty = 10)
            )
        )
    }
}
