package `in`.hexcommand.asktoagri.ui.user.Trending

import `in`.hexcommand.asktoagri.R
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONArray

@OptIn(DelicateCoroutinesApi::class)
class TrendingActivity : AppCompatActivity() {

    private lateinit var mTrendingItems: RecyclerView

    private var mTrendingList = ArrayList<TrendingModel>()
    private var mTrendingAdapter = TrendingAdapter( mTrendingList, this)
    private lateinit var mTrendingModel: TrendingModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trending)

        mTrendingItems = findViewById(R.id.trending_article)

        renderSelectionItems()

    }

    fun onItemClick(materialCardView: MaterialCardView, item: TrendingModel, adapterPosition: Int) {
    //
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderSelectionItems() {

        val mLayoutManager =
            LinearLayoutManager(
                applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )

        this.mTrendingItems.layoutManager = mLayoutManager

        this.mTrendingItems.adapter = this.mTrendingAdapter
        mTrendingList.clear()
        mTrendingAdapter.notifyDataSetChanged()

        demoData()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun demoData() {

        val text = resources.openRawResource(R.raw.trending_item).bufferedReader().use { it.readText() }
        val selectionItems = JSONArray(text)

        (0 until selectionItems.length()).forEach { i ->
            val item = selectionItems.getJSONObject(i)

            val selectionModel = TrendingModel(
                item.getInt("id"),
                item.getString("title"),
                item.getString("caption"),
                item.getString("tags"),
                item.getString("image")
            )
            mTrendingList.add(selectionModel)
        }

        mTrendingAdapter.notifyDataSetChanged()

    }

}