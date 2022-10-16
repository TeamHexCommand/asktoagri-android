package `in`.hexcommand.asktoagri.data

import com.google.gson.annotations.SerializedName

data class ConfigData(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("user") var user: Int = 0,
    @SerializedName("name") var name: String = "",
    @SerializedName("value") var value: String = "",
    @SerializedName("createdAt") var createdAt: String = ""
)
