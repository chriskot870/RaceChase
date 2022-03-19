package net.quietwind.racechase.database

import androidx.room.Dao
import androidx.room.Query

@Dao
interface EntrantDao {

    @Query("SELECT * FROM Entrants")
    suspend fun getAllEntrants(): List<Entrant>
}