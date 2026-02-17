package com.example.retromultiplataformkotin.model

import org.jetbrains.compose.resources.DrawableResource
import retromultiplataformkotin.composeapp.generated.resources.*
import retromultiplataformkotin.composeapp.generated.resources.Res

data class TeamMember(
    val id: String,
    val name: String,
    val role: String,
    val avatarRes: DrawableResource,
    val isLeader: Boolean = false,
)

enum class RetroPhase(val title: String, val emoji: String, val description: String) {
    WAITING("Sala de Espera", "🎮", "Esperando a que todos se unan..."),
    MOOD_CHECK("Estado de Ánimo", "😊", "¿Cómo te fue este sprint?"),
    WENT_WELL("¿Qué salió bien?", "🌟", "Comparte los logros del sprint"),
    TO_IMPROVE("¿Qué podemos mejorar?", "🔧", "Identifica áreas de mejora"),
    ACTION_ITEMS("Acciones", "🎯", "Votemos las acciones a tomar"),
    RESULTS("Resultados", "🏆", "Resumen de la retrospectiva"),
    LEADER_VOTE("Vota por tu Líder", "⭐", "Reparte tus estrellas"),
}

data class RetroCard(
    val id: String,
    val authorId: String,
    val content: String,
    val phase: RetroPhase,
    val votes: Int = 0,
    val isApproved: Boolean = false,
)

data class RetroSession(
    val id: String = "",
    val phase: RetroPhase = RetroPhase.MOOD_CHECK,
    val cards: List<RetroCard> = emptyList(),
    val members: List<TeamMember> = emptyList(),
)

enum class SprintMood(val emoji: String, val label: String) {
    GREAT("😄", "Genial"),
    GOOD("🙂", "Bien"),
    NEUTRAL("😐", "Normal"),
    TOUGH("😓", "Difícil"),
    FRUSTRATED("😤", "Frustrante"),
}

object TeamData {
    val members = listOf(
        TeamMember("elizabeth", "Elizabeth", "Android Dev", Res.drawable.avatar_Eli, isLeader = true),
        TeamMember("david", "David", "Android Dev", Res.drawable.avatar_david),
        TeamMember("marcos", "Marcos", "Android Dev", Res.drawable.avatar_marcos),
        TeamMember("juan", "Juan", "Android Dev", Res.drawable.avatar_juan),
        TeamMember("shirley", "Shirley", "Android Dev", Res.drawable.avatar_shirley),
        TeamMember("luistorres", "Luis Torres", "Android Dev", Res.drawable.avatar_luistorres),
        TeamMember("freddy", "Freddy", "Android Dev", Res.drawable.avatar_freddy),
        TeamMember("hector", "Héctor", "Android Dev", Res.drawable.avatar_hector),
        TeamMember("nestor", "Néstor", "Android Dev", Res.drawable.avatar_nestor),
        TeamMember("leonar", "Leonar", "Android Dev", Res.drawable.avatar_leonar),
        TeamMember("cecilia", "Cecilia", "Android Dev", Res.drawable.avatar_cecilia),
        TeamMember("carlos", "Carlos", "Android Dev", Res.drawable.avatar_carlos),
        TeamMember("jofree", "Jofree", "Android Dev", Res.drawable.avatar_jofree),
        TeamMember("lizette", "Lizette", "Android Dev", Res.drawable.avatar_lizette),
        TeamMember("santiago", "Santiago", "Android Dev", Res.drawable.avatar_Santiago),
        TeamMember("victor", "Victor", "Android Dev", Res.drawable.avatar_Vicotr),
    )

    val leaderIds = listOf("juan", "lizette", "marcos", "david", "nestor")
}
