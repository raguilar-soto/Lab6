package com.example.lab5

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    var pokemonImageURL = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.pokemonButton)
        val imageView = findViewById<ImageView>(R.id.pokemonImage)
        val pokeNameTextView= findViewById<TextView>(R.id.pokeName)
        val pokeDiscTextView= findViewById<TextView>(R.id.pokeDisc)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getNextPokemon(button, imageView, pokeNameTextView, pokeDiscTextView)
    }

    private fun getPokemonImageUrl(onSuccess: (String, String, Int) -> Unit) {
        val client = AsyncHttpClient()
        val randomId = Random.nextInt(1, 1010)
        val url = "https://pokeapi.co/api/v2/pokemon/$randomId"

        client[url, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Headers,
                json: JsonHttpResponseHandler.JSON
            ) {
                Log.d("Pokemon", "response successful$json")
                val imageUrl = json.jsonObject
                    .getJSONObject("sprites")
                    .getString("front_default")

                val name = json.jsonObject.getString("name")

                val id= json.jsonObject.getInt("id")

                onSuccess(imageUrl, name, id)
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                errorResponse: String,
                throwable: Throwable?
            ) {
                Log.d("Pokemon Error", errorResponse)
            }
        }]
    }

    private fun getPokemonDescription(id: Int, onSuccess: (String) -> Unit) {
        val client = AsyncHttpClient()
        val url = "https://pokeapi.co/api/v2/pokemon-species/$id"

        client[url, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Headers,
                json: JsonHttpResponseHandler.JSON
            ) {
                Log.d("Pokemon Species", "species response successful$json")

                val flavorTextEntries = json.jsonObject.getJSONArray("flavor_text_entries")
                var description = ""

                // Loop through flavor text entries and find the English one
                for (i in 0 until flavorTextEntries.length()) {
                    val entry = flavorTextEntries.getJSONObject(i)
                    if (entry.getJSONObject("language").getString("name") == "en") {
                        description = entry.getString("flavor_text")
                        break
                    }
                }
                onSuccess(description)
            }
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                errorResponse: String,
                throwable: Throwable?
            ) {
                Log.d("Pokemon Species Error", errorResponse)
            }
        }]
    }


    private fun getNextPokemon(button: Button, imageView: ImageView, pokeNameTextView: TextView, pokeDiscTextView: TextView) {
        button.setOnClickListener {
            getPokemonImageUrl { url, name, id ->
                Glide.with(this)
                    .load(url)
                    .fitCenter()
                    .into(imageView)

                pokeNameTextView.text= name
                getPokemonDescription(id) { description ->
                    pokeDiscTextView.text = description
                }
            }
        }
    }
}

