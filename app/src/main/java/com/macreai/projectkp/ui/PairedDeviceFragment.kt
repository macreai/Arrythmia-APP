package com.macreai.projectkp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.macreai.projectkp.adapter.PairedDeviceAdapter
import com.macreai.projectkp.databinding.FragmentPairedDeviceBinding


class PairedDeviceFragment : Fragment() {

    private var _binding: FragmentPairedDeviceBinding? = null
    private val binding get() = _binding!!

    private lateinit var pairedDeviceAdapter: PairedDeviceAdapter
    // private val pairedDevices: MutableList<BluetoothDevice> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPairedDeviceBinding.inflate(inflater, container, false)
        val view = binding.root

        val pairedDevice = BleManager.getInstance().allConnectedDevice
        pairedDeviceAdapter = PairedDeviceAdapter(pairedDevice)
        binding.rvPairedDevice.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pairedDeviceAdapter
        }
        pairedDeviceAdapter.setOnClickListener(object : PairedDeviceAdapter.OnClickListener{
            override fun onClick(data: BleDevice) {
                BleManager.getInstance().disconnect(data)
                pairedDeviceAdapter.removeDevice(data)
                Toast.makeText(requireContext(), "Device Disconnected", Toast.LENGTH_SHORT).show()
//                if (BleManager.getInstance().isConnected(data)){
//                    Toast.makeText(requireContext(), "Failed to Disconnect", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(requireContext(), "Device Disconnected", Toast.LENGTH_SHORT).show()
//                }
            }

        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}