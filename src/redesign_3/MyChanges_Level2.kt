package redesign_3

/**
 * Represents a product in the inventory system.
 * @property id Unique identifier for the product
 * @property name Name of the product
 * @property stock Current quantity in stock
 * @property price Price of the product
 * @property category Category of the product (defaults to GENERAL)
 */
data class Products(
    val id: Int,
    val name: String,
    val stock: Int,
    val price: Double,
    val category: ProductCategorys = ProductCategorys.CLOTHING
) {
    /**
     * Checks if the product is out of stock
     */
    val isOutOfStock: Boolean
        get() = stock <= 0

    /**
     * Checks if the product is in critical stock (less than 3 items)
     */
    val isCriticalStock: Boolean
        get() = stock in 1..2

    /**
     * Checks if the product is a premium product (price > 20000)
     */
    val isPremium: Boolean
        get() = price > 20000
}

/**
 * Enum representing different product categories
 */
enum class ProductCategorys {
    ELECTRONICS,
    CLOTHING,
    HOME_APPLIANCES,
    BOOKS,
    GENERAL
}

/**
 * Service class for product-related operations
 */
class ProductService {
    private val products = mutableListOf<Products>()

    /**
     * Adds a product to the inventory
     * @param product The product to add
     */
    fun addProduct(product: Products) {
        require(product.stock >= 0) { "Stock cannot be negative" }
        require(product.price >= 0) { "Price cannot be negative" }
        products.add(product)
    }

    /**
     * Processes products based on a given rule
     * @param rule The condition to filter products
     * @return List of products that match the rule
     */
    fun processProducts(rule: (Products) -> Boolean): List<Products> {
        return products.filter(rule)
    }

    /**
     * Gets all out-of-stock products
     */
    fun getOutOfStockProducts(): List<Products> = processProducts { it.isOutOfStock }

    /**
     * Gets products with critical stock levels
     */
    fun getCriticalStockProducts(): List<Products> = processProducts { it.isCriticalStock }

    /**
     * Gets all premium products
     */
    fun getPremiumProducts(): List<Products> = processProducts { it.isPremium }

    /**
     * Gets products by category
     * @param category The category to filter by
     */
    fun getProductsByCategory(category: ProductCategorys): List<Products> =
        processProducts { it.category == category }
}

/**
 * Extension function to format a list of products for display
 */
private fun List<Products>.formatForDisplay(header: String): String {
    if (this.isEmpty()) return "$header: No products found"
    
    val builder = StringBuilder()
    builder.appendLine(header)
    this.forEach { product ->
        builder.appendLine(" - ${product.name} (ID: ${product.id}): ${product.stock} in stock, Price: ${product.price}")
    }
    return builder.toString().trim()
}

/**
 * Main function demonstrating the usage of Product and ProductService
 */
fun main() {
    // Initialize product service
    val productService = ProductService()

    // Add sample products
    val products = listOf(
        Products(1, "Laptop", 10, 60000.0, ProductCategorys.ELECTRONICS),
        Products(2, "Phone", 0, 25000.0, ProductCategorys.ELECTRONICS),
        Products(3, "Headphones", 5, 2000.0, ProductCategorys.ELECTRONICS),
        Products(4, "Keyboard", 2, 1500.0, ProductCategorys.ELECTRONICS),
        Products(5, "T-Shirt", 1, 500.0, ProductCategorys.CLOTHING),
        Products(6, "Smart Watch", 0, 15000.0, ProductCategorys.ELECTRONICS)
    )
    
    products.forEach { productService.addProduct(it) }

    // Get out of stock products
    val outOfStock = productService.getOutOfStockProducts()
    println(outOfStock.formatForDisplay("OUT OF STOCK PRODUCTS"))

    // Get critical stock products
    val criticalStock = productService.getCriticalStockProducts()
    println("\n" + criticalStock.formatForDisplay("CRITICAL STOCK (1-2 items left)"))

    // Get premium products
    val premiumProducts = productService.getPremiumProducts()
    println("\n" + premiumProducts.formatForDisplay("PREMIUM PRODUCTS (Price > 20000)"))

    // Get products by category
    val electronics = productService.getProductsByCategory(ProductCategorys.ELECTRONICS)
    println("\n" + electronics.formatForDisplay("ELECTRONICS CATEGORY"))

    // Custom filter example
    val customFiltered = productService.processProducts { 
        it.price < 2000 && it.stock > 0 
    }
    println("\n" + customFiltered.formatForDisplay("AFFORDABLE ITEMS IN STOCK (Price < 2000)"))
}
