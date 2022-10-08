package `in`.hexcommand.asktoagri.ui.user.Query

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.model.Query
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import org.json.JSONObject

class UserQueryActivity : AppCompatActivity() {

    private lateinit var mTrendingItems: RecyclerView

    private var mTrendingList = ArrayList<Query>()
    private var mTrendingAdapter = UserQueryAdapter(mTrendingList, this)
    private lateinit var mTrendingModel: Query

    private lateinit var mRequestQueue: RequestQueue
    private lateinit var mQuery: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_query)

        this.mRequestQueue = Volley.newRequestQueue(this)
        mTrendingItems = findViewById(R.id.userQueryList)
        renderSelectionItems()
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
                jsonQuery.put("task", "queryByUserId")
                jsonQuery.put(
                    "data",
                    JSONObject()
                        .put("userId", currentuser)
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
}