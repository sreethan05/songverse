package com.example.data

data class Song(
    val title: String,
    val artist: String,
    val album: String = "Unknown Album",
    val durationSeconds: Int = 180,
    val genre: String = "Unknown",
    val releaseYear: String = "Unknown",
    val lyrics: String = "No lyrics available.",
    val trivia: String = "No fun facts available.",
    val coverUrl: String = "",
    val audioUrl: String = "",
    val source: String = "Local",
    val tempoBpm: Int = 120,
    val mood: String = "Balanced",
    val energy: Float = 0.6f,
    val isFavorite: Boolean = false
) {
    val durationText: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}
