package com.example.chinesedictionary2.api

import com.google.gson.annotations.SerializedName

data class CharacterResponse(
    @SerializedName("character") val character: String?,
    @SerializedName("kMandarin") val pinyin: String?,
    @SerializedName("kDefinition") val meaning: String?
)
