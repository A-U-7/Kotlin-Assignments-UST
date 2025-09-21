package redesign_1

import java.time.LocalTime
import java.time.format.DateTimeFormatter


/**
 * Data class representing a Flight with validation
 */
data class Flight(
    val flightId: String,
    val airline: String,
    val source: String,
    val destination: String,
    val departureTime: String,
    val arrivalTime: String,
    val price: Int
) {
    init {
        require(flightId.isNotBlank()) { "Flight ID cannot be blank" }
        require(airline.isNotBlank()) { "Airline cannot be blank" }
        require(source.isNotBlank() && source.length == 3) { "Invalid source airport code" }
        require(destination.isNotBlank() && destination.length == 3) { "Invalid destination airport code" }
        require(price > 0) { "Price must be greater than 0" }
        require(isValidTime(departureTime)) { "Invalid departure time format. Use HH:mm" }
        require(isValidTime(arrivalTime)) { "Invalid arrival time format. Use HH:mm" }
    }

    private fun isValidTime(time: String): Boolean {
        return try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm"))
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Sealed class representing the result of flight operations
 */
sealed class FlightResult {
    data class Success(val message: String) : FlightResult()
    data class Error(val message: String) : FlightResult()
    data class FlightFound(val flight: Flight) : FlightResult()
    data class FlightsFound(val flights: List<Flight>) : FlightResult()
}

/**
 * Repository interface for flight operations
 */
interface FlightRepository {
    fun addFlight(flight: Flight): FlightResult
    fun getFlightById(id: String): FlightResult
    fun searchFlights(source: String, destination: String, maxPrice: Int): FlightResult
}

/**
 * Implementation of FlightRepository
 */
class FlightRepositoryImpl : FlightRepository {
    private val flights = mutableListOf<Flight>()

    override fun addFlight(flight: Flight): FlightResult {
        return try {
            flights.add(flight)
            FlightResult.Success("Flight ${flight.flightId} added successfully")
        } catch (e: Exception) {
            FlightResult.Error("Failed to add flight: ${e.message}")
        }
    }

    override fun getFlightById(id: String): FlightResult {
        return flights.find { it.flightId == id }?.let {
            FlightResult.FlightFound(it)
        } ?: FlightResult.Error("Flight with ID $id not found")
    }

    override fun searchFlights(source: String, destination: String, maxPrice: Int): FlightResult {
        if (maxPrice <= 0) {
            return FlightResult.Error("Max price must be greater than 0")
        }
        
        val results = flights.filter {
            it.source.equals(source, ignoreCase = true) &&
            it.destination.equals(destination, ignoreCase = true) &&
            it.price <= maxPrice
        }.sortedBy { it.price }

        return if (results.isNotEmpty()) {
            FlightResult.FlightsFound(results)
        } else {
            FlightResult.Error("No flights found for the given criteria")
        }
    }
}

/**
 * Service class to handle flight operations
 */
class FlightService(private val repository: FlightRepository) {
    
    fun addFlight(flight: Flight): FlightResult {
        return try {
            repository.addFlight(flight)
        } catch (e: Exception) {
            FlightResult.Error("Error adding flight: ${e.message}")
        }
    }

    fun getFlightById(id: String): FlightResult {
        return try {
            repository.getFlightById(id)
        } catch (e: Exception) {
            FlightResult.Error("Error retrieving flight: ${e.message}")
        }
    }

    fun searchFlights(source: String, destination: String, maxPrice: Int): FlightResult {
        return try {
            repository.searchFlights(source, destination, maxPrice)
        } catch (e: Exception) {
            FlightResult.Error("Error searching flights: ${e.message}")
        }
    }
}

/**
 * Main function demonstrating the usage
 */
fun main() {
    val flightRepository = FlightRepositoryImpl()
    val flightService = FlightService(flightRepository)

    // Add sample flights
    listOf(
        Flight("6144", "Indigo", "BLR", "TIR", "10:25", "11:35", 7000),
        Flight("7144", "Sharathfly", "BLR", "KOC", "7:25", "9:35", 1000),
        Flight("7777", "Flyvishnu", "BLR", "CAL", "10:25", "11:35", 900)
    ).forEach { flight ->
        val result = flightService.addFlight(flight)
        when (result) {
            is FlightResult.Success -> println("Success: ${result.message}")
            is FlightResult.Error -> println("Error: ${result.message}")
            else -> {}
        }
    }

    // Search flights
    val searchResult = flightService.searchFlights("BLR", "TIR", 10000)
    when (searchResult) {
        is FlightResult.FlightsFound -> {
            println("\nFound ${searchResult.flights.size} flights:")
            searchResult.flights.forEach { flight ->
                println("${flight.airline} (${flight.flightId}): ${flight.source} to ${flight.destination} at ${flight.departureTime} for ₹${flight.price}")
            }
        }
        is FlightResult.Error -> println("\n${searchResult.message}")
        else -> {}
    }

    // Get flight by ID
    val flightResult = flightService.getFlightById("7777")
    when (flightResult) {
        is FlightResult.FlightFound -> {
            val flight = flightResult.flight
            println("\nFlight found: ${flight.airline} (${flight.flightId})")
            println("Route: ${flight.source} -> ${flight.destination}")
            println("Time: ${flight.departureTime} - ${flight.arrivalTime}")
            println("Price: ₹${flight.price}")
        }
        is FlightResult.Error -> println("\n${flightResult.message}")
        else -> {}
    }
}
