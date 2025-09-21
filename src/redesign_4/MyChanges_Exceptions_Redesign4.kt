package redesign_4

import java.time.LocalDateTime

/**
 * Sealed hierarchy for retail-specific exceptions
 */
sealed class RetailException(
    message: String,
    val errorCode: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
) : RuntimeException(message)

/**
 * Exception thrown when an order cannot be found
 */
class OrderNotFoundException(
    orderId: String,
    errorCode: Int = 404
) : RetailException(
    message = "Order with ID $orderId not found",
    errorCode = errorCode
)

/**
 * Exception thrown when payment processing fails
 */
class PaymentFailedException(
    orderId: String,
    reason: String,
    errorCode: Int = 402
) : RetailException(
    message = "Payment failed for order $orderId: $reason",
    errorCode = errorCode
)

/**
 * Exception thrown when a product is out of stock
 */
class ProductOutOfStockException(
    productId: String,
    errorCode: Int = 409
) : RetailException(
    message = "Product $productId is out of stock",
    errorCode = errorCode
)

/**
 * Data class representing an API error response
 */
data class ApiError(
    val code: Int,
    val message: String,
    val timestamp: String = LocalDateTime.now().toString(),
    val details: Map<String, String> = emptyMap()
)

/**
 * Service responsible for inventory management
 */
class InventoryService {
    private val stock = mutableMapOf(
        "PROD1" to 10,
        "PROD2" to 0,  // out of stock
        "PROD3" to 5,
        "PROD4" to 100 // high stock item
    )

    /**
     * Checks if the requested quantity of a product is available
     * @throws ProductOutOfStockException if the product is not available in the requested quantity
     */
    fun validateStock(productId: String, quantity: Int) {
        val available = stock.getOrElse(productId) {
            throw ProductOutOfStockException(productId, 404)
        }
        
        if (available < quantity) {
            throw ProductOutOfStockException(
                productId = productId,
                errorCode = 409
            )
        }
    }

    /**
     * Reduces the stock level for a product
     * @throws ProductOutOfStockException if the product is not available in the requested quantity
     */
    fun reduceStock(productId: String, quantity: Int) {
        validateStock(productId, quantity)
        stock.computeIfPresent(productId) { _, current -> current - quantity }
    }

    /**
     * Gets the current stock level for a product
     */
    fun getStockLevel(productId: String): Int {
        return stock[productId] ?: 0
    }
}

/**
 * Service responsible for processing payments
 */
class PaymentService {
    /**
     * Processes a payment for an order
     * @return true if payment was successful, false otherwise
     */
    fun processPayment(orderId: String, amount: Double): Boolean {
        // Simulate payment processing
        return when {
            amount <= 0 -> throw IllegalArgumentException("Invalid payment amount")
            amount > 1000 -> false // Simulate payment failure for high amounts
            else -> true
        }
    }
}

/**
 * Service responsible for order processing
 */
class OrderService(
    private val inventoryService: InventoryService,
    private val paymentService: PaymentService
) {
    private val orders = mutableMapOf<String, Triple<String, Int, Double>>()

    /**
     * Places a new order
     * @throws ProductOutOfStockException if the product is out of stock
     * @throws PaymentFailedException if payment processing fails
     */
    fun placeOrder(orderId: String, productId: String, quantity: Int, amount: Double) {
        require(quantity > 0) { "Quantity must be positive" }
        require(amount >= 0) { "Amount cannot be negative" }

        // Check inventory first (fail fast)
        inventoryService.validateStock(productId, quantity)

        // Process payment
        if (!paymentService.processPayment(orderId, amount)) {
            throw PaymentFailedException(
                orderId = orderId,
                reason = "Payment of $$amount failed",
                errorCode = 402
            )
        }

        // Update inventory
        inventoryService.reduceStock(productId, quantity)

        // Record the order
        orders[orderId] = Triple(productId, quantity, amount)
        
        println(" Order $orderId placed successfully for product $productId (Qty: $quantity, Amount: $$amount)")
    }

    /**
     * Gets order details by ID
     * @throws OrderNotFoundException if the order is not found
     */
    fun getOrderDetails(orderId: String): Triple<String, Int, Double> {
        return orders[orderId] ?: throw OrderNotFoundException(orderId)
    }
}

/**
 * Global error handler for the application
 */
object GlobalErrorHandler {
    /**
     * Handles an exception and returns an appropriate API error
     */
    fun handleException(ex: Exception): ApiError {
        return when (ex) {
            is RetailException -> ApiError(
                code = ex.errorCode,
                message = ex.message ?: "An error occurred",
                details = mapOf("timestamp" to ex.timestamp.toString())
            )
            is IllegalArgumentException -> ApiError(
                code = 400,
                message = "Invalid request: ${ex.message}",
                details = mapOf("type" to "VALIDATION_ERROR")
            )
            else -> ApiError(
                code = 500,
                message = "An unexpected error occurred",
                details = mapOf<String, Any>(
                    "error" to (ex.message ?: "Unknown error"),
                    "type" to ex.javaClass.simpleName
                ) as Map<String, String>
            )
        }.also { logError(it, ex) }
    }

    private fun logError(apiError: ApiError, ex: Exception) {
        println("\uD83D [${apiError.code}] ${apiError.message}")
        println("Details: ${apiError.details}")
        if (ex !is RetailException) {
            println("Stack trace: ${ex.stackTraceToString()}")
        }
    }
}

/**
 * Main simulation function
 */
fun main() {
    // Initialize services
    val inventoryService = InventoryService()
    val paymentService = PaymentService()
    val orderService = OrderService(inventoryService, paymentService)

    // Test data
    val testOrders = listOf(
        mapOf("id" to "ORD1", "product" to "PROD1", "qty" to 2, "price" to 500.0),
        mapOf("id" to "ORD2", "product" to "PROD2", "qty" to 1, "price" to 250.0),  // Out of stock
        mapOf("id" to "ORD3", "product" to "PROD3", "qty" to 10, "price" to 5000.0), // High amount
        mapOf("id" to "ORD4", "product" to "PROD1", "qty" to 3, "price" to 750.0),  // Should succeed
        mapOf("id" to "ORD5", "product" to "PROD4", "qty" to 0, "price" to 0.0)    // Invalid quantity
    )

    // Process orders
    testOrders.forEach { order ->
        try {
            orderService.placeOrder(
                orderId = order["id"] as String,
                productId = order["product"] as String,
                quantity = order["qty"] as Int,
                amount = order["price"] as Double
            )
        } catch (ex: Exception) {
            val error = GlobalErrorHandler.handleException(ex)
            println("Failed to process order ${order["id"]}: ${error.message}")
        }
    }

    // Display final inventory
    println("\n=== Final Inventory ===")
    listOf("PROD1", "PROD2", "PROD3", "PROD4").forEach { productId ->
        println("$productId: ${inventoryService.getStockLevel(productId)} in stock")
    }
}
