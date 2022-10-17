package `in`.hexcommand.asktoagri.ui.view

import `in`.hexcommand.asktoagri.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide

class CustomImageView : LinearLayout {
    lateinit var imageSrc: String
    lateinit var imageView: ImageView

    constructor(context: Context) : super(context) {}

    constructor(
        context: Context,
        imageSrc: String,
    ) : super(context) {
        this.imageSrc = imageSrc
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val customAttributesStyle =
            context.obtainStyledAttributes(attributeSet, R.styleable.CustomImageView, 0, 0)

        this.imageSrc =
            customAttributesStyle.getString(R.styleable.CustomImageView_image_src).toString()

        customAttributesStyle.recycle()

        init()
    }

    private fun init() {
        inflate(context, R.layout.view_image_custom, this)

        this.imageView = findViewById(R.id.image_view)

        this.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        setView()
    }

    private fun setView() {
        Glide.with(context)
            .load(this.imageSrc)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(this.imageView)

    }

    fun setImageSrc(uri: Uri) {
        Glide.with(context)
            .load(uri)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(this.imageView)
        this.imageSrc = uri.toString()
    }

    fun setImageSrc(drawable: Drawable) {
        Glide.with(context)
            .load(drawable)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(this.imageView)
    }

    fun setImageSrc(bitmap: Bitmap) {
        Glide.with(context)
            .load(bitmap)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(this.imageView)
    }

    @JvmName("setAudioSrc1")
    fun setImageSrc(url: String) {
        if (url.isNotEmpty()) {
            Glide.with(context)
                .load(Uri.parse(url).toString())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(this.imageView)
            this.imageSrc = url
        }
    }
}