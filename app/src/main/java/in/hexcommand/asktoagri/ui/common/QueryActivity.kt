package `in`.hexcommand.asktoagri.ui.common

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.data.CategoryData
import `in`.hexcommand.asktoagri.data.DistrictData
import `in`.hexcommand.asktoagri.data.QueryData
import `in`.hexcommand.asktoagri.data.SolutionData
import `in`.hexcommand.asktoagri.helper.ApiHelper
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.model.Crops
import `in`.hexcommand.asktoagri.model.Upload
import `in`.hexcommand.asktoagri.ui.expert.AddSolutionActivity
import `in`.hexcommand.asktoagri.ui.view.CustomAudioView
import `in`.hexcommand.asktoagri.ui.view.CustomImageView
import `in`.hexcommand.asktoagri.ui.view.CustomQueryCardView
import `in`.hexcommand.asktoagri.ui.view.CustomVideoView
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject

class QueryActivity : AppCompatActivity() {

    private lateinit var api: ApiHelper
    private lateinit var ls: LocalStorage
    private lateinit var app: AppHelper

    private var type: String = "text"

    private lateinit var mFileHolder: MaterialCardView
    private lateinit var mSolutionCard: MaterialCardView
    private lateinit var mSolutionHolder: MaterialCardView
    private lateinit var mQueryTitleText: MaterialTextView
    private lateinit var mQueryCaptionText: MaterialTextView
    private lateinit var mQueryBodyText: MaterialTextView
    private lateinit var mQueryChipGroup: ChipGroup
    private lateinit var mQueryLocationChip: Chip
    private lateinit var mQueryCropsChip: Chip
    private lateinit var mQueryCategoryChip: Chip
    private lateinit var mCustomImageView: CustomImageView
    private lateinit var mCustomVideoView: CustomVideoView
    private lateinit var mCustomAudioView: CustomAudioView
    private lateinit var mSolutionCardView: CustomQueryCardView
    private lateinit var mAddSolutionBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query)

        init()
        main()

    }

    private fun init() {
        api = ApiHelper(this)
        ls = LocalStorage(this)
        app = AppHelper(this)

        mFileHolder = findViewById(R.id.queryFileHolder)
        mSolutionCard = findViewById(R.id.querySolutionCard)
        mSolutionHolder = findViewById(R.id.querySolutionHolder)
        mQueryTitleText = findViewById(R.id.queryTitleText)
        mQueryCaptionText = findViewById(R.id.queryCaptionText)
        mQueryBodyText = findViewById(R.id.queryBodyText)
        mQueryChipGroup = findViewById(R.id.queryChipGroup)
        mQueryLocationChip = findViewById(R.id.queryLocationChip)
        mQueryCropsChip = findViewById(R.id.queryCropsChip)
        mQueryCategoryChip = findViewById(R.id.queryCategoryChip)
        mAddSolutionBtn = findViewById(R.id.add_solution_btn)
    }

    private fun main() {
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val extra = intent.extras
        if (extra != null) {
            if (intent.hasExtra("id")) {
                renderQuery(intent.getStringExtra("id").toString().toInt())
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    private fun renderQuery(id: Int) {

        val queryData = QueryData(
            id = id
        )

        GlobalScope.launch {
            val queryResult: JSONObject = withContext(Dispatchers.Default) {
                return@withContext JSONObject(async {
                    ApiHelper(this@QueryActivity).getQueryById(queryData)
                }.await())
            }

            if (queryResult.getInt("code") == 200) {

                val queryArray = Gson().fromJson(
                    queryResult.getJSONObject("result").getJSONArray("data").toString(),
                    Array<QueryData>::class.java
                )

                val district: DistrictData? = withContext(Dispatchers.Default) {
                    return@withContext async {
                        app.getDistrictById(DistrictData(id = queryArray[0].district))
                    }.await()
                }

                val category: CategoryData? = withContext(Dispatchers.Default) {
                    return@withContext async {
                        app.getCategoryById(CategoryData(id = queryArray[0].category))
                    }.await()
                }

                val crops: Crops? = withContext(Dispatchers.Default) {
                    return@withContext async {
                        app.getCropsById(Crops(id = queryArray[0].crops))
                    }.await()
                }

                val upload: Upload? = withContext(Dispatchers.Default) {
                    return@withContext async {
                        app.getUploadById(Upload(id = queryArray[0].file))
                    }.await()
                }

                if (category != null && crops != null && upload != null && district != null) {
                    runOnUiThread {

                        mFileHolder.removeAllViews()
                        type = queryArray[0].type

                        when (queryArray[0].type) {
                            "image" -> {
                                mCustomImageView = CustomImageView(
                                    this@QueryActivity,
                                    app.getFileUrl(upload)
                                )

                                mFileHolder.addView(mCustomImageView)
                            }
                            "audio" -> {
                                mCustomAudioView = CustomAudioView(
                                    this@QueryActivity,
                                    app.getFileUrl(upload),
                                    true
                                )

                                mFileHolder.addView(mCustomAudioView)
                            }
                            "video" -> {
                                mCustomVideoView = CustomVideoView(
                                    this@QueryActivity,
                                    app.getFileUrl(upload),
                                    true,
                                    false
                                )

                                mFileHolder.addView(mCustomVideoView)
                            }
                        }

                        mQueryTitleText.text = queryArray[0].title

                        var resolve = "Not resolved"

                        if (queryArray[0].resolved == 1) {
                            resolve = "Resolved"
                        }

                        mQueryCaptionText.text =
                            "${category.name} • ${queryArray[0].type} query • ${resolve}"

                        mQueryBodyText.text = queryArray[0].body

                        mQueryCropsChip.text = crops.name.capitalize()
                        mQueryLocationChip.text = district.name.capitalize()
                        mQueryCategoryChip.text = category.name.capitalize()

                    }

                    if (queryArray[0].resolved == 1) {
                        withContext(Dispatchers.Default) {
                            renderSolution(queryArray[0].solution, district, category, crops)
                        }
                        runOnUiThread {
                            mSolutionCard.visibility = View.VISIBLE
                            mAddSolutionBtn.visibility = View.GONE
                        }
                    } else {
                        runOnUiThread {
                            mSolutionCard.visibility = View.GONE

                            if (ls.getValueString("user") == "expert") {
                                mAddSolutionBtn.visibility = View.VISIBLE
                                mAddSolutionBtn.setOnClickListener {
                                    startActivity(
                                        Intent(
                                            this@QueryActivity,
                                            AddSolutionActivity::class.java
                                        ).putExtra("id", queryArray[0].id)
                                            .putExtra("data", Gson().toJson(queryArray[0]).toString())
                                    )
                                }
                            } else {
                                mAddSolutionBtn.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun renderSolution(
        id: Int,
        district: DistrictData,
        category: CategoryData,
        crops: Crops
    ) {

        val solutionData = SolutionData(
            id = id
        )

        val solutionResult: JSONObject = withContext(Dispatchers.Default) {
            return@withContext JSONObject(async {
                ApiHelper(this@QueryActivity).getSolutionById(solutionData)
            }.await())
        }

        if (solutionResult.getInt("code") == 200) {

            val solutionArray = Gson().fromJson(
                solutionResult.getJSONObject("result").getJSONArray("data").toString(),
                Array<SolutionData>::class.java
            )

            val upload: Upload? = withContext(Dispatchers.Default) {
                return@withContext async {
                    app.getUploadById(Upload(id = solutionArray[0].file))
                }.await()
            }

            if (upload != null) {
                runOnUiThread {
                    mSolutionHolder.removeAllViews()

                    mSolutionCardView = CustomQueryCardView(
                        this@QueryActivity,
                        solutionArray[0].type,
                        category.name,
                        solutionArray[0].title,
                        app.getFileUrl(upload),
                        false,
                        "solution"
                    )

                    mSolutionCardView.cardType = "solution"

                    mSolutionCard.visibility = View.VISIBLE
                    mSolutionHolder.addView(mSolutionCardView)

                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        when (type) {
            "audio" -> {
                mCustomAudioView.pauseAudio()
            }
            "video" -> {
                mCustomVideoView.videoView.pause()
            }
        }

    }

}