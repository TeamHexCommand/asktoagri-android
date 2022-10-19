package `in`.hexcommand.asktoagri.ui.expert

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.model.Query
import `in`.hexcommand.asktoagri.ui.common.QueryListActivity
import `in`.hexcommand.asktoagri.ui.user.Trending.TrendingActivity
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONException
import org.json.JSONObject

class ExpertActivity : AppCompatActivity() {

    private lateinit var mTrendingItems: RecyclerView

    private var mTrendingList = ArrayList<Query>()
    private var mTrendingAdapter = ExpertQueryAdapter(mTrendingList, this)
    private lateinit var mTrendingModel: Query

    private lateinit var title: TextView

    private lateinit var mRequestQueue: RequestQueue
    private lateinit var mQuery: String
    private lateinit var ls: LocalStorage

    private lateinit var bottomBar: BottomNavigationView

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert)

        this.mRequestQueue = Volley.newRequestQueue(this)
        title = findViewById(R.id.expert_region)
        bottomBar = findViewById(R.id.expertBottomNav)
        mTrendingItems = findViewById(R.id.expertQueryList)
        ls = LocalStorage(this@ExpertActivity)
//        renderSelectionItems()

        title.text = "${ls.getValueString("region")} region"

        bottomBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.pageExpertTrending -> {
                    startActivity(Intent(this, TrendingActivity::class.java))
                    true
                }
                R.id.pageExpertAdd -> {
                    startActivity(Intent(this, AddSolutionActivity::class.java))
                    true
                }
                R.id.pageExpertQuery -> {
                    startActivity(
                        Intent(this, QueryListActivity::class.java).putExtra(
                            "filter",
                            "notResolved"
                        )
                    )
                    true
                }
                else -> {
                    false
                }
            }
        }

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

        val currentuser = FirebaseAuth.getInstance().currentUser!!.uid

        val stringRequest = @SuppressLint("Range")
        object : StringRequest(
            Method.POST,
            "https://asktoagri.planckstudio.in/api/v1/",
            Response.Listener {
                try {
                    val jsonObject = JSONObject(it)
                    val rStatus = jsonObject.getJSONObject("result").getInt("code")
                    if (rStatus == 200) {
                        val data = jsonObject.getJSONObject("result").getJSONArray("result")

                        (0 until data.length()).forEach { i ->
                            val item = data.getJSONObject(i)

                            val selectionModel = Query(
                                item.getInt("id"),
                                item.getInt("userId"),
                                item.getString("title"),
                                item.getString("content"),
                                item.getString("category"),
                                item.getString("crops"),
                                item.getString("region")
                            )
                            mTrendingList.add(selectionModel)
                        }

                        mTrendingAdapter.notifyDataSetChanged()

                    }
                } catch (e: JSONException) {
                    //
                }
            },
            Response.ErrorListener {
                Log.e("CRAFTY", it.toString())
            }) {

            override fun getBody(): ByteArray {
                val jsonBody = JSONObject()
                val jsonQuery = JSONObject()
                jsonQuery.put("task", "queryByRegion")
                jsonQuery.put(
                    "data",
                    JSONObject()
                        .put("region", ls.getValueString("region"))
                )
                jsonBody.put("type", "get")
                jsonBody.put("param", jsonQuery)
                mQuery = jsonBody.toString()
                return mQuery.toByteArray()
            }
        }
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest)

    }

    fun onItemClick(item: Query, adapterPosition: Int) {
        startActivity(
            Intent(this, AddSolutionActivity::class.java)
                .putExtra("title", item.getTitle())
                .putExtra("content", item.getContent())
                .putExtra("id", item.getId())
                .putExtra("userId", item.getUserId())
                .putExtra("getCategory", item.getCategory())
                .putExtra("getRegion", item.getRegion())
        )
    }
}