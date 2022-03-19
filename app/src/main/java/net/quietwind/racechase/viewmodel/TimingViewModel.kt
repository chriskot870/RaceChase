package net.quietwind.racechase.viewmodel

import android.graphics.Color
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.quietwind.racechase.databinding.TimingFragmentBinding
import net.quietwind.racechase.repository.RaceChaseRepository
import net.quietwind.racechase.ui.CLOCK1_BUTTON
import net.quietwind.racechase.ui.CLOCK2_BUTTON
import net.quietwind.racechase.ui.EntrantAdapter
import net.quietwind.racechase.ui.SIMULTANEOUS_BUTTON

class TimingViewModel(private val repository: RaceChaseRepository) : ViewModel() {

    val running: Array<Boolean> = arrayOf(false, false)
    val lastBase: Array<Long> = arrayOf(0,0)
    var lastDiff: Long? = null;
    object track {
        val length: Float = 1.5F
        val units: String = "miles"
        val type: String = "Oval"
    }

    val speedUnits: String = "mph"

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
    fun clockAction(binding: TimingFragmentBinding, button: Int) {
        val now: Long = SystemClock.elapsedRealtime()

        if ( button != SIMULTANEOUS_BUTTON ) {
            /*
             * Don't start any clocks or count any laps if simultaneous button is in RESET\nBOTH state
             */
            if ( binding.simultaneousStartStopButton.text != "RESET\nBOTH" ) {
                if (!running[button]) {
                    /*
                     * If it's one of the clock buttons
                     * Start it
                     * Change the button's text
                     * Change the simultaneous button's text
                     * (We go ahead and just reset the simultaneous test whether it's already set or not)
                     */
                    when (button) {
                        CLOCK1_BUTTON -> {
                            /*
                         * Set the clocks base to now so it starts counting from now
                         */
                            binding.lapClock1.base = now
                            binding.lapClock1.start()
                            lastBase[button] = now
                            binding.lapButton1.text = "LAP\n1"
                            binding.simultaneousStartStopButton.text = "STOP\nBOTH"
                            running[button] = true
                        }
                        CLOCK2_BUTTON -> {
                            /*
                         * Only start clock2 if clock1 is running
                         */
                            if (running[CLOCK1_BUTTON]) {
                                binding.lapClock2.base = now
                                binding.lapClock2.start()
                                lastBase[button] = now
                                binding.lapButton2.text = "LAP\n2"
                                binding.simultaneousStartStopButton.text = "STOP\nBOTH"
                                running[button] = true
                                lastDiff = null
                            }
                        }
                    }
                } else {
                    /*
                 * This clock is running, so we are tracking laps
                 * This means we want to show lap times and
                 * start another lap time. We start a new
                 * lap time by resetting the base to now
                 */
                    when (button) {
                        CLOCK1_BUTTON -> {
                            /*
                         * Find out how long the clock has been running since last changing the base
                         */
                            val interval: Long = now - binding.lapClock1.base
                            /*
                         * Now reset the Base time to get the next lap split
                         */
                            binding.lapClock1.base = now
                            lastBase[button] = now
                            /*
                         * Now Display the speed
                         */
                            binding.lapReport1.text =
                                "%3.2f\n%s".format(getSpeed(interval), speedUnits)
                        }
                        CLOCK2_BUTTON -> {
                            /*
                         * Find out how long the clock has been running since last changing the base
                         */
                            val interval: Long = now - binding.lapClock2.base
                            /*
                         * Now reset the Base time to get the next lap split
                         */
                            binding.lapClock2.base = now
                            lastBase[button] = now
                            /*
                         * Now Display the speed
                         */
                            binding.lapReport2.text =
                                "%3.2f\n%s".format(getSpeed(interval), speedUnits)
                            val currentDiff: Long = now - binding.lapClock1.base
                            if (lastDiff != null) {
                                val gain: Long = currentDiff - lastDiff!!
                                if (gain < 0) {
                                    binding.lapGain.text =
                                        "%3.2f".format((gain.toFloat()) / 1000.0F)
                                    binding.lapGain.setTextColor(Color.RED)
                                } else {
                                    binding.lapGain.text =
                                        "+%3.2f".format((gain.toFloat()) / 1000.0F)
                                    binding.lapGain.setTextColor(Color.BLUE)
                                }
                            }
                            lastDiff = currentDiff
                            binding.lapDifference.text = "%3.2f".format(
                                (lastDiff!!.toFloat()) / 1000.0F
                            )
                        }
                    }
                }
            }
        } else {
            /*
             * It was the simultaneous button
             */

            when(binding.simultaneousStartStopButton.text) {
                "START\nBOTH" -> {
                    binding.lapClock1.base = now
                    binding.lapClock1.start()
                    binding.lapButton1.text = "LAP\n1"
                    binding.lapClock2.base = now
                    binding.lapClock2.start()
                    binding.lapButton2.text = "LAP\n2"
                    binding.simultaneousStartStopButton.text = "STOP\nBOTH"

                }
                "STOP\nBOTH" -> {
                    if (running[CLOCK1_BUTTON]) {
                        binding.lapClock1.stop()
                        running[CLOCK1_BUTTON] = false
                    }
                    if (running[CLOCK2_BUTTON]) {
                        binding.lapClock2.stop()
                        running[CLOCK2_BUTTON] = false
                    }
                    binding.simultaneousStartStopButton.text = "RESET\nBOTH"
                }
                "RESET\nBOTH" -> {
                    binding.lapClock1.base = now
                    binding.lapClock1.stop()
                    running[CLOCK1_BUTTON] = false
                    binding.lapClock2.base = now
                    binding.lapClock2.stop()
                    running[CLOCK2_BUTTON] = false
                    binding.lapButton1.text = "START\n1"
                    binding.lapButton2.text = "START\n2"
                    binding.lapReport1.text = "Report 1"
                    binding.lapReport2.text = "Report 2"
                    binding.simultaneousStartStopButton.text = "START\nBOTH"
                    binding.lapDifference.text = "Lap Diff"
                    binding.lapGain.text = "Lap Gain"
                    binding.lapGain.setTextColor(Color.BLACK)
                    lastDiff = 0
                }

            }
        }
    }
    private fun getSpeed(interval:Long): Float {
        /*
         * the interval is from SystemClock.elapsedRealTime() which is in milleseconds.
         * We have to convert this to a speed based on the track length units and the speedUnits
         */
        var speed: Float = 0.0F

        when(TimingViewModel.track.units) {
            "miles" -> {
                when(speedUnits) {
                    "mph" -> {
                        /*
                         * length is in miles and speed units is mile per hour
                         * There are 60*60*1000 milliseconds per hour
                         */
                        speed = (TimingViewModel.track.length * (60*60*1000))/interval
                    }
                }
            }
        }
        return(speed)
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