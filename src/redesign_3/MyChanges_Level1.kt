/*
package redesign_3

import java.text.NumberFormat
import java.util.*

*/
/**
 * Represents a product in the inventory system.
 * @property id Unique identifier for the product
 * @property name Name of the product
 * @property stock Current quantity in stock
 * @property basePrice Base price before any discounts
 * @property category Category of the product
 *//*

data class Product(
    val id: Int,
    val name: String,
    val stock: Int,
    val basePrice: Double,
    val category: ProductCategorys = ProductCategorys.ELECTRONICS
) {
    */
/**
     * Applies a discount to the product's price
     * @param percentage Discount percentage (0-100)
     * @return New price after discount
     *//*

    fun applyDiscount(percentage: Double): Double {
        require(percentage in 0.0..100.0) { "Discount percentage must be between 0 and 100" }
        return basePrice * (1 - percentage / 100)
    }

    */
/**
     * Checks if the product is out of stock
     *//*

    val isOutOfStock: Boolean
        get() = stock <= 0

    */
/**
     * Checks if the product is low in stock
     *//*

    val isLowStock: Boolean
        get() = stock in 1..3

    */
/**
     * Formatted price string with currency symbol
     *//*

    val formattedPrice: String
        get() = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(basePrice)
}

*/
/**
 * Represents different product categories
 *//*

enum class ProductCategory {
    ELECTRONICS,
    CLOTHING,
    HOME_APPLIANCES,
    BOOKS,
    SPORTS
}

*/
/**
 * Service class for product-related operations
 *//*

class ProductService {
    private val products = mutableListOf<Products>()

    */
/**
     * Adds a product to the inventory
     *//*

    fun addProduct(product: Products) {
        require(product.stock >= 0) { "Stock cannot be negative" }
        require(product.basePrice >= 0) { "Price cannot be negative" }
        products.add(product)
    }

    */
/**
     * Gets all out-of-stock products
     *//*

    fun getOutOfStockProducts(): List<Products> = products.filter { it.isOutOfStock }

    */
/**
     * Applies a discount to products matching the given predicate
     * @param discountPercentage Discount percentage (0-100)
     * @param predicate Condition to apply discount
     * @return List of products with discounted prices
     *//*

    fun applyDiscountToProducts(
        discountPercentage: Double,
        predicate: (Products) -> Boolean = { true }
    ): Map<Products, Double> {
        return products
            .filter(predicate)
            .associateWith { it.applyDiscount(discountPercentage) }
    }

    */
/**
     * Gets products with low stock (1-3 items)
     *//*

    fun getLowStockProducts(): List<Products> = products.filter { it.isLowStock }

    */
/**
     * Gets products by category
     *//*

    fun getProductsByCategory(category: ProductCategorys): List<Products> =
        products.filter { it.category == category }

    */
/**
     * Gets the total inventory value
     *//*

    fun getTotalInventoryValue(): Double =
        products.sumOf { it.basePrice * it.stock }
}

*/
/**
 * Extension function to format a double as currency
 *//*

private fun Double.toCurrency(): String =
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(this)

*/
/**
 * Main function demonstrating the usage of Product and ProductService
 *//*

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

    // 1. Get out of stock products
    val outOfStock = productService.getOutOfStockProducts()
    println("🛒 Out of stock products (${outOfStock.size}):")
    outOfStock.forEach { println(" - ${it.name} (ID: ${it.id})") }

    // 2. Apply 10% discount to all electronics
    val discountedProducts = productService.applyDiscountToProducts(10.0) { 
        it.category == ProductCategorys.ELECTRONICS
    }
    
    println("\n💰 Discounted Electronics (10% off):")
    discountedProducts.forEach { (product, discountedPrice) ->
        println(" - ${product.name}: ${product.formattedPrice} → ${discountedPrice.toCurrency()}")
    }

    // 3. Get low stock products
    val lowStock = productService.getLowStockProducts()
    println("\n⚠️  Low stock products (${lowStock.size}):")
    lowStock.forEach { 
        println(" - ${it.name}: ${it.stock} left (${it.formattedPrice} each)")
    }

    // 4. Get total inventory value
    val totalValue = productService.getTotalInventoryValue()
    println("\n📊 Total inventory value: ${totalValue.toCurrency()}")

    // 5. Get products by category
    val electronics = productService.getProductsByCategory(ProductCategorys.ELECTRONICS)
    println("\n📱 Electronics (${electronics.size}):")
    electronics.forEach { println(" - ${it.name}: ${it.stock} in stock") }
}
*/
