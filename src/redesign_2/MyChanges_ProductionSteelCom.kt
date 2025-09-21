package redesign_2

/**
 * Represents the operational status of a steel manufacturing plant's network infrastructure.
 * Uses sealed class to represent different states of the network operation.
 */
sealed class NetworkStatus {
    /**
     * Represents a successful tower operation - tower is down
     */
    object TowerDown : NetworkStatus()

    /**
     * Represents a successful tower operation - tower is up
     */
    object TowerUp : NetworkStatus()

    /**
     * Represents a network failure with a specific reason
     * @property reason Detailed description of the failure
     */
    data class NetworkFailure(
        val reason: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkStatus() {
        /**
         * Logs the network failure with timestamp
         */
        fun logError() {
            println("[$timestamp] Network Error: $reason")
        }
    }

    /**
     * Factory method to create a NetworkStatus based on the tower status
     */
    companion object {
        fun createTowerStatus(isUp: Boolean): NetworkStatus = if (isUp) TowerUp else TowerDown
    }
}

/**
 * Main function demonstrating the usage of NetworkStatus sealed class
 */
fun main() {
    // Example 1: Create and handle a network failure
    val plantNetwork = NetworkStatus.NetworkFailure("Tower connection lost")
    handleNetworkStatus(plantNetwork)

    // Example 2: Handle different network statuses
    val statuses = listOf(
        NetworkStatus.TowerUp,
        NetworkStatus.TowerDown,
        NetworkStatus.NetworkFailure("Power outage in sector B"),
        NetworkStatus.createTowerStatus(isUp = true)
    )

    statuses.forEach { status ->
        handleNetworkStatus(status)
    }
}

/**
 * Handles different network statuses using when expression
 */
private fun handleNetworkStatus(status: NetworkStatus) {
    when (status) {
        is NetworkStatus.TowerUp -> println("Status: Tower is operational")
        is NetworkStatus.TowerDown -> println("Status: Tower is down for maintenance")
        is NetworkStatus.NetworkFailure -> {
            println("Status: Network failure detected")
            status.logError()
            // Additional error handling logic can be added here
        }
    }
}
