package `in`.hexcommand.asktoagri.ui.user.Trending

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.data.CategoryData
import `in`.hexcommand.asktoagri.database.ArticalDatabase
import `in`.hexcommand.asktoagri.helper.ApiHelper
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.helper.WebViewHelper
import `in`.hexcommand.asktoagri.model.Upload
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@OptIn(DelicateCoroutinesApi::class)
class TrendingActivity : AppCompatActivity() {

    private lateinit var mTrendingItems: RecyclerView

    private var mTrendingList = ArrayList<TrendingModel>()
    private var mTrendingAdapter = TrendingAdapter(mTrendingList, this)
    private lateinit var mTrendingModel: TrendingModel

    private val articalDb by lazy { ArticalDatabase.getDatabase(this).articalDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trending)

        mTrendingItems = findViewById(R.id.trending_article)

        renderSelectionItems()

    }

    fun onItemClick(materialCardView: MaterialCardView, item: TrendingModel, adapterPosition: Int) {
        startActivity(
            Intent(this, WebViewHelper::class.java)
                .putExtra("title", "Artical")
                .putExtra("weburl", item.getUrl())
        )
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

        try {
            liveData()
        } catch (e: JSONException) {
            demoData()
        }

    }

    private fun liveData() {
        GlobalScope.launch(Dispatchers.IO) {
            val res =
                JSONObject(async { ApiHelper(this@TrendingActivity).getTrendingArtical() }.await()).getJSONObject(
                    "result"
                ).getJSONArray("data")

            articalDb.getAllArtical().collect() { articalList ->
                if (articalList.isNotEmpty()) {
                    (0 until articalList.size).forEach { i ->

                        val articalData = articalList.get(i)

//                        val articalData = Gson().fromJson(
//                            res.getJSONObject(i).toString(), ArticalData::class.java
//                        )

                        val img = JSONObject(async {
                            ApiHelper(this@TrendingActivity).getUploadById(
                                Upload(id = articalData.image)
                            )
                        }.await()).getJSONObject("result").getJSONArray("data").getJSONObject(0)

                        val category = JSONObject(async {
                            ApiHelper(this@TrendingActivity).getCategoryById(
                                CategoryData(id = articalData.category)
                            )
                        }.await()).getJSONObject("result").getJSONArray("data").getJSONObject(0)
                            .getString("name")


                        val image =
                            "${AppHelper(this@TrendingActivity).getConfigUrl("uploads")}${
                                img.getString("name")
                            }.${img.getString("type")}"

                        val trendingModel = TrendingModel(
                            articalData.id,
                            articalData.name,
                            category,
                            articalData.tags,
                            image,
                            articalData.body
                        )
                        mTrendingList.add(trendingModel)
                    }

                    runOnUiThread {
                        mTrendingAdapter.notifyDataSetChanged()
                    }
                }
            }


            try {


            } catch (e: JSONException) {
                Log.e("OnBoard", "Error")
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun demoData() {

        val text =
            resources.openRawResource(R.raw.trending_item).bufferedReader().use { it.readText() }
        val selectionItems = JSONArray(text)

        (0 until selectionItems.length()).forEach { i ->
            val item = selectionItems.getJSONObject(i)

            val selectionModel = TrendingModel(
                item.getInt("id"),
                item.getString("title"),
                item.getString("caption"),
                item.getString("tags"),
                item.getString("image"),
                ""
            )
            mTrendingList.add(selectionModel)
        }

        mTrendingAdapter.notifyDataSetChanged()
    }
}