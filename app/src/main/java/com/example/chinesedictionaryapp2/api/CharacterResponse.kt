package com.example.chinesedictionary2.api

import com.google.gson.annotations.SerializedName

data class CharacterResponse(
    @SerializedName("character") val character: String?,
    @SerializedName("pinyin") val pinyin: String?,
    @SerializedName("meaning") val meaning: String?
)
