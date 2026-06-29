package com.example.model

data class Hero(
    val id: String,
    val name: String,
    val imageUrl: String,
    val lanes: List<String>,
    val tacticalTips: List<String>,
    val abilities: List<Ability>,
    val roles: List<RoleBuild>
)

data class Ability(
    val name: String,
    val description: String,
    val proTip: String,
    val imageUrl: String = "" // Optional, can be empty
)

data class RoleBuild(
    val roleName: String,
    val earlyGameItems: List<String>,
    val coreTimings: List<String>,
    val situationalItems: List<String>,
    val neutralItems: NeutralItems
)

data class NeutralItems(
    val tier1: List<String>,
    val tier2: List<String>,
    val tier3: List<String>,
    val tier4: List<String>,
    val tier5: List<String>
)
