package com.macreai.projectkp.repo

import android.util.Log
import com.macreai.projectkp.model.local.UserPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor

class AuthRepository(private val pref: UserPreference): Interceptor {
    /*
    private val userToken = runBlocking {
        pref.getUser()
            .map { value: UserDataStore -> value.token }
            .first()
    }

    private val userId = runBlocking {
        pref.getUser()
            .map { value: UserDataStore -> value.id }
            .first()
    }
    */

    suspend fun getUserId(): Int{
        return withContext(Dispatchers.IO){
            val user = pref.getUserId().first()
            user
        }
    }

    private suspend fun getUserToken(): String{
        return withContext(Dispatchers.IO){
            val user = pref.getUserToken().first()
            user
        }
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val token = runBlocking {
            getUserToken()
        }
        Log.d(TAG, "intercept: $token")
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }

    companion object {
        private const val TAG = "authRepository"
    }
}