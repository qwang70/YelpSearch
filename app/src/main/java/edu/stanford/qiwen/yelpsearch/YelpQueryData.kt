package edu.stanford.qiwen.yelpsearch

data class YelpQueryData (
    var term: String? = "Restaurant",
    var location: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var sortBy: String = "best_match",
    var price1: Boolean = false,
    var price2: Boolean = false,
    var price3: Boolean = false,
    var price4: Boolean = false,
    var openNow: Boolean = false,
    var attributes: String? = null
) {
    fun getLoc(): String? {
        if (location != null) return location
        if (latitude == null || longitude == null) return "New York"
        return null
    }
    fun getLat(): Double? {
        if (location != null) return null
        return latitude
    }
    fun getLon(): Double? {
        if (location != null) return null
        return longitude
    }
    fun getPrice() : String? {
        if (!price1 && !price2 && !price3 && !price4) return null
        var concatPrice = ""
        if (price1)
            concatPrice += "1,"
        if (price2)
            concatPrice += "2,"
        if (price3)
            concatPrice += "3,"
        if (price4)
            concatPrice += "4,"
        return concatPrice.dropLast(1)
    }
}