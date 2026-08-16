package com.example.data.api

import android.util.Log
import com.example.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object JioSaavnClient {

    private const val TAG = "JioSaavnClient"

    // High-performance music search endpoints
    private const val ITUNES_BASE = "https://itunes.apple.com/search"

    /**
     * Search songs globally in parallel across global and regional storefronts with zero lag.
     */
    suspend fun searchSongs(query: String, limit: Int = 30): List<Song> = withContext(Dispatchers.IO) {
        val cleanedQuery = query.trim()
        if (cleanedQuery.isEmpty()) return@withContext emptyList()

        val encodedQuery = try { URLEncoder.encode(cleanedQuery, "UTF-8") } catch (e: Exception) { cleanedQuery }
        val allSongs = mutableListOf<Song>()

        // Concurrently query Global catalog + India Storefront for instant, rich coverage
        kotlinx.coroutines.coroutineScope {
            val globalDeferred = this.async {
                try {
                    val url = "$ITUNES_BASE?term=$encodedQuery&media=music&limit=$limit"
                    val resp = httpGet(url)
                    if (resp != null) parseResponse(resp) else emptyList()
                } catch (e: Exception) {
                    Log.e(TAG, "Global search failed: ${e.localizedMessage}")
                    emptyList<Song>()
                }
            }

            val regionalDeferred = this.async {
                try {
                    val url = "$ITUNES_BASE?term=$encodedQuery&country=IN&media=music&limit=$limit"
                    val resp = httpGet(url)
                    if (resp != null) parseResponse(resp) else emptyList()
                } catch (e: Exception) {
                    Log.e(TAG, "Regional search failed: ${e.localizedMessage}")
                    emptyList<Song>()
                }
            }

            val regionalResults = regionalDeferred.await()
            val globalResults = globalDeferred.await()

            // Merge prioritizing regional matches first for Indian users, followed by global catalog
            allSongs.addAll(regionalResults)
            allSongs.addAll(globalResults)
        }

        allSongs.distinctBy { "${it.title.lowercase().trim()}-${it.artist.lowercase().trim()}" }.take(limit)
    }

    /**
     * Fetch trending world & regional tracks from global catalog.
     */
    suspend fun fetchTrendingSongs(): List<Song> = withContext(Dispatchers.IO) {
        val trendingQueries = listOf("Kesariya", "Arijit Singh", "Diljit Dosanjh", "Taylor Swift", "Blinding Lights", "Sidhu Moose Wala", "Coldplay", "Anirudh")
        val combinedResults = mutableListOf<Song>()

        for (q in trendingQueries) {
            val results = searchSongs(q, limit = 5)
            combinedResults.addAll(results)
            if (combinedResults.size >= 35) break
        }

        combinedResults.distinctBy { "${it.title.lowercase().trim()}-${it.artist.lowercase().trim()}" }
    }

    private fun httpGet(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2500
                readTimeout = 2500
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.w(TAG, "HTTP error ${connection.responseCode} for $urlString")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed for $urlString: ${e.message}")
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseResponse(jsonString: String): List<Song> {
        val songList = mutableListOf<Song>()
        try {
            val trimmed = jsonString.trim()
            val resultsArray: JSONArray? = try {
                if (trimmed.startsWith("[")) {
                    JSONArray(trimmed)
                } else {
                    val root = JSONObject(trimmed)
                    when {
                        root.has("data") -> {
                            val dataObj = root.get("data")
                            when (dataObj) {
                                is JSONObject -> dataObj.optJSONArray("results") ?: dataObj.optJSONArray("songs")
                                is JSONArray -> dataObj
                                else -> null
                            }
                        }
                        root.has("results") -> root.optJSONArray("results")
                        else -> null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting JSONArray: ${e.localizedMessage}")
                null
            }

            if (resultsArray == null) return emptyList()

            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.optJSONObject(i) ?: continue

                var title = item.optString("name", "")
                if (title.isBlank()) title = item.optString("title", "")
                if (title.isBlank()) title = item.optString("trackName", "Unknown Title")
                title = unescapeHtml(title)

                if (title.isBlank() || title.equals("Unknown Title", ignoreCase = true)) continue

                val albumObj = item.optJSONObject("album")
                var albumName = albumObj?.optString("name", "") ?: ""
                if (albumName.isBlank()) albumName = item.optString("album", "")
                if (albumName.isBlank()) albumName = item.optString("collectionName", "Single")
                albumName = unescapeHtml(albumName)

                // Parse artists
                val artistName = parseArtists(item)

                // Parse duration
                val durationSec = try {
                    if (item.has("trackTimeMillis")) {
                        (item.optLong("trackTimeMillis", 180000) / 1000).toInt()
                    } else {
                        val dur = item.optString("duration", "180")
                        dur.toIntOrNull() ?: 180
                    }
                } catch (e: Exception) { 180 }

                val releaseYear = item.optString("year", item.optString("releaseDate", "2024")).take(4)

                // Parse Image URL
                val coverUrl = parseBestImage(item)

                // Parse Audio Stream URL
                val audioUrl = parseBestAudioUrl(item)

                val song = Song(
                    title = title,
                    artist = artistName,
                    album = albumName,
                    durationSeconds = durationSec,
                    genre = item.optString("primaryGenreName", item.optString("language", "Global Music")),
                    releaseYear = if (releaseYear.isNotBlank()) releaseYear else "2024",
                    lyrics = "Enjoy streaming high-quality online music.",
                    trivia = "Streamed directly from global music catalog.",
                    coverUrl = coverUrl,
                    audioUrl = audioUrl,
                    source = if (audioUrl.contains("apple") || audioUrl.contains("itunes")) "iTunes" else "JioSaavn",
                    tempoBpm = 120,
                    mood = "Dynamic",
                    energy = 0.75f
                )
                songList.add(song)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON: ${e.localizedMessage}")
        }
        return songList
    }

    private fun parseArtists(item: JSONObject): String {
        val moreInfo = item.optJSONObject("more_info")
        if (moreInfo != null && moreInfo.has("singers")) {
            val singers = moreInfo.optString("singers")
            if (singers.isNotBlank()) return unescapeHtml(singers)
        }
        if (item.has("artistName")) {
            val a = item.optString("artistName")
            if (a.isNotBlank()) return unescapeHtml(a)
        }
        val primaryObj = item.optJSONObject("artists")?.opt("primary")
        if (primaryObj is JSONArray && primaryObj.length() > 0) {
            val names = mutableListOf<String>()
            for (i in 0 until primaryObj.length()) {
                val artistItem = primaryObj.optJSONObject(i)
                artistItem?.optString("name")?.let { if (it.isNotBlank()) names.add(unescapeHtml(it)) }
            }
            if (names.isNotEmpty()) return names.joinToString(", ")
        } else if (primaryObj is String && primaryObj.isNotBlank()) {
            return unescapeHtml(primaryObj)
        }

        val directPrimary = item.optString("primaryArtists", item.optString("artist", ""))
        if (directPrimary.isNotBlank()) return unescapeHtml(directPrimary)

        val desc = item.optString("description", "")
        if (desc.contains("·")) {
            val parts = desc.split("·")
            if (parts.size > 1 && parts[1].trim().isNotBlank()) {
                return unescapeHtml(parts[1].trim())
            }
        }

        return "Various Artists"
    }

    private fun parseBestImage(item: JSONObject): String {
        if (item.has("artworkUrl100")) {
            val art = item.optString("artworkUrl100")
            if (art.isNotBlank()) {
                return art.replace("100x100bb", "600x600bb").replace("http://", "https://")
            }
        }
        val imagesObj = item.optJSONObject("images")
        if (imagesObj != null) {
            val img500 = imagesObj.optString("500x500", "")
            if (img500.isNotBlank()) return img500.replace("http://", "https://")
            val img150 = imagesObj.optString("150x150", "")
            if (img150.isNotBlank()) return img150.replace("http://", "https://")
        }

        val imageObj = item.opt("image")
        if (imageObj is JSONArray && imageObj.length() > 0) {
            val lastImage = imageObj.optJSONObject(imageObj.length() - 1)
            val link = lastImage?.optString("link", lastImage?.optString("url", "")) ?: ""
            if (link.isNotBlank()) return link.replace("http://", "https://")

            val firstImage = imageObj.optJSONObject(0)
            return firstImage?.optString("link", firstImage?.optString("url", ""))?.replace("http://", "https://") ?: ""
        } else if (imageObj is String && imageObj.isNotBlank()) {
            return imageObj.replace("150x150", "500x500").replace("http://", "https://")
        }
        return ""
    }

    private fun parseBestAudioUrl(item: JSONObject): String {
        val moreInfo = item.optJSONObject("more_info")
        if (moreInfo != null) {
            val vlink = moreInfo.optString("vlink", "")
            if (vlink.isNotBlank()) return vlink.replace("http://", "https://")
            val mediaUrl = moreInfo.optString("media_url", "")
            if (mediaUrl.isNotBlank()) return mediaUrl.replace("http://", "https://")
        }

        if (item.has("previewUrl")) {
            val prev = item.optString("previewUrl")
            if (prev.isNotBlank()) return prev.replace("http://", "https://")
        }

        val downloadObj = item.opt("downloadUrl")
        if (downloadObj is JSONArray && downloadObj.length() > 0) {
            var bestUrl = ""
            for (i in downloadObj.length() - 1 downTo 0) {
                val dlObj = downloadObj.optJSONObject(i) ?: continue
                val quality = dlObj.optString("quality", "").lowercase()
                val link = dlObj.optString("link", dlObj.optString("url", ""))
                if (link.isNotBlank()) {
                    if (quality.contains("320") || quality.contains("160")) {
                        return link.replace("http://", "https://")
                    }
                    if (bestUrl.isEmpty()) {
                        bestUrl = link
                    }
                }
            }
            if (bestUrl.isNotBlank()) return bestUrl.replace("http://", "https://")
        } else if (downloadObj is String && downloadObj.isNotBlank()) {
            return downloadObj.replace("http://", "https://")
        }

        val mediaUrl = item.optString("media_url", item.optString("url", ""))
        if (mediaUrl.isNotBlank() && (mediaUrl.contains(".mp3") || mediaUrl.contains(".m4a") || mediaUrl.contains("saavn") || mediaUrl.contains("apple") || mediaUrl.contains("jio"))) {
            return mediaUrl.replace("http://", "https://")
        }

        return ""
    }

    private fun unescapeHtml(text: String): String {
        return text.replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&#039;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
    }
}
