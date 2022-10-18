package `in`.hexcommand.asktoagri.data

import com.google.gson.annotations.SerializedName

data class DistrictData(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("name") var name: String = "",
    @SerializedName("state") var state: Int = 0,
    @SerializedName("stateName") var stateName: String = ""
)
