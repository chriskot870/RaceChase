package net.quietwind.racechase.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Entrants")
data class Entrant(
    /*
     * The definitions and types here should match the database Schema for the Category table
     */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "Id") val id: Int,
    @NonNull @ColumnInfo(name = "CarNumber") val carNumber: String,
    @NonNull @ColumnInfo(name = "Driver") val driver: String,
    @NonNull @ColumnInfo(name = "Sponsor") val sponsor: String,
    @NonNull @ColumnInfo(name = "Team") val team: String,
    @NonNull @ColumnInfo(name = "Engine") val engine: String
)