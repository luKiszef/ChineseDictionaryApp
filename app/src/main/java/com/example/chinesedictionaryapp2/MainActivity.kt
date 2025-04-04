package com.example.chinesedictionary2

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chinesedictionary2.api.ChineseCharacterApi
import com.example.chinesedictionary2.api.CharacterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var favoriteButton: Button
    private lateinit var favoritesListView: ListView
    private val api = ChineseCharacterApi.create()

    private val favoritesList = mutableListOf<String>()
    private lateinit var favoritesAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        resultTextView = findViewById(R.id.resultTextView)
        favoriteButton = findViewById(R.id.favoriteButton)
        favoritesListView = findViewById(R.id.favoritesListView)

        favoritesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, favoritesList)
        favoritesListView.adapter = favoritesAdapter

        searchButton.setOnClickListener {
            val character = searchEditText.text.toString().trim()
            if (character.isNotEmpty()) {
                searchCharacter(character)
            } else {
                Toast.makeText(this, "Wpisz znak!", Toast.LENGTH_SHORT).show()
            }
        }

        favoriteButton.setOnClickListener {
            val characterData = resultTextView.text.toString()
            if (characterData.isNotEmpty() && !favoritesList.contains(characterData)) {
                favoritesList.add(characterData)
                favoritesAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Ten znak już jest w ulubionych!", Toast.LENGTH_SHORT).show()
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
                        Znak: ${charData.character}
                        Pinyin: $cleanedPinyin
                        Znaczenie: ${charData.meaning ?: "Brak"}
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
