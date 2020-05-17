package edu.stanford.qiwen.yelpsearch

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_restaurant.view.*

private const val TAG = "RestaurantAdapter"

class RestaurantAdapter(
    val context: Context, private val restaurants: List<YelpRestaurant>,
    private val onClickListener: OnClickListener
) :
    RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onItemClick(restaurant: YelpRestaurant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_restaurant,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = restaurants.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.itemView.setOnClickListener {
            Log.i(TAG, "Tapped on position $position")
            onClickListener.onItemClick(restaurant)
        }
        holder.bind(restaurant)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(restaurant: YelpRestaurant) {
            itemView.tvName.text = restaurant.name
            itemView.ratingBar.setImageResource(restaurant.getStarRatingRes())
            itemView.tvNumReviews.text = "${restaurant.numReviews} Reviews"
            itemView.tvAddress.text = restaurant.location.address
            itemView.tvCategory.text = restaurant.categories[0].title
            itemView.tvDistance.text = restaurant.displayDistance()
            itemView.tvPrice.text = restaurant.price
            Glide.with(context).load(restaurant.imageUrl).apply(
                RequestOptions().transform(
                    CenterCrop(), RoundedCorners(20)
                )
            ).into(itemView.imageView)
        }


    }
}
