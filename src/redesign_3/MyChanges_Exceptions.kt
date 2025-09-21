package redesign_3



/**
 * Sealed class representing the result of an operation that can either be a success or a failure
 * @param T The type of data contained in a successful result
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation containing the resulting data
     * @property data The successful result data
     */
    data class Success<out T>(val data: T) : Result<T>()
    
    /**
     * Maps the success value using the provided transform function if this is a Success,
     * or returns the same Error if this is an Error
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    /**
     * Represents a failed operation with an error message and optional cause
     * @property message A description of the error
     * @property cause The underlying cause of the error, if any
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : Result<Nothing>() {
        /**
         * Creates a new Error with an additional message prefix
         */
        fun withContext(context: String): Error = 
            Error("$context: $message", cause)
    }

    companion object {
        /**
         * Creates a Success result with the given value
         */
        fun <T> success(value: T): Result<T> = Success(value)

        /**
         * Creates an Error result with the given message and optional cause
         */
        fun <T> error(message: String, cause: Throwable? = null): Result<T> = 
            Error(message, cause)

        /**
         * Executes the given block and wraps the result in a Result
         */
        inline fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                success(block())
            } catch (e: Exception) {
                error(e.message ?: "An unknown error occurred", e)
            }
        }
    }

    /**
     * Executes the appropriate block based on whether this is a Success or Error
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Error) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(this)
    }

    /**
     * Gets the success value or null if this is an error
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Gets the success value or a default if this is an error
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = 
        (this as? Success)?.data ?: defaultValue
}

/**
 * Represents common validation errors
 */
sealed class ValidationError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    class InvalidFormat(message: String) : ValidationError(message)
    class ValueTooLow(message: String) : ValidationError(message)
    class ValueTooHigh(message: String) : ValidationError(message)
}

/**
 * Validates that a price string is in the correct format and within valid range
 */
fun validatePrice(price: String): Result<Int> {
    return try {
        val value = price.toInt()
        when {
            value < 0 -> Result.error("Price cannot be negative")
            value > 1_000_000 -> Result.error("Price exceeds maximum allowed value")
            else -> Result.success(value)
        }
    } catch (e: NumberFormatException) {
        Result.error("Invalid price format: '$price' is not a valid number", e)
    }
}

/**
 * Parses a price string into a Result
 */
fun parsePrice(price: String): Result<Int> {
    return Result.runCatching { price.toInt() }
        .mapError { "Failed to parse price: ${it.message}" }
}

/**
 * Extension function to map an error to a new message
 */
fun <T> Result<T>.mapError(transform: (Throwable) -> String): Result<T> {
    return when (this) {
        is Result.Success -> this
        is Result.Error -> Result.error(transform(cause ?: Exception("Unknown error")), cause)
    }
}

/**
 * Extension function to log errors
 */
fun <T> Result<T>.logError(tag: String = "App"): Result<T> {
    if (this is Result.Error) {
        println("[$tag] Error: $message")
        cause?.printStackTrace()
    }
    return this
}

/**
 * Main function demonstrating error handling patterns
 */
fun main() {
    //  Basic error handling with try-catch
    println("=== Example 1: Basic Error Handling ===")
    try {
        val items = listOf(1, 2, 3)
        println("Item at index 5: ${items[5]}")
    } catch (e: IndexOutOfBoundsException) {
        println("Error: ${e.message}")
    }

    // Using Result type for functional error handling
    println("\n=== Example 2: Functional Error Handling ===")
    val priceResults = listOf(
        "100",
        "250",
        "-50",
        "1,000,000",
        "abc"
    )

    priceResults.forEach { priceStr ->
        println("\nProcessing price: $priceStr")
        
        when (val result = validatePrice(priceStr)) {
            is Result.Success -> {
                println("Valid price: ${result.data}")
                // Process the valid price here
            }
            is Result.Error -> {
                println("Invalid price: ${result.message}")
                result.cause?.let { 
                    println("Cause: ${it.javaClass.simpleName}: ${it.message}")
                }
            }
        }
    }

    // Chaining operations with Result
    println("\n=== Example 3: Chaining Operations ===")
    val complexResult = parsePrice("500")
        .map { it * 2 }  // Double the price
        .map { it + 100 } // Add 100
        .logError("PriceProcessing") // Log any errors
        .fold(
            onSuccess = { "Final amount: $${it}" },
            onError = { "Error: ${it.message}" }
        )
    
    println(complexResult)

    // Handling multiple operations
    println("\n=== Example 4: Handling Multiple Operations ===")
    val operations = listOf(
        { parsePrice("100") },
        { parsePrice("200") },
        { parsePrice("abc") },
        { parsePrice("300") }
    )

    val results = operations.map { it() }
    val (successes, errors) = results.partition { it is Result.Success }
    
    println("Successful operations: ${successes.size}")
    println("Failed operations: ${errors.size}")
    
    // Process successful results
    val total = successes
        .filterIsInstance<Result.Success<Int>>()
        .sumOf { it.data }
    
    println("Total of successful operations: $total")
}
