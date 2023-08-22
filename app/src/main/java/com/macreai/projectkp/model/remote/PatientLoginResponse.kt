package com.macreai.projectkp.model.remote

import com.google.gson.annotations.SerializedName

data class PatientLoginResponse(

	@field:SerializedName("data")
	val data: User,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String,

	@field:SerializedName("token")
	val token: String
)