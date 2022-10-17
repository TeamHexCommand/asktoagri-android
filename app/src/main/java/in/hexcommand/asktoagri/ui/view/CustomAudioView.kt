package `in`.hexcommand.asktoagri.ui.view

import `in`.hexcommand.asktoagri.R
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

class CustomAudioView : LinearLayout {

    var autoPlay: Boolean = false
    lateinit var audioSrc: String
    private lateinit var mediaPlayer: MediaPlayer

    lateinit var audioActionBtn: MaterialCardView
    lateinit var audioActionIcon: ImageView
    lateinit var audioProgress: LinearProgressIndicator
    lateinit var tempUri: Uri

    constructor(context: Context) : super(context) {}

    constructor(
        context: Context,
        audioSrc: String,
        autoPlay: Boolean = false
    ) : super(context) {
        this.audioSrc = audioSrc
        this.autoPlay = autoPlay
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val customAttributesStyle =
            context.obtainStyledAttributes(attributeSet, R.styleable.AudioView, 0, 0)

        this.audioSrc = customAttributesStyle.getString(R.styleable.AudioView_audio_src).toString()
        this.autoPlay = customAttributesStyle.getBoolean(R.styleable.AudioView_autoplay, false)

        customAttributesStyle.recycle()

        init()
    }

    private fun init() {
        inflate(context, R.layout.holder_audio, this)

        mediaPlayer = MediaPlayer()

        this.audioActionBtn = findViewById(R.id.audio_action)
        this.audioActionIcon = findViewById(R.id.audio_action_icon)
        this.audioProgress = findViewById(R.id.audio_progress)

        this.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        try {
            mediaPlayer.setDataSource(this.audioSrc)
        } catch (e: IOException) {
//            mediaPlayer.setDataSource(context, Uri.parse(this.audioSrc))
        }

        setView()
    }

    private fun setView() {

        if (this.autoPlay) {
            playAudio()
        }

        if (mediaPlayer.isPlaying) {
            setStopIcon()
        } else {
            setPlayIcon()
        }

        audioActionBtn.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                pauseAudio()
            } else {
                setStopIcon()
                playAudio()
            }
        }

        mediaPlayer.setOnCompletionListener {
            audioProgress.progress = 0
            audioActionIcon.setImageDrawable(context.getDrawable(R.drawable.round_play_arrow_24))
            mediaPlayer.reset()
            mediaPlayer.release()
            mediaPlayer = MediaPlayer()

            if (this.audioSrc.startsWith("http")) {
                mediaPlayer.setDataSource(this.audioSrc)
            } else {
                setAudioSrc(tempUri)
            }
        }
    }

    private fun setPlayIcon() {
        audioActionIcon.setImageDrawable(context.getDrawable(R.drawable.round_play_arrow_24))
    }

    private fun setStopIcon() {
        audioActionIcon.setImageDrawable(context.getDrawable(R.drawable.round_stop_24))
    }

    fun pauseAudio() {
        if (mediaPlayer.isPlaying) {
            audioActionIcon.setImageDrawable(context.getDrawable(R.drawable.round_play_arrow_24))
            mediaPlayer.pause()
            mediaPlayer.reset()
            mediaPlayer.release()
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(this.audioSrc)
        }
    }

    fun setAudioSrc(uri: Uri) {
        mediaPlayer = MediaPlayer()
        this.tempUri = uri
        mediaPlayer.setDataSource(context, uri)
    }

    @JvmName("setAudioSrc1")
    fun setAudioSrc(url: String) {
        if (url.isNotEmpty()) {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(Uri.parse(url).toString())
            this.audioSrc = url
        }
    }

    @DelicateCoroutinesApi
    fun playAudio() {
        if (this.audioSrc.isNotEmpty()) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                mediaPlayer.prepare()
                audioProgress.max = mediaPlayer.duration
                mediaPlayer.start()

                val job = GlobalScope.launch {
                    println("${Thread.currentThread()} has run.")
                    var currentPosition = mediaPlayer.currentPosition
                    val total = mediaPlayer.duration
                    audioProgress.max = total

                    while (mediaPlayer.isPlaying && currentPosition < total) {
                        currentPosition = try {
                            Thread.sleep(500)
                            mediaPlayer.currentPosition
                        } catch (e: InterruptedException) {
                            return@launch
                        } catch (e: Exception) {
                            return@launch
                        }

                        (context as Activity).runOnUiThread {
                            audioProgress.progress = currentPosition
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}