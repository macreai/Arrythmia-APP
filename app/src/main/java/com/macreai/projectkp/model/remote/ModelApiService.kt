package com.macreai.projectkp.model.remote

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ModelApiService {
    @FormUrlEncoded
    @POST("/predict")
    suspend fun predict(
        @Field("patient_id") id: String,
        @Field("lead1") lead1: String,
        @Field("lead2") lead2: String
    ): PredictResponse

}