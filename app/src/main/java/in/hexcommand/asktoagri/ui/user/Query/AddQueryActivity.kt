package `in`.hexcommand.asktoagri.ui.user.Query

import `in`.hexcommand.asktoagri.MainActivity
import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.data.CategoryData
import `in`.hexcommand.asktoagri.data.DistrictData
import `in`.hexcommand.asktoagri.data.QueryData
import `in`.hexcommand.asktoagri.helper.ApiHelper
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.model.Crops
import `in`.hexcommand.asktoagri.model.Upload
import `in`.hexcommand.asktoagri.ui.view.CustomAudioView
import `in`.hexcommand.asktoagri.ui.view.CustomImageView
import `in`.hexcommand.asktoagri.ui.view.CustomVideoView
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.media2.common.VideoSize
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.abedelazizshe.lightcompressorlibrary.config.StorageConfiguration
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_add_query.*
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File


@DelicateCoroutinesApi
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
    private var uploadId = 0
    private var imageUri: Uri? = null
    private var img: String = ""

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mAudioProgress: LinearProgressIndicator
    private lateinit var mAudioActionBtn: MaterialCardView
    private lateinit var mAudioActionIcon: ImageView
    private var mIsFileSelected: Boolean = false
    private var mFileType: String = "text"
    private var mFileBase64: String = ""
    private var mFileExt: String = ""

    private lateinit var filePreview: MaterialCardView
    private lateinit var filePreviewHolder: MaterialCardView
    private lateinit var fileRemoveBtn: MaterialButton

    private lateinit var mCategoryTextField: AutoCompleteTextView
    private lateinit var mCropsTextField: AutoCompleteTextView
    private lateinit var mDistrictTextField: AutoCompleteTextView

    private lateinit var ls: LocalStorage

    private lateinit var customAudioView: CustomAudioView

    private var queryData: QueryData = QueryData(
        id = 0,
        user = 0,
        "",
        "text",
        "",
        0,
        0,
        0,
        0,
        0,
        0,
        "",
        0,
        ""
    )

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_query)

        title = findViewById(R.id.addQueryTitle)
        content = findViewById(R.id.addQueryContent)
        btn = findViewById(R.id.submit_query_btn)
        camera = findViewById(R.id.cameraBtn)

        mCategoryTextField = findViewById(R.id.addQueryCategoryMenuText)
        mCropsTextField = findViewById(R.id.addQueryCropsMenuText)
        mDistrictTextField = findViewById(R.id.addQueryDistrictMenuText)
        mDistrictTextField = findViewById(R.id.addQueryDistrictMenuText)

        filePreview = findViewById(R.id.queryFilePreview)
        filePreviewHolder = findViewById(R.id.queryFilePreviewHolder)
        fileRemoveBtn = findViewById(R.id.addQueryRemoveFile)

        this.ls = LocalStorage(this)
        queryData.user = ls.getValueInt("user_id")

        this.mRequestQueue = Volley.newRequestQueue(this)

        btn.setOnClickListener {

            if (addQueryTitle.text.isNullOrEmpty()) {
                addQueryTitle.error = "Enter query title"
            } else {
                queryData.title = addQueryTitle.text.toString()
                queryData.body = addQueryContent.text.toString()
                val d = startDialog("Please wait submitting your query")

                if (this.mIsFileSelected) {
                    val user = LocalStorage(this).getValueInt("user_id")
                    val upload = Upload(user = user, base64 = this.mFileBase64)

                    GlobalScope.launch {

                        val dataUpload: JSONObject = withContext(Dispatchers.Default) {
                            return@withContext JSONObject(async {
                                ApiHelper(this@AddQueryActivity).uploadFile(
                                    upload
                                )
                            }.await()).getJSONObject("result").getJSONObject("data")
                        }

                        if (dataUpload.toString().isEmpty()) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@AddQueryActivity,
                                    "Failed to add new query",
                                    Toast.LENGTH_SHORT
                                ).show()
                                d?.dismiss()
                            }
                        } else {
                            val uploadData =
                                Gson().fromJson(
                                    dataUpload.toString(),
                                    Upload::class.java
                                )

                            uploadId = uploadData.id
                            queryData.file = uploadId

                            addQueryRequest(d)
                        }
                    }
                } else {
                    GlobalScope.launch {
                        addQueryRequest(d)
                    }
                }
            }
        }

        fileRemoveBtn.setOnClickListener {

            if (queryData.type == "audio") {
                customAudioView.pauseAudio()
            }

            filePreviewHolder.visibility = View.GONE
            filePreview.removeAllViews()
            this.mIsFileSelected = false
            mFileType = "text"
        }

        camera.setOnClickListener {
            val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_CODE)
        }

        demoMenu()
    }

    private suspend fun addQueryRequest(d: AlertDialog?) {
        try {
            val dataQuery = withContext(Dispatchers.Default) {
                return@withContext JSONObject(async {
                    ApiHelper(this@AddQueryActivity).addQuery(
                        queryData
                    )
                }.await())
            }

            runOnUiThread {
                queryResult(dataQuery.getInt("code"), d)
            }
        } catch (e: JSONException) {
            //
        }

    }

    private fun queryResult(code: Int, dialog: AlertDialog?) {
        Handler().postDelayed({
            dialog?.dismiss()
            if (code == 200) {
                Toast.makeText(
                    this@AddQueryActivity,
                    "Query submitted",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(
                    Intent(
                        this@AddQueryActivity,
                        MainActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            } else {
                Toast.makeText(
                    this@AddQueryActivity,
                    "Failed to add new query",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, 3000)
    }

    private fun startDialog(msg: String): androidx.appcompat.app.AlertDialog? {
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_common_loading, null)
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog
                .Builder(this).setView(view)
                .setView(view)

        val dialogText = view.findViewById<MaterialTextView>(R.id.dialogLoadingText)
        dialogText.text = msg
        return builder.show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun demoMenu() {
        val categoryItem = ArrayList<String>()
        val cropsItem = ArrayList<String>()
        val districtItem = ArrayList<String>()

        val categoryItemData = ArrayList<CategoryData>()
        val cropsItemData = ArrayList<Crops>()
        val districtItemData = ArrayList<DistrictData>()

        GlobalScope.launch {
            val data =
                JSONObject(async { ApiHelper(this@AddQueryActivity).getAllCategory() }.await()).getJSONObject(
                    "result"
                ).getJSONArray("data")

            val dataCrops =
                JSONObject(async { ApiHelper(this@AddQueryActivity).getAllCrops() }.await()).getJSONObject(
                    "result"
                ).getJSONArray("data")

            val dataDistrict = JSONObject(async {
                ApiHelper(this@AddQueryActivity).getDistrictByState(
                    DistrictData(
                        0, "", 0, "Gujarat"
                    )
                )
            }.await()).getJSONObject("result").getJSONArray("data")

            (0 until data.length()).forEach { i ->
                val categoryData =
                    Gson().fromJson(data.getJSONObject(i).toString(), CategoryData::class.java)
                categoryItemData.add(categoryData)
                categoryItem.add(categoryData.name)
            }

            (0 until dataCrops.length()).forEach { i ->
                val cropsData =
                    Gson().fromJson(dataCrops.getJSONObject(i).toString(), Crops::class.java)
                cropsItemData.add(cropsData)
                cropsItem.add(cropsData.name.capitalize())
            }

            (0 until dataDistrict.length()).forEach { i ->
                val districtData = Gson().fromJson(
                    dataDistrict.getJSONObject(i).toString(), DistrictData::class.java
                )
                districtItemData.add(districtData)
                districtItem.add(districtData.name.capitalize())
            }

            queryData.category = categoryItemData[0].id
            queryData.crops = cropsItemData[0].id
            queryData.district = districtItemData[0].id
        }

        val categoryAdapter = ArrayAdapter(this@AddQueryActivity, R.layout.list_item, categoryItem)
        val cropsAdapter = ArrayAdapter(this@AddQueryActivity, R.layout.list_item, cropsItem)
        val districtAdapter = ArrayAdapter(this@AddQueryActivity, R.layout.list_item, districtItem)

        mCategoryTextField.setAdapter(categoryAdapter)
        mCropsTextField.setAdapter(cropsAdapter)
        mDistrictTextField.setAdapter(districtAdapter)

        mCategoryTextField.setOnItemClickListener { _, _, position, _ ->
            queryData.category = categoryItemData[position].id
        }

        mCropsTextField.setOnItemClickListener { _, _, position, _ ->
            queryData.crops = cropsItemData[position].id
        }

        mDistrictTextField.setOnItemClickListener { _, _, position, _ ->
            queryData.district = districtItemData[position].id
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == FILE_CODE) {

            val selectedFile = data?.data?.also { uri ->
                val returnCursor: Cursor? = contentResolver.query(uri, null, null, null, null)
                val sizeIndex: Int = returnCursor!!.getColumnIndex(OpenableColumns.SIZE)
                returnCursor.moveToFirst()
                val size: Int = returnCursor.getInt(sizeIndex) / 1024

                returnCursor.close()

                if (size >= 51200) {
                    Toast.makeText(this, "Please select file less then 10MB", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val cR: ContentResolver = this.contentResolver
                    val mime = cR.getType(uri)
                    this.mFileType = mime.toString()

                    GlobalScope.launch {
                        val base = AppHelper(this@AddQueryActivity).fileUriToBase64(
                            uri,
                            applicationContext.contentResolver
                        )!!
                        mFileBase64 = "data:$mime;base64,$base"
                    }

                    renderPreview(uri, mime.toString())
                    Log.e("AdQuery", mime.toString())
                }
            }
        }
    }

    private fun renderPreview(uri: Uri, type: String) {

        filePreviewHolder.visibility = View.VISIBLE
        filePreview.removeAllViews()

        this.mIsFileSelected = true

        if (type == "image/jpeg" || type == "image/png" || type == "image/jpg") {
            queryData.type = "image"
            mFileType = "image"
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            filePreview.addView(
                CustomImageView(
                    this, uri.toString()
                )
            )
        } else if (type == "video/mpeg" || type == "video/mp4" || type == "video/x-matroska" || type == "video/x-msvideo") {
            queryData.type = "video"
            mFileType = "video"
            mFileExt
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

            val customVideoView = CustomVideoView(
                this, uri.toString(), false, false
            )

            customVideoView.videoView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio = vratio
            }

            filePreview.addView(
                customVideoView
            )

            compressVideo(uri)

        } else if (type == "audio/mpeg" || type == "audio/x-matroska" || type == "audio/mp3" || type == "audio/ogg") {
            queryData.type = "audio"
            mFileType = "audio"
            Toast.makeText(this, "Audio selected", Toast.LENGTH_SHORT).show()
            this.customAudioView = CustomAudioView(this, uri.toString(), false)
            this.customAudioView.setAudioSrc(uri)

            filePreview.addView(
                this.customAudioView
            )

        } else {

            filePreviewHolder.visibility = View.GONE
            filePreview.removeAllViews()
            this.mIsFileSelected = false

            Toast.makeText(
                this, "Invalid file, Only image,videos and audio supports", Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun compressVideo(uri: Uri) {

        val view: View =
            LayoutInflater.from(this@AddQueryActivity).inflate(R.layout.dialog_common_loading, null)
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog
                .Builder(this@AddQueryActivity).setView(view)
                .setView(view)

        val dialogText = view.findViewById<MaterialTextView>(R.id.dialogLoadingText)
        val dialogProgress = view.findViewById<ProgressBar>(R.id.dialogProgress)
        dialogProgress.isIndeterminate = false
        dialogProgress.max = 100
        dialogProgress.progress = 0
        dialogText.text = "Compressing your video"
        val dialog = builder.show()

        GlobalScope.launch {
            VideoCompressor.start(
                context = applicationContext, // => This is required
                uris = listOf(uri), // => Source can be provided as content uris
                isStreamable = true,
                storageConfiguration = StorageConfiguration(
                    saveAt = Environment.DIRECTORY_MOVIES, // => the directory to save the compressed video(s). Will be ignored if isExternal = false.
                    isExternal = true // => false means save at app-specific file directory. Default is true.
                ),
                configureWith = Configuration(
                    quality = VideoQuality.LOW,
                    isMinBitrateCheckEnabled = false,
                    disableAudio = false, /*Boolean, or ignore*/
                    keepOriginalResolution = true, /*Boolean, or ignore*/
                ),
                listener = object : CompressionListener {
                    override fun onProgress(index: Int, percent: Float) {
                        // Update UI with progress value
                        runOnUiThread {
                            dialogProgress.progress = percent.toInt()
                        }
                    }

                    override fun onStart(index: Int) {
                        // Compression start
                    }

                    override fun onSuccess(index: Int, size: Long, path: String?) {
                        dialog.dismiss()
                        val base = AppHelper(this@AddQueryActivity).fileUriToBase64(
                            Uri.fromFile(File(path.toString())),
                            applicationContext.contentResolver
                        )!!
                        mFileBase64 = "data:video/mpeg;base64,$base"
                    }

                    override fun onFailure(index: Int, failureMessage: String) {
                        // On Failure
                    }

                    override fun onCancelled(index: Int) {
                        // On Cancelled
                    }

                }
            )
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

    override fun onBackPressed() {
        super.onBackPressed()

        if (queryData.type == "audio") {
            customAudioView.pauseAudio()
        }
    }
}