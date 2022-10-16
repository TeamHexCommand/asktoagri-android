package `in`.hexcommand.asktoagri.data

import com.google.gson.annotations.SerializedName

data class CategoryData(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("name") var name: String = ""
)
