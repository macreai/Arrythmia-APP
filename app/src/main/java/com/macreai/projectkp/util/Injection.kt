package com.macreai.projectkp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.macreai.projectkp.model.local.UserPreference

object Injection {
//    fun provideRepository(preference: UserPreference): AuthRepository{
//        return AuthRepository(preference)
//    }

    fun provideUserPreference(dataStore: DataStore<Preferences>): UserPreference{
        return UserPreference.getInstance(
            dataStore
        )
    }
}