package com.example.flightsearch.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface UserPreferencesRepository {
    val searchQuery: Flow<String>
    suspend fun saveSearchQuery(query: String)
}

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    companion object {
        private val SEARCH_QUERY = stringPreferencesKey("search_query")
    }

    override val searchQuery: Flow<String> = dataStore.data.map { preferences ->
        preferences[SEARCH_QUERY] ?: ""
    }

    override suspend fun saveSearchQuery(query: String) {
        dataStore.edit { preferences ->
            preferences[SEARCH_QUERY] = query
        }
    }
}
