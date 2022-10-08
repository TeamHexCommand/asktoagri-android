package `in`.hexcommand.asktoagri.ui.user.Query

import `in`.hexcommand.asktoagri.MainActivity
import `in`.hexcommand.asktoagri.R
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toBitmap
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class AddQueryActivity : AppCompatActivity() {

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
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(intent, pickImage)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            tmpImg.setImageURI(imageUri)
        }
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
}