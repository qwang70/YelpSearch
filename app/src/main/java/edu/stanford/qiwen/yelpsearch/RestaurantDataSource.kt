package edu.stanford.qiwen.yelpsearch

import androidx.paging.PositionalDataSource

class RestaurantDataSource : PositionalDataSource<YelpRestaurant>() {
    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<YelpRestaurant>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<YelpRestaurant>
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}