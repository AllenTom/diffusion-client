package com.allentom.diffusion.api

fun getApiClient(): SDWApi {
    return ApiHelper.instance.create(SDWApi::class.java)
}