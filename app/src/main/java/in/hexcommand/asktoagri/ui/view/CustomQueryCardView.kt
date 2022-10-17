package `in`.hexcommand.asktoagri.ui.view

import `in`.hexcommand.asktoagri.R
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class CustomQueryCardView : LinearLayout {

    lateinit var type: String
    lateinit var category: String
    lateinit var title: String
    lateinit var file: String
    var autoplay: Boolean = false

    lateinit var mShareBtn: MaterialButton
    lateinit var mOpenBtn: MaterialButton
    lateinit var mCategoryText: TextView
    lateinit var mTitleText: TextView
    lateinit var mFileHolder: MaterialCardView
    lateinit var mCardHolder: MaterialCardView
    lateinit var mCustomAudioView: CustomAudioView
    lateinit var mCustomVideoView: CustomVideoView
    lateinit var mCustomImageView: CustomImageView

    constructor(context: Context) : super(context) {}

    constructor(
        context: Context,
        type: String,
        category: String,
        title: String,
        file: String,
        autoplay: Boolean = false
    ) : super(context) {
        this.type = type
        this.category = category
        this.title = title
        this.file = file
        this.autoplay = autoplay
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val customAttributesStyle =
            context.obtainStyledAttributes(attributeSet, R.styleable.QueryCardView, 0, 0)

        this.category =
            customAttributesStyle.getString(R.styleable.QueryCardView_caption).toString()
        this.title = customAttributesStyle.getString(R.styleable.QueryCardView_title).toString()
        this.file = customAttributesStyle.getString(R.styleable.QueryCardView_file_url).toString()
        this.type = customAttributesStyle.getString(R.styleable.QueryCardView_type).toString()

        customAttributesStyle.recycle()

        init()
    }

    private fun init() {
        inflate(context, R.layout.card_query, this)

        this.mShareBtn = findViewById(R.id.query_share_btn)
        this.mOpenBtn = findViewById(R.id.query_open_btn)
        this.mCategoryText = findViewById(R.id.query_category)
        this.mTitleText = findViewById(R.id.query_title)
        this.mCardHolder = findViewById(R.id.query_card)
        this.mFileHolder = findViewById(R.id.query_file_holder)

        this.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )

        setView()
    }

    private fun setView() {

        when (type) {
            "text" -> {
                //
            }
            "audio" -> {
                this.mFileHolder.visibility = View.VISIBLE

                this.mCustomAudioView = CustomAudioView(
                    context, this.file, this.autoplay
                )

                this.mFileHolder.addView(this.mCustomAudioView)
            }
            "video" -> {
                this.mFileHolder.visibility = View.VISIBLE

                this.mCustomVideoView = CustomVideoView(
                    context, this.file, this.autoplay, false
                )

                this.mFileHolder.addView(this.mCustomVideoView)
            }
            "image" -> {
                this.mFileHolder.visibility = View.VISIBLE

                this.mCustomImageView = CustomImageView(
                    context, this.file
                )

                this.mFileHolder.addView(this.mCustomImageView)
            }
        }

        mCategoryText.text = "${this.category} • ${type} query"
        mTitleText.text = this.title

        mShareBtn.setOnClickListener {
            Toast.makeText(context, "Share button", Toast.LENGTH_SHORT).show()
        }

        mOpenBtn.setOnClickListener {
            Toast.makeText(context, "Open button", Toast.LENGTH_SHORT).show()
        }

        mCardHolder.setOnClickListener {
            Toast.makeText(context, "Open query", Toast.LENGTH_SHORT).show()
        }
    }
}