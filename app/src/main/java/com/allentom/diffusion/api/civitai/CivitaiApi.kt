package com.allentom.diffusion.api.civitai

import com.allentom.diffusion.api.civitai.entities.CivitaiImageListResult
import com.allentom.diffusion.api.civitai.entities.CivitaiModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CivitaiApi {
    @GET("/api/v1/model-versions/by-hash/{hash}")
    suspend fun getModelVersionByHash(
        @Path("hash") hash: String
    ): Response<CivitaiModel>

    @GET("/api/v1/model-versions/{id}")
    suspend fun getModelVersionById(
        @Path("id") id: String
    ): Response<CivitaiModel>

    //    limit (OPTIONAL)	number	The number of results to be returned per page. This can be a number between 0 and 200. By default, each page will return 100 results.
//    postId (OPTIONAL)	number	The ID of a post to get images from
//    modelId (OPTIONAL)	number	The ID of a model to get images from (model gallery)
//    modelVersionId (OPTIONAL)	number	The ID of a model version to get images from (model gallery filtered to version)
//    username (OPTIONAL)	string	Filter to images from a specific user
//    nsfw (OPTIONAL)	boolean | enum (None, Soft, Mature, X)	Filter to images that contain mature content flags or not (undefined returns all)
//    sort (OPTIONAL)	enum (Most Reactions, Most Comments, Newest)	The order in which you wish to sort the results
//    period (OPTIONAL)	enum (AllTime, Year, Month, Week, Day)	The time frame in which the images will be sorted
//    page (OPTIONAL)	number	The page from which to start fetching creators
    @GET("/api/v1/images")
    suspend fun getImageList(
        @Query("limit") limit: Int = 20,
        @Query("postId") postId: Long? = null,
        @Query("modelId") modelId: Long? = null,
        @Query("modelVersionId") modelVersionId: Long? = null,
        @Query("username") username: String? = null,
        @Query("nsfw") nsfw: String? = "X",
        @Query("sort") sort: String? = "Most Reactions",
        @Query("period") period: String? = "Month",
        @Query("page") page: Int? = 1,
    ): Response<CivitaiImageListResult>
}