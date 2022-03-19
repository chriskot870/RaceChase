package net.quietwind.racechase.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.quietwind.racechase.R
import net.quietwind.racechase.RaceChaseApplication
import net.quietwind.racechase.databinding.TimingFragmentBinding
import net.quietwind.racechase.viewmodel.TimingViewModel
import net.quietwind.racechase.viewmodel.TimingViewModelFactory

class TimingFragment : Fragment() {
    private var _binding: TimingFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var entrantView: RecyclerView

    private val viewModel: TimingViewModel by activityViewModels {
        TimingViewModelFactory(
            (activity?.application as RaceChaseApplication).repository
        )
    }

    companion object {
        fun newInstance() = TimingFragment()
    }

    //private lateinit var viewModel: TimingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TimingFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}