package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.flightsearch.data.database.FlightDatabase
import com.example.flightsearch.data.repository.FlightRepository
import com.example.flightsearch.data.repository.FlightRepositoryImpl
import com.example.flightsearch.data.repository.UserPreferencesRepository
import com.example.flightsearch.data.repository.UserPreferencesRepositoryImpl

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

interface AppContainer {
    val flightRepository: FlightRepository
    val userPreferencesRepository: UserPreferencesRepository
}

class AppContainerImpl(private val applicationContext: Context) : AppContainer {

    private val database by lazy {
        FlightDatabase.getDatabase(applicationContext)
    }

    override val flightRepository: FlightRepository by lazy {
        FlightRepositoryImpl(
            database.airportDao(),
            database.favoriteDao()
        )
    }
    
    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(applicationContext.dataStore)
    }
}
