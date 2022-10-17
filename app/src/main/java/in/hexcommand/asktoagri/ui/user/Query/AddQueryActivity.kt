package `in`.hexcommand.asktoagri.ui.user.Query

import `in`.hexcommand.asktoagri.MainActivity
import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.ui.view.AudioView
import `in`.hexcommand.asktoagri.ui.view.CustomAudioView
import `in`.hexcommand.asktoagri.ui.view.CustomImageView
import `in`.hexcommand.asktoagri.ui.view.CustomVideoView
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.updateLayoutParams
import androidx.media2.common.VideoSize
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException


class AddQueryActivity : AppCompatActivity() {

    companion object {
        const val FILE_CODE = 16
    }

    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCrops: Spinner
    private lateinit var spinnerDistricts: Spinner
    private lateinit var title: TextInputEditText
    private lateinit var content: TextInputEditText
    private lateinit var region: TextInputEditText

    private lateinit var btn: MaterialButton

    private lateinit var mRequestQueue: RequestQueue
    private lateinit var mQuery: String

    private lateinit var camera: CardView

    private lateinit var tmpImg: ImageView

    private val pickImage = 100
    private var imageUri: Uri? = null
    private var img: String = ""

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mAudioProgress: LinearProgressIndicator
    private lateinit var mAudioActionBtn: MaterialCardView
    private lateinit var mAudioActionIcon: ImageView
    private var mIsPlaying: Boolean = false

    private lateinit var mVideoView: VideoView
    private lateinit var audioView: AudioView

    private lateinit var filePreview: MaterialCardView

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_query)

        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCrops = findViewById(R.id.spinnerCrops)
        spinnerDistricts = findViewById(R.id.spinnerDistricts)
        title = findViewById(R.id.addQueryTitle)
        content = findViewById(R.id.addQueryContent)
        region = findViewById(R.id.addQueryRegion)
        btn = findViewById(R.id.submit_query_btn)
        camera = findViewById(R.id.cameraBtn)
        tmpImg = findViewById(R.id.tmpImg)

        mVideoView = findViewById(R.id.addQueryVideoView)
        mAudioProgress = findViewById(R.id.audio_progress)
        mAudioActionBtn = findViewById(R.id.audio_action)
        mAudioActionIcon = findViewById(R.id.audio_action_icon)

        audioView = findViewById(R.id.audioView)

        filePreview = findViewById(R.id.queryFilePreview)

        mVideoView.setVideoURI(Uri.parse("http://www.ebookfrenzy.com/android_book/movie.mp4"))
        val mediaController = MediaController(this)

        // sets the anchor view
        // anchor view for the videoView
        mediaController.setAnchorView(mVideoView)

        // sets the media player to the videoView
        mediaController.setMediaPlayer(mVideoView)

        // sets the media controller to the videoView
        mVideoView.setMediaController(mediaController)
//        mVideoView.start()

        mediaPlayer = MediaPlayer()

        if (mediaPlayer.isPlaying) {
            mAudioActionIcon.setImageDrawable(getDrawable(R.drawable.round_stop_24))
        } else {
            mAudioActionIcon.setImageDrawable(getDrawable(R.drawable.round_play_arrow_24))
        }

        mAudioActionBtn.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                pauseAudio()
            } else {
                playAudio()
            }
        }

        this.mRequestQueue = Volley.newRequestQueue(this)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.category_list,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val adapterCrops = ArrayAdapter.createFromResource(
            this,
            R.array.crops_list,
            android.R.layout.simple_spinner_item
        )
        adapterCrops.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val adapterDis = ArrayAdapter.createFromResource(
            this,
            R.array.districts_list,
            android.R.layout.simple_spinner_item
        )
        adapterDis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        spinnerCategory.adapter = adapter
        spinnerCrops.adapter = adapterCrops
        spinnerDistricts.adapter = adapterDis

        btn.setOnClickListener {

            val b = bitmapToBase64(
                tmpImg.drawable.toBitmap()
            ).toString()

            addQuery(
                title.text.toString(),
                b,
                spinnerCategory.selectedItem.toString(),
                spinnerCrops.selectedItem.toString(),
                spinnerDistricts.selectedItem.toString()
            )
        }

        camera.setOnClickListener {
//            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//            startActivityForResult(gallery, pickImage)


            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_CODE)
        }
    }

    private fun pauseAudio() {
        if (mediaPlayer.isPlaying) {
            // pausing the media player if media player
            // is playing we are calling below line to
            // stop our media player.
            mediaPlayer.pause()
            mAudioActionIcon.setImageDrawable(getDrawable(R.drawable.round_play_arrow_24))

//            mediaPlayer.reset();
//            mediaPlayer.release();

            // below line is to display a message
            // when media player is paused.
            Toast.makeText(this, "Audio has been paused", Toast.LENGTH_SHORT).show();
        } else {
            // this method is called when media
            // player is not playing.
            Toast.makeText(this, "Audio has not played", Toast.LENGTH_SHORT).show();
        }
    }

    private fun playAudio() {
        val audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

        // initializing media player
        mediaPlayer = MediaPlayer()

        // below line is use to set the audio
        // stream type for our media player.
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

        // below line is use to set our
        // url to our media player.
        try {
            mediaPlayer.setDataSource(audioUrl)
            // below line is use to prepare
            // and start our media player.
            mAudioActionIcon.setImageDrawable(getDrawable(R.drawable.round_stop_24))
            mediaPlayer.prepare()
            mAudioProgress.max = mediaPlayer.duration
            mediaPlayer.start()

//            val t = Thread().start()

            val job = GlobalScope.launch {
                println("${Thread.currentThread()} has run.")
                var currentPosition = mediaPlayer.currentPosition
                val total = mediaPlayer.duration
                mAudioProgress.max = total
                while (mediaPlayer != null && mediaPlayer.isPlaying && currentPosition < total) {
                    currentPosition = try {
                        Thread.sleep(500)
                        mediaPlayer.currentPosition
                    } catch (e: InterruptedException) {
                        return@launch
                    } catch (e: Exception) {
                        return@launch
                    }
                    runOnUiThread { mAudioProgress.setProgress(currentPosition) }

                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        // below line is use to display a toast message.
        Toast.makeText(this, "Audio started playing..", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            tmpImg.setImageURI(imageUri)
        }

        if (resultCode == RESULT_OK && requestCode == FILE_CODE) {
            val selectedFile = data?.data?.also { uri ->
                val cR: ContentResolver = this.contentResolver
                val mime = cR.getType(uri)
                renderPreview(uri, mime.toString())
                Log.e("AdQuery", mime.toString())
            }
        }
    }

    fun renderPreview(uri: Uri, type: String) {

        filePreview.removeAllViews()

        if (type == "image/jpeg" || type == "image/png" || type == "image/jpg") {
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            filePreview.addView(
                CustomImageView(
                    this,
                    uri.toString()
                )
            )
        } else if (type == "video/mpeg" || type == "video/mp4" || type == "video/x-matroska" || type == "video/x-msvideo") {
            Toast.makeText(this, "Video selected", Toast.LENGTH_SHORT).show()
            val vid = uri.getVideoSize()
            Log.e("AddQuery", vid.toString())

            val vratio = if (vid.width < vid.height) {
                "9:16"
            } else if (vid.width == vid.height) {
                "1:1"
            } else {
                "16:9"
            }

            Log.e("AddQuery", vratio)
            val customVideoView = CustomVideoView(
                this,
                uri.toString(),
                false,
                false
            )

            customVideoView.videoView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio = vratio
            }

            filePreview.addView(
                customVideoView
            )
        } else if (type == "audio/mpeg" || type == "audio/x-matroska" || type == "audio/mp3" || type == "audio/ogg") {
            Toast.makeText(this, "Audio selected", Toast.LENGTH_SHORT).show()
            val customAudioView = CustomAudioView(
                this,
                uri.toString(),
                false
            )

            customAudioView.setAudioSrc(uri)

            filePreview.addView(
                customAudioView
            )
        } else {
            Toast.makeText(
                this,
                "Invalid file, Only image,videos and audio supports",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun Uri.getVideoSize(): VideoSize {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this@AddQueryActivity, this)
        val width =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
        val height =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                ?: 0
        retriever.release()
        return VideoSize(width, height)
    }

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            val mime = MimeTypeMap.getSingleton()
            type = mime.getMimeTypeFromExtension(extension)
        }
        return type
    }

    private fun bitmapToBase64(bitmap: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun base64ToBitmap(b64: String): Bitmap? {
        val imageAsBytes = Base64.decode(b64.toByteArray(), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
    }

    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun addQuery(title: String, content: String, category: String, crops: String, region: String) {

        val currentuser = FirebaseAuth.getInstance().currentUser!!.uid

        val stringRequest = @SuppressLint("Range")
        object : StringRequest(
            Method.POST,
            "https://asktoagri.planckstudio.in/api/v1/",
            Response.Listener {
                try {
                    val jsonObject = JSONObject(it)
                    val rStatus = jsonObject.getJSONObject("result").getInt("code")
                    if (rStatus == 200) {
                        Toast.makeText(this, "Query submitted", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                } catch (e: JSONException) {
                    //
                }
            },
            Response.ErrorListener {
                Log.e("CRAFTY", it.toString())
            }) {

            override fun getBody(): ByteArray {
                val jsonBody = JSONObject()
                val jsonQuery = JSONObject()
                jsonQuery.put("task", "query")
                jsonQuery.put(
                    "data",
                    JSONObject()
                        .put("title", title)
                        .put("content", content)
                        .put("userId", currentuser)
                        .put("region", region)
                        .put("category", category)
                        .put("crops", crops)
                )
                jsonBody.put("type", "add")
                jsonBody.put("param", jsonQuery)
                mQuery = jsonBody.toString()
                return mQuery.toByteArray()
            }
        }
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        audioView.pauseAudio()
        pauseAudio()
    }
}