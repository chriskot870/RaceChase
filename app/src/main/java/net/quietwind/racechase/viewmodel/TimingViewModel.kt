package net.quietwind.racechase.viewmodel

import android.graphics.Color
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import androidx.annotation.ColorInt
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
import net.quietwind.racechase.ui.CONTROL_BUTTON
import net.quietwind.racechase.ui.*

const val STATUS_CLOCK_START = 0
const val STATUS_CLOCK_RUNNING = 1
const val STATUS_CLOCK_STOPPED = 2

class TimingViewModel(private val repository: RaceChaseRepository) : ViewModel() {

    object track {
        val length: Float = 1.5F
        val units: String = "miles"
        val type: String = "Oval"
    }

    data class ButtonProperties (val text: String, val color: Int)

    data class ButtonState(var state:Int , var base: Long, var propertiesByState: Array<ButtonProperties>)

    private object storage {
        val status = arrayOf(
            ButtonState(STATUS_CLOCK_START,
                0L,
                arrayOf(
                    ButtonProperties("START\n%s", Color.BLUE),
                    ButtonProperties("LAP\n%s", Color.BLUE),
                    ButtonProperties("HALTED\n%s", Color.GRAY)
                )
            ),
            ButtonState(STATUS_CLOCK_START,
                0L,
                arrayOf(
                    ButtonProperties("START\n%s", Color.BLUE),
                    ButtonProperties("LAP\n%s", Color.BLUE),
                    ButtonProperties("HALTED\n%s", Color.GRAY)
                )
            ),
            ButtonState(STATUS_CLOCK_START,
                0L,
                arrayOf(
                    ButtonProperties("START\nBOTH", Color.BLUE),
                    ButtonProperties("STOP\nBOTH", Color.RED),
                    ButtonProperties("RESET\nALL", Color.BLUE)
                )
            )
        )
        val reports: Array<String> = arrayOf(
            "Report 1",
            "Report 2"
        )
        var lastDiff: Long? = null
        var gain: Long? = null
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

    fun clockInit(timingBindings: UiTimingBindings) {
        val now: Long = SystemClock.elapsedRealtime()

        val clocks = timingBindings.chronometers
        val buttons = timingBindings.timingButtons
        val reports = timingBindings.lapReports
        val lapDifference = timingBindings.lapDifference
        val lapGain = timingBindings.lapGain

        val status = storage.status
        /*
         * Walk through the status buttons and set the values in the UI
         */
       for(button in arrayOf(CLOCK1_BUTTON, CLOCK2_BUTTON, CONTROL_BUTTON)) {
           if ( button != CONTROL_BUTTON ) {
               /*
                * Set the chronometers
                */
               when (status[button].state) {
                   STATUS_CLOCK_START -> {
                       /*
                        * In the start state we want the clock to be stopped and show zero
                        */
                       clocks[button].stop()
                       clocks[button].base = now
                       buttons[button].text =
                           status[button].propertiesByState[status[button].state].text.format(button+1)
                       buttons[button].setBackgroundColor(
                           status[button].propertiesByState[status[button].state].color)
                   }
                   STATUS_CLOCK_RUNNING -> {
                       /*
                        * In the running state we need to set the clock to running and set the base
                        * so it picks up where it left off
                        */
                       clocks[button].base = status[button].base
                       clocks[button].start()
                       buttons[button].text =
                           status[button].propertiesByState[status[button].state].text.format(button+1)
                       buttons[button].setBackgroundColor(
                           status[button].propertiesByState[status[button].state].color)
                   }
                   STATUS_CLOCK_STOPPED -> {
                       /*
                        * In the stopped state we want the clock to be stopped and the base set so we can
                        * see the final settings
                        */
                       clocks[button].stop()
                       clocks[button].base = status[button].base
                       buttons[button].text =
                           status[button].propertiesByState[status[button].state].text.format(button+1)
                       buttons[button].setBackgroundColor(
                           status[button].propertiesByState[status[button].state].color)
                   }
               }
               /*
                * Restore the reports
                */
               reports[button].text = storage.reports[button]

           } else {
               /*
                * for the control button just change the button text and color
                */
               buttons[button].text =
                   status[button].propertiesByState[status[button].state].text.format(button+1)
               buttons[button].setBackgroundColor(
                   status[button].propertiesByState[status[button].state].color)
           }
       }
        /*
         * Now set the lapdiff and lapGain
         */
        if ( storage.lastDiff == null ) {
            lapDifference.text = "Difference"
        } else {
            lapDifference.text = "%3.2f".format(
                (storage.lastDiff!!.toFloat()) / 1000.0F
            )
        }

        if ( storage.gain == null ) {
            lapGain.setTextColor(Color.BLACK)
            lapGain.text = "Gain"
        } else {
            if (storage.gain!! < 0) {
                lapGain.text =
                    "%3.2f".format((storage.gain!!.toFloat()) / 1000.0F)
                lapGain.setTextColor(Color.RED)
            } else {
                lapGain.text =
                    "+%3.2f".format((storage.gain!!.toFloat()) / 1000.0F)
                lapGain.setTextColor(Color.BLUE)
            }
        }

    }
    fun clockAction(timingBindings: UiTimingBindings, buttonId: Int) {
        val now: Long = SystemClock.elapsedRealtime()

        val clocks = timingBindings.chronometers
        val buttons = timingBindings.timingButtons
        val reports = timingBindings.lapReports
        val lapDifference = timingBindings.lapDifference
        val lapGain = timingBindings.lapGain

        val status = storage.status

        if ( buttonId != CONTROL_BUTTON ) {
            /*
             * Don't start any clocks or count any laps if control button is STOPPED
             */
            if ( status[CONTROL_BUTTON].state != STATUS_CLOCK_STOPPED ) {
                /*
                 * If the clock is in START state then go ahead and START it
                 */
                if (status[buttonId].state == STATUS_CLOCK_START) {
                    /*
                     * We only START clock2 is clock1 is already running.
                     * If we are here we know it is either clock1 or clock2
                     * So, we test clock1 first and go on through. We will
                     * only do the second part of the || if it's clock2.
                     * But we only want to START clock2 if clock1 is running
                     */
                    if ( buttonId == CLOCK1_BUTTON ||
                        status[CLOCK1_BUTTON].state == STATUS_CLOCK_RUNNING ) {
                        /*
                         * We ant to set local values and also update the buttons.
                         * The local values keep the clock and control states so if the
                         * UI has to restart, like when the device is rotated,  we can get
                         * things back to the state it was in.
                         */
                        status[buttonId].state = STATUS_CLOCK_RUNNING
                        status[buttonId].base = now
                        /*
                         * Set the chronometer to the proper runnign state
                         */
                        clocks[buttonId].base = status[buttonId].base
                        clocks[buttonId].start()
                        /*
                         * Now set the button properties to reflect the new state
                         */
                        buttons[buttonId].text =
                            status[buttonId].propertiesByState[status[buttonId].state].text.format(buttonId+1)
                        buttons[buttonId].setBackgroundColor(
                            status[buttonId].propertiesByState[status[buttonId].state].color)
                        /*
                         * If it is clock1 changing to running, then control moves to running also
                         */
                        if ( buttonId == CLOCK1_BUTTON ) {
                            status[CONTROL_BUTTON].state = STATUS_CLOCK_RUNNING
                            /*
                             * Now update the control button itself
                             */
                            buttons[CONTROL_BUTTON].text =
                                status[CONTROL_BUTTON].propertiesByState[status[CONTROL_BUTTON].state].text
                            buttons[CONTROL_BUTTON].setBackgroundColor(
                                status[CONTROL_BUTTON].propertiesByState[status[CONTROL_BUTTON].state].color)
                        }
                        /*
                         * If it's clock 2 we know clock 1 is already running so we need to set the difference field
                         */
                        if ( buttonId == CLOCK2_BUTTON ) {
                            /*
                             * Record this difference
                             */
                            storage.lastDiff = status[CLOCK2_BUTTON].base - status[CLOCK1_BUTTON].base
                            /*
                             * Update the lap difference text view
                             */
                            lapDifference.text = "%3.2f".format(
                                (storage.lastDiff!!.toFloat()) / 1000.0F)
                        }
                    }
                } else {
                    /*
                     * This clock is running, so we are tracking laps
                     * This means we want to show lap times and
                     * start another lap time. We start a new
                     * lap time by resetting the base to now
                     */
                    val interval: Long = now - status[buttonId].base
                    /*
                     * Now reset the Base time in the local status for the clock
                     * Then reset the base time in the chronometer
                     */
                    status[buttonId].base = now
                    clocks[buttonId].base = status[buttonId].base
                    /*
                     * Update the clock's report,but don't change anything in the button
                     */
                    storage.reports[buttonId] = "%3.2f\n%s".format(getSpeed(interval), speedUnits)
                    reports[buttonId].text = storage.reports[buttonId]

                    /*
                     * If it is the second clock then update the Difference and Gain
                     */
                    if ( buttonId == CLOCK2_BUTTON ) {
                        val currentDiff: Long =
                            status[CLOCK2_BUTTON].base - status[CLOCK1_BUTTON].base
                        if (storage.lastDiff != null) {
                            storage.gain = currentDiff - storage.lastDiff!!
                            if (storage.gain!! < 0) {
                                lapGain.text =
                                    "%3.2f".format((storage.gain!!.toFloat()) / 1000.0F)
                                lapGain.setTextColor(Color.RED)
                            } else {
                                lapGain.text =
                                    "+%3.2f".format((storage.gain!!.toFloat()) / 1000.0F)
                                lapGain.setTextColor(Color.BLUE)
                            }
                        }
                        storage.lastDiff = currentDiff
                        lapDifference.text = "%3.2f".format(
                            (storage.lastDiff!!.toFloat()) / 1000.0F )
                    }
                }
            }
        } else {
            /*
             * It was the simultaneous button
             */
            when (status[buttonId].state) {
                STATUS_CLOCK_START -> {
                    /*
                     * Change the state and base of both clocks
                     */
                    status[CLOCK1_BUTTON].state = STATUS_CLOCK_RUNNING
                    status[CLOCK1_BUTTON].base = now
                    status[CLOCK2_BUTTON].state = STATUS_CLOCK_RUNNING
                    status[CLOCK2_BUTTON].base = now
                    status[CONTROL_BUTTON].state = STATUS_CLOCK_RUNNING
                    /*
                     * Use the status to start the chronometers
                     */
                    clocks[CLOCK1_BUTTON].base = status[CLOCK1_BUTTON].base
                    clocks[CLOCK1_BUTTON].start()
                    clocks[CLOCK2_BUTTON].base = status[CLOCK2_BUTTON].base
                    clocks[CLOCK2_BUTTON].start()
                    /*
                     * Since they started at the same time set lastDiff to 0
                     */
                    storage.lastDiff = 0
                    lapDifference.text = "%3.2f".format(
                        (storage.lastDiff!!.toFloat()) / 1000.0F )
                    /*
                     * Now update all the necessary buttons' text and color
                     */
                    buttons[CLOCK1_BUTTON].text =
                        status[CLOCK1_BUTTON].propertiesByState[status[CLOCK1_BUTTON].state].text.format(CLOCK1_BUTTON+1)
                    buttons[CLOCK1_BUTTON].setBackgroundColor(
                        status[CLOCK1_BUTTON].propertiesByState[status[CLOCK1_BUTTON].state].color
                    )

                    buttons[CLOCK2_BUTTON].text =
                        status[CLOCK2_BUTTON].propertiesByState[status[CLOCK2_BUTTON].state].text.format(CLOCK2_BUTTON+1)
                    buttons[CLOCK2_BUTTON].setBackgroundColor(
                        status[CLOCK2_BUTTON].propertiesByState[status[CLOCK2_BUTTON].state].color
                    )

                    buttons[CONTROL_BUTTON].text =
                        status[CONTROL_BUTTON].propertiesByState[status[CONTROL_BUTTON].state].text
                    buttons[CONTROL_BUTTON].setBackgroundColor(
                        status[CONTROL_BUTTON].propertiesByState[status[CONTROL_BUTTON].state].color
                    )
                }
                STATUS_CLOCK_RUNNING -> {
                    /*
                     * Set all the clocks to stopped even is they aren't running
                     * WE don't reset the base so the data for the last leg still shows
                     */
                    status[CLOCK1_BUTTON].state = STATUS_CLOCK_STOPPED
                    clocks[CLOCK1_BUTTON].stop()
                    buttons[CLOCK1_BUTTON].text =
                        status[CLOCK1_BUTTON].propertiesByState[status[CLOCK1_BUTTON].state].text.format(CLOCK1_BUTTON+1)
                    buttons[CLOCK1_BUTTON].setBackgroundColor(
                        status[CLOCK1_BUTTON].propertiesByState[status[CLOCK1_BUTTON].state].color
                    )

                    status[CLOCK2_BUTTON].state = STATUS_CLOCK_STOPPED
                    clocks[CLOCK2_BUTTON].stop()
                    buttons[CLOCK2_BUTTON].text =
                        status[CLOCK2_BUTTON].propertiesByState[status[CLOCK2_BUTTON].state].text.format(CLOCK2_BUTTON+1)
                    buttons[CLOCK2_BUTTON].setBackgroundColor(
                        status[CLOCK2_BUTTON].propertiesByState[status[CLOCK2_BUTTON].state].color
                    )
                    /*
                     * Change the state of the CONTROl button and update the text and color
                     */
                    status[CONTROL_BUTTON].state = STATUS_CLOCK_STOPPED
                    buttons[CONTROL_BUTTON].text =
                        status[CONTROL_BUTTON].propertiesByState[status[CONTROL_BUTTON].state].text
                    buttons[CONTROL_BUTTON].setBackgroundColor(
                        status[CONTROL_BUTTON].propertiesByState[status[CONTROL_BUTTON].state].color
                    )
                }
                STATUS_CLOCK_STOPPED -> {
                    /*
                     * Change the state and base of both clocks
                     * and the control button
                     */
                    status[CLOCK1_BUTTON].state = STATUS_CLOCK_START
                    status[CLOCK1_BUTTON].base = now
                    status[CLOCK2_BUTTON].state = STATUS_CLOCK_START
                    status[CLOCK2_BUTTON].base = now
                    status[CONTROL_BUTTON].state = STATUS_CLOCK_START
                    /*
                     * Use the status to stop the chronometers
                     * We need to set the base to now to make chronometer show
                     * 00:00.
                     * The clocks are probably already stopped but there is no harm
                     * in stopping them again to be sure.
                     */
                    clocks[CLOCK1_BUTTON].stop()
                    clocks[CLOCK1_BUTTON].base = status[CLOCK1_BUTTON].base
                    clocks[CLOCK2_BUTTON].stop()
                    clocks[CLOCK2_BUTTON].base = status[CLOCK2_BUTTON].base
                    /*
                     * Now update all the necessary buttons' text and color
                     */
                    buttons[CLOCK1_BUTTON].text =
                        status[CLOCK1_BUTTON].propertiesByState[status[CLOCK1_BUTTON].state].text.format(CLOCK1_BUTTON+1)
                    buttons[CLOCK1_BUTTON].setBackgroundColor(
                        status[CLOCK1_BUTTON].propertiesByState[status[CLOCK1_BUTTON].state].color
                    )

                    buttons[CLOCK2_BUTTON].text =
                        status[CLOCK2_BUTTON].propertiesByState[status[CLOCK2_BUTTON].state].text.format(CLOCK2_BUTTON+1)
                    buttons[CLOCK2_BUTTON].setBackgroundColor(
                        status[CLOCK2_BUTTON].propertiesByState[status[CLOCK2_BUTTON].state].color
                    )

                    buttons[CONTROL_BUTTON].text =
                        status[CONTROL_BUTTON].propertiesByState[status[CONTROL_BUTTON].state].text
                    buttons[CONTROL_BUTTON].setBackgroundColor(
                        status[CONTROL_BUTTON].propertiesByState[status[CONTROL_BUTTON].state].color
                    )
                    /*
                     * Now clear lastDiff
                     * Then clear the reports and the difference and gain text fields
                     */
                    storage.lastDiff = null
                    lapDifference.text = "Difference"
                    storage.gain = null
                    lapGain.setTextColor(Color.BLACK)
                    lapGain.text = "Gain"
                    storage.reports[CLOCK1_BUTTON] = "Report 1"
                    reports[CLOCK1_BUTTON].text = storage.reports[CLOCK1_BUTTON]
                    storage.reports[CLOCK2_BUTTON] = "Report 2"
                    reports[CLOCK2_BUTTON].text = storage.reports[CLOCK2_BUTTON]
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