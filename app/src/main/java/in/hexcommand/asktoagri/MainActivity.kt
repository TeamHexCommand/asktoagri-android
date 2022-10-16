package `in`.hexcommand.asktoagri

import `in`.hexcommand.asktoagri.adapter.TrendingAdapter
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.helper.WebViewHelper
import `in`.hexcommand.asktoagri.ui.user.Query.AddQueryActivity
import `in`.hexcommand.asktoagri.ui.user.Query.UserQueryActivity
import `in`.hexcommand.asktoagri.ui.user.Trending.TrendingActivity
import `in`.hexcommand.asktoagri.ui.user.Trending.TrendingModel
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray


class MainActivity : AppCompatActivity() {

    private lateinit var ls: LocalStorage
    private lateinit var bottomBar: BottomNavigationView
    private lateinit var mTrendingItems: RecyclerView

    private var mTrendingList = ArrayList<TrendingModel>()
    private var mTrendingAdapter = TrendingAdapter(mTrendingList, this)
    private lateinit var mTrendingModel: TrendingModel

    private lateinit var mChat: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ls = LocalStorage(this)
        mChat = findViewById(R.id.openChat)

        mTrendingItems = findViewById(R.id.trending_article_main)

        bottomBar = findViewById(R.id.mainBottomNav)

        renderTrendingItems()

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
//                    val currentuser = FirebaseAuth.getInstance().currentUser!!
//                        .uid
//                    var u = "https://asktoagri.planckstudio.in/app/addquery.php?userId=${currentFocus}}"
//                    startActivity(
//                        Intent(this, WebViewHelper::class.java).putExtra(
//                            "title",
//                            "Add query"
//                        ).putExtra("weburl", u)
//                    )
                    //                    startActivity(
                    startActivity(Intent(this, AddQueryActivity::class.java))
                    true
                }
                R.id.pageArticle -> {
                    startActivity(Intent(this, UserQueryActivity::class.java))
//                    val currentuser = FirebaseAuth.getInstance().currentUser!!
//                        .uid
//                    var u = "https://asktoagri.planckstudio.in/app/querybyuserid.php?userId=${currentuser}"
//                    startActivity(
//                        Intent(this, WebViewHelper::class.java).putExtra(
//                            "title",
//                            "Your query"
//                        ).putExtra("weburl", u)
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