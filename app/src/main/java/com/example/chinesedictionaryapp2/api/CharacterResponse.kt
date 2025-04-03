package com.example.chinesedictionary2.api

import com.google.gson.annotations.SerializedName

data class CharacterResponse(
    @SerializedName("string") val character: String?,
    @SerializedName("kMandarin") val pinyin: String?,
    @SerializedName("kDefinition") val meaning: String?
)
