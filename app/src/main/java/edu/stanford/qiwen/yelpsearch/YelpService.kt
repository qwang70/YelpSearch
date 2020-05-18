package edu.stanford.qiwen.yelpsearch

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

const val BASE_URL = "https://api.yelp.com/v3/"
const val EXTRA_ID = "extra_id"
const val EXTRA_DISPLAY_DISTANCE = "extra_display_distance"
interface YelpService {

    @GET("businesses/search")
    fun searchRestaurants(
        @Header("Authorization") authHeader: String,
        @Query("term") searchTerm: String?,
        @Query("location") location: String?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("sort_by") sortBy: String,
        @Query("price") price: String?,
        @Query("open_now") openNow: Boolean,
        @Query("attributes") attributes: String?,
        @Query("offset") offset: Int? = null
    ): Call<YelpSearchResult>

    @GET("businesses/{id}")
    fun queryBusinessDetails(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): Call<YelpBusinessDetail>
}