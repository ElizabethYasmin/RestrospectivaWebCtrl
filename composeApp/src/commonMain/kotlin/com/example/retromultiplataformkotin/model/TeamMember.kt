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
    WENT_WELL("¿Qué salió bien?", "🌟", "Comparte los logros del sprint"),
    TO_IMPROVE("¿Qué podemos mejorar?", "🔧", "Identifica áreas de mejora"),
    ACTION_ITEMS("Acciones", "🎯", "Votemos las acciones a tomar"),
    RESULTS("Resultados", "🏆", "Resumen de la retrospectiva"),
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
    val phase: RetroPhase = RetroPhase.WAITING,
    val cards: List<RetroCard> = emptyList(),
    val members: List<TeamMember> = emptyList(),
)

object TeamData {
    val members = listOf(
        TeamMember("sho", "Sho", "Tech Lead", Res.drawable.avatar_sho, isLeader = true),
        TeamMember("david", "David", "Android Dev", Res.drawable.avatar_david),
        TeamMember("marcos", "Marcos", "Android Dev", Res.drawable.avatar_marcos),
        TeamMember("juan", "Juan", "iOS Dev", Res.drawable.avatar_juan),
        TeamMember("shirley", "Shirley", "iOS Dev", Res.drawable.avatar_shirley),
        TeamMember("luistorres", "Luis Torres", "Android Dev", Res.drawable.avatar_luistorres),
        TeamMember("freddy", "Freddy", "QA Engineer", Res.drawable.avatar_freddy),
        TeamMember("hector", "Héctor", "Backend Dev", Res.drawable.avatar_hector),
        TeamMember("nestor", "Néstor", "Android Dev", Res.drawable.avatar_nestor),
        TeamMember("leonar", "Leonar", "iOS Dev", Res.drawable.avatar_leonar),
        TeamMember("cecilia", "Cecilia", "UX Designer", Res.drawable.avatar_cecilia),
        TeamMember("carlos", "Carlos", "Android Dev", Res.drawable.avatar_carlos),
        TeamMember("israel", "Israel", "Scrum Master", Res.drawable.avatar_israel),
        TeamMember("jofree", "Jofree", "iOS Dev", Res.drawable.avatar_jofree),
        TeamMember("lizette", "Lizette", "QA Engineer", Res.drawable.avatar_lizette),
    )
}
