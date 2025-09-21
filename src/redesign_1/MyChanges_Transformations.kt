package redesign_1

/**
 * Data class representing a Retail Store with its sales data
 */
data class RetailStore(
    val name: String,
    val monthlySales: Int,
    val isActive: Boolean = true
) {
    init {
        require(monthlySales >= 0) { "Monthly sales cannot be negative" }
        require(name.isNotBlank()) { "Store name cannot be blank" }
    }
}

/**
 * Service class to handle store operations and transformations
 */
class StoreAnalyticsService {
    private val stores = mutableListOf<RetailStore>()

    /**
     * Add a new store to the analytics service
     */
    fun addStore(store: RetailStore) {
        stores.add(store)
    }

    /**
     * Get all stores sorted by sales in ascending order
     */
    fun getStoresBySalesAscending(): List<RetailStore> = stores.sortedBy { it.monthlySales }

    /**
     * Get stores with sales greater than the specified threshold
     */
    fun getHighPerformingStores(threshold: Int): List<RetailStore> = 
        stores.filter { it.monthlySales > threshold }

    /**
     * Apply a discount to all stores' sales
     */
    fun applyDiscountToAllStores(discount: Int): List<Int> =
        stores.map { it.monthlySales - discount }

    /**
     * Check if any store exceeds the given sales target
     */
    fun hasStoreExceedingSales(target: Int): Boolean =
        stores.any { it.monthlySales > target }

    /**
     * Check if all stores meet the minimum sales requirement
     */
    fun allStoresMeetMinimumSales(minimum: Int): Boolean =
        stores.all { it.monthlySales >= minimum }

    /**
     * Calculate total sales across all stores
     */
    fun calculateTotalSales(): Int =
        stores.sumOf { it.monthlySales }

    /**
     * Calculate total sales with a bonus added to each store's sales
     */
    fun calculateTotalSalesWithBonus(bonus: Int): Int =
        stores.fold(0) { acc, store -> acc + store.monthlySales + bonus }
}

/**
 * Extension function to print store information in a formatted way
 */
private fun RetailStore.printInfo() {
    println("""
        |------------------------
        | Store: $name
        | Monthly Sales: ₹${String.format("%,d", monthlySales)}
        | Status: ${if (isActive) "Active" else "Inactive"}
        |------------------------
    """.trimMargin())
}

/**
 * Main function demonstrating the usage
 */
fun main() {
    // Initialize the analytics service
    val analyticsService = StoreAnalyticsService()

    // Add some sample stores
    val stores = listOf(
        RetailStore("Free Margin", 50_000),
        RetailStore("Kunil", 200_000),
        RetailStore("Jai Mart", 1_000_000),
        RetailStore("Mega Mart", 100_000)
    )
    
    stores.forEach { analyticsService.addStore(it) }

    // Demonstrate various transformations and operations
    println("=== All Stores by Sales (Ascending) ===")
    analyticsService.getStoresBySalesAscending().forEach { it.printInfo() }

    println("\n=== High Performing Stores (Sales > 100,000) ===")
    analyticsService.getHighPerformingStores(100_000).forEach { it.printInfo() }

    println("\n=== Sales After 1,000 Discount ===")
    val discountedSales = analyticsService.applyDiscountToAllStores(1_000)
    println("Discounted sales: ${discountedSales.joinToString(", ")}")

    println("\n=== Store Performance Analysis ===")
    println("Any store with sales > 200,000: ${analyticsService.hasStoreExceedingSales(200_000)}")
    println("All stores meet minimum 50,000 sales: ${analyticsService.allStoresMeetMinimumSales(50_000)}")
    
    val totalSales = analyticsService.calculateTotalSales()
    println("\n=== Financial Summary ===")
    println("Total Sales: ₹${String.format("%,d", totalSales)}")
    
    val totalWithBonus = analyticsService.calculateTotalSalesWithBonus(5_000)
    println("Total Sales with ₹5,000 bonus per store: ₹${String.format("%,d", totalWithBonus)}")
}
