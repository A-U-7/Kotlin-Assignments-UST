package redesign_3

/**
 * Represents a time slot for a delivery with start and end times
 * @property start The start time in 24-hour format (e.g., 9 for 9 AM, 14 for 2 PM)
 * @property end The end time in 24-hour format
 */
data class DeliveryTimeSlot(val start: Int, val end: Int) {
    init {
        require(start in 0..23) { "Start time must be between 0 and 23" }
        require(end in 0..23) { "End time must be between 0 and 23" }
        require(start < end) { "Start time must be before end time" }
    }

    /**
     * Calculates the duration of the time slot in hours
     */
    val duration: Int
        get() = end - start
}

/**
 * Represents a training room with its schedule and details
 */
data class TrainingRoom(
    val name: String,
    val capacity: Int,
    val roomType: String,
    val schedule: List<DeliveryTimeSlot> = emptyList(),
    val equipment: List<String> = emptyList()
) {
    /**
     * Checks if the room is available at the given time
     * @param time The time to check (in 24-hour format)
     * @return true if the room is available, false otherwise
     */
    fun isAvailableAt(time: Int): Boolean {
        return schedule.none { time in it.start until it.end }
    }

    /**
     * Books the room for a specific time slot
     * @param start Start time of the booking
     * @param end End time of the booking
     * @return A new TrainingRoom with the updated schedule
     */
    fun bookTimeSlot(start: Int, end: Int): TrainingRoom {
        val newSlot = DeliveryTimeSlot(start, end)
        return copy(schedule = schedule + newSlot)
    }
}

/**
 * Main function demonstrating the use of Triple and custom data classes
 */
fun main() {
    // Basic Triple usage
    val basicRoomInfo = Triple("Presentation Room", 25, "Conference")
    println("=== Basic Room Information ===")
    println("Room Name: ${basicRoomInfo.first}")
    println("Capacity: ${basicRoomInfo.second}")
    println("Type: ${basicRoomInfo.third}")
    println()

    // Triple with collections
    val scheduledRoom = Triple(
        first = listOf("Projector", "Whiteboard", "Sound System"),
        second = listOf(9, 11, 14), // Scheduled hours
        third = true // Is available
    )
    
    println("=== Scheduled Room Details ===")
    println("Equipment: ${scheduledRoom.first.joinToString()}")
    println("Booked Hours: ${scheduledRoom.second.joinToString()}")
    println("Available: ${if (scheduledRoom.third) "Yes" else "No"}")
    println()

    // Using custom data classes for better type safety
    val timeSlots = listOf(
        DeliveryTimeSlot(9, 12),
        DeliveryTimeSlot(14, 16)
    )
    
    val trainingRoom = TrainingRoom(
        name = "Advanced Training Room",
        capacity = 30,
        roomType = "Workshop",
        equipment = listOf("Projector", "Whiteboard", "Video Conferencing"),
        schedule = timeSlots
    )

    println("=== Training Room Details ===")
    println("Name: ${trainingRoom.name}")
    println("Capacity: ${trainingRoom.capacity}")
    println("Type: ${trainingRoom.roomType}")
    println("Equipment: ${trainingRoom.equipment.joinToString()}")
    println("Scheduled Slots: ${trainingRoom.schedule.map { "${it.start}:00-${it.end}:00" }}")
    
    // Check availability
    val checkTime = 10
    println("\nChecking availability at $checkTime:00")
    println("Room ${if (trainingRoom.isAvailableAt(checkTime)) "is" else "is not"} available at $checkTime:00")

    // Book a new time slot
    println("\nBooking a new time slot (13:00-15:00)")
    val updatedRoom = trainingRoom.bookTimeSlot(13, 15)
    println("Updated Schedule: ${updatedRoom.schedule.map { "${it.start}:00-${it.end}:00" }}")

    // Destructuring a Triple
    val (equipment, bookedHours, isAvailable) = scheduledRoom
    println("\n=== Destructured Room Info ===")
    println("Equipment: $equipment")
    println("Booked Hours: $bookedHours")
    println("Available: $isAvailable")

    // Using Triple with when expression
    val roomStatus = when (scheduledRoom.third) {
        true -> "The room is currently available"
        false -> "The room is currently in use"
    }
    println("\nRoom Status: $roomStatus")
}
