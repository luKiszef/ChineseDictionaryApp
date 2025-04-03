package com.example.chinesedictionary2.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChineseCharacterApi {
    @GET("characters/string/{char}")
    suspend fun getCharacterInfo(
        @Path("char") character: String,
        @Query("fields") fields: String = "kDefinition,kMandarin"
    ): List<CharacterResponse>

    companion object {
        private const val BASE_URL = "http://ccdb.hemiola.com/"

        fun create(): ChineseCharacterApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ChineseCharacterApi::class.java)
        }
    }
}
