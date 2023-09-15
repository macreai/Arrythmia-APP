package com.macreai.projectkp.model.remote

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    fun registerPatient(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("address") address: String,
        @Field("phone") phone: String,
        @Field("emergency_phone") emergencyPhone: String,
        @Field("age") age: String,
        @Field("gender") gender: String
    ): Call<PatientResponse>

    @FormUrlEncoded
    @POST("login")
    suspend fun loginPatient(
        @Field("username") username: String,
        @Field("password") password: String
    ): PatientLoginResponse

    @FormUrlEncoded
    @PUT("patient/edit")
    fun editPatient(
        @Field("address") address: String,
        @Field("phone") phone: String,
        @Field("emergency_phone") emergencyPhone: String,
        @Field("age") age: String,
        @Field("gender") gender: String,
        @Field("password") password: String
    ): Call<PatientEditResponse>

    @GET("patient")
    fun getDetailUser(): Call<User>

    @POST("patient")
    fun logoutPatient(): Call<PatientLogoutResponse>
}