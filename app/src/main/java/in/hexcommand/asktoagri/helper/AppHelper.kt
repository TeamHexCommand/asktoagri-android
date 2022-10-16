@file:OptIn(DelicateCoroutinesApi::class)

package `in`.hexcommand.asktoagri.helper

import `in`.hexcommand.asktoagri.R
import `in`.hexcommand.asktoagri.data.ArticalData
import `in`.hexcommand.asktoagri.database.ArticalDatabase
import `in`.hexcommand.asktoagri.model.AddressModel
import `in`.hexcommand.asktoagri.model.User
import `in`.hexcommand.asktoagri.util.shared.LocalStorage
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Base64
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
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
            return context.getString(R.string.api_url).decode()
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
        GlobalScope.launch(Dispatchers.IO) {
            val res =
                async { ApiHelper(context).sendGetRequest(url.decode()) }
            ls.save("remote_config", res.await())
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
            val data =
                JSONObject(async { ApiHelper(context).getTrendingArtical() }.await())
                    .getJSONObject("result").getJSONArray("data")

            (0 until data.length()).forEach { i ->
                val articalData =
                    Gson().fromJson(data.getJSONObject(i).toString(), ArticalData::class.java)
                articalDb.insert(articalData)
            }
        }
    }

    fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        ls.clearSharedPreference()
    }

    companion object {
        private val TAG = "AppHelper"
    }

}