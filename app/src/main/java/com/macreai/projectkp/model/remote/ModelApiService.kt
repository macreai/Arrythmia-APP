package com.macreai.projectkp.model.remote

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ModelApiService {
    @FormUrlEncoded
    @POST("/predict")
    suspend fun predict(
        @Field("ekg1") ekg1: String,
        @Field("ekg2") ekg2: String
    ): PredictResponse

}