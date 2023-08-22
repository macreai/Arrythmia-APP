package com.macreai.projectkp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.macreai.projectkp.databinding.ItemDeviceBinding


@SuppressLint("MissingPermission")
class ScanDeviceAdapter(private val devices: List<BleDevice>): RecyclerView.Adapter<ScanDeviceAdapter.ViewHolder>() {

    private val mutableDevice: MutableList<BleDevice> = devices.toMutableList()

    private var onClickListener: OnClickListener? = null

    interface OnClickListener {
        fun onClick(data: BleDevice)
    }

    class ViewHolder(private val binding: ItemDeviceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(device: BleDevice){
            binding.tvDeviceName.text = device.name ?: "Unknown Device"
            binding.tvDeviceId.text = device.mac
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun getItemCount(): Int = mutableDevice.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mutableDevice[position])

        holder.itemView.setOnClickListener {
            onClickListener?.onClick(mutableDevice[holder.adapterPosition])
        }
    }

    fun addDevice(bleDevice: BleDevice) {
        removeDevice(bleDevice)
        mutableDevice.add(bleDevice)
    }

    fun removeDevice(bleDevice: BleDevice) {
        for (i in 0 until mutableDevice.size) {
            val device: BleDevice = mutableDevice[i]
            if (bleDevice.key == device.key) {
                mutableDevice.removeAt(i)
            }
        }
    }

    fun clearConnectedDevice() {
        for (i in 0 until mutableDevice.size) {
            val device: BleDevice = mutableDevice[i]
            if (BleManager.getInstance().isConnected(device)) {
                mutableDevice.removeAt(i)
            }
        }
    }

    fun clearScanDevice() {
        for (i in 0 until mutableDevice.size) {
            val device: BleDevice = mutableDevice[i]
            if (!BleManager.getInstance().isConnected(device)) {
                mutableDevice.removeAt(i)
            }
        }
    }

    fun clear() {
        clearConnectedDevice()
        clearScanDevice()
    }

}