package com.utb.scuffedviruschecker.funs


import com.utb.scuffedviruschecker.model.GetResults
import com.utb.scuffedviruschecker.model.ScanResult
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface VirusTotalApiService {

    @Headers(
        "accept: application/json",
        "x-apikey: 747aa19cdc005173c8a99f6e8479db36a3b7fe468b460bb03d3bad390c0d2107"
    )
    @Multipart
    @POST("files")
    suspend fun scanFile(
        @Part file: MultipartBody.Part
    ): Response<ScanResult>
}

interface GetFileinformation {

    @Headers("x-apikey: 747aa19cdc005173c8a99f6e8479db36a3b7fe468b460bb03d3bad390c0d2107")
    @GET("files/{id}")
    suspend fun getFileInformation(@Path("id") fileId: String): Response<GetResults>
}