package com.macreai.projectkp.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.macreai.projectkp.model.local.UserPreference
import com.macreai.projectkp.repo.AuthRepository

object Injection {
    fun provideRepository(preference: UserPreference): AuthRepository{
        return AuthRepository(preference)
    }

    fun provideUserPreference(dataStore: DataStore<Preferences>): UserPreference{
        return UserPreference.getInstance(
            dataStore
        )
    }
}