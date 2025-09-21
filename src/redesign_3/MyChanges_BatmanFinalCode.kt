package redesign_3

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * Sealed class representing the status of a transaction with additional metadata
 */
sealed class TransactionStatusProcess {
    data class Pending(val since: LocalDateTime) : TransactionStatusProcess()
    data class Settled(val settlementDate: LocalDateTime) : TransactionStatusProcess()
    data class Failed(val reason: String, val failedAt: LocalDateTime) : TransactionStatusProcess()
    
    companion object {
        fun createPending() = Pending(LocalDateTime.now())
        fun createSettled() = Settled(LocalDateTime.now())
        fun createFailed(reason: String) = Failed(reason, LocalDateTime.now())
    }
}

/**
 * Base class for all transaction types with common properties
 */
open class BaseTransaction(
    val id: Int,
    var customer: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Returns a formatted timestamp string
     */
    val formattedTimestamp: String
        get() = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

/**
 * Represents a financial transaction with enhanced features
 */
class TransactionModel(
    id: Int,
    customer: String,
    val amount: Double = 0.0,
    val reconciledAmount: Double? = null,
    status: TransactionStatusProcess = TransactionStatusProcess.createSettled(),
    val currency: String = "Rupees",
    val description: String = "",
    val referenceNumber: String = generateReferenceNumber()
) : BaseTransaction(id, customer) {
    
    var status: TransactionStatusProcess = status
        private set
    
    /**
     * Checks if the transaction is fully reconciled
     */
    val isFullyReconciled: Boolean
        get() = !isMismatched()
    
    /**
     * Calculates the pending amount for reconciliation
     */
    val pendingAmount: Double
        get() = (amount - (reconciledAmount ?: 0.0)).absoluteValue
    
    /**
     * Checks if this is a high-value transaction
     */
    val isHighValue: Boolean
        get() = amount > 50_000
    
    /**
     * Checks if there's a mismatch between amount and reconciled amount
     */
    fun isMismatched(): Boolean {
        return (reconciledAmount ?: 0.0) != amount
    }
    
    /**
     * Updates the transaction status
     */
    fun updateStatus(newStatus: TransactionStatusProcess) {
        status = newStatus
    }
    
    companion object {
        private const val REF_PREFIX = "TXN"
        
        /**
         * Generates a unique reference number for the transaction
         */
        private fun generateReferenceNumber(): String {
            val timestamp = System.currentTimeMillis() % 1_000_000
            val random = Random.nextInt(1000, 9999)
            return "$REF_PREFIX-$timestamp-$random"
        }
    }
}

/**
 * Service class for transaction processing and analysis
 */
class BatmanTransactionProcessor {
    private val transactions = mutableListOf<TransactionModel>()
    
    /**
     * Adds a new transaction
     */
    fun addTransaction(transaction: TransactionModel) {
        require(transaction.amount >= 0) { "Amount cannot be negative" }
        require(transaction.customer.isNotBlank()) { "Customer name cannot be blank" }
        transactions.add(transaction)
    }
    
    /**
     * Categorizes transactions in a single pass for better performance
     */
    fun analyzeTransactions(): TransactionAnalysis {
        val pending = mutableListOf<TransactionModel>()
        val highValue = mutableListOf<TransactionModel>()
        val mismatched = mutableListOf<TransactionModel>()
        
        transactions.forEach { transaction ->
            if (transaction.status is TransactionStatusProcess.Pending) pending.add(transaction)
            if (transaction.isHighValue) highValue.add(transaction)
            if (transaction.isMismatched()) mismatched.add(transaction)
        }
        
        return TransactionAnalysis(pending, highValue, mismatched)
    }
    
    /**
     * Gets transaction by ID
     */
    fun getTransactionById(id: Int): TransactionModel? {
        return transactions.find { it.id == id }
    }
    
    /**
     * Gets total transaction value
     */
    fun getTotalTransactionValue(): Double {
        return transactions.sumOf { it.amount }
    }
    
    /**
     * Gets total pending settlement value
     */
    fun getTotalPendingAmount(): Double {
        return transactions
            .filter { it.status is TransactionStatusProcess.Pending }
            .sumOf { it.amount }
    }
}

/**
 * Data class to hold the result of transaction analysis
 */
data class TransactionAnalysis(
    val pendingTransactions: List<TransactionModel>,
    val highValueTransactions: List<TransactionModel>,
    val mismatchedTransactions: List<TransactionModel>
)

/**
 * Extension function to format currency
 */
private fun Double.formatCurrency(currency: String = "Rupees"): String {
    return String.format("$currency %,.2f", this)
}

/**
 * Main function demonstrating the transaction processing system
 */
fun main() {
    // Initialize transaction processor
    val processor = BatmanTransactionProcessor()
    
    // Generate transactions
    val numberOfTransactions = 1_000_000
    println("Generating $numberOfTransactions transactions...")
    
    val startTime = System.currentTimeMillis()
    
    (1..numberOfTransactions).forEach { id ->
        val isHighValue = id % 1000 == 0
        val isPending = id % 200 == 0
        val isMismatched = id % 500 == 0
        
        val amount = when {
            isHighValue -> 100_000.0
            else -> 5_000.0
        }
        
        val reconciledAmount = when {
            isMismatched -> null
            else -> amount
        }
        
        val status = when {
            isPending -> TransactionStatusProcess.createPending()
            else -> TransactionStatusProcess.createSettled()
        }
        
        val transaction = TransactionModel(
            id = id,
            customer = "Customer$id",
            amount = amount,
            reconciledAmount = reconciledAmount,
            status = status,
            description = if (isHighValue) "High Value Transaction" else "Standard Transaction"
        )
        
        processor.addTransaction(transaction)
    }
    
    val generationTime = System.currentTimeMillis() - startTime
    println("Generated $numberOfTransactions transactions in ${generationTime}ms")
    
    // Analyze transactions
    println("\nAnalyzing transactions...")
    val analysisStartTime = System.currentTimeMillis()
    val analysis = processor.analyzeTransactions()
    val analysisTime = System.currentTimeMillis() - analysisStartTime
    
    // Print analysis results
    println("\n=== TRANSACTION ANALYSIS ===")
    println("Total Transactions    : ${numberOfTransactions}")
    println("Pending Settlements   : ${analysis.pendingTransactions.size}")
    println("High Value Txns       : ${analysis.highValueTransactions.size}")
    println("Mismatched Records    : ${analysis.mismatchedTransactions.size}")
    println("\nTotal Value           : ${processor.getTotalTransactionValue().formatCurrency()}")
    println("Total Pending Amount  : ${processor.getTotalPendingAmount().formatCurrency()}")
    println("\nAnalysis completed in ${analysisTime}ms")
    
    // Show  transaction details
    if (analysis.pendingTransactions.isNotEmpty()) {
        val transactionModel = analysis.pendingTransactions.first()
        println("\n Pending Transaction:")
        println("  ID: ${transactionModel.id}")
        println("  Customer: ${transactionModel.customer}")
        println("  Amount: ${transactionModel.amount.formatCurrency()}")
        println("  Status: ${transactionModel.status}")
        println("  Reference: ${transactionModel.referenceNumber}")
    }
}
