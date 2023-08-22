package com.macreai.projectkp.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.macreai.projectkp.ui.PairedDeviceFragment
import com.macreai.projectkp.ui.ScannedDeviceFragment

class DeviceAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = ScannedDeviceFragment()
            1 -> fragment = PairedDeviceFragment()
        }
        return fragment as Fragment
    }
}