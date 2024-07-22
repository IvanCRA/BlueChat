package com.example.bluechat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.bluechat.databinding.FragmentControlBinding

class ControlFragment : Fragment() {
    private lateinit var viewModel: ControlViewModel
    private var _binding: FragmentControlBinding? = null
    private val binding: FragmentControlBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory = ControlViewModelFactory((requireActivity().application as App).adapterProvider)
        viewModel = ViewModelProvider(this, factory).get(ControlViewModel::class.java)
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ledFirst.setOnCheckedChangeListener { _, checked ->
            viewModel.enableLed(Led.FIRST, checked)
        }

        binding.ledSecond.setOnCheckedChangeListener { _, checked ->
            viewModel.enableLed(Led.SECOND, checked)
        }

    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnect()
    }

    override fun onResume() {
        super.onResume()

        val deviceAddress = requireArguments().getString(KEY_DEVICE_ADDRESS)!!
        viewModel.connect(deviceAddress)
    }

    companion object {

        private const val KEY_DEVICE_ADDRESS = "key_device_address"
        @JvmStatic
        fun newInstance(deviceAddress: String) = ControlFragment().apply {
            arguments = Bundle().apply {
                putString(KEY_DEVICE_ADDRESS, deviceAddress)
            }
        }
    }
}