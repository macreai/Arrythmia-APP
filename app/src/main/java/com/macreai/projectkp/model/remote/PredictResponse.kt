package com.macreai.projectkp.model.remote

import com.google.gson.annotations.SerializedName

data class PredictResponse(

	@field:SerializedName("hasil")
	val hasil: String? = null
)
