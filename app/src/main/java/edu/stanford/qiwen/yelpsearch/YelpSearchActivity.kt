package edu.stanford.qiwen.yelpsearch

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_yelp_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "MainActivity"
private const val LOCATION_NY = "New York"
private const val LOCATION_PA = "Palo Alto"
private const val LOCATION_CHAM = "Champaign, IL"
private const val MY_PERMISSIONS_ACCESS_LOCATION = 1234
class YelpSearchActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchView: SearchView
    private lateinit var adapter: RestaurantAdapter
    private val retrofit =
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
    private val yelpService = retrofit.create(YelpService::class.java)
    private val restaurants = mutableListOf<YelpRestaurant>()
    private var queryData = YelpQueryData()
    private var sortByCheckedItem = 0

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yelp_search)

        // Get current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set Restaurant search result adapter
        adapter = RestaurantAdapter(this, restaurants, object : RestaurantAdapter.OnClickListener {
            override fun onItemClick(restaurant: YelpRestaurant) {
                // When user taps on view in RV, navigate to new activity
                val intent = Intent(this@YelpSearchActivity, YelpBusinessDetailActivity::class.java)
                intent.putExtra(EXTRA_ID, restaurant.id)
                intent.putExtra(EXTRA_DISPLAY_DISTANCE, restaurant.displayDistance())
                startActivity(intent)
            }
        })
        rvRestaurants.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        rvRestaurants.layoutManager = linearLayoutManager
        // add divider between items
        rvRestaurants.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )

        requestPermissionAndGetLastKnownLocation()
        setOnClickListenerForTabBar()

        // Endless scrolling
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(
                page: Int,
                totalItemsCount: Int,
                view: RecyclerView
            ) {
                Log.d(TAG, "Load more data. page $page, totalItemsCount $totalItemsCount")
                loadMoreData(queryData, totalItemsCount)
            }
        }
        // Adds the scroll listener to RecyclerView
        rvRestaurants.addOnScrollListener(scrollListener)
        swipeContainer.setOnRefreshListener {
            searchRestaurant(queryData)
        }
    }

    /** Hide the soft keyboard after the user click back button from the search bar */
    private fun hideSoftKeyBoard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        if (imm.isAcceptingText) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        // Search Item
        val searchMenuItem = menu?.findItem(R.id.action_search) as MenuItem
        searchView = searchMenuItem.actionView as SearchView
        // Set the default query text
        searchView.setQuery("Restaurant", false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.i(TAG, "onQueryTextSubmit: $query")
                if (query == null || query.isEmpty()) {
                    queryData.term = null
                } else {
                    queryData.term = query
                }
                searchRestaurant(queryData)
                hideSoftKeyBoard()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.i(TAG, "onQueryTextChange: $newText")
                return true
            }
        })

        // Hide the soft keyboard on menu item action collapse
        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                hideSoftKeyBoard()
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    /** Verify the network connection.
     * Return true if connected.
     */
    private fun verifyAvailableNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    /** Request permission for the location access and set the last known location in YelpQueryData
     */
    private fun requestPermissionAndGetLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                searchRestaurant(queryData)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_ACCESS_LOCATION
                )

                // MY_PERMISSIONS_ACCESS_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            getLastKnownLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionsResult code: $requestCode")
        when (requestCode) {
            MY_PERMISSIONS_ACCESS_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLastKnownLocation()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    searchRestaurant(queryData)
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    /** Get the last known location and set it in the queryData. */
    private fun getLastKnownLocation() {
        Log.i(TAG, "getLastKnownLocation")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    queryData.latitude = location.latitude
                    queryData.longitude = location.longitude
                }
                searchRestaurant(queryData)
            }
    }

    private fun setOnClickListenerForTabBar() {
        btSort.setOnClickListener {
            showSortByAlertDialog()
        }

        btHot.setOnClickListener {
            btHot.isSelected = !btHot.isSelected
            if (btHot.isSelected) {
                queryData.attributes = "hot_and_new"
            } else {
                queryData.attributes = null
            }

            searchRestaurant(queryData)
        }

        btOpen.setOnClickListener {
            btOpen.isSelected = !btOpen.isSelected
            queryData.openNow = !queryData.openNow
            searchRestaurant(queryData)
        }

        // Set listener to price buttons. Multiple buttons can be pressed together.
        btPrice1.setOnClickListener {
            btPrice1.isSelected = !btPrice1.isSelected
            queryData.price1 = !queryData.price1
            searchRestaurant(queryData)
        }

        btPrice2.setOnClickListener {
            btPrice2.isSelected = !btPrice2.isSelected
            queryData.price2 = !queryData.price2
            searchRestaurant(queryData)
        }

        btPrice3.setOnClickListener {
            btPrice3.isSelected = !btPrice3.isSelected
            queryData.price3 = !queryData.price3
            searchRestaurant(queryData)
        }

        btPrice4.setOnClickListener {
            btPrice4.isSelected = !btPrice4.isSelected
            queryData.price4 = !queryData.price4
            searchRestaurant(queryData)
        }


        // Set listener to location buttons
        btNY.setOnClickListener {
            btNY.isSelected = !btNY.isSelected
            if (btNY.isSelected) {
                btPA.isSelected = false
                btChamp.isSelected = false
                queryData.location = LOCATION_NY
            } else {
                queryData.location = null
            }
            searchRestaurant(queryData)
        }
        btPA.setOnClickListener {
            btPA.isSelected = !btPA.isSelected
            if (btPA.isSelected) {
                btNY.isSelected = false
                btChamp.isSelected = false
                queryData.location = LOCATION_PA
            } else {
                queryData.location = null
            }
            searchRestaurant(queryData)
        }
        btChamp.setOnClickListener {
            btChamp.isSelected = !btChamp.isSelected
            if (btChamp.isSelected) {
                btNY.isSelected = false
                btPA.isSelected = false
                queryData.location = LOCATION_CHAM
            } else {
                queryData.location = null
            }
            searchRestaurant(queryData)
        }
    }

    /** Search the restaurant on Yelp using Retrofit service and the query
     * parameters in YelpQueryData.
     */
    fun searchRestaurant(queryData: YelpQueryData) {
        // Verify the network connection.
        if (!verifyAvailableNetwork()) {
            Toast.makeText(this, "Network is unavailable.", Toast.LENGTH_SHORT).show()
            return
        }
        yelpService.searchRestaurants(
            "Bearer ${BuildConfig.API_KEY}",
            queryData.term,
            queryData.getLoc(),
            queryData.getLat(),
            queryData.getLon(),
            queryData.sortBy,
            queryData.getPrice(),
            queryData.openNow,
            queryData.attributes
        ).enqueue(
            object : Callback<YelpSearchResult> {
                override fun onResponse(
                    call: Call<YelpSearchResult>,
                    response: Response<YelpSearchResult>
                ) {
                    Log.i(TAG, "onResponse $response")
                    swipeContainer.isRefreshing = false
                    val body = response.body()
                    if (body == null) {
                        Log.w(TAG, "Did you receive valid response body from Yelp API... exiting")
                        return
                    }
                    // Clear the existing results
                    restaurants.clear()
                    restaurants.addAll(body.restaurants)
                    adapter.notifyDataSetChanged()
                    scrollListener.resetState()
                }

                override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                    Log.i(TAG, "onFail $t")
                }
            }
        )
    }

    /** Load more pages from Yelp API. Do not clear the existing items in the list.
     */
    fun loadMoreData(queryData: YelpQueryData, offset: Int) {
        // Verify the network connection.
        if (!verifyAvailableNetwork()) {
            Toast.makeText(this, "Network is unavailable.", Toast.LENGTH_SHORT).show()
            return
        }
        yelpService.searchRestaurants(
            "Bearer ${BuildConfig.API_KEY}",
            queryData.term,
            queryData.getLoc(),
            queryData.getLat(),
            queryData.getLon(),
            queryData.sortBy,
            queryData.getPrice(),
            queryData.openNow,
            queryData.attributes,
            offset
        ).enqueue(
            object : Callback<YelpSearchResult> {
                override fun onResponse(
                    call: Call<YelpSearchResult>,
                    response: Response<YelpSearchResult>
                ) {
                    Log.i(TAG, "onResponse $response")
                    val body = response.body()
                    if (body == null) {
                        Log.w(TAG, "Did you receive valid response body from Yelp API... exiting")
                        return
                    }
                    restaurants.addAll(body.restaurants)
                    adapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                    Log.i(TAG, "onFail $t")
                }
            }
        )
    }

    /**
     * Show the AlertDialog for the sort by button.
     */
    private fun showSortByAlertDialog() {
        // Late initialize an alert dialog object
        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sort By")
        val items = arrayOf("Recommended (default)", "Distance", "Rating", "Most reviewed")
        builder.setSingleChoiceItems(items, sortByCheckedItem) { _, which ->
            when (which) {
                0 -> queryData.sortBy = "best_match"
                1 -> queryData.sortBy = "distance"
                2 -> queryData.sortBy = "rating"
                3 -> queryData.sortBy = "review_count"
            }
            sortByCheckedItem = which
            searchRestaurant(queryData)
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }
}
