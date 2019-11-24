package com.abbyberkers.apphome.ns

import com.abbyberkers.apphome.ns.json.Trips
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NsApiService {
    @GET("trips")
    fun getTrips(
        @Query("fromStation") fromStation: String,
        @Query("toStation") toStation: String,
        @Query("previousAdvices") numberOfPreviousAdvices: Int = 3,
        @Query("nextAdvices") numberOfNextAdvices: Int = 3,
        @Header("Ocp-Apim-Subscription-Key") apiKey: String = key
): Call<Trips>

    companion object {
        fun create(): NsApiService =
                Retrofit.Builder()
                        .baseUrl("https://gateway.apiportal.ns.nl/reisinformatie-api/api/v3/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(NsApiService::class.java)
    }
}