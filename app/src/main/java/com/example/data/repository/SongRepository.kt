package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.data.Song
import com.example.data.api.JioSaavnClient
import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import android.util.Log

class SongRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.songDao()

    // --- Preloaded Songs (Static Library) ---
    val preloadedSongs = listOf(
        Song(
            title = "Kesariya (From \"Brahmastra\")",
            artist = "Pritam, Arijit Singh & Amitabh Bhattacharya",
            album = "Kesariya (From \"Brahmastra\") - Single",
            durationSeconds = 268,
            genre = "Bollywood",
            releaseYear = "2022",
            lyrics = """Mujhko itna bataaye koi
Kaise tujhse dil na lagaaye koi
Rabba ne tujhko banaane mein
Kardi hai husn ki khaali tijoriyaan

Kesar ki si yaari hai
Teri aashiqi yaari hai
Rang jaaun jo main haath lagaaun
Din beete saare teri fikr mein
Rain saari teri khair manaayein

Kesariya tera ishq hai piya
Rang jaaun jo main haath lagaaun
Din beete saare teri fikr mein
Rain saari teri khair manaayein""",
            trivia = "The monumental romantic anthem from Brahmastra composed by Pritam and sung by Arijit Singh. It broke all streaming records across India with over a billion plays.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/9f/13/ca/9f13ca3b-e533-03e0-f19a-f0aaa774581d/196589311191.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/38/4c/5c/384c5c8f-3ff8-e457-b2f7-3158ce108649/mzaf_12389299033886433185.plus.aac.p.m4a",
            tempoBpm = 94,
            mood = "Romantic",
            energy = 0.70f
        ),
        Song(
            title = "Apna Bana Le",
            artist = "Arijit Singh, Sachin-Jigar & Amitabh Bhattacharya",
            album = "Bhediya",
            durationSeconds = 262,
            genre = "Bollywood",
            releaseYear = "2022",
            lyrics = """Tu mera koi na hoke bhi kuch laage
Tu mera koi na hoke bhi kuch laage
Kiya re jo bhi tune kaisa kiya re
Jiya ko mere baandh aise liya re

Apna bana le piya, apna bana le piya
Apna bana le mujhe apna bana le piya
Dil ke nagar mein shehar tu basa le piya
Apna bana le mujhe apna bana le piya""",
            trivia = "A soulful romantic ballad from the film Bhediya composed by Sachin-Jigar with heartwarming vocals by Arijit Singh.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/2e/0b/c0/2e0bc070-112f-a827-6ad8-6bc64f7caaff/840214460180.png/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview122/v4/09/51/0d/09510dea-6579-5cd0-b13b-696abc2c520b/mzaf_10718921821360997069.plus.aac.p.m4a",
            tempoBpm = 88,
            mood = "Soulful",
            energy = 0.60f
        ),
        Song(
            title = "Lover",
            artist = "Diljit Dosanjh",
            album = "MoonChild Era",
            durationSeconds = 190,
            genre = "Punjabi Pop",
            releaseYear = "2021",
            lyrics = """Tera ni mai, tera ni mai lover
Tera ni mai lover, baby girl
Karda haan tere utte dil toh fida
Jaan le le meri chahe kar de juda

Ho tera ni mai lover
Tera ni mai lover""",
            trivia = "The sensational Punjabi pop track from Diljit Dosanjh's acclaimed album MoonChild Era, known for its smooth synth-pop production and vibrant beats.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/8a/89/e4/8a89e445-d2c6-f8ac-a828-27818b0c1afe/859749638209_cover.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/f9/9b/37/f99b37bf-44ef-9237-72ba-15a32437c832/mzaf_15116792754153694687.plus.aac.p.m4a",
            tempoBpm = 110,
            mood = "Vibrant",
            energy = 0.85f
        ),
        Song(
            title = "So High",
            artist = "Sidhu Moose Wala",
            album = "So High - Single",
            durationSeconds = 234,
            genre = "Punjabi",
            releaseYear = "2017",
            lyrics = """Hoge charche ni kende billo gabru di thuk
Kayi khande ne khar kaiyan ditti ae fuk
Ni tu aakheya si munda kitho karega afford
Dekh billo aj tera yaar so high!""",
            trivia = "The iconic breakthrough track that propelled Sidhu Moose Wala into global superstardom with Byg Byrd's thunderous production.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/1b/6e/74/1b6e74cd-b93a-5dd9-e7a2-c7623df73d10/cover.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/cf/6f/b6/cf6fb600-051d-66f2-4acb-df0259000b60/mzaf_13129631117141800671.plus.aac.p.m4a",
            tempoBpm = 96,
            mood = "Fierce",
            energy = 0.90f
        ),
        Song(
            title = "Chaleya",
            artist = "Anirudh Ravichander, Arijit Singh & Shilpa Rao",
            album = "Jawan",
            durationSeconds = 200,
            genre = "Bollywood",
            releaseYear = "2023",
            lyrics = """Ishq mein dil bana hai
Ishq mein dil fanaa hai
Jitna bhi roko isko
Ye toh chalta gaya hai

Chaleya teri ore chaleya
Chaleya teri ore""",
            trivia = "From Shah Rukh Khan's blockbuster Jawan, composed by Anirudh Ravichander with a mesmerizing duet by Arijit Singh and Shilpa Rao.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/bb/f4/f5/bbf4f511-3c12-c25e-a475-b6d06faa8c13/8902894362047_cover.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/76/05/d9/7605d905-f631-517d-df7f-e162affcd414/mzaf_9976541859961700749.plus.aac.p.m4a",
            tempoBpm = 102,
            mood = "Groovy",
            energy = 0.80f
        ),
        Song(
            title = "Brown Munde",
            artist = "AP Dhillon, Gurinder Gill & Shinda Kahlon",
            album = "Brown Munde",
            durationSeconds = 254,
            genre = "Punjabi Hip-Hop",
            releaseYear = "2020",
            lyrics = """Desi je geet aa
Taan vi trap beat aa
Sir kadde gaddiyan ch
Brown munde!""",
            trivia = "A global cultural phenomenon that united South Asian youth worldwide with its iconic trap beat and unforgettable chorus.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/26/a3/ac/26a3ac64-69e4-95ec-80ab-1f5a477537d2/859742042973_cover.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/97/74/69/977469be-a9d5-35a7-80ad-ebe12a799ccc/mzaf_804867738726203367.plus.aac.p.m4a",
            tempoBpm = 95,
            mood = "Energetic",
            energy = 0.88f
        ),
        Song(
            title = "Cruel Summer",
            artist = "Taylor Swift",
            album = "Lover",
            durationSeconds = 178,
            genre = "Pop",
            releaseYear = "2019",
            lyrics = """Fever dream high in the quiet of the night
You know that I caught it
Bad, bad boy, shiny toy with a price
You know that I bought it

I'm drunk in the back of the car
And I cried like a baby coming home from the bar
Said, "I'm fine," but it wasn't true
I don't wanna keep secrets just to keep you
And I snuck in through the garden gate
Every night that summer just to seal my fate
And I scream, "For whatever it's worth
I love you, ain't that the worst thing you ever heard?"
He looks up grinning like a devil!""",
            trivia = "A beloved fan-favorite that became a massive worldwide #1 hit during The Eras Tour. Produced with Jack Antonoff and St. Vincent.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/49/3d/ab/493dab54-f920-9043-6181-80993b8116c9/19UMGIM53909.rgb.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/44/af/81/44af8168-9609-1b85-5048-ada08dceacf3/mzaf_1341699644335558812.plus.aac.p.m4a",
            tempoBpm = 170,
            mood = "Euphoric",
            energy = 0.92f
        ),
        Song(
            title = "Starboy (feat. Daft Punk)",
            artist = "The Weeknd",
            album = "Starboy",
            durationSeconds = 230,
            genre = "R&B/Soul",
            releaseYear = "2016",
            lyrics = """I'm tryna put you in the worst mood, ah
P1 cleaner than your church shoes, ah
Milli point two just to hurt you, ah
All red Lamb' just to tease you, ah

Look what you've done
I'm a motherfuckin' starboy!""",
            trivia = "The Weeknd's iconic collaboration with electronic legends Daft Punk, featuring infectious robotic synth rhythms and crisp percussion.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/b5/92/bb/b592bb72-52e3-e756-9b26-9f56d08f47ab/16UMGIM67864.rgb.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/11/71/d6/1171d6ad-3c96-e027-2af6-58028426588c/mzaf_15137631797407745471.plus.aac.p.m4a",
            tempoBpm = 186,
            mood = "Swagger",
            energy = 0.85f
        ),
        Song(
            title = "Shape of You",
            artist = "Ed Sheeran",
            album = "÷ (Deluxe)",
            durationSeconds = 234,
            genre = "Pop",
            releaseYear = "2017",
            lyrics = """The club isn't the best place to find a lover
So the bar is where I go
Me and my friends at the table doing shots
Drinking fast and then we talk slow

Girl, you know I want your love
Your love was handmade for somebody like me
Come on now, follow my lead
I may be crazy, don't mind me

I'm in love with the shape of you
We push and pull like a magnet do
Although my heart is falling too
I'm in love with your body!""",
            trivia = "One of the most streamed songs in music history, holding the record for over 3.5 billion Spotify streams and 6 billion YouTube views.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/15/e6/e8/15e6e8a4-4190-6a8b-86c3-ab4a51b88288/190295851286.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/44/c7/4f/44c74f0d-72dc-6143-d4d0-ba14d661ca0d/mzaf_9566898362556366703.plus.aac.p.m4a",
            tempoBpm = 96,
            mood = "Upbeat",
            energy = 0.80f
        ),
        Song(
            title = "Yellow",
            artist = "Coldplay",
            album = "Parachutes",
            durationSeconds = 269,
            genre = "Alternative Rock",
            releaseYear = "2000",
            lyrics = """Look at the stars
Look how they shine for you
And everything you do
Yeah, they were all yellow

I came along
I wrote a song for you
And all the things you do
And it was called "Yellow"

Your skin, oh yeah, your skin and bones
Turn into something beautiful
You know, you know I love you so
You know I love you so...""",
            trivia = "Coldplay's international breakthrough single that established their signature atmospheric arena sound and acoustic warmth.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/f5/93/8c/f5938c49-964c-31d1-4b33-78b634f71fb7/190295978075.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/66/f3/1a/66f31a76-a6ed-cb4c-f353-23310a7ae9a8/mzaf_10593596652344378873.plus.aac.p.m4a",
            tempoBpm = 88,
            mood = "Nostalgic",
            energy = 0.60f
        ),
        Song(
            title = "Bohemian Rhapsody",
            artist = "Queen",
            album = "A Night at the Opera",
            durationSeconds = 354,
            genre = "Rock",
            releaseYear = "1975",
            lyrics = """Is this the real life? Is this just fantasy?
Caught in a landslide, no escape from reality
Open your eyes, look up to the skies and see
I'm just a poor boy, I need no sympathy
Because I'm easy come, easy go, little high, little low
Any way the wind blows doesn't really matter to me, to me

Mama, just killed a man
Put a gun against his head, pulled my trigger, now he's dead
Mama, life had just begun
But now I've gone and thrown it all away

Mama, ooh, didn't mean to make you cry
If I'm not back again this time tomorrow
Carry on, carry on as if nothing really matters

I see a little silhouetto of a man
Scaramouche, Scaramouche, will you do the Fandango?
Thunderbolt and lightning, very, very frightening me
(Galileo) Galileo, (Galileo) Galileo, Galileo Figaro magnifico!""",
            trivia = "Written by Freddie Mercury, this operatic rock masterpiece spent 9 weeks at number one in the UK. It was recorded in six different studios and pushed 1970s multitracking technology to its absolute physical limits.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/58/2b/a0/582ba0bb-80d0-080e-ec97-3f30d32c5fbe/00602547500366.rgb.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/36/4c/80/364c802e-c534-5858-69aa-863a13098522/mzaf_13506141369348128912.plus.aac.p.m4a",
            tempoBpm = 72,
            mood = "Epic",
            energy = 0.65f
        ),
        Song(
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            durationSeconds = 200,
            genre = "Synthwave",
            releaseYear = "2019",
            lyrics = """I've been tryna call
I've been on my own for long enough
Maybe you can show me how to love, maybe
I'm going through withdrawals
You don't even have to do too much
You can turn me on with just a touch, baby

I look around and Sin City's cold and empty
No one's around to judge me
I can't see clearly when you're gone

I said, ooh, I'm blinded by the lights
No, I can't sleep until I feel your touch
I said, ooh, I'm drowning in the night
Oh, when I'm like this, you're the one I trust
(Hey, hey, hey!)""",
            trivia = "An absolute global juggernaut, this track became the #1 Billboard Hot 100 Song of All Time. Its pulsing, 171 BPM retro-synth-pop bassline was inspired by the driving energy of 1980s synthwave.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/57/02/fb/5702fb78-ddb2-e932-b883-9366df0472e3/20UMGIM08249.rgb.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/fd/19/2d/fd192d19-5838-89f4-3453-61a7a224a100/mzaf_16401087819875487770.plus.aac.p.m4a",
            tempoBpm = 171,
            mood = "Energetic",
            energy = 0.90f
        ),
        Song(
            title = "Someone Like You",
            artist = "Adele",
            album = "21",
            durationSeconds = 285,
            genre = "Pop",
            releaseYear = "2011",
            lyrics = """I heard that you're settled down
That you found a girl and you're married now
I heard that your dreams came true
I guess she gave you things, I didn't give to you
Old friend, why are you so shy?
Ain't like you to hold back or hide from the light

I hate to turn up out of the blue, uninvited
But I couldn't stay away, I couldn't fight it
I had hoped you'd see my face
And that you'd be reminded that for me, it isn't over

Never mind, I'll find someone like you
I wish nothing but the best for you, too
Don't forget me, I beg, I remember you said
Sometimes it lasts in love, but sometimes it hurts instead...""",
            trivia = "This heartbreaking piano ballad became a cultural phenomenon, praised for its raw emotional vulnerability. It was co-written with Dan Wilson and is driven by an elegant, melancholic chord progression.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/a4/09/f3/a409f3ed-65f0-6126-b8cb-40292bf18fb1/886443177198.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/4b/32/30/4b3230a1-4202-b2fa-1011-8be52e79b3ee/mzaf_15783321528620247656.plus.aac.p.m4a",
            tempoBpm = 135,
            mood = "Melancholic",
            energy = 0.30f
        ),
        Song(
            title = "Stay",
            artist = "The Kid LAROI & Justin Bieber",
            album = "F*CK LOVE 3: OVER YOU",
            durationSeconds = 141,
            genre = "Pop",
            releaseYear = "2021",
            lyrics = """I do the same thing I told you that I never would
I told you I'd change, even when I knew I never could
I know that I can't find nobody else as good as you
I need you to stay, need you to stay, yeah

I get drunk, wake up, I'm wasted still
I realize the time that I wasted here
I feel like you can't feel the way I feel
Oh, I'll be fucked up if you can't be right here

Oh, ooh-woah
Oh, ooh-woah, ooh-woah
Oh, ooh-woah
I need you to stay, need you to stay, yeah...""",
            trivia = "A hyper-energetic, fast-paced hybrid of pop and synth-punk. At only 2 minutes and 21 seconds long, it became an instant chart-topping sensation due to its addictive, high-tempo hook.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/8e/bc/0a/8ebc0a87-c1d0-1cfa-7299-4c12bb1476f5/195497122119.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/80/75/c6/8075c6c8-5221-5f25-8822-2637f374e2d3/mzaf_7466542032049877478.plus.aac.p.m4a",
            tempoBpm = 170,
            mood = "Uplifting",
            energy = 0.85f
        ),
        Song(
            title = "Fly Me to the Moon",
            artist = "Frank Sinatra",
            album = "It Might as Well Be Swing",
            durationSeconds = 147,
            genre = "Jazz",
            releaseYear = "1964",
            lyrics = """Fly me to the moon
Let me play among the stars
Let me see what spring is like on
A-Jupiter and Mars
In other words, hold my hand
In other words, baby, kiss me

Fill my heart with song and let me sing forevermore
You are all I long for, all I worship and adore
In other words, please be true
In other words, I love you...""",
            trivia = "Originally titled 'In Other Words', Frank Sinatra's swing version was famously associated with NASA's Apollo missions, being played on cassette players aboard Apollo 10 and during the Apollo 11 lunar landing mission.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/52/bd/d8/52bdd898-d4c6-43b9-1f48-a00d83e20037/00602537877232.rgb.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/34/eb/a1/34eba144-80ee-60fa-2983-0498d35fbc0a/mzaf_13019888924687483838.plus.aac.p.m4a",
            tempoBpm = 119,
            mood = "Elegant",
            energy = 0.45f
        ),
        Song(
            title = "Clair de Lune",
            artist = "Claude Debussy",
            album = "Suite bergamasque",
            durationSeconds = 300,
            genre = "Classical",
            releaseYear = "1905",
            lyrics = "[Instrumental - No lyrics, pure emotional soundscape]",
            trivia = "Debussy's impressionistic piano masterpiece, translating to 'Moonlight'. It was inspired by Paul Verlaine's poem of the same name and is characterized by its fluid rhythm and atmospheric harmonic colors.",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/91/d8/50/91d85093-e40f-7848-1250-7171e54911d7/886443831847.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/2b/ef/11/2bef11dd-9333-8758-132d-222851d02c81/mzaf_15830953689617478053.plus.aac.p.m4a",
            tempoBpm = 60,
            mood = "Relaxing",
            energy = 0.15f
        )
    )

    // --- Favorites Logic ---

    fun getFavorites(): Flow<List<Song>> {
        return dao.getFavoritesFlow().map { list ->
            list.map { it.toSong(isFavorite = true) }
        }
    }

    fun isFavoriteFlow(title: String, artist: String): Flow<Boolean> {
        return dao.isFavoriteFlow(getSongId(title, artist))
    }

    suspend fun isFavorite(title: String, artist: String): Boolean {
        return dao.isFavorite(getSongId(title, artist))
    }

    suspend fun toggleFavorite(song: Song) {
        val songId = getSongId(song.title, song.artist)
        if (dao.isFavorite(songId)) {
            dao.deleteFavoriteById(songId)
        } else {
            dao.insertFavorite(song.toFavoriteEntity())
        }
    }

    // --- Playlists Logic ---

    fun getPlaylists(): Flow<List<PlaylistEntity>> = dao.getPlaylistsFlow()

    suspend fun createPlaylist(name: String, description: String): Long {
        val entity = PlaylistEntity(name = name, description = description)
        return dao.insertPlaylist(entity)
    }

    suspend fun importPlaylist(name: String, description: String, songs: List<Song>) {
        val playlistId = createPlaylist(name, description).toInt()
        songs.forEach { song ->
            addSongToPlaylist(playlistId, song)
        }
    }

    suspend fun deletePlaylist(playlistId: Int) {
        dao.deleteSongsByPlaylistId(playlistId)
        dao.deletePlaylistById(playlistId)
    }

    fun getPlaylistSongs(playlistId: Int): Flow<List<Song>> {
        return dao.getPlaylistSongsFlow(playlistId).map { list ->
            list.map { entity ->
                val fav = dao.isFavorite(getSongId(entity.title, entity.artist))
                entity.toSong(isFavorite = fav)
            }
        }
    }

    suspend fun addSongToPlaylist(playlistId: Int, song: Song) {
        val entity = PlaylistSongEntity(
            playlistId = playlistId,
            title = song.title,
            artist = song.artist,
            album = song.album,
            durationSeconds = song.durationSeconds,
            genre = song.genre,
            releaseYear = song.releaseYear,
            lyrics = song.lyrics,
            trivia = song.trivia,
            coverUrl = song.coverUrl,
            tempoBpm = song.tempoBpm,
            mood = song.mood,
            energy = song.energy
        )
        dao.insertPlaylistSong(entity)
    }

    suspend fun removeSongFromPlaylist(playlistSongId: Int) {
        dao.deletePlaylistSongById(playlistSongId)
    }

    suspend fun removeSongFromPlaylist(playlistId: Int, title: String, artist: String) {
        dao.deletePlaylistSong(playlistId, title, artist)
    }

    // --- Recent History Logic ---

    fun getRecentHistory(): Flow<List<Song>> {
        return dao.getRecentsFlow().map { list ->
            list.map { entity ->
                val fav = dao.isFavorite(getSongId(entity.title, entity.artist))
                entity.toSong(isFavorite = fav)
            }
        }
    }

    suspend fun addRecentSong(song: Song) {
        val entity = RecentSongEntity(
            title = song.title,
            artist = song.artist,
            album = song.album,
            durationSeconds = song.durationSeconds,
            genre = song.genre,
            releaseYear = song.releaseYear,
            lyrics = song.lyrics,
            trivia = song.trivia,
            coverUrl = song.coverUrl,
            tempoBpm = song.tempoBpm,
            mood = song.mood,
            energy = song.energy
        )
        dao.insertRecent(entity)
    }

    suspend fun clearHistory() {
        dao.clearRecents()
    }

    // --- Recent Searches Logic ---

    fun getRecentSearches(): Flow<List<RecentSearchEntity>> = dao.getRecentSearchesFlow()

    suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) {
            val entity = RecentSearchEntity(query = trimmed, timestamp = System.currentTimeMillis())
            dao.insertRecentSearch(entity)
        }
    }

    suspend fun removeRecentSearch(query: String) {
        dao.deleteRecentSearch(query)
    }

    suspend fun clearRecentSearches() {
        dao.clearRecentSearches()
    }

    // --- Search Engine (JioSaavn Global Songs API) ---

    /**
     * Search live global music database directly for play-ready songs.
     */
    suspend fun searchAllSongs(query: String, searchAI: Boolean = false): List<Song> {
        val cleanedQuery = query.trim().lowercase(Locale.ROOT)
        if (cleanedQuery.isEmpty()) return emptyList()

        val results = mutableListOf<Song>()

        // 1. Search matching local preloaded songs
        val localMatches = preloadedSongs.filter {
            it.title.lowercase(Locale.ROOT).contains(cleanedQuery) ||
            it.artist.lowercase(Locale.ROOT).contains(cleanedQuery) ||
            it.genre.lowercase(Locale.ROOT).contains(cleanedQuery) ||
            it.album.lowercase(Locale.ROOT).contains(cleanedQuery)
        }.map {
            val fav = dao.isFavorite(getSongId(it.title, it.artist))
            it.copy(isFavorite = fav)
        }
        results.addAll(localMatches)

        // 2. Search Live Online Database
        try {
            val onlineResults = JioSaavnClient.searchSongs(query, limit = 30)
            if (onlineResults.isNotEmpty()) {
                val mappedOnline = onlineResults.map { song ->
                    val fav = dao.isFavorite(getSongId(song.title, song.artist))
                    song.copy(isFavorite = fav)
                }
                results.addAll(mappedOnline)
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "searchAllSongs failed", e)
        }

        return results.distinctBy { "${it.title.lowercase(Locale.ROOT)}-${it.artist.lowercase(Locale.ROOT)}" }
    }

    suspend fun fetchTrendingWorldSongs(): List<Song> {
        val jioTrending = try {
            JioSaavnClient.fetchTrendingSongs()
        } catch (e: Exception) {
            emptyList()
        }

        val mapped = jioTrending.map { song ->
            val fav = dao.isFavorite(getSongId(song.title, song.artist))
            song.copy(isFavorite = fav)
        }

        return (mapped + preloadedSongs).distinctBy { "${it.title}-${it.artist}".lowercase(Locale.ROOT) }
    }

    // Try to find a playable preview/audio URL for a song by querying the search endpoints with title+artist
    private suspend fun resolveAudioForSong(song: Song): String {
        return try {
            val candidates = JioSaavnClient.searchSongs("${song.title} ${song.artist}", limit = 5)
            for (c in candidates) {
                if (!c.audioUrl.isNullOrBlank()) return c.audioUrl
            }
            ""
        } catch (e: Exception) {
            Log.e("SongRepository", "resolveAudioForSong failed for ${song.title} - ${song.artist}", e)
            ""
        }
    }

    // --- Utility Mapping Functions ---

    private fun getSongId(title: String, artist: String): String {
        return "$title - $artist".lowercase(Locale.ROOT).trim()
    }

    private fun FavoriteSongEntity.toSong(isFavorite: Boolean): Song {
        return Song(
            title = title,
            artist = artist,
            album = album,
            durationSeconds = durationSeconds,
            genre = genre,
            releaseYear = releaseYear,
            lyrics = lyrics,
            trivia = trivia,
            coverUrl = coverUrl,
            tempoBpm = tempoBpm,
            mood = mood,
            energy = energy,
            isFavorite = isFavorite
        )
    }

    private fun PlaylistSongEntity.toSong(isFavorite: Boolean): Song {
        return Song(
            title = title,
            artist = artist,
            album = album,
            durationSeconds = durationSeconds,
            genre = genre,
            releaseYear = releaseYear,
            lyrics = lyrics,
            trivia = trivia,
            coverUrl = coverUrl,
            tempoBpm = tempoBpm,
            mood = mood,
            energy = energy,
            isFavorite = isFavorite
        )
    }

    private fun RecentSongEntity.toSong(isFavorite: Boolean): Song {
        return Song(
            title = title,
            artist = artist,
            album = album,
            durationSeconds = durationSeconds,
            genre = genre,
            releaseYear = releaseYear,
            lyrics = lyrics,
            trivia = trivia,
            coverUrl = coverUrl,
            tempoBpm = tempoBpm,
            mood = mood,
            energy = energy,
            isFavorite = isFavorite
        )
    }

    private fun Song.toFavoriteEntity(): FavoriteSongEntity {
        return FavoriteSongEntity(
            songId = getSongId(title, artist),
            title = title,
            artist = artist,
            album = album,
            durationSeconds = durationSeconds,
            genre = genre,
            releaseYear = releaseYear,
            lyrics = lyrics,
            trivia = trivia,
            coverUrl = coverUrl,
            tempoBpm = tempoBpm,
            mood = mood,
            energy = energy
        )
    }

    fun scanDeviceAudio(): List<Song> {
        val songList = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val yearCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val title = it.getString(titleCol) ?: "Unknown Track"
                    val artist = it.getString(artistCol) ?: "Unknown Artist"
                    val album = it.getString(albumCol) ?: "Unknown Album"
                    val durationMs = it.getLong(durCol)
                    val data = it.getString(dataCol) ?: ""
                    val year = it.getString(yearCol) ?: "2024"
                    val albumId = it.getLong(albumIdCol)

                    val artworkUri = "content://media/external/audio/albumart/$albumId"
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()

                    if (durationMs > 5000) { // filter out ultra short clips
                        songList.add(
                            Song(
                                title = title,
                                artist = artist,
                                album = album,
                                durationSeconds = (durationMs / 1000).toInt(),
                                genre = "Device Audio",
                                releaseYear = year,
                                lyrics = "Local offline audio file from your device storage.",
                                trivia = "Played directly from device storage.",
                                coverUrl = artworkUri,
                                audioUrl = if (data.isNotBlank()) data else contentUri,
                                source = "Device",
                                tempoBpm = 120,
                                mood = "Local",
                                energy = 0.7f
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Failed to scan device audio: ${e.localizedMessage}")
        }
        return songList
    }
}
