package com.macreai.projectkp.model.remote

import com.google.gson.annotations.SerializedName

data class PatientEditResponse(

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("user")
	val user: User
)