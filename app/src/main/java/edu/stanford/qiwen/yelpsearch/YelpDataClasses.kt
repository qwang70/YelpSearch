package edu.stanford.qiwen.yelpsearch

import com.google.gson.annotations.SerializedName

data class YelpBusinessDetail(
    val name: String,
    @SerializedName("image_url") val imageUrl: String,
    val url: String,
    @SerializedName("display_phone") val phone: String,
    @SerializedName("review_count") val reviewCount: Int,
    val categories: List<YelpCategory>,
    val rating: Double,
    val location: YelpLocation,
    val photos: List<String>,
    val price: String,
    val hours: List<YelpHours>
) {

    fun getStarRatingRes(): Int {
        return getStarRatingRes(rating)
    }
}


data class YelpSearchResult(
    val total: Int,
    @SerializedName("businesses") val restaurants: List<YelpRestaurant>
)

data class YelpRestaurant(
    val name: String,
    val rating: Double,
    val price: String,
    val id: String,
    @SerializedName("review_count") val numReviews: Int,
    @SerializedName("distance") val distanceInMeters: Double,
    @SerializedName("image_url") val imageUrl: String,
    val categories: List<YelpCategory>,
    val location: YelpLocation
) {
    fun displayDistance(): String {
        return displayDistance(distanceInMeters)
    }

    fun getStarRatingRes(): Int {
        return getStarRatingRes(rating)
    }
}

data class YelpCategory(
    val title: String
)

data class YelpLocation(
    @SerializedName("address1") val address: String,
    @SerializedName("display_address") val displayAddress: List<String>
)

data class YelpHours(
    @SerializedName("open") val open: List<YelpHour>,
    @SerializedName("is_open_now") val isOpenNow: Boolean
)

data class YelpHour(
    val start: String,
    val end: String
)

fun getStarRatingRes(rating: Double): Int {
    return when (rating) {
        0.0 -> R.drawable.stars_regular_0
        1.0 -> R.drawable.stars_regular_1
        1.5 -> R.drawable.stars_regular_1_half
        2.0 -> R.drawable.stars_regular_2
        2.5 -> R.drawable.stars_regular_2_half
        3.0 -> R.drawable.stars_regular_3
        3.5 -> R.drawable.stars_regular_3_half
        4.0 -> R.drawable.stars_regular_4
        4.5 -> R.drawable.stars_regular_4_half
        5.0 -> R.drawable.stars_regular_5
        else -> R.drawable.stars_regular_0
    }
}

fun displayDistance(distanceInMeters: Double): String {
    val milesPerMeter = 0.000621371
    val distanceInMiles = "%.2f".format(distanceInMeters * milesPerMeter)
    return "$distanceInMiles mi"
}