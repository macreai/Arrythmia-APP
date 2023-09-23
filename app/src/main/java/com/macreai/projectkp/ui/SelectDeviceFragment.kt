package com.macreai.projectkp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.macreai.projectkp.adapter.DeviceAdapter
import com.macreai.projectkp.databinding.FragmentSelectDeviceBinding

class SelectDeviceFragment : Fragment() {

    private var _binding: FragmentSelectDeviceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectDeviceBinding.inflate(inflater, container, false)
        val view = binding.root

        val adapter = DeviceAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager){ tab, position ->
            tab.text = when (position){
                0 -> "Scanned Device"
                1 -> "Paired Device"
                else -> null
            }
        }.attach()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}