package `in`.hexcommand.asktoagri.data

import com.google.gson.annotations.SerializedName

data class SolutionData(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("user") var user: Int = 0,
    @SerializedName("title") var title: String = "",
    @SerializedName("type") var type: String = "",
    @SerializedName("body") var body: String = "",
    @SerializedName("file") var file: Int = 0,
    @SerializedName("crops") var crops: Int = 0,
    @SerializedName("category") var category: Int = 0,
    @SerializedName("district") var district: Int = 0,
    @SerializedName("common") var common: Int = 0,
    @SerializedName("tags") var tags: String = "",
    @SerializedName("createdAt") var createdAt: String = ""
)
