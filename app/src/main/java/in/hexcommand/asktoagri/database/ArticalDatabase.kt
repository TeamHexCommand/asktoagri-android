package `in`.hexcommand.asktoagri.database

import `in`.hexcommand.asktoagri.dao.ArticalDao
import `in`.hexcommand.asktoagri.data.ArticalData
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ArticalData::class], version = 1, exportSchema = true)
abstract class ArticalDatabase : RoomDatabase() {
    abstract fun articalDao(): ArticalDao

    companion object {
        private var instance: ArticalDatabase? = null

        @Volatile
        private var INSTANCE: ArticalDatabase? = null

        fun getDatabase(context: Context): ArticalDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            if (INSTANCE == null) {
                synchronized(this) {
                    // Pass the database to the INSTANCE
                    INSTANCE = buildDatabase(context)
                }
            }
            // Return database.
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): ArticalDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ArticalDatabase::class.java,
                "artical_database"
            ).build()
        }

        @Synchronized
        fun getInstance(ctx: Context): ArticalDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, ArticalDatabase::class.java,
                    "artical_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build()

            return instance!!

        }

        private val roomCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                populateDatabase(instance!!)
            }
        }

        private fun populateDatabase(db: ArticalDatabase) {
            val articalDao = db.articalDao()
        }
    }
}