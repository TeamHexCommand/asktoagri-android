package `in`.hexcommand.asktoagri.ui.view

import `in`.hexcommand.asktoagri.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException


@OptIn(DelicateCoroutinesApi::class)
@SuppressLint("UseCompatLoadingForDrawables")
class AudioView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private lateinit var mediaPlayer: MediaPlayer
    private var mIsPlaying: Boolean = false
    lateinit var audioActionBtn: MaterialCardView
    lateinit var audioActionIcon: ImageView
    lateinit var audioProgress: LinearProgressIndicator
    private lateinit var src: String
    private var autoplay: Boolean = false
    private lateinit var currentSrc: String

    init {
        LayoutInflater.from(context).inflate(R.layout.holder_audio, this, true)
        val customAttributesStyle =
            context.obtainStyledAttributes(attrs, R.styleable.AudioView, 0, 0)

        mediaPlayer = MediaPlayer()

        this.src = customAttributesStyle.getString(R.styleable.AudioView_audio_src).toString()
        this.autoplay = customAttributesStyle.getBoolean(R.styleable.AudioView_autoplay, false)

        this.audioActionBtn = findViewById(R.id.audio_action)
        this.audioActionIcon = findViewById(R.id.audio_action_icon)
        this.audioProgress = findViewById(R.id.audio_progress)


        try {
            setAudioSrc(this.src)

            if (this.autoplay) {
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

        } finally {
            customAttributesStyle.recycle()
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
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(this.src)
        }
    }

    fun setAudioSrc(uri: Uri) {
        mediaPlayer.setDataSource(context, uri)
    }

    fun setAudioSrc(url: String) {
        if (url.isNotEmpty()) {
            mediaPlayer.setDataSource(Uri.parse(url).toString())
            this.src = url
            Log.e("AudioView", url)
        }
    }

    @DelicateCoroutinesApi
    fun playAudio() {

        if (this.src.isNotEmpty()) {
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