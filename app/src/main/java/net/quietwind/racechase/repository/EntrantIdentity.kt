package net.quietwind.racechase.repository

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EntrantIdentity(
    val id: Int,
    val carNumber: String,
    val driver: String,
    val sponsor: String,
    val team: String,
    val engine: String
): Parcelable {
    override fun toString()   = "%3s %-25s".format(carNumber,driver)
}
