package net.quietwind.racechase.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Entrant::class],
    version = 1,
    exportSchema = false
)
abstract class EntrantDatabase: RoomDatabase() {

    abstract fun EntrantDao(): EntrantDao

    companion object {
        @Volatile
        private var INSTANCE: EntrantDatabase? = null

        fun getEntrantDatabase(context: Context): EntrantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    EntrantDatabase::class.java,
                    "entrant")
                    .createFromAsset("database/entrants.db")
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}