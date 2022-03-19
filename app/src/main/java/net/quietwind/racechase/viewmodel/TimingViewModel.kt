package net.quietwind.racechase.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.quietwind.racechase.repository.RaceChaseRepository
import net.quietwind.racechase.ui.EntrantAdapter

class TimingViewModel(private val repository: RaceChaseRepository) : ViewModel() {

    fun loadEntrantList(entrantAdapter: EntrantAdapter) {
        /*
         * We are going to do some I/O so launch a coroutine to do the work
         */
        Log.d("RaceChase", "In loadEntrantList launching coroutine to getAllEntrants")
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllEntrants().collect {
                /*
                 * We need to go into the main thread in order to update the symbolAdapter List
                 */
                withContext(Dispatchers.Main) {
                    Log.d("RaceChase", "ViewModel got %d Entries Submitting to adapter now".format(it.size))
                    it.forEach { entrant ->
                        Log.d("RaceChase", "\t%s".format(entrant.toString()))
                    }
                    Log.d("RaceChase", "Adapter current list size before submitted %d".format(entrantAdapter.currentList.size))
                    entrantAdapter.submitList(it)
                    Log.d("RaceChase", "Adapter current list size after submitted %d".format(entrantAdapter.currentList.size))
                }
            }
        }
    }
}

class TimingViewModelFactory(
    private val repository: RaceChaseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}