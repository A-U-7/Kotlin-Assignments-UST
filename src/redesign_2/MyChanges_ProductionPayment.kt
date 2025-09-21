package redesign_2

import java.time.LocalDateTime

/**
 * Represents the status of a payment transaction in the production system.
 * Uses sealed class to represent different states of a payment.
 */
sealed class PaymentStatus {
    /**
     * Represents a payment that is pending processing.
     * @property timestamp When the payment was marked as pending
     */
    data class Pending(
        val timestamp: LocalDateTime = LocalDateTime.now()
    ) : PaymentStatus()

    /**
     * Represents a successfully completed payment.
     * @property transactionId Unique identifier for the transaction
     * @property timestamp When the payment was completed
     */
    data class Completed(
        val transactionId: String,
        val timestamp: LocalDateTime = LocalDateTime.now()
    ) : PaymentStatus()

    /**
     * Represents a failed payment with a reason.
     * @property reason Detailed description of the failure
     * @property timestamp When the failure occurred
     * @property errorCode Optional error code for programmatic handling
     */
    data class Failed(
        val reason: String,
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val errorCode: String? = null
    ) : PaymentStatus() {
        /**
         * Logs the payment failure with timestamp and optional error code
         */
        fun logFailure() {
            val errorInfo = errorCode?.let { " (Error Code: $it)" } ?: ""
            println("[$timestamp] Payment Failed: $reason$errorInfo")
        }
    }

    companion object {
        /**
         * Creates a new Pending payment status
         */
        fun pending() = Pending()

        /**
         * Creates a new Completed payment status
         * @param transactionId The transaction ID for the completed payment
         */
        fun completed(transactionId: String) = Completed(transactionId)

        /**
         * Creates a new Failed payment status
         * @param reason The reason for failure
         * @param errorCode Optional error code
         */
        fun failed(reason: String, errorCode: String? = null) = Failed(reason, errorCode = errorCode)
    }
}

/**
 * Handles different payment statuses and performs appropriate actions
 * @param status The payment status to handle
 */
fun handlePaymentStatus(status: PaymentStatus) {
    when (status) {
        is PaymentStatus.Pending -> {
            println("Payment is pending. Please wait for processing to complete.")
            println("Pending since: ${status.timestamp}")
        }
        is PaymentStatus.Completed -> {
            println("Payment completed successfully!")
            println("Transaction ID: ${status.transactionId}")
            println("Completed at: ${status.timestamp}")
        }
        is PaymentStatus.Failed -> {
            println("Payment failed!")
            status.logFailure()
            // Additional failure handling logic can be added here
        }
    }
}

/**
 * Main function demonstrating the usage of PaymentStatus
 */
fun main() {
    //Pending payment
    val pendingPayment = PaymentStatus.pending()
    
    //  Completed payment
    val completedPayment = PaymentStatus.completed("TXN${(1000..9999).random()}")
    
    // Failed payment
    val failedPayment = PaymentStatus.failed(
        reason = "Insufficient funds",
        errorCode = "ERR_INSUFFICIENT_FUNDS"
    )
    
    // Handle different payment statuses
    listOf(pendingPayment, completedPayment, failedPayment).forEach { status ->
        println("\n--- Processing Payment Status ---")
        handlePaymentStatus(status)
    }
}
