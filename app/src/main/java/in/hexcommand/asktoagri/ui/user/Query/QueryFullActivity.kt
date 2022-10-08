package `in`.hexcommand.asktoagri.ui.user.Query

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class QueryFullActivity : AppCompatActivity() {

    private lateinit var title: TextView
    private lateinit var content: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var solBtn: MaterialButton

    private lateinit var ls: LocalStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query_full)

        ls = LocalStorage(this)

        title = findViewById(R.id.queryTitle)
        content = findViewById(R.id.queryContent)
        chipGroup = findViewById(R.id.chipGroupQueryFull)
        solBtn = findViewById(R.id.addSolBtn)

        title.text = intent.getStringExtra("title")
        content.text = intent.getStringExtra("content")
        chipGroup.addChip(this)

        solBtn.isEnabled = ls.getValueString("user") == "expert"

        solBtn.setOnClickListener {
            startActivity(
                Intent(this, QueryFullActivity::class.java)
                    .putExtra("title", intent.getStringExtra("title"))
                    .putExtra("content", intent.getStringExtra("content"))
                    .putExtra("id", intent.getStringExtra("id"))
                    .putExtra("userId", intent.getStringExtra("userId"))
                    .putExtra("category", intent.getStringExtra("category"))
                    .putExtra("region", intent.getStringExtra("region"))
            )
        }

    }

    private fun ChipGroup.addChip(context: Context) {

        var label = "English"

        Chip(context).apply {
            id = View.generateViewId()
            text = intent.getStringExtra("crops")
            isClickable = true
            isCheckable = true
            isCheckedIconVisible = false
            isFocusable = true
            addView(this)
        }.setOnClickListener {

        }
    }

}