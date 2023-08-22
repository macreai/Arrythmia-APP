package com.macreai.projectkp.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.macreai.projectkp.adapter.ScanDeviceAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("NotifyDataSetChanged")
class DeviceViewModel(private val scanDeviceAdapter: ScanDeviceAdapter): ViewModel() {

    fun startScan(){
        CoroutineScope(Dispatchers.Default).launch {
            BleManager.getInstance().scan(object : BleScanCallback() {
                override fun onScanStarted(success: Boolean) {
                    scanDeviceAdapter.clearScanDevice()
                    scanDeviceAdapter.notifyDataSetChanged()
                    // isScanning = true
                }

                override fun onScanning(bleDevice: BleDevice?) {
                    scanDeviceAdapter.addDevice(bleDevice!!)
                    scanDeviceAdapter.notifyDataSetChanged()
                    // isScanning = true
                }

                override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                    // Toast.makeText(requireContext(), "Scan Finish", Toast.LENGTH_SHORT).show()
                    // isScanning = false
                }

            })
        }
    }
}