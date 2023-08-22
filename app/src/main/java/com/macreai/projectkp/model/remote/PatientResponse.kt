package com.macreai.projectkp.model.remote

import com.google.gson.annotations.SerializedName

data class PatientResponse(

	@field:SerializedName("message")
	val message: String,

	// @field:SerializedName("user")
	// val user: User,

	@field:SerializedName("status")
	val status: String
)
/*
data class User(

	@field:SerializedName("emergency_phone")
	val emergencyPhone: String,

	@field:SerializedName("address")
	val address: String,

	@field:SerializedName("gender")
	val gender: String,

	@field:SerializedName("updated_at")
	val updatedAt: String,

	@field:SerializedName("phone")
	val phone: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("age")
	val age: String,

	@field:SerializedName("username")
	val username: String
)
 */
