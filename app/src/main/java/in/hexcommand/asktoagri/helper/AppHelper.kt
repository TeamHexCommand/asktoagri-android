@file:OptIn(DelicateCoroutinesApi::class)

package `in`.hexcommand.asktoagri.helper

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.data.ArticalData
import `in`.hexcommand.asktoagri.data.CategoryData
import `in`.hexcommand.asktoagri.data.DistrictData
import `in`.hexcommand.asktoagri.data.SolutionData
import `in`.hexcommand.asktoagri.database.ArticalDatabase
import `in`.hexcommand.asktoagri.model.AddressModel
import `in`.hexcommand.asktoagri.model.Crops
import `in`.hexcommand.asktoagri.model.Upload
import `in`.hexcommand.asktoagri.model.User
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.provider.Settings.Secure
import android.util.Base64
import android.util.Log
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*


class AppHelper(private val context: Context) {

    private val ls: LocalStorage = LocalStorage(context)
    private var mRequestQueue = Volley.newRequestQueue(context)
    private var mQuery: String = ""
    private val articalDb by lazy { ArticalDatabase.getDatabase(context).articalDao() }

    fun String.decode(): String {
        return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
    }

    fun String.encode(): String {
        return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.DEFAULT)
    }

    fun getConfigUrl(name: String): String {
        val data = JSONObject(ls.getValueString("remote_config"))
        return data.getJSONObject("url").getString(name)
    }

    fun getApiUrl(): String {
        try {
            val data = JSONObject(ls.getValueString("remote_config"))
            return data.getJSONObject("url").getString("api")
        } catch (e: JSONException) {
            return context.getString(R.string.apiUrl)
        }
    }

    fun setAppLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getLocationFromCode(latitude: Double, longitude: Double): AddressModel {

        val addressModel: AddressModel = AddressModel()

        val geocoder = Geocoder(context, Locale.getDefault())
        var result: String? = null
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(
                latitude, longitude, 1
            )
            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]

                addressModel.setPincode(address.postalCode)
                addressModel.setVillage(address.subLocality)
                addressModel.setCity(address.locality)
                addressModel.setDistrict(address.subAdminArea)
                addressModel.setState(address.adminArea)
                addressModel.setCountry(address.countryCode)
                addressModel.setLatitude(latitude.toString())
                addressModel.setLongitude(longitude.toString())
            }
        } catch (e: IOException) {
            // Log.e(TAG, "Unable connect to Geocoder", e);
        }

        return addressModel
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun getRemoteConfig(url: String) {
        GlobalScope.launch {
            withContext(Dispatchers.Default) {
                launch {
                    val res =
                        async { ApiHelper(context).sendGetRequest(url.decode()) }.await()
                    ls.save("remote_config", res)
                    Log.e("APP", res)
                }
            }
        }
    }

    fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            ls.save("userFcm", task.result)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveUserInfo() {

        val u = User(
            mobile = ls.getValueString("user_mobile")
        )

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val checkUser = async { ApiHelper(context).checkUser(u) }.await()
                val userData = JSONObject(checkUser)
                ls.save(
                    "current_user",
                    userData.getJSONObject("result").getJSONArray("data").getJSONObject(0)
                        .toString()
                )
            } catch (e: JSONException) {
                logoutUser()
            }
        }
    }

    fun storeTrendingArtical() {
        GlobalScope.launch {
            try {
                val data =
                    JSONObject(async { ApiHelper(context).getTrendingArtical() }.await())
                        .getJSONObject("result").getJSONArray("data")

                (0 until data.length()).forEach { i ->
                    val articalData =
                        Gson().fromJson(data.getJSONObject(i).toString(), ArticalData::class.java)
                    articalDb.insert(articalData)
                }
            }catch (e: JSONException) {
                //
            }

        }
    }

    fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        ls.clearSharedPreference()
    }

    fun fileUriToBase64(uri: Uri, resolver: ContentResolver): String? {
        var encodedBase64: String? = ""
        try {
            val bytes: ByteArray = readBytes(uri, resolver)
            encodedBase64 = Base64.encodeToString(bytes, 0)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return encodedBase64
    }

    @SuppressLint("Recycle")
    @Throws(IOException::class)
    private fun readBytes(uri: Uri, resolver: ContentResolver): ByteArray {
        // this dynamically extends to take the bytes you read
        val inputStream: InputStream? = resolver.openInputStream(uri)
        val byteBuffer = ByteArrayOutputStream()

        // this is storage overwritten on each iteration with bytes
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        // we need to know how may bytes were read to write them to the
        // byteBuffer
        var len = 0
        while (inputStream?.read(buffer).also { len = it!! } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        // and then we can return your byte array.
        return byteBuffer.toByteArray()
    }

    suspend fun getCategoryById(model: CategoryData): CategoryData? {
        val response = JSONObject(ApiHelper(context).getCategoryById(model))
        return if (response.getInt("code") == 200) {
            val data = response.getJSONObject("result").getJSONArray("data").getJSONObject(0)
            Gson().fromJson(data.toString(), CategoryData::class.java)
        } else null
    }

    suspend fun getUploadById(model: Upload): Upload? {

        val response = JSONObject(ApiHelper(context).getUploadById(model))
        val data = this.getResponseData(response)

        return if (data != null) {
            return Gson().fromJson(data.toString(), Upload::class.java)
        } else null

    }

    suspend fun getCropsById(model: Crops): Crops? {
        val response = JSONObject(ApiHelper(context).getCropsById(model))
        val data = this.getResponseData(response)
        return if (data != null) {
            return Gson().fromJson(data.toString(), Crops::class.java)
        } else null
    }

    suspend fun getSolutionById(model: SolutionData): SolutionData? {
        val response = JSONObject(ApiHelper(context).getSolutionById(model))
        val data = this.getResponseData(response)
        return if (data != null) {
            return Gson().fromJson(data.toString(), SolutionData::class.java)
        } else null
    }

    suspend fun getDistrictById(model: DistrictData): DistrictData? {
        val response = JSONObject(ApiHelper(context).getDistrictById(model))
        val data = this.getResponseData(response)
        return if (data != null) {
            return Gson().fromJson(data.toString(), DistrictData::class.java)
        } else null
    }

    private suspend fun getResponseData(response: JSONObject): String? {
        return if (response.getInt("code") == 200) {
            response.getJSONObject("result").getJSONArray("data").getJSONObject(0).toString()
        } else null
    }

    fun getFileUrl(upload: Upload): String {
        return "${getConfigUrl("uploads")}${upload.name}.${upload.type}"
    }

    @SuppressLint("HardwareIds")
    fun getHeaders(): Map<String, String> {

        val hashMap = HashMap<String, String>()

        if (ls.getValueBoolean("is_login")) {
            hashMap["x-user-token"] = ls.getValueString("user_token")
            hashMap["x-user-id"] = ls.getValueString("user_id")
        }

        val android_id = Secure.getString(
            context.getContentResolver(),
            Secure.ANDROID_ID
        )

        hashMap["x-user-device"] = android_id
        return hashMap
    }

    companion object {
        private val TAG = "AppHelper"
    }

}