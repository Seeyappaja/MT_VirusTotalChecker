package com.utb.scuffedviruschecker.funs


import com.utb.scuffedviruschecker.model.GetFileInformation
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
        "x-apikey: REDACTED"
    )
    @Multipart
    @POST("files")
    suspend fun scanFile(
        @Part file: MultipartBody.Part
    ): Response<ScanResult>
}

interface GetFileinformation {

    @Headers("x-apikey: REDACTED")
    @GET("files/{id}")
    suspend fun getFileInformation(@Path("id") fileId: String): Response<GetFileInformation>
}