package `in`.hexcommand.asktoagri.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "artical_table")
data class ArticalData(
    @PrimaryKey(autoGenerate = true) @SerializedName("id") var id: Int = 0,
    @SerializedName("user") var user: Int = 0,
    @SerializedName("title") var name: String = "",
    @SerializedName("category") var category: Int = 0,
    @SerializedName("tags") var tags: String = "",
    @SerializedName("image") var image: Int = 0,
    @SerializedName("body") var body: String = "",
    @SerializedName("createdAt") var createdAt: String = ""
)
