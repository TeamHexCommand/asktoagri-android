package `in`.hexcommand.asktoagri.ui.expert

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.data.CategoryData
import `in`.hexcommand.asktoagri.data.DistrictData
import `in`.hexcommand.asktoagri.data.QueryData
import `in`.hexcommand.asktoagri.data.SolutionData
import `in`.hexcommand.asktoagri.helper.ApiHelper
import `in`.hexcommand.asktoagri.helper.AppHelper
import `in`.hexcommand.asktoagri.model.Crops
import `in`.hexcommand.asktoagri.model.Upload
import `in`.hexcommand.asktoagri.ui.common.QueryActivity
import `in`.hexcommand.asktoagri.ui.view.CustomAudioView
import `in`.hexcommand.asktoagri.ui.view.CustomImageView
import `in`.hexcommand.asktoagri.ui.view.CustomQueryCardView
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
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

@DelicateCoroutinesApi
class AddSolutionActivity : AppCompatActivity() {

    companion object {
        const val FILE_CODE = 16
    }

    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCrops: Spinner
    private lateinit var spinnerDistricts: Spinner
    private lateinit var title: TextInputEditText
    private lateinit var content: TextInputEditText
    private lateinit var region: TextInputEditText
    private lateinit var dropdown: LinearLayout
    private lateinit var queryPreview: MaterialCardView

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
    private lateinit var app: AppHelper
    private lateinit var api: ApiHelper

    private lateinit var customAudioView: CustomAudioView

    private var solutionData: SolutionData = SolutionData(
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
        "",
        ""
    )

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
        setContentView(R.layout.activity_add_solution)

        title = findViewById(R.id.addSolutionTitle)
        content = findViewById(R.id.addSolutionContent)
        btn = findViewById(R.id.submit_solution_btn)
        camera = findViewById(R.id.cameraBtn)
        dropdown = findViewById(R.id.addSolutionDropdown)
        queryPreview = findViewById(R.id.addSolutionQueryHolder)

        mCategoryTextField = findViewById(R.id.addSolutionCategoryMenuText)
        mCropsTextField = findViewById(R.id.addSolutionCropsMenuText)
        mDistrictTextField = findViewById(R.id.addSolutionDistrictMenuText)
        mDistrictTextField = findViewById(R.id.addSolutionDistrictMenuText)

        filePreview = findViewById(R.id.queryFilePreview)
        filePreviewHolder = findViewById(R.id.queryFilePreviewHolder)
        fileRemoveBtn = findViewById(R.id.addSolutionRemoveFile)

        this.ls = LocalStorage(this)
        this.app = AppHelper(this)
        this.api = ApiHelper(this)
        solutionData.user = ls.getValueInt("user_id")

        this.mRequestQueue = Volley.newRequestQueue(this)

        handleIntent(intent)

        btn.setOnClickListener {

            if (title.text.isNullOrEmpty()) {
                title.error = "Enter query title"
            } else {
                solutionData.title = title.text.toString()
                solutionData.body = content.text.toString()
                val d = startDialog("Please wait submitting your solution")

                if (this.mIsFileSelected) {
                    val user = LocalStorage(this).getValueInt("user_id")
                    val upload = Upload(user = user, base64 = this.mFileBase64)

                    GlobalScope.launch {

                        val dataUpload: JSONObject = withContext(Dispatchers.Default) {
                            return@withContext JSONObject(async {
                                ApiHelper(this@AddSolutionActivity).uploadFile(
                                    upload
                                )
                            }.await()).getJSONObject("result").getJSONObject("data")
                        }

                        if (dataUpload.toString().isEmpty()) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@AddSolutionActivity,
                                    "Failed to add new solution",
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
                            solutionData.file = uploadId

                            addSolutionRequest(d)
                        }
                    }
                } else {
                    GlobalScope.launch {
                        addSolutionRequest(d)
                    }
                }
            }
        }

        fileRemoveBtn.setOnClickListener {

            if (solutionData.type == "audio") {
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

    private fun handleIntent(intent: Intent) {
        val extra = intent.extras
        if (extra != null) {
            if (intent.hasExtra("id") && intent.hasExtra("data")) {
                dropdown.visibility = View.GONE
                queryPreview.visibility = View.VISIBLE
                queryData =
                    Gson().fromJson(intent.getStringExtra("data").toString(), QueryData::class.java)
                solutionData.common = 0
                solutionData.category = queryData.category
                solutionData.crops = queryData.crops
                solutionData.district = queryData.district
                solutionData.tags = queryData.tags
                solutionData.user = ls.getValueInt("user_id")

                GlobalScope.launch {
                    async { renderQueryPreview(queryData) }.await()
                }

            } else {
                solutionData.common = 1
                queryPreview.visibility = View.GONE
                dropdown.visibility = View.VISIBLE
            }
        } else {
            solutionData.common = 1
            queryPreview.visibility = View.GONE
            dropdown.visibility = View.VISIBLE
        }
    }

    private suspend fun renderQueryPreview(queryData: QueryData) {
        val category: CategoryData? = withContext(Dispatchers.Default) {
            return@withContext async {
                app.getCategoryById(CategoryData(id = queryData.category))
            }.await()
        }

        val crops: Crops? = withContext(Dispatchers.Default) {
            return@withContext async {
                app.getCropsById(Crops(id = queryData.crops))
            }.await()
        }

        val upload: Upload? = withContext(Dispatchers.Default) {
            return@withContext async {
                app.getUploadById(Upload(id = queryData.file))
            }.await()
        }

        if (category != null && crops != null && upload != null) {

            Log.e("Query", app.getFileUrl(upload))
            runOnUiThread {

                val customQueryCardView = CustomQueryCardView(
                    this@AddSolutionActivity,
                    queryData.type,
                    category.name,
                    queryData.title,
                    app.getFileUrl(upload),
                    false
                )

                customQueryCardView.mCardHolder.setOnClickListener {
                    openQuery(queryData.id)
                }

                customQueryCardView.mOpenBtn.setOnClickListener {
                    openQuery(queryData.id)
                }

                queryPreview.addView(customQueryCardView)
            }
        }
    }

    private fun openQuery(id: Int) {
        startActivity(
            Intent(
                this@AddSolutionActivity,
                QueryActivity::class.java
            ).putExtra("id", id.toString())
        )
    }

    private suspend fun addSolutionRequest(d: AlertDialog?) {
        try {

            val dataQuery = withContext(Dispatchers.Default) {
                return@withContext JSONObject(async {
                    ApiHelper(this@AddSolutionActivity).addSolution(
                        solutionData
                    )
                }.await())
            }

            withContext(Dispatchers.Default) {
                queryResult(dataQuery, d)
            }

        } catch (e: JSONException) {
            //
        }

    }

    private suspend fun queryResult(data: JSONObject, dialog: AlertDialog?) {
        dialog?.dismiss()

        Log.e("AddSolution", data.getInt("code").toString())

        if (data.getInt("code") == 200) {

            val res = Gson().fromJson(
                data.getJSONObject("result").getJSONObject("data").toString(),
                SolutionData::class.java
            )

            Log.e("AddSolution", "Common = " + solutionData.common.toString())

            if (solutionData.common == 0) {
                val newQuery = QueryData(
                    id = queryData.id,
                    solution = res.id
                )

                withContext(Dispatchers.Default) {
                    ApiHelper(this@AddSolutionActivity).updateQuerySolution(
                        newQuery
                    )
                }

            }

            runOnUiThread {
                Toast.makeText(
                    this@AddSolutionActivity,
                    "Solution submitted",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(
                    Intent(
                        this@AddSolutionActivity,
                        ExpertActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
        } else {
            runOnUiThread {
                Toast.makeText(
                    this@AddSolutionActivity,
                    "Failed to add new query",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
                JSONObject(async { ApiHelper(this@AddSolutionActivity).getAllCategory() }.await()).getJSONObject(
                    "result"
                ).getJSONArray("data")

            val dataCrops =
                JSONObject(async { ApiHelper(this@AddSolutionActivity).getAllCrops() }.await()).getJSONObject(
                    "result"
                ).getJSONArray("data")

            val dataDistrict = JSONObject(async {
                ApiHelper(this@AddSolutionActivity).getDistrictByState(
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

            solutionData.category = categoryItemData[0].id
            solutionData.crops = cropsItemData[0].id
            solutionData.district = districtItemData[0].id
        }

        val categoryAdapter =
            ArrayAdapter(this@AddSolutionActivity, R.layout.list_item, categoryItem)
        val cropsAdapter = ArrayAdapter(this@AddSolutionActivity, R.layout.list_item, cropsItem)
        val districtAdapter =
            ArrayAdapter(this@AddSolutionActivity, R.layout.list_item, districtItem)

        mCategoryTextField.setAdapter(categoryAdapter)
        mCropsTextField.setAdapter(cropsAdapter)
        mDistrictTextField.setAdapter(districtAdapter)

        mCategoryTextField.setOnItemClickListener { _, _, position, _ ->
            solutionData.category = categoryItemData[position].id
        }

        mCropsTextField.setOnItemClickListener { _, _, position, _ ->
            solutionData.crops = cropsItemData[position].id
        }

        mDistrictTextField.setOnItemClickListener { _, _, position, _ ->
            solutionData.district = districtItemData[position].id
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
                        val base = AppHelper(this@AddSolutionActivity).fileUriToBase64(
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
            solutionData.type = "image"
            mFileType = "image"
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            filePreview.addView(
                CustomImageView(
                    this, uri.toString()
                )
            )
        } else if (type == "video/mpeg" || type == "video/mp4" || type == "video/x-matroska" || type == "video/x-msvideo") {
            solutionData.type = "video"
            mFileType = "video"
            mFileExt
            Toast.makeText(this, "Video selected", Toast.LENGTH_SHORT).show()
            val vid = uri.getVideoSize()
            Log.e("addSolution", vid.toString())

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
            solutionData.type = "audio"
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
            LayoutInflater.from(this@AddSolutionActivity)
                .inflate(R.layout.dialog_common_loading, null)
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog
                .Builder(this@AddSolutionActivity).setView(view)
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
                        val base = AppHelper(this@AddSolutionActivity).fileUriToBase64(
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
        retriever.setDataSource(this@AddSolutionActivity, this)
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

        if (solutionData.type == "audio") {
            customAudioView.pauseAudio()
        }
    }
}