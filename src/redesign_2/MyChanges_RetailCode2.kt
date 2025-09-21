package redesign_2

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * Sealed class representing different types of promotions with validation
 */
sealed class Promotion {
    abstract val id: String
    abstract val name: String
    abstract val validUntil: LocalDate?
    abstract val isActive: Boolean
    
    data class FlatDiscount(
        override val id: String,
        override val name: String,
        val amount: BigDecimal,
        override val validUntil: LocalDate? = null,
        override val isActive: Boolean = true
    ) : Promotion() {
        init {
            require(amount >= BigDecimal.ZERO) { "Discount amount cannot be negative" }
            require(id.isNotBlank()) { "Promotion ID cannot be blank" }
        }
    }

    data class PercentageDiscount(
        override val id: String,
        override val name: String,
        val percent: Double,
        override val validUntil: LocalDate? = null,
        override val isActive: Boolean = true,
        val maxDiscount: BigDecimal? = null
    ) : Promotion() {
        init {
            require(percent in 0.0..100.0) { "Percentage must be between 0 and 100" }
            require(id.isNotBlank()) { "Promotion ID cannot be blank" }
            maxDiscount?.let { require(it >= BigDecimal.ZERO) { "Max discount cannot be negative" } }
        }
    }

    object NoPromotion : Promotion() {
        override val id: String = "NO_PROMO"
        override val name: String = "No Promotion"
        override val validUntil: LocalDate? = null
        override val isActive: Boolean = false
    }
}

/**
 * Interface for products that can have promotions applied
 */
interface Promotable {
    val id: Int
    val name: String
    var price: BigDecimal
    var currentPromotion: Promotion
    
    fun applyPromotion(promotion: Promotion): BigDecimal
    fun removePromotion()
    fun getDiscountedPrice(): BigDecimal
}

/**
 * Abstract base product class with common functionality
 */
abstract class AbstractProduct(
    final override val id: Int,
    final override val name: String,
    price: BigDecimal,
    val category: String
) : Promotable {
    final override var price: BigDecimal = price.setScale(2, RoundingMode.HALF_EVEN)
        set(value) {
            require(value >= BigDecimal.ZERO) { "Price cannot be negative" }
            field = value.setScale(2, RoundingMode.HALF_EVEN)
        }

    final override var currentPromotion: Promotion = Promotion.NoPromotion

    init {
        require(id > 0) { "ID must be positive" }
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(price >= BigDecimal.ZERO) { "Price cannot be negative" }
    }

    final override fun applyPromotion(promotion: Promotion): BigDecimal {
        if (!promotion.isActive) return price
        
        val now = LocalDate.now()
        if (promotion.validUntil != null && now.isAfter(promotion.validUntil)) {
            return price
        }

        currentPromotion = promotion
        return getDiscountedPrice()
    }

    final override fun removePromotion() {
        currentPromotion = Promotion.NoPromotion
    }

    final override fun getDiscountedPrice(): BigDecimal = when (val promo = currentPromotion) {
        is Promotion.FlatDiscount -> (price - promo.amount).coerceAtLeast(BigDecimal.ZERO)
        is Promotion.PercentageDiscount -> {
            val discount = price.multiply(BigDecimal(promo.percent / 100.0))
            val finalDiscount = promo.maxDiscount?.let { discount.min(it) } ?: discount
            (price - finalDiscount).setScale(2, RoundingMode.HALF_EVEN)
        }
        is Promotion.NoPromotion -> price
    }

    abstract fun productInfo(): String

    protected fun baseProductInfo(): String {
        return """
            |ID:          $id
            |Name:        $name
            |Category:    $category
            |Base Price:  ₹$price
            |Promotion:   ${currentPromotion.name}
            |Final Price: ₹${getDiscountedPrice()}
        """.trimMargin()
    }
}

/**
 * Standard product implementation
 */
class StandardProduct(
    id: Int,
    name: String,
    price: BigDecimal,
    category: String = "General",
    val brand: String? = null
) : AbstractProduct(id, name, price, category) {

    override fun productInfo(): String {
        return """
            |=== Standard Product ===
            |${baseProductInfo()}
            |${brand?.let { "Brand:      $it\n" } ?: ""}|==========================
        """.trimMargin()
    }
}

/**
 * Grocery product with additional properties
 */
class GroceryProduct(
    id: Int,
    name: String,
    price: BigDecimal,
    val expiryDate: LocalDate? = null,
    val weight: String? = null
) : AbstractProduct(id, name, price, "Grocery") {

    override fun productInfo(): String {
        return """
            |=== Grocery Product ===
            |${baseProductInfo()}
            |${expiryDate?.let { "Expires:    $it\n" } ?: ""}${weight?.let { "Weight:     $it\n" } ?: ""}===========================
        """.trimMargin()
    }
}

/**
 * Main function demonstrating the usage
 */
fun main() {
    // Create some promotions
    val weekendSale = Promotion.PercentageDiscount(
        id = "WEEKEND25",
        name = "25% Weekend Special",
        percent = 25.0,
        validUntil = LocalDate.now().plusDays(2),
        maxDiscount = BigDecimal(5000)
    )

    val clearanceSale = Promotion.FlatDiscount(
        id = "CLEAR50",
        name = "Clearance Sale - ₹50 off",
        amount = BigDecimal(50),
        validUntil = LocalDate.now().plusMonths(1)
    )

    // Create products
    val laptop = StandardProduct(
        id = 201,
        name = "Gaming Laptop",
        price = BigDecimal(75000),
        category = "Electronics",
        brand = "GamerPro"
    )

    val apple = GroceryProduct(
        id = 202,
        name = "Organic Apples",
        price = BigDecimal(250),
        weight = "1kg",
        expiryDate = LocalDate.now().plusWeeks(2)
    )

    // Apply promotions
    println("=== Applying Promotions ===\n")
    
    laptop.applyPromotion(weekendSale)
    println(laptop.productInfo())
    
    println()
    
    apple.applyPromotion(clearanceSale)
    println(apple.productInfo())
    
    // Test removing promotion
    println("\n=== After Removing Promotion ===\n")
    laptop.removePromotion()
    println(laptop.productInfo())
    
    // Test expired promotion
    println("\n=== Testing Expired Promotion ===\n")
    val expiredPromo = Promotion.PercentageDiscount(
        id = "OLD_PROMO",
        name = "Expired Promotion",
        percent = 50.0,
        validUntil = LocalDate.now().minusDays(1)
    )
    
    laptop.applyPromotion(expiredPromo)
    println(laptop.productInfo())
}
