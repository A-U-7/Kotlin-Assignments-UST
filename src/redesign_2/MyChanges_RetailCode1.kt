package redesign_2

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

/**
 * Sealed class representing different types of discounts
 */
sealed class DiscountType {
    data class Percentage(val value: Double) : DiscountType()
    data class FixedAmount(val amount: Double) : DiscountType()
    object NoDiscount : DiscountType()
}

/**
 * Data class representing a Product with validation
 */
data class Product(
    val id: Int,
    val name: String,
    private var _price: BigDecimal,
    val category: String = "General",
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    var price: BigDecimal
        get() = _price.setScale(2, RoundingMode.HALF_EVEN)
        set(value) {
            require(value >= BigDecimal.ZERO) { "Price cannot be negative" }
            _price = value
        }

    init {
        require(id > 0) { "ID must be positive" }
        require(name.isNotBlank()) { "Product name cannot be blank" }
        require(_price >= BigDecimal.ZERO) { "Price cannot be negative" }
    }

    /**
     * Applies a discount to the product's price
     * @param discount The discount to apply
     * @return The new price after discount
     */
    fun applyDiscount(discount: DiscountType): BigDecimal {
        price = when (discount) {
            is DiscountType.Percentage -> {
                require(discount.value in 0.0..100.0) { "Discount percentage must be between 0 and 100" }
                val discountAmount = _price.multiply(BigDecimal(discount.value / 100))
                _price - discountAmount
            }
            is DiscountType.FixedAmount -> {
                require(discount.amount >= 0) { "Discount amount cannot be negative" }
                (_price - BigDecimal(discount.amount)).coerceAtLeast(BigDecimal.ZERO)
            }
            is DiscountType.NoDiscount -> _price
        }
        return price
    }
}

/**
 * Interface for discount strategies
 */
interface DiscountStrategy {
    fun applyDiscount(product: Product): BigDecimal
}

/**
 * Implementation of percentage discount strategy
 */
class PercentageDiscount(private val percentage: Double) : DiscountStrategy {
    init {
        require(percentage in 0.0..100.0) { "Percentage must be between 0 and 100" }
    }

    override fun applyDiscount(product: Product): BigDecimal {
        return product.applyDiscount(DiscountType.Percentage(percentage))
    }
}

/**
 * Implementation of fixed amount discount strategy
 */
class FixedAmountDiscount(private val amount: Double) : DiscountStrategy {
    init {
        require(amount >= 0) { "Discount amount cannot be negative" }
    }

    override fun applyDiscount(product: Product): BigDecimal {
        return product.applyDiscount(DiscountType.FixedAmount(amount))
    }
}

/**
 * Product catalog service for managing products
 */
class ProductCatalog {
    private val products = mutableListOf<Product>()

    fun addProduct(product: Product) {
        require(products.none { it.id == product.id }) { "Product with ID ${product.id} already exists" }
        products.add(product)
    }

    fun getProduct(id: Int): Product? = products.find { it.id == id }

    fun getAllProducts(): List<Product> = products.toList()

    fun applyDiscountToAll(discountStrategy: DiscountStrategy): List<Pair<Product, BigDecimal>> {
        return products.map { product ->
            val newPrice = discountStrategy.applyDiscount(product)
            product to newPrice
        }
    }
}

/**
 * Extension function to display product information
 */
fun Product.displayInfo() {

    println("""
        |=== Product Information ===
        |ID:          $id
        |Name:        $name
        |Category:    $category
        |Price:       ₹${price.setScale(2, RoundingMode.HALF_EVEN)}
        |Added on:    ${createdAt}
        |===========================
    """.trimMargin())
}

/**
 * Main function demonstrating the usage
 */
fun main() {
    // Initialize product catalog
    val catalog = ProductCatalog()

    // Add some sample products
    val products = listOf(
        Product(101, "Running Shoes", BigDecimal("2000.00"), "Footwear"),
        Product(102, "Wireless Earbuds", BigDecimal(5000), "Electronics"),
        Product(103, "Yoga Mat", BigDecimal(1200), "Fitness"),
        Product(104, "Water Bottle", BigDecimal(500), "Accessories")
    )

    products.forEach { catalog.addProduct(it) }

    // Display all products
    println("=== All Products ===")
    catalog.getAllProducts().forEach { it.displayInfo() }

    // Apply percentage discount to all products
    println("\n=== Applying 10% Discount to All Products ===")
    val percentageDiscount = PercentageDiscount(10.0)
    catalog.applyDiscountToAll(percentageDiscount)
    catalog.getAllProducts().forEach { it.displayInfo() }

    // Apply fixed amount discount to a specific product
    println("\n=== Applying ₹500 Fixed Discount to Product ID 101 ===")
    val product = catalog.getProduct(101)
    product?.let {
        val fixedDiscount = FixedAmountDiscount(500.0)
        fixedDiscount.applyDiscount(it)
        it.displayInfo()
    } ?: println("Product not found")

    // Try to create a product with invalid data (will throw exception)
    try {
        Product(-1, "", BigDecimal(-100))
    } catch (e: IllegalArgumentException) {
        println("\nError creating product: ${e.message}")
    }
}
