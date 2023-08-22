package com.macreai.projectkp.model.remote

import com.google.gson.annotations.SerializedName

data class PatientLogoutResponse(

	@field:SerializedName("data")
	val data: String,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Int
)
