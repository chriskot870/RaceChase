package net.quietwind.racechase.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.quietwind.racechase.R
import net.quietwind.racechase.databinding.TimingFragmentBinding
import net.quietwind.racechase.viewmodel.TimingViewModel

class TimingFragment : Fragment() {
    private var _binding: TimingFragmentBinding? = null

    private val binding get() = _binding!!

    companion object {
        fun newInstance() = TimingFragment()
    }

    private lateinit var viewModel: TimingViewModel

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
        viewModel = ViewModelProvider(this).get(TimingViewModel::class.java)
        // TODO: Use the ViewModel
    }

}