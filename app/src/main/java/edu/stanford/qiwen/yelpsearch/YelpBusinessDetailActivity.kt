package edu.stanford.qiwen.yelpsearch

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.request.target.Target
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_business_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "YelpBusinessDetailActivity"
class YelpBusinessDetailActivity : AppCompatActivity() {
    private val retrofit =
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
    private val yelpService = retrofit.create(YelpService::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        supportActionBar?.hide()
        setContentView(R.layout.activity_business_detail)
        queryDetails()
    }

    /** Initiate query to get business detail by the business ID. */
    private fun queryDetails() {
        if (intent.getStringExtra(EXTRA_ID) == null) {
            Toast.makeText(this, "Can't get ID of the business", Toast.LENGTH_SHORT).show()
            return
        }
        yelpService.queryBusinessDetails(
            "Bearer ${BuildConfig.API_KEY}",
            intent.getStringExtra(EXTRA_ID)!!
        ).enqueue(
            object : Callback<YelpBusinessDetail> {
                override fun onResponse(
                    call: Call<YelpBusinessDetail>,
                    response: Response<YelpBusinessDetail>
                ) {
                    Log.i(TAG, "onResponse $response")
                    val body = response.body()
                    if (body == null) {
                        Log.w(TAG, "Did you receive valid response body from Yelp API... exiting")
                        return
                    }
                    renderBusinessDetails(body)
                }

                override fun onFailure(call: Call<YelpBusinessDetail>, t: Throwable) {
                    Log.i(TAG, "onFail $t")
                }
            }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun renderBusinessDetails(businessDetails: YelpBusinessDetail) {
        // Load main business info
        val distanceStr = intent.getStringExtra(EXTRA_DISPLAY_DISTANCE) ?: ""
        tvName.text = businessDetails.name
        ratingBar.setImageResource(businessDetails.getStarRatingRes())
        tvNumReviews.text = "${businessDetails.reviewCount} Reviews"
        tvCategory.text = businessDetails.categories.joinToString(" ⋅ ") { it.title }
        tvPriceDistance.text = "${businessDetails.price} ⋅ $distanceStr"

        // Load additional business info
        tvAddress.text = businessDetails.location.displayAddress.joinToString(", ")
        if (!businessDetails.hours.isNullOrEmpty()) {
            val isOpenNowStr = if (businessDetails.hours[0].isOpenNow) "Open now" else "Closed now"
            var openStart: String? = null
            var openEnd: String? = null
            if (!businessDetails.hours[0].open.isNullOrEmpty()) {
                openStart = "${businessDetails.hours[0].open[0].start.subSequence(
                    0, 2
                )}:${businessDetails.hours[0].open[0].start.subSequence(2, 4)}"
                openEnd = "${businessDetails.hours[0].open[0].end.subSequence(
                    0, 2
                )}:${businessDetails.hours[0].open[0].end.subSequence(2, 4)}"
            }
            val hoursText = if (openStart != null) "Hours: $openStart - $openEnd" else ""
            tvOpenTime.text = "$isOpenNowStr.\t$hoursText"
        } else {
            tvOpenTime.text = "Open time unavailable."
        }

        tvPhone.text = businessDetails.phone
        // Load banner images
        Glide.with(this).load(businessDetails.imageUrl).apply(
            RequestOptions().transform(
                CenterCrop()
            )
        ).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                progress_bar_holder.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                Log.d(TAG, "OnResourceReady")
                progress_bar_holder.visibility = View.GONE
                return false
            }
        }).into(ivBanner)
        // Load highlight images
        val highlightImageViews = arrayOf(ivHighlight1, ivHighlight2, ivHighlight3)
        for (idx in businessDetails.photos.indices) {
            loadHighlightImageInto(businessDetails.photos[idx], highlightImageViews[idx])
        }

        // Open the browser
        btWebsite.setOnClickListener {
            if (businessDetails.url.isEmpty()) return@setOnClickListener
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(businessDetails.url)
            startActivity(intent)
        }
    }

    /** Load the highlight image using Glide. */
    private fun loadHighlightImageInto(url: String, iv: ImageView) {
        Glide.with(this).load(url).apply(
            RequestOptions().transform(
                CenterCrop(), RoundedCorners(20)
            )
        ).into(iv)
    }
}