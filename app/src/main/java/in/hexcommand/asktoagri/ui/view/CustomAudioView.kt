package `in`.hexcommand.asktoagri.ui.view

import `in`.hexcommand.asktoagri.R
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
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
        context: Context, audioSrc: String, autoPlay: Boolean = false
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



        this.audioActionBtn = findViewById(R.id.audio_action)
        this.audioActionIcon = findViewById(R.id.audio_action_icon)
        this.audioProgress = findViewById(R.id.audio_progress)

        this.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )

        setView()
    }

    private fun setView() {
        this.mediaPlayer = MediaPlayer()

        try {
            if (this.audioSrc.startsWith("http")) {
                this.mediaPlayer.setDataSource(this.audioSrc)
            } else {
                this.mediaPlayer.setDataSource(context, Uri.parse(this.audioSrc))
            }
        } catch (e: IOException) {
            //
        }

        if (this.autoPlay) {
            playAudio()
        }

        if (this.mediaPlayer.isPlaying) {
            setStopIcon()
        } else {
            setPlayIcon()
        }

        audioActionBtn.setOnClickListener {
            if (this.audioSrc.isNotEmpty() && this.mediaPlayer.isPlaying) {
                pauseAudio()
            } else {
                setStopIcon()
                setView()
                playAudio()
            }
        }

        this.mediaPlayer.setOnCompletionListener { mp ->
            try {
                Log.e("AudioView", "completed audio")
                mp.release()
                setView()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
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
        if (this.mediaPlayer.isPlaying) {
            audioProgress.progress = 0
            setPlayIcon()
            this.mediaPlayer.pause()
            this.mediaPlayer.reset()
            this.mediaPlayer.release()
            setView()
        }
    }

    fun setAudioSrc(uri: Uri) {
        this.mediaPlayer = MediaPlayer()
        this.tempUri = uri
        this.mediaPlayer.setDataSource(context, uri)
    }

    @JvmName("setAudioSrc1")
    fun setAudioSrc(url: String) {
        if (url.isNotEmpty()) {
            this.mediaPlayer = MediaPlayer()
            this.mediaPlayer.setDataSource(Uri.parse(url).toString())
            this.audioSrc = url
        }
        if (!url.startsWith("http")) {
            setAudioSrc(Uri.parse(url))
        }
    }

    @DelicateCoroutinesApi
    fun playAudio() {
        if (this.audioSrc.isNotEmpty()) {
            this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                setStopIcon()
                this.mediaPlayer.prepare()
                audioProgress.max = this.mediaPlayer.duration
                this.mediaPlayer.start()

                val job = GlobalScope.launch {
                    println("${Thread.currentThread()} has run.")
                    var currentPosition = mediaPlayer.currentPosition
                    val total = mediaPlayer.duration
                    audioProgress.max = total

                    while (mediaPlayer.isPlaying && currentPosition <= total) {

                        if (currentPosition == total) {
                            pauseAudio()
                            break
                        }

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