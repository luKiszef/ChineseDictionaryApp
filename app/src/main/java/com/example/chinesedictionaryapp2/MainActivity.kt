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
    private lateinit var listNameEditText: EditText
    private lateinit var listCharactersEditText: EditText
    private lateinit var addToListButton: Button
    private lateinit var customListsListView: ListView

    private val api = ChineseCharacterApi.create()

    private val favorites = mutableMapOf<String, CharacterResponse>()
    private lateinit var favoritesAdapter: ArrayAdapter<String>

    private val customLists = mutableMapOf<String, MutableSet<String>>()
    private lateinit var customListsAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Powiązanie z widokami
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        resultTextView = findViewById(R.id.resultTextView)
        favoriteButton = findViewById(R.id.favoriteButton)
        favoritesListView = findViewById(R.id.favoritesListView)
        listNameEditText = findViewById(R.id.listNameEditText)
        listCharactersEditText = findViewById(R.id.listCharactersEditText)
        addToListButton = findViewById(R.id.addToListButton)
        customListsListView = findViewById(R.id.customListsListView)

        // Adaptery
        favoritesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        favoritesListView.adapter = favoritesAdapter

        customListsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        customListsListView.adapter = customListsAdapter

        // Wyszukiwanie znaku
        searchButton.setOnClickListener {
            val character = searchEditText.text.toString().trim()
            if (character.isNotEmpty()) {
                searchCharacter(character)
            } else {
                Toast.makeText(this, "Wpisz znak!", Toast.LENGTH_SHORT).show()
            }
        }

        // Dodawanie do ulubionych
        favoriteButton.setOnClickListener {
            val character = searchEditText.text.toString().trim()
            if (character.isNotEmpty() && favorites.containsKey(character).not()) {
                lifecycleScope.launch {
                    val response = fetchCharacter(character)
                    if (response != null) {
                        favorites[character] = response
                        updateFavoritesView()
                        Toast.makeText(this@MainActivity, "Dodano do ulubionych", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Nie udało się pobrać danych", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Znak już znajduje się w ulubionych", Toast.LENGTH_SHORT).show()
            }
        }

        // Dodawanie do listy
        addToListButton.setOnClickListener {
            val listName = listNameEditText.text.toString().trim()
            val characters = listCharactersEditText.text.toString().trim()

            if (listName.isEmpty() || characters.isEmpty()) {
                Toast.makeText(this, "Wpisz nazwę listy i znaki!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val charList = characters.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val currentList = customLists.getOrPut(listName) { mutableSetOf() }
            val beforeSize = currentList.size
            currentList.addAll(charList)

            if (currentList.size > beforeSize) {
                Toast.makeText(this, "Dodano do listy $listName", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Znaki już znajdują się na liście", Toast.LENGTH_SHORT).show()
            }

            updateCustomListsView()
        }

        // Kliknięcie w listę — pokaż szczegóły
        customListsListView.setOnItemClickListener { _, _, position, _ ->
            val selected = customListsAdapter.getItem(position) ?: return@setOnItemClickListener
            val listName = selected.substringBefore(":").trim()
            val characters = customLists[listName]?.toList() ?: return@setOnItemClickListener

            lifecycleScope.launch {
                val details = characters.mapNotNull { char -> fetchCharacter(char) }

                val message = if (details.isNotEmpty()) {
                    details.joinToString("\n\n") { item ->
                        val pinyin = formatPinyin(item.pinyin)
                        val meaning = item.meaning ?: "Brak znaczenia"
                        "${item.character}\nPinyin: $pinyin\nZnaczenie: $meaning"
                    }
                } else {
                    "Brak danych"
                }

                showDetailsDialog(listName, message)
            }
        }
    }

    private fun updateFavoritesView() {
        val displayList = favorites.map {
            val char = it.value.character ?: it.key
            val pinyin = formatPinyin(it.value.pinyin)
            val meaning = it.value.meaning ?: "Brak znaczenia"
            "$char - $pinyin - $meaning"
        }
        favoritesAdapter.clear()
        favoritesAdapter.addAll(displayList)
        favoritesAdapter.notifyDataSetChanged()
    }

    private fun updateCustomListsView() {
        val all = customLists.map { entry ->
            "${entry.key}: ${entry.value.joinToString(", ")}"
        }
        customListsAdapter.clear()
        customListsAdapter.addAll(all)
        customListsAdapter.notifyDataSetChanged()
    }

    private fun formatPinyin(pinyin: String?): String {
        return pinyin?.replace(Regex("\\d"), "") ?: "Brak"
    }

    private suspend fun fetchCharacter(character: String): CharacterResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getCharacterInfo(character)
                response.firstOrNull()?.let {
                    CharacterResponse(
                        character = it.character ?: character,
                        pinyin = it.pinyin,
                        meaning = it.meaning
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun searchCharacter(character: String) {
        lifecycleScope.launch {
            val result = fetchCharacter(character)
            if (result != null) {
                val pinyin = formatPinyin(result.pinyin)
                val meaning = result.meaning ?: "Brak znaczenia"
                resultTextView.text = "Znak: ${result.character}\nPinyin: $pinyin\nZnaczenie: $meaning"
            } else {
                resultTextView.text = "Brak wyników"
            }
        }
    }

    private fun showDetailsDialog(title: String, message: String) {
        runOnUiThread {
            android.app.AlertDialog.Builder(this)
                .setTitle("Lista: $title")
                .setMessage(message)
                .setPositiveButton("Zamknij", null)
                .show()
        }
    }
}
