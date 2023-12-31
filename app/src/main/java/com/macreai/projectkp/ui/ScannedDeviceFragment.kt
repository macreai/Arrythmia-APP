package com.macreai.projectkp.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.macreai.projectkp.adapter.ScanDeviceAdapter
import com.macreai.projectkp.dataStore
import com.macreai.projectkp.databinding.FragmentScannedDeviceBinding
import com.macreai.projectkp.viewmodel.AppViewModel
import com.macreai.projectkp.viewmodel.DeviceViewModel
import com.macreai.projectkp.viewmodel.ViewModelFactory
import java.util.*


@SuppressLint("MissingPermission")
class ScannedDeviceFragment : Fragment()  {

    private var _binding: FragmentScannedDeviceBinding? = null
    private val binding get() = _binding!!

    private lateinit var rotateAnimator: ObjectAnimator
    private lateinit var scanDeviceAdapter: ScanDeviceAdapter
    private val scannedDevices: List<BleDevice> = mutableListOf()

    private var isScanning = false

    private val viewModel: DeviceViewModel by viewModels { ViewModelFactory(scanDeviceAdapter) }
    private val appViewModel: AppViewModel by activityViewModels { ViewModelFactory(requireContext().dataStore) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannedDeviceBinding.inflate(inflater, container, false)
        val view = binding.root

        rotateAnimator = ObjectAnimator.ofFloat(binding.btnScan, "rotation", 0f, 360f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
        }

        binding.btnScan.setOnClickListener {
            if (isScanning) {
                rotateAnimator.pause()
                BleManager.getInstance().cancelScan()
                Toast.makeText(requireContext(), "Cancel Scan", Toast.LENGTH_SHORT).show()
                binding.btnScan.visibility = View.GONE
            } else if (!isScanning) {
                rotateAnimator.start()
                viewModel.startScan()
            }
            isScanning = !isScanning
        }

        scanDeviceAdapter = ScanDeviceAdapter(scannedDevices)
        binding.rvScannedDevice.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scanDeviceAdapter
        }
        scanDeviceAdapter.setOnClickListener(object : ScanDeviceAdapter.OnClickListener{
            override fun onClick(data: BleDevice) {
                connectDevice(data)
            }
        })

        return view
    }

    private fun connectDevice(device: BleDevice){
        BleManager.getInstance().connect(device, object : BleGattCallback(){
            override fun onStartConnect() {
                showLoading(true)
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                showLoading(false)
                Toast.makeText(requireContext(), "Failed to connect", Toast.LENGTH_SHORT).show()
            }

            override fun onConnectSuccess(
                bleDevice: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                showLoading(false)
                Toast.makeText(requireContext(), "Connected!", Toast.LENGTH_SHORT).show()
                appViewModel.setConnectedDevice(device)
                findNavController().popBackStack()
                // startNotifyData(device, "f50e45b0-eda3-4a90-8f5a-83172d9f5365", "beb5483e-36e1-4688-b7f5-ea07361b26a8")
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {

            }

        })
    }

    private fun showLoading(isLoading: Boolean){
        if (isLoading){
            binding.loading.visibility = View.VISIBLE
        } else {
            binding.loading.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isScanning)
            BleManager.getInstance().cancelScan()
        _binding = null
    }
}