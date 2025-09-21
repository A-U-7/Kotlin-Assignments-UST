package redesign_1

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Data class representing a Train with validation
 */
data class Train(
    val trainNo: String,
    val name: String,
    val source: String,
    val destination: String,
    val departureTime: Long,
    val arrivalTime: Long,
    val fare: Int,
    val classType: String
) {
    init {
        require(trainNo.isNotBlank()) { "Train number cannot be blank" }
        require(name.isNotBlank()) { "Train name cannot be blank" }
        require(source.isNotBlank() && source.length == 3) { "Invalid source station code" }
        require(destination.isNotBlank() && destination.length == 3) { "Invalid destination station code" }
        require(fare > 0) { "Fare must be greater than 0" }
        require(departureTime > 0) { "Invalid departure time" }
        require(arrivalTime > 0) { "Invalid arrival time" }
        require(classType.isNotBlank()) { "Class type cannot be blank" }
    }

    val formattedDepartureTime: String
        get() = formatEpochToTime(departureTime)

    val formattedArrivalTime: String
        get() = formatEpochToTime(arrivalTime)

    private fun formatEpochToTime(epoch: Long): String {
        return Instant.ofEpochMilli(epoch)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}

/**
 * Sealed class representing the result of train operations
 */
sealed class TrainResult {
    data class Success(val message: String) : TrainResult()
    data class Error(val message: String) : TrainResult()
    data class TrainFound(val train: Train) : TrainResult()
    data class TrainsFound(val trains: List<Train>) : TrainResult()
}

/**
 * Repository interface for train operations
 */
interface TrainRepository {
    fun addTrain(category: String, train: Train): TrainResult
    fun getTrainByNumber(trainNo: String): TrainResult
    fun searchTrains(source: String, destination: String): TrainResult
    fun getTrainsByCategory(category: String): List<Train>
}

/**
 * Implementation of TrainRepository
 */
class TrainRepositoryImpl : TrainRepository {
    private val trainsByCategory = mutableMapOf<String, MutableList<Train>>()

    override fun addTrain(category: String, train: Train): TrainResult {
        return try {
            val trainList = trainsByCategory.getOrPut(category) { mutableListOf() }
            trainList.add(train)
            TrainResult.Success("Train ${train.name} (${train.trainNo}) added to $category category")
        } catch (e: Exception) {
            TrainResult.Error("Failed to add train: ${e.message}")
        }
    }

    override fun getTrainByNumber(trainNo: String): TrainResult {
        return trainsByCategory.values
            .flatten()
            .find { it.trainNo == trainNo }
            ?.let { TrainResult.TrainFound(it) }
            ?: TrainResult.Error("Train with number $trainNo not found")
    }

    override fun searchTrains(source: String, destination: String): TrainResult {
        val results = trainsByCategory.values
            .flatten()
            .filter { 
                it.source.equals(source, ignoreCase = true) && 
                it.destination.equals(destination, ignoreCase = true)
            }

        return if (results.isNotEmpty()) {
            TrainResult.TrainsFound(results)
        } else {
            TrainResult.Error("No trains found from $source to $destination")
        }
    }

    override fun getTrainsByCategory(category: String): List<Train> {
        return trainsByCategory[category] ?: emptyList()
    }
}

/**
 * Service class to handle train operations
 */
class TrainService(private val repository: TrainRepository) {
    
    fun addTrain(category: String, train: Train): TrainResult {
        return try {
            repository.addTrain(category, train)
        } catch (e: Exception) {
            TrainResult.Error("Error adding train: ${e.message}")
        }
    }

    fun getTrainByNumber(trainNo: String): TrainResult {
        return try {
            repository.getTrainByNumber(trainNo)
        } catch (e: Exception) {
            TrainResult.Error("Error retrieving train: ${e.message}")
        }
    }

    fun searchTrains(source: String, destination: String): TrainResult {
        return try {
            repository.searchTrains(source, destination)
        } catch (e: Exception) {
            TrainResult.Error("Error searching trains: ${e.message}")
        }
    }

    fun getTrainsByCategory(category: String): List<Train> {
        return repository.getTrainsByCategory(category)
    }
}

/**
 * Extension function to print train details in a formatted way
 */
private fun Train.printDetails() {
    println("""
        |--------------------------------
        |Train No:    $trainNo
        |Name:        $name
        |Route:       $source to $destination
        |Departure:   $formattedDepartureTime
        |Arrival:     $formattedArrivalTime
        |Fare:        ₹$fare
        |Class:       ${classType.uppercase()}
        |--------------------------------
    """.trimMargin())
}

/**
 * Main function demonstrating the usage
 */
fun main() {
    val trainRepository = TrainRepositoryImpl()
    val trainService = TrainService(trainRepository)

    // Add sample trains
    val expressTrains = listOf(
        Train("12345", "Kerala Express", "TRV", "BLR", 1735000000, 1740000000, 1000, "AC"),
        Train("12346", "South Express", "TRV", "BLR", 1736000000, 1741000000, 800, "Sleeper"),
        Train("12347", "North Express", "SBC", "CGM", 1737000000, 1742000000, 1200, "AC")
    )

    expressTrains.forEach { train ->
        val result = trainService.addTrain("Express", train)
        when (result) {
            is TrainResult.Success -> println(result.message)
            is TrainResult.Error -> println("Error: ${result.message}")
            else -> {}
        }
    }

    // Search for trains
    println("\nSearching for trains from TRV to BLR:")
    when (val searchResult = trainService.searchTrains("TRV", "BLR")) {
        is TrainResult.TrainsFound -> {
            println("Found ${searchResult.trains.size} trains:")
            searchResult.trains.forEach { it.printDetails() }
        }
        is TrainResult.Error -> println(searchResult.message)
        else -> {}
    }

    // Get train by number
    println("\nSearching for train number 12347:")
    when (val trainResult = trainService.getTrainByNumber("12347")) {
        is TrainResult.TrainFound -> trainResult.train.printDetails()
        is TrainResult.Error -> println(trainResult.message)
        else -> {}
    }
}
