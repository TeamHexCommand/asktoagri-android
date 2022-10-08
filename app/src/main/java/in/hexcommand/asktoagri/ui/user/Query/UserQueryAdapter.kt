package `in`.hexcommand.asktoagri.ui.user.Query

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.model.Query
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class UserQueryAdapter(
    private val dataSet: ArrayList<Query>,
    private val clickListner: UserQueryActivity
) :
    RecyclerView.Adapter<UserQueryAdapter.ViewHolder>() {

    @DelicateCoroutinesApi
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

//                private val card: CardView = view.findViewById(R.id.trending_card)
        val trendingImage: ImageView = view.findViewById(R.id.article_image)
        val trendingTitle: MaterialTextView = view.findViewById(R.id.article_title)
        val trendingCaption: MaterialTextView = view.findViewById(R.id.article_caption)
        val trendingTags: ChipGroup = view.findViewById(R.id.chipGroupTags)

        fun initialize(item: Query, action: UserQueryActivity) {

//            trendingCard {
//                alpha = 0f
//                visibility = View.VISIBLE
//                animate().alpha(1f).setDuration(1000).setListener(null)
//            }

//            trendingCard.setOnClickListener {
//                action.onItemClick(it as MaterialCardView, item, adapterPosition)
//            }
        }

        init {
            // Define click listener for the ViewHolder's View.
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_article, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        var u = "https://asktoagri.planckstudio.in/${dataSet[position].getContent()}.jpg"

        Glide.with(viewHolder.trendingImage.context)
            .load(u)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(viewHolder.trendingImage)

//        Glide.with(viewHolder.trendingImage.context)
//            .load("https://asktoagri.planckstudio.in/assets/img/${dataSet[position].getCrops()}.jpg")
//            .placeholder(R.drawable.ic_launcher_foreground)
//            .into(viewHolder.trendingImage)

        viewHolder.trendingTitle.text = dataSet[position].getTitle()
//        viewHolder.trendingCaption.text = dataSet[position].getContent()

        viewHolder.trendingTags.addChip(dataSet[position].getCrops())

        viewHolder.initialize(dataSet[position], clickListner)
    }

    override fun getItemCount() = dataSet.size

    private fun ChipGroup.addChip(label: String) {
        Chip(context).apply {
            id = View.generateViewId()
            text = label
            isClickable = true
            isCheckable = true
            isCheckedIconVisible = false
            isFocusable = true
            addView(this)
        }.setOnClickListener {
            //
        }
    }
}

interface OnGridImageItemClickListner {
    fun onItemClick(item: Query, position: Int)
}