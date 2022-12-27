package `in`.hexcommand.asktoagri

import `in`.hexcommand.asktoagri.adapter.TrendingAdapter
import `in`.hexcommand.asktoagri.data.CategoryData
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.helper.WebViewHelper
import `in`.hexcommand.asktoagri.ui.common.QueryListActivity
import `in`.hexcommand.asktoagri.ui.user.Query.AddQueryActivity
import `in`.hexcommand.asktoagri.ui.user.Trending.TrendingActivity
import `in`.hexcommand.asktoagri.ui.user.Trending.TrendingModel
import `in`.hexcommand.asktoagri.ui.view.QueryCardView
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.*
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var ls: LocalStorage
    private lateinit var bottomBar: BottomNavigationView
    private lateinit var mTrendingItems: RecyclerView

    private var mTrendingList = ArrayList<TrendingModel>()
    private var mTrendingAdapter = TrendingAdapter(mTrendingList, this)
    private lateinit var mTrendingModel: TrendingModel

    private lateinit var mChat: MaterialCardView

    private lateinit var queryCard: QueryCardView

    private lateinit var customView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ls = LocalStorage(this)
        mChat = findViewById(R.id.openChat)
        mTrendingItems = findViewById(R.id.trending_article_main)
        bottomBar = findViewById(R.id.mainBottomNav)
        customView = findViewById(R.id.mainCustomView)

        val categoryData = CategoryData(id = 1)

        AppHelper(this@MainActivity).getRemoteConfig(getString(R.string.config_url))

        GlobalScope.launch {
            val category: CategoryData? = withContext(Dispatchers.Default) {
                return@withContext async { AppHelper(this@MainActivity).getCategoryById(categoryData) }.await()
            }

            if (category != null) {
                Log.e("MainActivity", category.name)
            }
        }

//        renderTrendingItems()

        AppHelper(this).storeTrendingArtical()

        mChat.setOnClickListener {
            startActivity(
                Intent(this, WebViewHelper::class.java)
                    .putExtra("title", "Chat with expert")
                    .putExtra("weburl", "https://tawk.to/asktoagriexpert")
            )
        }

        bottomBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.pageTrending -> {
                    startActivity(Intent(this, TrendingActivity::class.java))
                    true
                }
                R.id.pageAdd -> {
                    startActivity(Intent(this, AddQueryActivity::class.java))
                    true
                }
                R.id.pageArticle -> {
                    startActivity(
                        Intent(this, QueryListActivity::class.java).putExtra(
                            "filter",
                            "userQuery"
                        ).putExtra("title", resources.getString(R.string.title_query_list_farmer))
                    )
//                    )
                    true
                }
                else -> {
                    false
                }
            }
        }

        if (ls.getValueBoolean("is_guest_login")) {
            Toast.makeText(this, R.string.welcome_note_guest, Toast.LENGTH_SHORT).show()
        }

//        val apiKey = Keys.apiKey()
//        Log.e("KEY", apiKey)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderTrendingItems() {

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