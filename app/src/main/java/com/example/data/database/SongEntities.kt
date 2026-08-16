package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteSongEntity(
    @PrimaryKey val songId: String, // Combined "title - artist"
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val genre: String,
    val releaseYear: String,
    val lyrics: String,
    val trivia: String,
    val coverUrl: String,
    val tempoBpm: Int,
    val mood: String,
    val energy: Float,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val coverUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs")
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playlistId: Int,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val genre: String,
    val releaseYear: String,
    val lyrics: String,
    val trivia: String,
    val coverUrl: String,
    val tempoBpm: Int,
    val mood: String,
    val energy: Float,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recents")
data class RecentSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val genre: String,
    val releaseYear: String,
    val lyrics: String,
    val trivia: String,
    val coverUrl: String,
    val tempoBpm: Int,
    val mood: String,
    val energy: Float,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

