package com.example.data

import android.content.Context
import com.example.model.Hero
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStreamReader

object HeroRepository {
    var heroes: List<Hero> = emptyList()
        private set

    fun initialize(context: Context) {
        if (heroes.isNotEmpty()) return
        
        try {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            
            val listType = Types.newParameterizedType(List::class.java, Hero::class.java)
            val adapter = moshi.adapter<List<Hero>>(listType)
            
            val inputStream = context.assets.open("heroes.json")
            val reader = InputStreamReader(inputStream)
            val jsonString = reader.readText()
            
            heroes = adapter.fromJson(jsonString) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getHeroById(id: String): Hero? {
        return heroes.find { it.id == id }
    }

    fun updateHero(updatedHero: Hero) {
        heroes = heroes.map { if (it.id == updatedHero.id) updatedHero else it }
    }
}
