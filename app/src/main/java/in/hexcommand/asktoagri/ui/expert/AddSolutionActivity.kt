package `in`.hexcommand.asktoagri.ui.expert

import `in`.hexcommand.asktoagri.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import org.json.JSONObject

class AddSolutionActivity : AppCompatActivity() {

    private lateinit var title: TextInputEditText
    private lateinit var content: TextInputEditText
    private lateinit var btn: MaterialButton

    private lateinit var mRequestQueue: RequestQueue
    private lateinit var mQuery: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_solution)

        title = findViewById(R.id.addSolutionTitle)
        content = findViewById(R.id.addSolutionContent)
        btn = findViewById(R.id.submit_solution_btn)

        this.mRequestQueue = Volley.newRequestQueue(this)

        btn.setOnClickListener {
            addSolution(
                title.text.toString(),
                content.text.toString(),
                intent.getStringExtra("crops").toString(),
                intent.getStringExtra("category").toString(),
                intent.getStringExtra("queryId").toString()
            )
        }

    }

    fun addSolution(
        title: String,
        content: String,
        crops: String,
        category: String,
        queryId: String
    ) {

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
                        Toast.makeText(this, "Solution submitted", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ExpertActivity::class.java))
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
                jsonQuery.put("task", "solution")
                jsonQuery.put(
                    "data",
                    JSONObject()
                        .put("title", title)
                        .put("content", content)
                        .put("crops", crops)
                        .put("category", category)
                        .put("isCommon", false)
                        .put("userId", currentuser)
                        .put("queryId", queryId)
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