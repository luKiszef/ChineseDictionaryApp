package com.example.chinesedictionary2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chinesedictionary2.api.ChineseCharacterApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var resultTextView: TextView
    private val api = ChineseCharacterApi.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        resultTextView = findViewById(R.id.resultTextView)

        searchButton.setOnClickListener {
            val character = searchEditText.text.toString().trim()
            if (character.isNotEmpty()) {
                searchCharacter(character)
            } else {
                Toast.makeText(this, "Wpisz znak!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchCharacter(character: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { api.getCharacterInfo(character) }
                if (response.isNotEmpty()) {
                    val charData = response[0]

                    val cleanedPinyin = charData.pinyin?.replace(Regex("\\d"), "") ?: "Brak"

                    val resultText = """
                    
                    Pinyin: $cleanedPinyin
                    Znaczenia: ${charData.meaning ?: "Brak"}
                """.trimIndent()
                    resultTextView.text = resultText
                } else {
                    resultTextView.text = "Brak wyników dla \"$character\""
                }
            } catch (e: Exception) {
                resultTextView.text = "Błąd: ${e.localizedMessage}"
            }
        }
    }

}
