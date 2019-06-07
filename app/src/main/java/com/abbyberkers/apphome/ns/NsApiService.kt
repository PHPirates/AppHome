package com.abbyberkers.apphome.ns

import com.abbyberkers.apphome.ns.json.Trips
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NsApiService {
    @Headers("Ocp-Apim-Subscription-Key: 2a9a7c173a6e4790b0cc4ec35929e43b")
    @GET("trips")
    fun getTrips(
            @Query("fromStation") fromStation: String,
            @Query("toStation") toStation: String,
            @Query("previousAdvices") numberOfPreviousAdvices: Int = 3,
            @Query("nextAdvices") numberOfNextAdvices: Int = 3
    ): Call<Trips>

    companion object {
        fun create(): NsApiService =
                Retrofit.Builder()
                        .baseUrl("https://gateway.apiportal.ns.nl/public-reisinformatie/api/v3/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(NsApiService::class.java)
    }
}