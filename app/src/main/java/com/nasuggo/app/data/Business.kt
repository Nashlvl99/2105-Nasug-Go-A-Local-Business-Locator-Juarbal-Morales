package com.nasuggo.app.data

import java.util.Calendar

abstract class Business(dto: BusinessDto, private val searchTerms: SearchTerms = SearchTerms.NONE) {
    val id: String = dto.id
    val name: String = dto.name
    val category: String = dto.category
    val description: String = dto.description.orEmpty()
    val address: String = dto.address.orEmpty()
    val phone: String? = dto.phone
    val latitude: Double = dto.latitude
    val longitude: Double = dto.longitude
    val offerings: List<String> = dto.offerings.orEmpty().toList()
    val keywords: List<String> = dto.keywords.orEmpty().toList()
    val barangay: String = dto.barangay.orEmpty()

    val schedule: OpeningHours = OpeningHours(dto.hours.orEmpty())

    abstract val group: BusinessGroup?

    val hasHours: Boolean get() = schedule.isListed

    fun isOpenAt(now: Calendar = Calendar.getInstance()): Boolean = schedule.isOpenAt(now)
    fun todayCode(now: Calendar = Calendar.getInstance()): String = OpeningHours.dayCode(now)
    fun todayText(now: Calendar = Calendar.getInstance()): String = schedule.todayText(now)
    fun hoursText(): String = schedule.weeklyText()

    fun matches(query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return true
        val terms = listOf(name, category, description, address, barangay) +
            offerings + keywords + listOfNotNull(group?.label)
        return terms.any { it.lowercase().contains(q) } || searchTerms.contains(q)
    }

    override fun equals(other: Any?): Boolean = other is Business && other.id == id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "${this::class.simpleName}($id, $name)"
}

class FoodBusiness(dto: BusinessDto) : Business(dto, SearchTerms.FOOD) {
    override val group = BusinessGroup.FOOD
}

class RetailBusiness(dto: BusinessDto) : Business(dto, SearchTerms.RETAIL) {
    override val group = BusinessGroup.RETAIL
}

class ServiceBusiness(dto: BusinessDto) : Business(dto) {
    override val group = BusinessGroup.SERVICES
}

class GeneralBusiness(dto: BusinessDto) : Business(dto) {
    override val group: BusinessGroup? = null
}

object BusinessFactory {
    private val food = setOf("dining", "food", "restaurants")
    private val retail = setOf("groceries", "grocery", "shopping", "hardware", "pharmacies", "pharmacy")
    private val services = setOf("beauty & fitness", "entertainment", "services", "health")

    fun create(dto: BusinessDto): Business {
        val c = dto.category.trim().lowercase()
        return when {
            c in food -> FoodBusiness(dto)
            c in retail -> RetailBusiness(dto)
            c in services -> ServiceBusiness(dto)
            else -> GeneralBusiness(dto)
        }
    }
}
