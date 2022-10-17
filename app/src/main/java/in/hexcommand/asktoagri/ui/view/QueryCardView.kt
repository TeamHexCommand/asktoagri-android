package `in`.hexcommand.asktoagri.ui.view

import `in`.hexcommand.asktoagri.R
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

public class QueryCardView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private lateinit var type: String
    private lateinit var category: String
    private lateinit var title: String
    private lateinit var file: String

    public fun setType(type: String) {
        this.type = type
    }

    public fun getType(): String {
        return this.type
    }

    public fun setCategory(s: String) {
        this.category = s
    }

    public fun getCategory(): String {
        return this.category
    }

    public fun setTitle(s: String) {
        this.title = s
    }

    public fun getTitle(): String {
        return this.title
    }

    public fun setFile(s: String) {
        this.file = s
    }

    public fun getFile(): String {
        return this.file
    }


    init {

        inflate(context, R.layout.card_query, this)

        val shareBtn = findViewById<MaterialButton>(R.id.query_share_btn)
        val openBtn = findViewById<MaterialButton>(R.id.query_open_btn)
        val categoryText = findViewById<TextView>(R.id.query_category)
        val titleText = findViewById<TextView>(R.id.query_title)
        val videoHolder = findViewById<CardView>(R.id.video_holder)
        val audioHolder = findViewById<CardView>(R.id.audio_holder)
        val imageHolder = findViewById<CardView>(R.id.image_holder)
        val queryHolder = findViewById<MaterialCardView>(R.id.query_card)
        val imageView = findViewById<ImageView>(R.id.image_view)
        val videoView = findViewById<VideoView>(R.id.addQueryVideoView)
        val audioView = findViewById<AudioView>(R.id.audioViewQuery)

        val customAttributesStyle =
            context.obtainStyledAttributes(attrs, R.styleable.QueryCardView, 0, 0)

        try {
            categoryText.text = customAttributesStyle.getString(R.styleable.QueryCardView_caption)
            titleText.text = customAttributesStyle.getString(R.styleable.QueryCardView_title)

            val type = customAttributesStyle.getString(R.styleable.QueryCardView_type)
            val fileUrl = customAttributesStyle.getString(R.styleable.QueryCardView_file_url)

            audioView.setAudioSrc(fileUrl.toString())

            when (type) {
                "image" -> {
                    imageHolder.visibility = View.VISIBLE
                }
                "video" -> {
                    videoHolder.visibility = View.VISIBLE
                }
                "audio" -> {
                    audioHolder.visibility = View.VISIBLE
                }
                "text" -> {
                    imageHolder.visibility = View.GONE
                    videoHolder.visibility = View.GONE
                    audioHolder.visibility = View.GONE
                }
            }
        } finally {
            customAttributesStyle.recycle()
        }

        shareBtn.setOnClickListener {
            Toast.makeText(context, "Share button", Toast.LENGTH_SHORT).show()
        }

        openBtn.setOnClickListener {
            Toast.makeText(context, "Open button", Toast.LENGTH_SHORT).show()
        }

        queryHolder.setOnClickListener {
            Toast.makeText(context, "Open query", Toast.LENGTH_SHORT).show()
        }

    }
}