package `in`.hexcommand.asktoagri.data

import com.google.gson.annotations.SerializedName

data class StateData(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("stateName") var stateName: String = ""
)