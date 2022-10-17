package `in`.hexcommand.asktoagri.ui.view

import `in`.hexcommand.asktoagri.R
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.VideoView

class CustomVideoView : LinearLayout {
    var autoPlay: Boolean = false
    var mute: Boolean = false
    lateinit var videoSrc: String
    private lateinit var mediaController: MediaController

    lateinit var videoView: VideoView

    constructor(context: Context) : super(context) {}

    constructor(
        context: Context,
        videoSrc: String,
        autoPlay: Boolean,
        mute: Boolean
    ) : super(context) {
        this.videoSrc = videoSrc
        this.autoPlay = autoPlay
        this.mute = mute
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val customAttributesStyle =
            context.obtainStyledAttributes(attributeSet, R.styleable.CustomVideoView, 0, 0)

        this.videoSrc =
            customAttributesStyle.getString(R.styleable.CustomVideoView_video_src).toString()
        this.autoPlay =
            customAttributesStyle.getBoolean(R.styleable.CustomVideoView_video_autoplay, false)
        this.mute = customAttributesStyle.getBoolean(R.styleable.CustomVideoView_video_mute, false)

        customAttributesStyle.recycle()

        init()
    }

    private fun init() {
        inflate(context, R.layout.view_video_custom, this)

        this.mediaController = MediaController(context)

        this.videoView = findViewById(R.id.addQueryVideoView)

        this.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        videoView.setVideoURI(Uri.parse(this.videoSrc))

        setView()
    }

    private fun setView() {
        mediaController.setAnchorView(videoView)
        mediaController.setMediaPlayer(videoView)
        videoView.seekTo(1)
        mediaController.hide()

        if (this.autoPlay) {
            videoView.start()
        }

        videoView.setOnPreparedListener { mp: MediaPlayer ->
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            mp.isLooping = false
            mp.setScreenOnWhilePlaying(false)
        }

        videoView.setOnClickListener {
            videoView.start()
            videoView.setMediaController(mediaController)
        }

    }

    fun setVideoSrc(uri: Uri) {
        this.videoSrc = uri.toString()
        videoView.setVideoURI(uri)
    }

    @JvmName("setAudioSrc1")
    fun setVideoSrc(url: String) {
        if (url.isNotEmpty()) {
            videoView.setVideoURI(Uri.parse(url))
            this.videoSrc = url
        }
    }
}