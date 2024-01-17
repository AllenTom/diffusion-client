package com.allentom.diffusion.api.entity

import com.google.gson.Gson
import retrofit2.Response
import java.io.Serializable

data class ApiError(
    val error: String,
    val detail: String,
    val body: String,
    val errors: String
) : Serializable

class ApiException(val error: ApiError) : Exception() {
    override val message: String
        get() = error.errors

    companion object {
        fun fromJson(json: String): ApiException {
            val gson = Gson()
            val apiError = gson.fromJson(json, ApiError::class.java)
            return ApiException(apiError)
        }
    }
}

fun <T> Response<T>.throwApiExceptionIfNotSuccessful() {
    if (!isSuccessful) {
        val errorBody = errorBody()?.string()
        if (errorBody != null) {
            throw ApiException.fromJson(errorBody)
        }
    }
}