package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.HeroRepository
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.model.Hero
import com.example.model.Ability
import com.example.model.RoleBuild
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeroViewModel : ViewModel() {
    private val _heroState = MutableStateFlow<HeroState>(HeroState.Loading)
    val heroState: StateFlow<HeroState> = _heroState.asStateFlow()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun loadHero(heroId: String) {
        viewModelScope.launch {
            _heroState.value = HeroState.Loading
            val hero = HeroRepository.getHeroById(heroId)
            
            if (hero == null) {
                _heroState.value = HeroState.Error("Hero not found")
                return@launch
            }

            _heroState.value = HeroState.Success(hero)
        }
    }

    fun generateGuide(hero: Hero, allies: String, enemies: String) {
        viewModelScope.launch {
            _heroState.value = HeroState.GeneratingBuild(hero)
            try {
                val prompt = """
                    You are a Dota 2 expert. Please provide tactical tips, abilities, and role builds for the hero "${hero.name}".
                    The user has allied heroes: "$allies" and enemy heroes: "$enemies". 
                    Please adjust your tips, recommended role, and item builds to specifically counter the enemies and synergize with the allies!
                    Return ONLY a valid JSON object with the following structure (do not include markdown code blocks like ```json):
                    {
                      "lanes": ["Safelane", "Midlane"],
                      "tacticalTips": ["Tip 1", "Tip 2"],
                      "abilities": [
                        {
                          "name": "Ability Name",
                          "description": "Ability Description",
                          "proTip": "Pro Tip"
                        }
                      ],
                      "roles": [
                        {
                          "roleName": "Core Build",
                          "earlyGameItems": ["Item1", "Item2"],
                          "coreTimings": ["Item3", "Item4"],
                          "situationalItems": ["Item5"],
                          "neutralItems": {
                            "tier1": ["Item1"],
                            "tier2": ["Item2"],
                            "tier3": ["Item3"],
                            "tier4": ["Item4"],
                            "tier5": ["Item5"]
                          }
                        }
                      ]
                    }
                    Provide detailed, high-quality analysis. Use Persian (Farsi) language for texts except for item/ability/role names.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(responseMimeType = "application/json") // Note: Some models need responseMimeType in GenerationConfig
                )

                val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                val jsonResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (jsonResponse != null) {
                    val adapter = moshi.adapter(GeminiHeroDetails::class.java)
                    // clean markdown if any
                    val cleanJson = jsonResponse.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                    val details = adapter.fromJson(cleanJson)
                    
                    if (details != null) {
                        val updatedHero = hero.copy(
                            lanes = details.lanes,
                            tacticalTips = details.tacticalTips,
                            abilities = details.abilities,
                            roles = details.roles
                        )
                        HeroRepository.updateHero(updatedHero)
                        _heroState.value = HeroState.Success(updatedHero)
                        return@launch
                    }
                }
                _heroState.value = HeroState.Error("Failed to parse Gemini response")
            } catch (e: Exception) {
                e.printStackTrace()
                _heroState.value = HeroState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed class HeroState {
    object Loading : HeroState()
    data class GeneratingBuild(val hero: Hero) : HeroState()
    data class Success(val hero: Hero) : HeroState()
    data class Error(val message: String) : HeroState()
}

@JsonClass(generateAdapter = true)
data class GeminiHeroDetails(
    val lanes: List<String>,
    val tacticalTips: List<String>,
    val abilities: List<Ability>,
    val roles: List<RoleBuild>
)
