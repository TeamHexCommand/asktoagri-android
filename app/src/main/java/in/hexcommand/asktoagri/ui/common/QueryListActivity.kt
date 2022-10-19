package `in`.hexcommand.asktoagri.ui.common

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.data.CategoryData
import `in`.hexcommand.asktoagri.data.QueryData
import `in`.hexcommand.asktoagri.helper.ApiHelper
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.model.Crops
import `in`.hexcommand.asktoagri.model.Upload
import `in`.hexcommand.asktoagri.ui.view.CustomQueryCardView
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject

class QueryListActivity : AppCompatActivity() {

    private lateinit var mListHolder: LinearLayout
    private lateinit var mListAdapter: ArrayList<QueryData>
    private lateinit var api: ApiHelper
    private lateinit var ls: LocalStorage
    private lateinit var app: AppHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query_list)
        init()
        main()
    }

    private fun init() {
        api = ApiHelper(this)
        ls = LocalStorage(this)
        app = AppHelper(this)

        mListHolder = findViewById(R.id.queryListHolder)
        mListAdapter = ArrayList()
    }

    private fun main() {
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val extra = intent.extras
        if (extra != null) {
            if (intent.hasExtra("filter")) {
                renderList(intent.getStringExtra("filter").toString())
            }
        }
    }

    private fun renderList(filter: String) {

        val queryData = QueryData(
            user = ls.getValueInt("user_id")
        )

        GlobalScope.launch {

            var queryResult = JSONObject()

            when (filter) {
                "userQuery" -> {
                    queryResult = withContext(Dispatchers.Default) {
                        return@withContext JSONObject(async {
                            ApiHelper(this@QueryListActivity).getLatestUserQuery(queryData)
                        }.await())
                    }
                }
                "notResolved" -> {
                    queryResult = withContext(Dispatchers.Default) {
                        return@withContext JSONObject(async {
                            ApiHelper(this@QueryListActivity).getNotResolvedQuery()
                        }.await())
                    }
                }
            }

            if (queryResult.getInt("code") == 200) {
                val queryArray = Gson().fromJson(
                    queryResult.getJSONObject("result").getJSONArray("data").toString(),
                    Array<QueryData>::class.java
                )

                (queryArray.indices).forEach { i ->

                    val category: CategoryData? = withContext(Dispatchers.Default) {
                        return@withContext async {
                            app.getCategoryById(CategoryData(id = queryArray[i].category))
                        }.await()
                    }

                    val crops: Crops? = withContext(Dispatchers.Default) {
                        return@withContext async {
                            app.getCropsById(Crops(id = queryArray[i].crops))
                        }.await()
                    }

                    val upload: Upload? = withContext(Dispatchers.Default) {
                        return@withContext async {
                            app.getUploadById(Upload(id = queryArray[i].file))
                        }.await()
                    }

                    if (category != null && crops != null && upload != null) {

                        Log.e("Query", app.getFileUrl(upload))
                        runOnUiThread {

                            val customQueryCardView = CustomQueryCardView(
                                this@QueryListActivity,
                                queryArray[i].type,
                                category.name,
                                queryArray[i].title,
                                app.getFileUrl(upload),
                                false
                            )

                            customQueryCardView.mCardHolder.setOnClickListener {
                                openQuery(queryArray[i].id)
                            }

                            customQueryCardView.mOpenBtn.setOnClickListener {
                                openQuery(queryArray[i].id)
                            }

                            mListHolder.addView(customQueryCardView)
                        }
                    }
                }
            }
        }
    }

    private fun openQuery(id: Int) {
        startActivity(
            Intent(
                this@QueryListActivity,
                QueryActivity::class.java
            ).putExtra("id", id.toString())
        )
    }
}