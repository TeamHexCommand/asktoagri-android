package `in`.hexcommand.asktoagri.ui.onboard

import `in`.hexcommand.asktoagri.R
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class SelectionAdapter(
    context: Context,
    private val dataSet: ArrayList<SelectionModel>,
    private val clickListner: OnBoardSelectionActivity
) :
    RecyclerView.Adapter<SelectionAdapter.ViewHolder>() {

    @DelicateCoroutinesApi
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val selectionCard: MaterialCardView = view.findViewById(R.id.selection_card)
        val selectionImage: ImageView = view.findViewById(R.id.selection_image)
        val selectionTitle: TextView = view.findViewById(R.id.selection_title)

        fun initialize(item: SelectionModel, action: OnBoardSelectionActivity) {

            selectionCard.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(1000).setListener(null)
            }

            selectionCard.setOnClickListener {
                action.onItemClick(it as MaterialCardView, item, adapterPosition)
            }
        }

        init {
            // Define click listener for the ViewHolder's View.
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_selection, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        Glide.with(viewHolder.selectionImage.context)
            .load(dataSet[position].getImage())
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(viewHolder.selectionImage)


        viewHolder.selectionTitle.visibility = View.VISIBLE
        viewHolder.selectionTitle.text = dataSet[position].getTitle().capitalize()
        viewHolder.initialize(dataSet[position], clickListner)
    }

    override fun getItemCount() = dataSet.size
}

interface OnGridImageItemClickListner {
    fun onItemClick(item: SelectionModel, position: Int)
}