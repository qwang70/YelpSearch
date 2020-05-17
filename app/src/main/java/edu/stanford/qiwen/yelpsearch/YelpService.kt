package edu.stanford.qiwen.yelpsearch

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

public interface YelpService {

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
        @Query("attributes") attributes: String?
    ): Call<YelpSearchResult>
}