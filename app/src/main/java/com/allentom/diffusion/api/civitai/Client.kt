package com.allentom.diffusion.api.civitai

fun getCivitaiApiClient(): CivitaiApi {
    return CivitaiApiHelper.instance.create(CivitaiApi::class.java)
}