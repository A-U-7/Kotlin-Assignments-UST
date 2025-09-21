package redesign_3

import java.text.NumberFormat
import java.util.*
import kotlin.require

/**
 * Represents the status of a transaction
 */
enum class TransactionStatus {
    PENDING,
    SETTLED,
    FAILED,
    CANCELLED
}

/**
 * Represents a financial transaction
 * @property id Unique identifier for the transaction
 * @property customer Name of the customer
 * @property amount Original transaction amount
 * @property reconciledAmount Amount that has been settled/reconciled
 * @property status Current status of the transaction
 * @property transactionDate Date when the transaction was initiated
 */
data class Transaction(
    val id: Int,
    val customer: String,
    val amount: Double,
    val reconciledAmount: Double,
    val status: TransactionStatusProcess,
    val transactionDate: Date = Date()
) {
    /**
     * Checks if the transaction is fully reconciled
     */
    val isFullyReconciled: Boolean
        get() = amount == reconciledAmount

    /**
     * Calculates the pending amount for settlement
     */
    val pendingAmount: Double
        get() = amount - reconciledAmount

    /**
     * Checks if this is a high-value transaction
     */
    val isHighValue: Boolean
        get() = amount > 50000

    /**
     * Formatted amount string with currency symbol
     */
    val formattedAmount: String
        get() = amount.toCurrency()
}

/**
 * Service class for transaction processing and analysis
 */
class TransactionService {
    private val transactions = mutableListOf<TransactionModel>()

    /**
     * Adds a transaction to the system
     * @param transaction The transaction to add
     */
    fun addTransaction(transaction: TransactionModel) {
        require(transaction.amount >= 0) { "Amount cannot be negative" }
        require((transaction.reconciledAmount ?: 0.0) in 0.0..transaction.amount) {
            "Reconciled amount must be between 0 and the transaction amount"
        }
        transactions.add(transaction)
    }

    /**
     * Processes transactions based on a given rule
     * @param rule The condition to filter transactions
     * @return List of transactions that match the rule
     */
    fun processTransactions(rule: (TransactionModel) -> Boolean): List<TransactionModel> {
        return transactions.filter(rule)
    }

    /**
     * Gets all pending transactions
     */
    fun getPendingTransactions(): List<TransactionModel> =
        processTransactions { it.status == TransactionStatusProcess.createPending() }

    /**
     * Gets all high-value transactions
     */
    fun getHighValueTransactions(): List<TransactionModel> =
        processTransactions { it.isHighValue }

    /**
     * Gets transactions with reconciliation mismatches
     */
    fun getMismatchedTransactions(): List<TransactionModel> =
        processTransactions { !it.isFullyReconciled && it.status != TransactionStatusProcess.createPending() }

    /**
     * Gets the total value of all transactions
     */
    fun getTotalTransactionValue(): Double =
        transactions.sumOf { it.amount }

    /**
     * Gets the total value of pending settlements
     */
    fun getTotalPendingSettlementValue(): Double =
        transactions.filter { it.status == TransactionStatusProcess.createPending() }.sumOf { it.amount }
}

/**
 * Extension function to format a double as currency
 */
private fun Double.toCurrency(): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(this)

/**
 * Extension function to format a list of transactions for display
 */
private fun List<TransactionModel>.formatForDisplay(header: String): String {
    if (this.isEmpty()) return "$header: No transactions found"
    
    val builder = StringBuilder()
    builder.appendLine(header)
    this.forEach { txn ->
        builder.appendLine(" - ID: ${txn.id}, Customer: ${txn.customer}, " +
                "Amount: ${txn.amount}, Status: ${txn.status}" +
                if (!txn.isFullyReconciled) " (Pending: ${txn.pendingAmount.toCurrency()})" else "")
    }
    return builder.toString().trim()
}

/**
 * Main function demonstrating the usage of Transaction and TransactionService
 */
fun main() {
    // Initialize transaction service
    val transactionService = TransactionService()

    // Add sample transactions
    val transactions = listOf(
        TransactionModel(1, "Alice", 60000.0, 60000.0, TransactionStatusProcess.createSettled()),
        TransactionModel(2, "Bob", 25000.0, 0.0, TransactionStatusProcess.createPending()),
        TransactionModel(3, "Charlie", 80000.0, 75000.0, TransactionStatusProcess.createSettled()),
        TransactionModel(4, "David", 1500.0, 1500.0, TransactionStatusProcess.createSettled()),
        TransactionModel(5, "Eve", 3000.0, 0.0, TransactionStatusProcess.createPending()),
        TransactionModel(6, "Frank", 120000.0, 100000.0, TransactionStatusProcess.createSettled())
    )
    
    transactions.forEach { transactionService.addTransaction(it) }

    // Get pending transactions
    val pendingTxns = transactionService.getPendingTransactions()
    println(pendingTxns.formatForDisplay("\n PENDING TRANSACTIONS"))

    // Get high-value transactions
    val highValueTxns = transactionService.getHighValueTransactions()
    println("\n HIGH-VALUE TRANSACTIONS (Amount > $50,000)")
    highValueTxns.forEach { txn ->
        println(" - ID: ${txn.id}, Customer: ${txn.customer}, " +
                "Amount: ${txn.amount}, Status: ${txn.status}")
    }

    // Get mismatched transactions
    val mismatchedTxns = transactionService.getMismatchedTransactions()
    println("\n  MISMATCHED TRANSACTIONS (Not fully reconciled)")
    mismatchedTxns.forEach { txn ->
        println(" - ID: ${txn.id}, Customer: ${txn.customer}, " +
                "Amount: ${txn.amount}, " +
                "Settled: ${txn.reconciledAmount?.toCurrency()}, " +
                "Pending: ${txn.pendingAmount.toCurrency()}")
    }

    // Show summary
    println("\n SETTLEMENT SUMMARY")
    println("-Total Transactions: ${transactions.size}")
    println("-Total Value: ${transactionService.getTotalTransactionValue().toCurrency()}")
    println("-Pending Settlement Value: ${transactionService.getTotalPendingSettlementValue().toCurrency()}")
    
    //  custom filter using processTransactions
    val largePendingTxns = transactionService.processTransactions { 
        it.status == TransactionStatusProcess.createPending() && it.amount > 10000
    }
    println("\n LARGE PENDING TRANSACTIONS (>$10,000)")
    println(largePendingTxns.formatForDisplay(""))
}
