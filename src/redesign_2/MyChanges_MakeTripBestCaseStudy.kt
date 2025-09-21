package redesign_2

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Represents different airlines in the system.
 * Uses sealed class to ensure type safety and fixed set of airline types.
 */
sealed class Airline(
    val code: String,
    val name: String,
    val baggageAllowance: String = "15kg"
) {
    object IndiGo : Airline("6E", "IndiGo", "15kg")
    object AirIndia : Airline("AI", "Air India", "23kg")
    object Vistara : Airline("UK", "Vistara", "25kg")
    object SpiceJet : Airline("SG", "SpiceJet", "15kg")
    object GoFirst : Airline("G8", "Go First", "20kg")

    override fun toString(): String = "$name ($code)"
}

/**
 * Represents a flight with all necessary details.
 * @property flightId Unique identifier for the flight
 * @property airline The airline operating the flight
 * @property source Departure airport code
 * @property destination Arrival airport code
 * @property departureTime Scheduled departure time as epoch seconds
 * @property arrivalTime Scheduled arrival time as epoch seconds
 * @property basePrice Base price of the flight
 * @property availableSeats Number of available seats
 * @property flightClass Class of service (Economy, Business, First)
 */
open class Flight(
    val flightId: String,
    val airline: Airline,
    val source: String,
    val destination: String,
    val departureTime: Long,
    val arrivalTime: Long,
    val basePrice: Double,
    val availableSeats: Int = 180,
    val flightClass: FlightClass = FlightClass.ECONOMY
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd yyyy HH:mm")
        .withZone(ZoneId.systemDefault())

    /**
     * Calculates the final price after applying any airline-specific pricing rules
     */
    open fun calculateFinalPrice(): Double = basePrice * flightClass.priceMultiplier

    /**
     * Gets the duration of the flight in hours
     */
    val durationInHours: Double
        get() = (arrivalTime - departureTime).toDouble() / 3600

    /**
     * Formatted string representation of the flight details
     */
    override fun toString(): String {
        val formattedDeparture = formatTime(departureTime)
        val formattedArrival = formatTime(arrivalTime)
        
        return """
            |✈️ ${airline.name} ($flightId) - ${flightClass.displayName}
            |🛫 $source → 🛬 $destination
            |🕒 $formattedDeparture - $formattedArrival (${String.format("%.1f", durationInHours)}h)
            |💼 Baggage: ${airline.baggageAllowance} | 💺 Seats: $availableSeats
            |💰 Price: ₹${calculateFinalPrice().toInt()}
            |${if (isFlightSoon()) "🚨 Last few seats!" else ""}
        """.trimMargin()
    }

    /**
     * Checks if the flight is departing soon (within next 24 hours)
     */
    fun isFlightSoon(): Boolean {
        val now = Instant.now().epochSecond
        return departureTime in (now + 1)..(now + 86400) // Next 24 hours
    }

    private fun formatTime(epochSeconds: Long): String {
        return dateTimeFormatter.format(Instant.ofEpochSecond(epochSeconds))
    }
}

/**
 * Represents different classes of flight service
 */
enum class FlightClass(
    val displayName: String,
    val priceMultiplier: Double
) {
    ECONOMY("Economy", 1.0),
    PREMIUM_ECONOMY("Premium Economy", 1.5),
    BUSINESS("Business", 2.5),
    FIRST("First Class", 4.0)
}

/**
 * Interface for flight search criteria
 */
interface SearchCriteria {
    fun matches(flight: Flight): Boolean
}

/**
 * Implementation of SearchCriteria for basic flight search
 */
data class BasicSearchCriteria(
    val source: String? = null,
    val destination: String? = null,
    val maxPrice: Double? = null,
    val preferredAirlines: Set<Airline> = emptySet()
) : SearchCriteria {
    override fun matches(flight: Flight): Boolean {
        return (source == null || flight.source.equals(source, ignoreCase = true)) &&
               (destination == null || flight.destination.equals(destination, ignoreCase = true)) &&
               (maxPrice == null || flight.calculateFinalPrice() <= maxPrice) &&
               (preferredAirlines.isEmpty() || flight.airline in preferredAirlines)
    }
}

/**
 * Flight repository interface following repository pattern
 */
interface FlightRepository {
    fun addFlight(flight: Flight)
    fun findById(flightId: String): Flight?
    fun search(criteria: SearchCriteria): List<Flight>
    fun getFlightsByAirline(airline: Airline): List<Flight>
}

/**
 * In-memory implementation of FlightRepository
 */
class InMemoryFlightRepository : FlightRepository {
    private val flights = mutableListOf<Flight>()
    private val flightById = mutableMapOf<String, Flight>()
    private val flightsByAirline = mutableMapOf<Airline, MutableList<Flight>>()

    override fun addFlight(flight: Flight) {
        flights.add(flight)
        flightById[flight.flightId] = flight
        flightsByAirline.getOrPut(flight.airline) { mutableListOf() }.add(flight)
    }

    override fun findById(flightId: String): Flight? = flightById[flightId]

    override fun search(criteria: SearchCriteria): List<Flight> {
        return flights.filter { criteria.matches(it) }
            .sortedBy { it.departureTime }
    }

    override fun getFlightsByAirline(airline: Airline): List<Flight> {
        return flightsByAirline[airline] ?: emptyList()
    }
}

/**
 * Flight booking service
 */
class FlightBookingService(private val repository: FlightRepository) {
    /**
     * Books a flight if seats are available
     */
    fun bookFlight(flightId: String, passengerCount: Int = 1): Boolean {
        val flight = repository.findById(flightId) ?: return false
        
        return if (flight.availableSeats >= passengerCount) {
            // In a real application, we would update the available seats here
            // and create a booking record
            true
        } else {
            false
        }
    }
}

/**
 * Example usage of the flight booking system
 */
fun main() {
    // Initialize repository and services
    val flightRepository = InMemoryFlightRepository()
    val bookingService = FlightBookingService(flightRepository)
    
    // Add sample flights
    val now = Instant.now().epochSecond
    val oneHour: Long = 3600
    
    // Add IndiGo flights
    flightRepository.addFlight(
        Flight(
            flightId = "6E-101",
            airline = Airline.IndiGo,
            source = "DEL",
            destination = "BOM",
            departureTime = now + (oneHour * 2),
            arrivalTime = now + (oneHour * 4),
            basePrice = 4500.0,
            availableSeats = 5,
            flightClass = FlightClass.ECONOMY
        )
    )
    
    // Add Air India flight
    flightRepository.addFlight(
        Flight(
            flightId = "AI-202",
            airline = Airline.AirIndia,
            source = "DEL",
            destination = "BOM",
            departureTime = now + (oneHour * 3),
            arrivalTime = now + (oneHour * 5),
            basePrice = 5200.0,
            availableSeats = 15,
            flightClass = FlightClass.BUSINESS
        )
    )
    
    // Search for flights
    println("🔍 Searching for flights from DEL to BOM under ₹6000:")
    val criteria = BasicSearchCriteria(
        source = "DEL",
        destination = "BOM",
        maxPrice = 6000.0,
        preferredAirlines = setOf(Airline.IndiGo, Airline.AirIndia)
    )
    
    val availableFlights = flightRepository.search(criteria)
    if (availableFlights.isEmpty()) {
        println("No flights found matching your criteria.")
    } else {
        availableFlights.forEach { println("\n$it") }
        
        // Try to book a flight
        val flightToBook = availableFlights.first()
        println("\nAttempting to book flight ${flightToBook.flightId}...")
        
        val bookingResult = bookingService.bookFlight(flightToBook.flightId)
        if (bookingResult) {
            println("✅ Booking successful!")
        } else {
            println("❌ Sorry, could not complete the booking. No seats available.")
        }
    }
    
    // Show flights by airline
    println("\n✈️ Flights by Airline:")
    listOf(Airline.IndiGo, Airline.AirIndia).forEach { airline ->
        val flights = flightRepository.getFlightsByAirline(airline)
        println("${airline.name}: ${flights.size} flights")
    }
}
