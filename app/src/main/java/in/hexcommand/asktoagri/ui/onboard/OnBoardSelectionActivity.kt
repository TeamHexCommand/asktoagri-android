package `in`.hexcommand.asktoagri.ui.onboard

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.helper.ApiHelper
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.model.Crops
import `in`.hexcommand.asktoagri.model.Upload
import `in`.hexcommand.asktoagri.ui.login.LoginSelectionActivity
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


@DelicateCoroutinesApi
class OnBoardSelectionActivity : AppCompatActivity() {

    private lateinit var mPrimaryBtn: MaterialButton
    private lateinit var mSecondaryBtn: MaterialButton

    private lateinit var mSelectionItems: RecyclerView

    private var mSelectionList = ArrayList<SelectionModel>()
    private var mSelectionAdapter = SelectionAdapter(this, mSelectionList, this)
    private lateinit var mSelectionModel: SelectionModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_board_selection)

        mPrimaryBtn = findViewById(R.id.onboard_selection_primary_btn)
        mSecondaryBtn = findViewById(R.id.onboard_selection_secondary_btn)
        mSelectionItems = findViewById(R.id.selection_items)

        mPrimaryBtn.setOnClickListener {
            updateSelection()
        }

        mSecondaryBtn.setOnClickListener {
            startActivity(Intent(this, OnBoardLanguageActivity::class.java))
        }

        renderSelectionItems()
    }

    private fun updateSelection() {

        var selectedItem = JSONArray()

        (0 until mSelectionList.size).forEach { i ->
            if (mSelectionList[i].getSelected()) {
                selectedItem.put(mSelectionList[i].getTitle())
            }
        }

        LocalStorage(this).save("selected_crops", selectedItem.toString())

        startActivity(Intent(this, LoginSelectionActivity::class.java))
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderSelectionItems() {

        val mLayoutManager = GridLayoutManager(
            applicationContext, 3, GridLayoutManager.VERTICAL, false
        )

        mLayoutManager.isUsingSpansToEstimateScrollbarDimensions = false
        this.mSelectionItems.layoutManager = mLayoutManager

        this.mSelectionItems.adapter = this.mSelectionAdapter
//        mSelectionList.clear()
//        mSelectionAdapter.notifyDataSetChanged()

//        demoData()

        liveData()
//        repeat((0 until 10).count()) {
//            mSelectionList.add(
//                SelectionModel(
//                    0,
//                    "Fruit",
//                    "Orange",
//                    "https://img.freepik.com/premium-photo/fresh-ripe-oranges-dark-background_87742-24567.jpg?w=360",
//                    false
//                )
//            )
//            mSelectionAdapter.notifyDataSetChanged()
//        }
    }

    private fun liveData() {

        GlobalScope.launch(Dispatchers.IO) {
            val res =
                JSONObject(async { ApiHelper(this@OnBoardSelectionActivity).getAllCrops() }.await()).getJSONObject(
                    "result"
                ).getJSONArray("data")

            try {

                (0 until res.length()).forEach { i ->

                    val cropsModel = Gson().fromJson(
                        res.getJSONObject(i).toString(), Crops::class.java
                    )

                    val img = JSONObject(async {
                        ApiHelper(this@OnBoardSelectionActivity).getUploadById(
                            Upload(id = cropsModel.image)
                        )
                    }.await()).getJSONObject("result").getJSONArray("data").getJSONObject(0)

                    val image =
                        "${AppHelper(this@OnBoardSelectionActivity).getConfigUrl("uploads")}${
                            img.getString("name")
                        }.${img.getString("type")}"

                    Log.e("OnBoard", image.toString())

                    val selectionModel = SelectionModel(
                        cropsModel.id, cropsModel.type, cropsModel.name, image, false
                    )
                    mSelectionList.add(selectionModel)

                    runOnUiThread {
                        mSelectionAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: JSONException) {
                Log.e("OnBoard", "Error")
            }
        }

    }

    private fun demoData() {
        val text =
            resources.openRawResource(R.raw.selection_items).bufferedReader().use { it.readText() }
        val selectionItems = JSONArray(text)

        (0 until selectionItems.length()).forEach { i ->
            val item = selectionItems.getJSONObject(i)

            val selectionModel = SelectionModel(
                item.getInt("id"),
                item.getString("category"),
                item.getString("title"),
                item.getString("image"),
                item.getBoolean("selected")
            )
            mSelectionList.add(selectionModel)
        }

        mSelectionAdapter.notifyDataSetChanged()

    }

    fun onItemClick(
        card: MaterialCardView, item: SelectionModel, adapterPosition: Int
    ) {
        mSelectionList[adapterPosition].setSelected(!card.isChecked)
        card.isChecked = !card.isChecked
    }
}