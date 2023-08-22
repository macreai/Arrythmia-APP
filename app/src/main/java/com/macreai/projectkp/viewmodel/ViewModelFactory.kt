package com.macreai.projectkp.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.macreai.projectkp.adapter.ScanDeviceAdapter
import com.macreai.projectkp.util.Injection

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val param: Any): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(DeviceViewModel::class.java)){
            return DeviceViewModel(param as ScanDeviceAdapter) as T
        }
        if (modelClass.isAssignableFrom(AppViewModel::class.java)){
            return AppViewModel(Injection.provideUserPreference(param as DataStore<Preferences>)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}