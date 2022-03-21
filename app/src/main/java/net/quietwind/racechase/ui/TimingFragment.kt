package net.quietwind.racechase.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.quietwind.racechase.R
import net.quietwind.racechase.RaceChaseApplication
import net.quietwind.racechase.databinding.TimingFragmentBinding
import net.quietwind.racechase.viewmodel.TimingViewModel
import net.quietwind.racechase.viewmodel.TimingViewModelFactory

const val CLOCK1_BUTTON = 0
const val CLOCK2_BUTTON = 1
const val CONTROL_BUTTON = 2

class TimingFragment : Fragment() {
    private var _binding: TimingFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var entrantView: RecyclerView

    private lateinit var clock1: Chronometer
    private lateinit var clock2: Chronometer

    private lateinit var timingBindings: UiTimingBindings

    private var running: Array<Boolean> = arrayOf(false, false)

    private val viewModel: TimingViewModel by activityViewModels {
        TimingViewModelFactory(
            (activity?.application as RaceChaseApplication).repository
        )
    }

    companion object {
        fun newInstance(): Fragment {
            Log.d("RaceChaseMain", "In TimingFrame.newInstance")
            return(TimingFragment())
        }
    }

    //private lateinit var viewModel: TimingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("RaceChaseMain", "Called TimingFragment onCreateView")
        _binding = TimingFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        /*
         * Put the chronometers into an array
         */
        timingBindings = UiTimingBindings(
            arrayOf(
                binding.lapClock1,
                binding.lapClock2),
            arrayOf(
                binding.lapButton1,
                binding.lapButton2,
                binding.simultaneousStartStopButton),
            arrayOf(
                binding.lapReport1,
                binding.lapReport2),
            binding.lapDifference,
            binding.lapGain
        )

        /*
         * set OnClickListener's for the timing buttons
         * We do the work down in the ViewModel
         */
        binding.lapButton1.setOnClickListener {
            viewModel.clockAction(timingBindings, CLOCK1_BUTTON)
        }
        binding.lapButton2.setOnClickListener {
            viewModel.clockAction(timingBindings, CLOCK2_BUTTON)
        }
        binding.simultaneousStartStopButton.setOnClickListener {
            viewModel.clockAction(timingBindings, CONTROL_BUTTON)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val viewModel = ViewModelProvider(this).get(TimingViewModel::class.java)

        /*
         * Get the Entrants recyclerView
         */
        entrantView = binding.recyclerEntrants
        /*
         * Set the recyclerView Layout manager
         */
        entrantView.layoutManager = LinearLayoutManager(requireContext())
        /*
         * Now set the Entrant RecyclerView Adapter
         */
        val entrantAdapter = EntrantAdapter()
        entrantView.adapter = entrantAdapter
        /*
         * Now Load the entrant List
         */
        Log.d("RaceChase", "Loading Entrant List")
        viewModel.loadEntrantList(entrantAdapter)
        Log.d("RaceChase", "Returned From Loading Entrant List")

        /*
         * Now initialize the Timing Info
         */
        viewModel.clockInit(timingBindings)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
data class UiTimingBindings(
    val chronometers: Array<Chronometer>,
    val timingButtons: Array<Button>,
    val lapReports: Array<TextView>,
    val lapDifference: TextView,
    val lapGain: TextView
)