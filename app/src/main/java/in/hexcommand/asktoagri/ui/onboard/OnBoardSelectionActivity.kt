package `in`.hexcommand.asktoagri.ui.onboard

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.ui.login.LoginActivity
import `in`.hexcommand.asktoagri.ui.login.LoginSelectionActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONArray


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
            startActivity(Intent(this, LoginSelectionActivity::class.java))
        }

        mSecondaryBtn.setOnClickListener {
            startActivity(Intent(this, OnBoardLanguageActivity::class.java))
        }

        renderSelectionItems()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderSelectionItems() {

        val mLayoutManager =
            GridLayoutManager(
                applicationContext,
                3,
                GridLayoutManager.VERTICAL,
                false
            )

        mLayoutManager.isUsingSpansToEstimateScrollbarDimensions = false
        this.mSelectionItems.layoutManager = mLayoutManager

        this.mSelectionItems.adapter = this.mSelectionAdapter
        mSelectionList.clear()
        mSelectionAdapter.notifyDataSetChanged()

        demoData()

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

    private fun demoData() {

        val text = resources.openRawResource(R.raw.selection_items).bufferedReader().use { it.readText() }
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
        card: MaterialCardView,
        item: SelectionModel,
        adapterPosition: Int
    ) {

        card.isChecked = !card.isChecked
    }
}