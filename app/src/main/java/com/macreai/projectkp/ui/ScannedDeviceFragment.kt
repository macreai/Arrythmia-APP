package com.macreai.projectkp.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.*
import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat.getDrawable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.macreai.projectkp.R
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
    ): View? {
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
                Toast.makeText(requireContext(), "Initiating..", Toast.LENGTH_SHORT).show()
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                Toast.makeText(requireContext(), "Failed to connect", Toast.LENGTH_SHORT).show()
            }

            override fun onConnectSuccess(
                bleDevice: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2
        private const val LIST_NAME = "NAME"
        private const val LIST_UUID = "uuid"
        const val EXTRA_DEVICE = "extra_device"
        private const val TAG = "ScannedDeviceFragment"
    }
}