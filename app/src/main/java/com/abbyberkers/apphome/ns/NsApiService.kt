package com.abbyberkers.apphome.ns

import com.abbyberkers.apphome.ns.xml.ReisMogelijkheden
import okhttp3.Credentials
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NsApiService {
    /**
     * Makes a request to the ns on the url
     * BASE/ns-api-treinplanner?fromStation={fromStation}&toStation={toStation}
     *
     * @param fromStation Station to go from, as a string.
     * @param toStation Station to go to, as a string.
     *
     * @return A call object of Reismogelijkheden.
     */
    @GET("ns-api-treinplanner")
    fun listTrips(
            @Header("Authorization") credentials: String =
                    Credentials.basic("t.m.schouten@student.tue.nl",
                            "sO-65AZxuErJmmC28eIRB85aos7oGVJ0C6tOZI9YeHDPLXeEv1nfBg"),
            @Query("fromStation") fromStation: String,
            @Query("toStation") toStation: String
    ): Call<ReisMogelijkheden>


    companion object {
        /**
         * Creates an NsApiService for the base url.
         * Adds an xml converter, which magically finds our xml classes to
         * parse the response.
         */
        fun create(): NsApiService =
                Retrofit.Builder()
                        .addConverterFactory(SimpleXmlConverterFactory.create())
                        .baseUrl("http://webservices.ns.nl/")
                        .build()
                        .create(NsApiService::class.java)
    }
}