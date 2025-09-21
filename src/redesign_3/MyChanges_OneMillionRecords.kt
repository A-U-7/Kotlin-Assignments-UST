package redesign_3

import kotlin.system.measureTimeMillis

/**
 * Represents the status of a transaction
 */
sealed class TransactionStatusForOneMilRecords {
    object Pending : TransactionStatusForOneMilRecords()
    object Settled : TransactionStatusForOneMilRecords()
    object Failed : TransactionStatusForOneMilRecords()
    
    companion object {
        fun fromString(status: String): TransactionStatusForOneMilRecords {
            return when (status.uppercase()) {
                "PENDING" -> Pending
                "SETTLED" -> Settled
                "FAILED" -> Failed
                else -> throw IllegalArgumentException("Invalid transaction status: $status")
            }
        }
    }
}

/**
 * Represents a financial transaction
 * @property id Unique identifier for the transaction
 * @property customer Name of the customer
 * @property amount Transaction amount
 * @property reconciledAmount Amount that has been reconciled
 * @property status Current status of the transaction
 */
data class TransactionForOneMillionRecords(
    val id: Int,
    val customer: String,
    val amount: Double,
    val reconciledAmount: Double,
    val status: TransactionStatusForOneMilRecords
) {
    /**
     * Checks if this is a high-value transaction
     */
    val isHighValue: Boolean
        get() = amount > 50_000

    /**
     * Checks if the transaction is fully reconciled
     */
    val isFullyReconciled: Boolean
        get() = amount == reconciledAmount

    /**
     * Calculates the pending amount for reconciliation
     */
    val pendingAmount: Double
        get() = (amount - reconciledAmount).coerceAtLeast(0.0)
}

/**
 * Data class to hold the result of transaction categorization
 */
data class TransactionCategories(
    val pending: List<TransactionForOneMillionRecords>,
    val highValue: List<TransactionForOneMillionRecords>,
    val mismatched: List<TransactionForOneMillionRecords>
)

/**
 * Service class for transaction processing
 */
class TransactionProcessor {
    /**
     * Categorizes transactions in a single pass for optimal performance
     * @param transactions List of transactions to categorize
     * @return TransactionCategories containing categorized transactions
     */
    fun categorizeTransactions(transactions: List<TransactionForOneMillionRecords>): TransactionCategories {
        val pending = mutableListOf<TransactionForOneMillionRecords>()
        val highValue = mutableListOf<TransactionForOneMillionRecords>()
        val mismatched = mutableListOf<TransactionForOneMillionRecords>()

        for (transaction in transactions) {
            if (transaction.status == TransactionStatusForOneMilRecords.Pending) {
                pending.add(transaction)
            }
            if (transaction.isHighValue) {
                highValue.add(transaction)
            }
            if (!transaction.isFullyReconciled) {
                mismatched.add(transaction)
            }
        }

        return TransactionCategories(pending, highValue, mismatched)
    }

    /**
     * Generates sample transactions for testing
     * @param count Number of transactions to generate
     * @return List of generated transactions
     */
    fun generateSampleTransactions(count: Int): List<TransactionForOneMillionRecords> {
        return (1..count).map { id ->
            val isHighValue = id % 1_000 == 0
            val isPending = id % 200 == 0
            val isMismatched = id % 500 == 0

            val amount = if (isHighValue) 100_000.0 else 5_000.0
            val reconciledAmount = if (isMismatched) 0.0 else amount
            val status = if (isPending) TransactionStatusForOneMilRecords.Pending else TransactionStatusForOneMilRecords.Settled

            TransactionForOneMillionRecords(
                id = id,
                customer = "Customer$id",
                amount = amount,
                reconciledAmount = reconciledAmount,
                status = status
            )
        }
    }
}

/**
 * Main function demonstrating transaction processing with 1M records
 */
fun main() {
    val processor = TransactionProcessor()
    
    // Generate 1M transactions
    println("Generating 1,000,000 transactions...")
    val transactions: List<TransactionForOneMillionRecords>
    val generationTime = measureTimeMillis {
        transactions = processor.generateSampleTransactions(1_000_000)
    }
    
    println("Generated ${transactions.size} transactions in ${generationTime}ms")
    
    // Categorize transactions
    println("\nCategorizing transactions...")
    val categories: TransactionCategories
    val categorizationTime = measureTimeMillis {
        categories = processor.categorizeTransactions(transactions)
    }
    
    // Calculate and display statistics
    val totalValue = transactions.sumOf { it.amount }.toInt()
    
    println("=== Financial Summary ===")
    println("Total Transaction Value  : $totalValue")
    println("Pending Settlements:${categories.pending.size}")
    println("High Value Transactions : ${categories.highValue.size}")
    println("Mismatched Records:${categories.mismatched.size}")
}
