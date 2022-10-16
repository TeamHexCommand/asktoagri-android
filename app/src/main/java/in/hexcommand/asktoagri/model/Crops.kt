package `in`.hexcommand.asktoagri.model

import com.google.gson.annotations.SerializedName

data class Crops(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("name") var name: String = "",
    @SerializedName("type") var type: String = "",
    @SerializedName("image") var image: Int = 0
)
