package com.example.flightsearch.data.repository

import com.example.flightsearch.data.database.dao.AirportDao
import com.example.flightsearch.data.database.dao.FavoriteDao
import com.example.flightsearch.data.database.entity.Airport
import com.example.flightsearch.data.database.entity.Favorite
import com.example.flightsearch.data.model.Flight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

interface FlightRepository {
    fun searchAirports(query: String): Flow<List<Airport>>
    suspend fun getFlightsFromAirport(airport: Airport): List<Flight>
    fun getAllFavorites(): Flow<List<Favorite>>
    fun isFavorite(departureCode: String, destinationCode: String): Flow<Boolean>
    suspend fun toggleFavorite(flight: Flight)
}

class FlightRepositoryImpl(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao
) : FlightRepository {

    override fun searchAirports(query: String): Flow<List<Airport>> {
        return airportDao.searchAirports(query)
    }

    override suspend fun getFlightsFromAirport(airport: Airport): List<Flight> {
        val destinations = airportDao.getPossibleDestinations(airport.id)
        return destinations.map { destination ->
            Flight(
                departureCode = airport.iata_code,
                departureName = airport.name,
                destinationCode = destination.iata_code,
                destinationName = destination.name
            )
        }
    }

    override fun getAllFavorites(): Flow<List<Favorite>> {
        return favoriteDao.getAllFavorites()
    }

    override fun isFavorite(departureCode: String, destinationCode: String): Flow<Boolean> {
        return favoriteDao.isFavorite(departureCode, destinationCode)
    }

    override suspend fun toggleFavorite(flight: Flight) {
        val isFavorite = favoriteDao.isFavorite(flight.departureCode, flight.destinationCode).firstOrNull() ?: false

        if (isFavorite) {

            favoriteDao.deleteFavoriteByRoute(flight.departureCode, flight.destinationCode)
        } else {
            val favorite = Favorite(
                departure_code = flight.departureCode,
                destination_code = flight.destinationCode
            )
            favoriteDao.insertFavorite(favorite)
        }
    }
}
