package com.macreai.projectkp.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.data.BleDevice
import com.macreai.projectkp.R
import com.macreai.projectkp.databinding.ItemDeviceBinding

@SuppressLint("MissingPermission")
class PairedDeviceAdapter(private val devices: MutableList<BleDevice>): RecyclerView.Adapter<PairedDeviceAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    interface OnClickListener {
        fun onClick(data: BleDevice)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    fun removeDevice(device: BleDevice) {
        val position = devices.indexOf(device)
        if (position != -1) {
            devices.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class ViewHolder(private val binding: ItemDeviceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(device: BleDevice){
            binding.tvDeviceName.text = device.name ?: "Unknown Device"
            binding.tvDeviceId.text = device.mac
            binding.vector.setImageResource(R.drawable.ic_close)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun getItemCount(): Int = devices.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position])

        holder.itemView.setOnClickListener {
            onClickListener?.onClick(devices[holder.adapterPosition])
        }
    }
}