package `in`.hexcommand.asktoagri.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("firstName") var firstName: String = "",
    @SerializedName("lastName") var lastName: String = "",
    @SerializedName("email") var email: String = "",
    @SerializedName("password") var password: String = "",
    @SerializedName("firebaseId") var firebaseID: String = "",
    @SerializedName("defaultFcm") var defaultFcm: String = "",
    @SerializedName("mobile") var mobile: String = "",
    @SerializedName("isAdmin") var isAdmin: Int = 0,
    @SerializedName("isExpert") var isExpert: Int = 0,
    @SerializedName("isBanned") var isBanned: Int = 0,
    @SerializedName("longitude") var longitude: String = "",
    @SerializedName("latitude") var latitude: String = "",
    @SerializedName("defaultLang") var defaultLang: Int = 0,
    @SerializedName("city") var city: Int = 0,
    @SerializedName("createdAt") var createdAt: String = ""
)