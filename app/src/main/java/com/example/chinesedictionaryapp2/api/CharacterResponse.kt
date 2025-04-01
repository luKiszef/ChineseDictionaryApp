package com.example.chinesedictionary2.api

data class CharacterResponse(
    val character: String,
    val pinyin: String,
    val meaning: String
)
