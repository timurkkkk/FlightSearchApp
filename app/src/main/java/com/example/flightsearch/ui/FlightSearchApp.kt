package com.example.flightsearch.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.data.AppContainer
import com.example.flightsearch.ui.screens.FlightSearchScreen
import com.example.flightsearch.ui.viewmodels.FlightSearchViewModel
import com.example.flightsearch.ui.viewmodels.FlightSearchViewModelFactory

@Composable
fun FlightSearchApp(
    appContainer: AppContainer
) {
    val viewModel: FlightSearchViewModel = viewModel(
        factory = FlightSearchViewModelFactory(
            appContainer.flightRepository,
            appContainer.userPreferencesRepository
        )
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    FlightSearchScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onAirportSelected = viewModel::onAirportSelected,
        onToggleFavorite = viewModel::toggleFavorite,
        onClearSearch = viewModel::clearSearch
    )
}
