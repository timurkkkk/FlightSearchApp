package com.example.flightsearch.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.database.entity.Airport
import com.example.flightsearch.data.database.entity.Favorite
import com.example.flightsearch.data.model.Flight
import com.example.flightsearch.data.repository.FlightRepository
import com.example.flightsearch.data.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class FlightSearchViewModel(
    private val flightRepository: FlightRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedAirport = MutableStateFlow<Airport?>(null)
    private val _flights = MutableStateFlow<List<Flight>>(emptyList())
    private val _favorites = flightRepository.getAllFavorites()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _suggestedAirports = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                MutableStateFlow(emptyList())
            } else {
                flightRepository.searchAirports(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<FlightSearchUiState> = combine(
        _searchQuery,
        _suggestedAirports,
        _selectedAirport,
        _flights,
        _favorites
    ) { query, suggestedAirports, selectedAirport, flights, favorites ->
        FlightSearchUiState(
            searchQuery = query,
            suggestedAirports = suggestedAirports,
            selectedAirport = selectedAirport,
            flights = flights,
            favorites = favorites
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FlightSearchUiState()
    )

    init {
        viewModelScope.launch {
            userPreferencesRepository.searchQuery.collect { savedQuery ->
                if (savedQuery.isNotEmpty()) {
                    _searchQuery.value = savedQuery
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _selectedAirport.value = null
        _flights.value = emptyList()
        
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery(query)
        }
    }

    fun onAirportSelected(airport: Airport) {
        _selectedAirport.value = airport
        viewModelScope.launch {
            _flights.value = flightRepository.getFlightsFromAirport(airport)
        }
    }

    fun toggleFavorite(flight: Flight) {
        viewModelScope.launch {
            flightRepository.toggleFavorite(flight)
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _selectedAirport.value = null
        _flights.value = emptyList()
        
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery("")
        }
    }
}


data class FlightSearchUiState(
    val searchQuery: String = "",
    val suggestedAirports: List<Airport> = emptyList(),
    val selectedAirport: Airport? = null,
    val flights: List<Flight> = emptyList(),
    val favorites: List<Favorite> = emptyList()
)

class FlightSearchViewModelFactory(
    private val flightRepository: FlightRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlightSearchViewModel::class.java)) {
            return FlightSearchViewModel(flightRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
