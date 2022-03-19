package net.quietwind.racechase

import android.app.Application
import net.quietwind.racechase.repository.RaceChaseRepository

class RaceChaseApplication : Application() {

    val repository: RaceChaseRepository by lazy { RaceChaseRepository.getRepository(this)}
}