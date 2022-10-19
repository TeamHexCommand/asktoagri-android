package `in`.hexcommand.asktoagri.helper

import `in`.hexcommand.asktoagri.data.*
import `in`.hexcommand.asktoagri.model.Crops
import `in`.hexcommand.asktoagri.model.Upload
import `in`.hexcommand.asktoagri.model.User
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ApiHelper(context: Context) : NetworkHelper(context), NetworkResponse {

    var res: NetworkResponse = this

    private var ls: LocalStorage
    private var mQuery: String
    private var mRequestQueue: RequestQueue

    init {
        this.ls = LocalStorage(context)
//        this.db = DatabaseHelper(context, null)
        this.mRequestQueue = Volley.newRequestQueue(context)
        this.mQuery = ""
    }

    private suspend fun sendRequest(
        request: String,
        type: String,
        filter: String,
        param: JSONObject,
        header: Boolean = true
    ) =
        suspendCoroutine<String> { r ->
            val waitFor = CoroutineScope(Dispatchers.IO).async {
                val stringRequest = object : StringRequest(
                    Method.POST,
                    AppHelper(context).getApiUrl(),
                    Response.Listener {
                        Log.e("API", it)
                        r.resume(it)
                    },
                    Response.ErrorListener {
                        r.resume(it.toString())
                    }) {

                    override fun getBody(): ByteArray {

                        val rqst = JSONObject()
                            .put("request", request)
                            .put("type", type)
                            .put("filter", filter)
                            .put("param", param)
                            .toString()

                        Log.e("API", rqst.toString())

                        return rqst.toByteArray()
                    }

//                    @Throws(AuthFailureError::class)
//                    override fun getHeaders(): Map<String, String> {
//                        return AppHelper(context).getHeaders()
//                    }
                }
                stringRequest.setShouldCache(false)
                mRequestQueue.add(stringRequest)
            }
        }


    private suspend fun sendMultipartRequest(
        request: String,
        type: String,
        filter: String,
        param: JSONObject
    ) =
        suspendCoroutine<String> { r ->
            val waitFor = CoroutineScope(Dispatchers.IO).async {

                val stringRequest = object : StringRequest(
                    Method.POST,
                    AppHelper(context).getApiUrl(),
                    Response.Listener {
                        Log.e("API", it)
                        r.resume(it)
                    },
                    Response.ErrorListener {
                        r.resume(it.toString())
                    }) {

                    override fun getBody(): ByteArray {
                        return JSONObject()
                            .put("request", request)
                            .put("type", type)
                            .put("filter", filter)
                            .put("param", param)
                            .toString()
                            .toByteArray()
                    }

//                    @Throws(AuthFailureError::class)
//                    override fun getHeaders(): Map<String, String> {
//                        return header
//                    }
                }
                stringRequest.setShouldCache(false)
                mRequestQueue.add(stringRequest)
            }
        }


    public suspend fun sendGetRequest(
        url: String,
        header: HashMap<String, String> = HashMap()
    ) =
        suspendCoroutine<String> { r ->
            val waitFor = CoroutineScope(Dispatchers.IO).async {
                val stringRequest = object : StringRequest(
                    Method.GET,
                    url,
                    Response.Listener {
                        r.resume(it)
                    },
                    Response.ErrorListener {
                        r.resume(it.toString())
                    }) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        return header
                    }
                }
                stringRequest.setShouldCache(false)
                mRequestQueue.add(stringRequest)
            }
        }

    suspend fun addUser(model: User): String {

        Log.e("LoginActivity", Gson().toJson(model))

        return ApiHelper(context).sendRequest(
            "add",
            "user",
            "",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun checkUser(model: User): String {
        return ApiHelper(context).sendRequest(
            "get",
            "user",
            "getByMobile",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun getUserById(model: User): String {
        return ApiHelper(context).sendRequest(
            "get",
            "user",
            "getById",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun addQuery(model: QueryData): String {
        return ApiHelper(context).sendRequest(
            "add",
            "query",
            "",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun addSolution(model: SolutionData): String {
        return ApiHelper(context).sendRequest(
            "add",
            "solution",
            "",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun getAllCrops(): String {
        return ApiHelper(context).sendRequest(
            "get",
            "crops",
            "getAll",
            JSONObject()
        )
    }

    suspend fun getCropsById(model: Crops): String {
        return ApiHelper(context).sendRequest(
            "get",
            "crops",
            "getById",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun getQueryById(model: QueryData): String {
        return ApiHelper(context).sendRequest(
            "get",
            "query",
            "getById",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun getSolutionById(model: SolutionData): String {
        return ApiHelper(context).sendRequest(
            "get",
            "solution",
            "getById",
            JSONObject(Gson().toJson(model))
        )
    }


    suspend fun getDistrictById(model: DistrictData): String {
        return ApiHelper(context).sendRequest(
            "get",
            "district",
            "getById",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun getUploadById(model: Upload): String {
        return ApiHelper(context).sendRequest(
            "get",
            "upload",
            "getById",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun getCategoryById(model: CategoryData): String {
        return ApiHelper(context).sendRequest(
            "get",
            "category",
            "getById",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun getAllCategory(): String {
        return ApiHelper(context).sendRequest(
            "get",
            "category",
            "getAll",
            JSONObject()
        )
    }

    suspend fun getDistrictByState(model: DistrictData): String {
        return ApiHelper(context).sendRequest(
            "get",
            "district",
            "getByStateName",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun getTrendingArtical(): String {
        return ApiHelper(context).sendRequest(
            "get",
            "artical",
            "getByTrending",
            JSONObject()
        )
    }

    suspend fun addConfig(model: ConfigData): String {
        return ApiHelper(context).sendRequest(
            "add",
            "config",
            "",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun uploadFile(model: Upload): String {
        return ApiHelper(context).sendRequest(
            "add",
            "upload",
            "",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun getLatestUserQuery(model: QueryData): String {
        return ApiHelper(context).sendRequest(
            "get",
            "query",
            "getByUser",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun updateQuerySolution(model: QueryData): String {
        return ApiHelper(context).sendRequest(
            "update",
            "query",
            "updateSolution",
            JSONObject(Gson().toJson(model))
        )
    }

    suspend fun getNotResolvedQuery(): String {
        return ApiHelper(context).sendRequest(
            "get",
            "query",
            "getByNotResolved",
            JSONObject()
        )
    }

    companion object {
        const val API = ""
    }
}