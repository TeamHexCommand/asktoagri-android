package `in`.hexcommand.asktoagri.dao

import `in`.hexcommand.asktoagri.data.ArticalData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticalDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(articalData: ArticalData)

    @Update
    fun update(articalData: ArticalData)

    @Delete
    fun deleteArtical(articalData: ArticalData)

    @Query("select * from artical_table order by id desc")
    fun getAllArtical(): Flow<List<ArticalData>>
}