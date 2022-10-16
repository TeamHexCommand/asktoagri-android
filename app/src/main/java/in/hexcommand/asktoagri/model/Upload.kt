package `in`.hexcommand.asktoagri.model

import com.google.gson.annotations.SerializedName

data class Upload(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("user") var user: Int = 0,
    @SerializedName("name") var name: String = "",
    @SerializedName("type") var type: String = "",
    @SerializedName("createdAt") var createdAt: String = ""
)