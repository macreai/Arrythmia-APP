package com.macreai.projectkp.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.clj.fastble.data.BleDevice
import com.macreai.projectkp.model.local.UserPreference
import com.macreai.projectkp.model.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.Flow

class AppViewModel(private val pref: UserPreference): ViewModel() {

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _connectedDevice = MutableLiveData<BleDevice>()
    val connectedDevice: LiveData<BleDevice> = _connectedDevice

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    fun setConnectedDevice(device: BleDevice){
        _connectedDevice.value = device
        Log.d(TAG, "setConnectedDevice: ${_connectedDevice.value}")
    }

    fun setUser(user: User?){
        _user.value = user
        Log.d(TAG, "setUser: ${user?.id}")
    }

    fun getLogin(): LiveData<String> = pref.getUserToken().asLiveData()

    fun getLoginId(): LiveData<Int> = pref.getUserId().asLiveData()


    fun getDetailUser(){
        ApiConfig.getApiService(pref).getDetailUser()
            .enqueue(object: Callback<User>{
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful){
                        val user = response.body()
                        setUser(user)
                    } else {
                        Log.d(TAG, "getDetailUser: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message}")
                }

            })
    }

    fun patientRegister(
        username: String,
        password: String,
        address: String,
        phone: String,
        emergencyPhone: String,
        age: String,
        gender: String,
    ){
        _isLoading.postValue(true)
        ApiConfig.getApiService(pref)
            .registerPatient(username, password, address, phone, emergencyPhone, age, gender)
            .enqueue(object : Callback<PatientResponse> {
                override fun onResponse(
                    call: Call<PatientResponse>,
                    response: Response<PatientResponse>
                ) {
                    val responseBody = response.body()
                    if (response.isSuccessful) {
                        _isLoading.postValue(false)
                        Log.d(TAG, "onResponse: Success")
                        _message.value = responseBody?.message
                    } else {
                        _isLoading.postValue(false)
                        Log.e(TAG, "onResponse: ${responseBody?.message}", )
                        _message.value = responseBody?.message
                    }
                }

                override fun onFailure(call: Call<PatientResponse>, t: Throwable) {
                    _isLoading.postValue(false)
                    Log.e(TAG, "onFailure: ${t.message}", )
                    _message.value = "${t.message}"
                }

            })
    }

//    fun patientLogIn(
//        username: String,
//        password: String
//    ){
//        executorService.execute {
//            ApiConfig.getApiService(pref).loginPatient(username, password)
//                .enqueue(object  : Callback<PatientLoginResponse>{
//                    override fun onResponse(
//                        call: Call<PatientLoginResponse>,
//                        response: Response<PatientLoginResponse>
//                    ) {
//                        val responseBody = response.body()
//                        viewModelScope.launch {
//                            if (response.isSuccessful){
//                                Log.d(TAG, "onResponse: $responseBody")
//                                pref.saveUserId(responseBody?.data?.id!!)
//                                pref.saveUserToken(responseBody.token)
//                                Log.d(TAG, "onResponse: ${pref.getUserId()} : ${pref.getUserToken()}")
//                                _message.value = responseBody.message
//                                _status.value = responseBody.status
//                            } else {
//                                Log.e(TAG, "onResponse: ${response.body()?.message}")
//                                _message.value = responseBody?.message
//                            }
//                        }
//                    }
//
//                    override fun onFailure(call: Call<PatientLoginResponse>, t: Throwable) {
//                        Log.e(TAG, "onFailure: ${t.message}", )
//                        _message.value = "${t.message}"
//                    }
//
//                })
//        }
//
//    }

    suspend fun patientLogin(
        username: String,
        password: String
    ): Flow<Result<PatientLoginResponse>> = flow {
        try {
            val response = ApiConfig.getApiService(pref).loginPatient(username, password)
            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun saveId(id: Int){
        viewModelScope.launch {
            pref.saveUserId(id)
        }
    }

    fun saveToken(token: String){
        viewModelScope.launch {
            pref.saveUserToken(token)
        }
    }

    fun patientEdit(
        address: String,
        phone: String,
        emergencyPhone: String,
        age: String,
        gender: String,
        password: String
    ){
        _isLoading.postValue(true)
        executorService.execute {
            ApiConfig.getApiService(pref).editPatient(address, phone, emergencyPhone, age, gender, password)
                .enqueue(object : Callback<PatientEditResponse>{
                    override fun onResponse(
                        call: Call<PatientEditResponse>,
                        response: Response<PatientEditResponse>
                    ) {
                        val responseBody = response.body()
                        if (response.isSuccessful){
                            _isLoading.postValue(false)
                            Log.d(TAG, "onResponse: ${responseBody?.message}")
                            _message.value = responseBody?.message
                        } else {
                            _isLoading.postValue(false)
                            Log.d(TAG, "onResponse: ${responseBody?.message}")
                            _message.value = responseBody?.message
                        }
                    }

                    override fun onFailure(call: Call<PatientEditResponse>, t: Throwable) {
                        _isLoading.postValue(false)
                        Log.e(TAG, "onFailure: ${t.message}", )
                        _message.value = "${t.message}"
                    }

                })
        }
    }

    fun patientLogout(){
        executorService.execute {
            ApiConfig.getApiService(pref).logoutPatient()
                .enqueue(object: Callback<PatientLogoutResponse>{
                    override fun onResponse(
                        call: Call<PatientLogoutResponse>,
                        response: Response<PatientLogoutResponse>
                    ) {
                        val responseBody = response.body()
                        viewModelScope.launch {
                            if (response.isSuccessful) {
                                pref.deleteLogIn()
                                _message.value = responseBody?.message
                                _user.value = null
                            } else {
                                Log.d(TAG, "onResponse: ${response.body()?.message}")
                                _message.value = responseBody?.message
                            }
                        }

                    }

                    override fun onFailure(call: Call<PatientLogoutResponse>, t: Throwable) {
                        Log.e(TAG, "onFailure: ${t.message}", )
                        _message.value = "${t.message}"
                    }

                })
        }
    }

    suspend fun modelPrediction(
        id: String,
        ekg1: String,
        ekg2: String
    ): Flow<Result<PredictResponse>> = flow {
        try {
            val response = ModelApiConfig.getModelApiService().predict(id, ekg1, ekg2)
            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    companion object{
        private const val TAG = "AppViewModel"
    }
}