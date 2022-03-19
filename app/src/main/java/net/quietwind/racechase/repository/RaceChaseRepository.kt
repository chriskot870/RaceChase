package net.quietwind.racechase.repository

import android.util.Log
import androidx.room.Database
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.quietwind.racechase.RaceChaseApplication
import net.quietwind.racechase.database.EntrantDatabase

class RaceChaseRepository(application: RaceChaseApplication) {

    private var entrantDatabase: EntrantDatabase = EntrantDatabase.getEntrantDatabase(application)

    companion object {
        @Volatile
        private var INSTANCE: RaceChaseRepository? = null

        fun getRepository(application: RaceChaseApplication): RaceChaseRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = RaceChaseRepository(application)
                INSTANCE = instance

                instance
            }
        }
    }

    suspend fun getAllEntrants(): Flow<List<EntrantIdentity>> = flow {
        val entrantIdentityList: MutableList<EntrantIdentity> = mutableListOf()
        Log.d("RaceChase", "In repository.getAllEntrants Calling Dao")
        val entrants = entrantDatabase.EntrantDao().getAllEntrants()
        Log.d("RaceChase","got %d entrants loading into entrantIdentityList".format(entrants.size))
        entrants.forEach { entrant ->
            entrantIdentityList.add(EntrantIdentity(
                entrant.id,
                entrant.carNumber,
                entrant.driver,
                entrant.sponsor,
                entrant.team,
                entrant.engine)
            )
        }
        entrantIdentityList.sortWith(EntrantIdentityByCarNumber)
        /*
         * Return a List not a MutableList
         */
        emit (entrantIdentityList)
    }
}

class EntrantIdentityByCarNumber() {
    companion object : Comparator<EntrantIdentity> {
        override fun compare(a:EntrantIdentity, b:EntrantIdentity): Int {
            /*
             * It is possible the car number isn't just digits. It's common
             * to have a backup car that has digits but ends in a 'T'. We
             * ignore that for now and assume all carNumbers have only digits.
             */
            return a.carNumber.toDouble()compareTo(b.carNumber.toDouble())
        }
    }
}