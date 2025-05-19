package com.example.flightsearch.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.flightsearch.data.database.entity.Airport
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {
    @Query("SELECT * FROM airport WHERE name LIKE '%' || :query || '%' OR iata_code LIKE '%' || :query || '%' ORDER BY passengers DESC")
    fun searchAirports(query: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE iata_code = :iataCode")
    suspend fun getAirportByCode(iataCode: String): Airport?

    @Query("""
    SELECT a.*
    FROM airport a
    LEFT JOIN favorite f ON a.iata_code = f.destination_code
                       AND f.departure_code = (SELECT iata_code FROM airport WHERE id = :airportId)
    WHERE a.id != :airportId
    ORDER BY (f.destination_code IS NOT NULL) DESC, a.passengers DESC
""")
    suspend fun getPossibleDestinations(airportId: Int): List<Airport>
}
