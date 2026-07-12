package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.remote.RetrofitClient
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Content
import com.example.data.remote.Part
import com.example.data.remote.GenerationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

data class MoodItem(
    val id: String,
    val name: String,
    val category: String,
    val colorHex: Long,
    val description: String,
    val suggestedBpm: Int,
    val suggestedInstruments: List<String>,
    val suggestedGenre: String,
    val suggestedVocal: String,
    val suggestedKey: String = "C#",
    val suggestedScale: String = "Major",
    val suggestedChords: String = "I - IV - V - vi",
    val isFavorite: Boolean = false
)

data class MoodCategory(
    val name: String,
    val colorHex: Long,
    val iconName: String,
    val items: List<MoodItem>
)

data class DetectionResult(
    val primaryMood: String,
    val secondaryMood: String,
    val emotionStrength: Int, // 0 - 100
    val confidenceScore: Int, // 0 - 100
    val suggestedMusicStyle: String,
    val suggestedTempo: String, // e.g. "90 BPM"
    val suggestedInstruments: List<String>,
    val suggestedSinger: String,
    val suggestedGenre: String,
    val suggestedKeyScale: String
)

data class CustomMoodPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val romanticPercent: Int,
    val sadPercent: Int,
    val energeticPercent: Int,
    val positivePercent: Int,
    val spiritualPercent: Int,
    val intensity: Int,
    val createdTimestamp: Long = System.currentTimeMillis()
)

class MoodViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("surmaya_mood_prefs", Context.MODE_PRIVATE)

    // --- Static Library definition ---
    val categories = listOf(
        MoodCategory("Happiness", 0xFFF9D142, "sentiment_very_satisfied", listOf(
            MoodItem("happy", "Happy", "Happiness", 0xFFF9D142, "Unbridled optimism and joyful rhythms", 125, listOf("Piano", "Acoustic Guitar", "Drums", "Bass"), "Bollywood Pop", "Energetic & Cheerful", "C#", "Bilawal / Major"),
            MoodItem("joyful", "Joyful", "Happiness", 0xFFFAD02C, "Bright celebration of positive vibes", 128, listOf("Violin", "Flute", "Acoustic Guitar", "Dholak"), "Modern Folk Fusion", "Bright & Playful"),
            MoodItem("cheerful", "Cheerful", "Happiness", 0xFFF7C325, "Lighthearted, simple and welcoming tunes", 115, listOf("Ukulele", "Piano", "Bansuri", "Handclaps"), "Acoustic Pop", "Sweet & Warm"),
            MoodItem("playful", "Playful", "Happiness", 0xFFECC45C, "Quirky tempo with delightful pitch swings", 130, listOf("Mandolin", "Shakers", "Bass", "Synthesizer"), "Upbeat Indie", "Sassy & Dynamic"),
            MoodItem("celebration", "Celebration", "Happiness", 0xFFFFE066, "Grand festive beats for mass gatherings", 135, listOf("Dhol", "Brass Section", "Shehnai", "Drums"), "Dandiya / Pop", "High Pitch & Powerful"),
            MoodItem("fun", "Fun", "Happiness", 0xFFFCD581, "Wacky grooves perfect for party playlists", 120, listOf("Synthesizer", "Electric Guitar", "Congas"), "Dance-Pop", "Playful & Bold"),
            MoodItem("excited", "Excited", "Happiness", 0xFFF39C12, "Rapid tempo build ups with ecstatic drops", 132, listOf("Synth Leads", "Electric Drums", "Sitar"), "EDM Fusion", "Vibrant & Euphoric"),
            MoodItem("positive", "Positive", "Happiness", 0xFFF1C40F, "Warm assurance and hopeful acoustic chords", 110, listOf("Acoustic Guitar", "Pads", "Bansuri"), "Sufi Pop", "Soothing & Confident")
        )),
        MoodCategory("Love & Romance", 0xFFFF6B6B, "favorite", listOf(
            MoodItem("romantic", "Romantic", "Love & Romance", 0xFFFF6B6B, "Intimate acoustic strumming with lush pads", 84, listOf("Piano", "Acoustic Guitar", "Strings", "Flute", "Soft Pads"), "Bollywood Romantic", "Warm, Soft & Romantic", "F#", "Yaman / Lydian"),
            MoodItem("love", "Love", "Love & Romance", 0xFFFA8282, "Deep emotional attachment and sweeping orchestration", 90, listOf("Violin Ensemble", "Grand Piano", "Nylon Guitar"), "Cinematic Ballad", "Soulful & Breathy"),
            MoodItem("first_love", "First Love", "Love & Romance", 0xFFFF8E8E, "Shy acoustic melodies and soft flute notes", 80, listOf("Acoustic Guitar", "Bansuri", "Chimes"), "Acoustic Pop", "Sweet, Innocent & Soft"),
            MoodItem("proposal", "Proposal", "Love & Romance", 0xFFFF4A4A, "Grand dramatic build up with symphonic strings", 72, listOf("Grand Piano", "Cello", "Harp", "French Horn"), "Classical Crossover", "Passionate & Heavy"),
            MoodItem("cute_love", "Cute Love", "Love & Romance", 0xFFFF7B9C, "Bubbly, lighthearted patterns with soft percussion", 100, listOf("Ukulele", "Marimba", "Acoustic Bass"), "Indie Cute", "Sweet & Soft"),
            MoodItem("deep_love", "Deep Love", "Love & Romance", 0xFFE056FD, "Intense spiritual romance with rich resonant keys", 76, listOf("Sitar", "Esraj", "Soft Synth Pads"), "Sufi Semi-Classical", "Resonant & Emotional"),
            MoodItem("soulmate", "Soulmate", "Love & Romance", 0xFFBE2EDD, "Eternal connection expressed in warm violin solos", 85, listOf("Violin", "Acoustic Guitar", "Harmonium"), "Ghazal Fusion", "Deep & Soulful"),
            MoodItem("long_distance_love", "Long Distance Love", "Love & Romance", 0xFF9B59B6, "Nostalgic piano keys combined with echoes", 68, listOf("Muted Guitar", "Ambient Piano", "Shehnai"), "Ambient Lo-Fi", "Melancholy & Breathy")
        )),
        MoodCategory("Emotional", 0xFF54A0FF, "sentiment_very_dissatisfied", listOf(
            MoodItem("sad", "Sad", "Emotional", 0xFF54A0FF, "Slow, heartbreaking melodies with heavy resonance", 65, listOf("Piano", "Violin", "Flute", "Soft Guitar", "Strings"), "Ghazal / Sad Pop", "Emotional, Soft & Breathy", "D#", "Bhairavi / Minor"),
            MoodItem("heartbreak", "Heartbreak", "Emotional", 0xFF2E86DE, "Wailing solo string sections and sharp drum hits", 75, listOf("Esraj", "Violin Solo", "Acoustic Guitar"), "Tragic Bollywood", "Painful & High Pitch"),
            MoodItem("lonely", "Lonely", "Emotional", 0xFF48DBFB, "Echoing solo flute surrounded by deep dark silence", 60, listOf("Bansuri Solo", "Ambient Synth Pads", "Harp"), "Sparsely Arranged Lo-Fi", "Whispering & Breathless"),
            MoodItem("missing_someone", "Missing Someone", "Emotional", 0xFF00D2D3, "Melancholy guitar plucks with slow wind instruments", 70, listOf("Nylon Guitar", "Shehnai", "Harmonium"), "Nostalgic Retro", "Soulful & Yearning"),
            MoodItem("crying", "Crying", "Emotional", 0xFF0ABDE3, "Intense weeping strings and high pitch lamentations", 58, listOf("Violoncello", "Sarangi Solo", "Piano"), "Symphonic Tragedy", "Trembling & Tearful"),
            MoodItem("regret", "Regret", "Emotional", 0xFF1DD1A1, "Muted somber piano notes with minor bass lines", 64, listOf("Upright Piano", "Synthesizer Pads", "Cello"), "Artistic Indie", "Low Pitch & Somber"),
            MoodItem("separation", "Separation", "Emotional", 0xFF10AC84, "Echoing separation of sitar strokes and slow tabla beats", 62, listOf("Sitar Solo", "Slow Tabla", "Tanpura"), "Classical Raga", "Grave & Philosophical"),
            MoodItem("emotional_healing", "Emotional Healing", "Emotional", 0xFF576574, "Hopeful minor-to-major scales with positive resolution", 80, listOf("Acoustic Guitar", "Bansuri", "Chimes", "Pads"), "Meditative New Age", "Warm & Calming")
        )),
        MoodCategory("Energy", 0xFFFF5252, "bolt", listOf(
            MoodItem("energetic", "Energetic", "Energy", 0xFFFF5252, "Screaming electric synth lines and stomping beats", 128, listOf("Electric Synth", "Electric Drums", "Sitar"), "EDM Pop", "Aggressive & Energetic"),
            MoodItem("powerful", "Powerful", "Energy", 0xFFFF7675, "Epic orchestrations with heavy percussions", 120, listOf("Timpani", "Brass Section", "Choir", "Distorted Sitar"), "Epic Cinematic", "Heroic & Thundering"),
            MoodItem("heroic", "Heroic", "Energy", 0xFFD63031, "Valiant lead horns and soaring string sweeps", 115, listOf("French Horns", "Snare Drums", "Symphonic Strings"), "Symphonic Rock", "Resolute & Bold"),
            MoodItem("motivational", "Motivational", "Energy", 0xFFEE5253, "Uplifting tempo with high resonance power chords", 110, listOf("Acoustic Guitar", "Rock Drums", "Electric Bass"), "Arena Rock", "Inspirational & High Pitch"),
            MoodItem("inspirational", "Inspirational", "Energy", 0xFFFF2E2E, "Beautiful progressive crescendo towards a peak", 95, listOf("Grand Piano", "Violin Sections", "Drums"), "Contemporary Orchestral", "Soulful & Triumphant"),
            MoodItem("victory", "Victory", "Energy", 0xFFEA2027, "Marching snare rhythms, trumpets and dhol beats", 130, listOf("Trumpet", "Dhol", "Marching Snares"), "Patriotic Rock", "Shouting & Triumphant"),
            MoodItem("workout", "Workout", "Energy", 0xFFFF4757, "Aggressive sub-bass lines and repetitive synth riffs", 132, listOf("Synth Sub-Bass", "Drum Machine", "Electric Guitar"), "Tech House Fusion", "Punchy & Intense"),
            MoodItem("dance", "Dance", "Energy", 0xFFFF6B81, "Punchy dholak rolls paired with energetic synths", 124, listOf("Dholak", "Synthesizer", "E-Bass", "Claps"), "Desi Club / Dance", "Vibrant & Catchy")
        )),
        MoodCategory("Spiritual", 0xFFFFA801, "brightness_high", listOf(
            MoodItem("spiritual", "Spiritual", "Spiritual", 0xFFFFA801, "Deep peaceful chants and traditional classical instruments", 75, listOf("Tabla", "Mridangam", "Veena", "Sitar", "Sarangi", "Flute", "Tanpura"), "Bhajan / Devotional", "Devotional, Peaceful & Pure", "A#", "Bhairav / Double Harmonic"),
            MoodItem("meditation", "Meditation", "Spiritual", 0xFFFFC048, "Sparsely mapped tanpura drone and whispering wind", 50, listOf("Tanpura", "Bansuri Solo", "Tibetan Bowls"), "Zen Ambient", "Whispery & Slow"),
            MoodItem("bhajan", "Bhajan", "Spiritual", 0xFFFF9F1A, "Rhythmic harmonium sweeps with traditional kartal sounds", 90, listOf("Harmonium", "Dholak", "Kartal", "Manjira"), "Traditional Bhajan", "Chanting & Devotional"),
            MoodItem("devotional", "Devotional", "Spiritual", 0xFFFECA57, "Lyrically intensive tracks with serene orchestration", 80, listOf("Violins", "Flute", "Acoustic Guitar", "Tabla"), "Devotional Pop", "Melodious & Soulful"),
            MoodItem("peaceful", "Peaceful", "Spiritual", 0xFFFFEAA7, "Gentle soundscapes that ease stress and calm the mind", 70, listOf("Acoustic Guitar", "Bansuri", "Warm Synth Pads"), "Chillout Ambient", "Soft & Calm"),
            MoodItem("divine", "Divine", "Spiritual", 0xFFFFEE58, "Ethereal female choir lines and high sitar frequencies", 68, listOf("Sitar Solo", "Female Ambient Choir", "Santoor"), "Spiritual Orchestral", "Angelic & Breathy"),
            MoodItem("temple", "Temple", "Spiritual", 0xFFFFA726, "Ringing temple bells combined with ancient chants", 65, listOf("Temple Bells", "Conch Shell", "Mridangam", "Chants"), "Vedic Sacred", "Deep Bass Chant"),
            MoodItem("prayer", "Prayer", "Spiritual", 0xFFFB8C00, "Subdued devotional hums with simple string chords", 72, listOf("Esraj", "Tanpura", "Harmonium"), "Traditional Prayer", "Sincere & Soft")
        )),
        MoodCategory("Seasonal", 0xFF00D2D3, "cloudy", listOf(
            MoodItem("rain", "Rain", "Seasonal", 0xFF00D2D3, "Soothing pitter patter beats with classical sarangi", 72, listOf("Rainmaker", "Sarangi", "Nylon Guitar", "Bansuri"), "Classical Rain Raga (Megh)", "Melancholic & Flowing"),
            MoodItem("winter", "Winter", "Seasonal", 0xFF54A0FF, "Cold ambient reverbs with glass-like santoor hits", 85, listOf("Santoor", "Chimes", "Ambient Reverb Pad"), "Winter Ambient", "Crisp, Clear & Soft"),
            MoodItem("summer", "Summer", "Seasonal", 0xFFFF9F1A, "Bright beach vibes with high pitch acoustic guitar", 120, listOf("Acoustic Guitar", "Shakers", "Bass", "Congas"), "Reggae Pop", "Sunny & Warm"),
            MoodItem("spring", "Spring", "Seasonal", 0xFF10AC84, "Fresh blossoming melodies with sweet flute notes", 105, listOf("Bansuri", "Sitar (Drut)", "Mridangam"), "Classical Basant Raga", "Playful & Vibrant"),
            MoodItem("autumn", "Autumn", "Seasonal", 0xFFD35400, "Rustic acoustic strums with nostalgic violin slides", 80, listOf("12-String Guitar", "Violoncello", "Flute"), "Indie Folk", "Warm, Nostalgic & Mellow"),
            MoodItem("morning", "Morning", "Seasonal", 0xFFFFF9C4, "Dawn birds chirping with santoor and tanpura", 60, listOf("Tanpura", "Santoor Solo", "Bansuri"), "Morning Alaap", "Serene & Refreshing"),
            MoodItem("evening", "Evening", "Seasonal", 0xFF5C6BC0, "Sunset orange colors depicted in slow warm chords", 82, listOf("Classical Guitar", "Harmonium", "Tabla"), "Ghazal Classic", "Warm & Mellow"),
            MoodItem("night", "Night", "Seasonal", 0xFF1A237E, "Starry velvet sky simulated via distant deep synths", 68, listOf("Synthesizer Pads", "Acoustic Guitar", "Cello"), "Midnight Chillout", "Dreamy & Whispering")
        )),
        MoodCategory("Festival", 0xFFE056FD, "celebration", listOf(
            MoodItem("festival", "Festival", "Festival", 0xFFE056FD, "Boisterous high volume percussions and loud chants", 132, listOf("Dhol", "Tasha", "Brass Band", "Chorus"), "Indian Festive Beat", "Shouting & High Energy"),
            MoodItem("wedding", "Wedding", "Festival", 0xFFF8A5C2, "Emotional yet joyful shehnai with heavy dholak", 115, listOf("Shehnai", "Dholak", "Acoustic Guitar", "Chimes"), "Traditional Wedding", "Emotional & Warm"),
            MoodItem("holi", "Holi", "Festival", 0xFFFF4757, "Explosion of colors through high speed dholak rolls", 130, listOf("Dholak", "Dhol", "Duff", "Flute"), "Holi Folk Pop", "Rowdy & Joyful"),
            MoodItem("diwali", "Diwali", "Festival", 0xFFFFE066, "Sparkling sitar speed runs paired with light dholak", 120, listOf("Sitar (Jhalla)", "Santoor", "Manjira", "Tabla"), "Festive Devotional", "Bright & Cheerful"),
            MoodItem("durga_puja", "Durga Puja", "Festival", 0xFFEA2027, "Traditional sound of Dhak drums with conch shell blows", 135, listOf("Dhak", "Kashor", "Conch Shell", "Choir"), "Devotional High-Beat", "Majestic & Powerful"),
            MoodItem("rath_yatra", "Rath Yatra", "Festival", 0xFFFF9100, "Ecstatic spiritual dancing beats with khol and cymbals", 125, listOf("Khol", "Kartal", "Harmonium"), "Traditional Sankirtan", "Chanting & Ecstatic"),
            MoodItem("eid", "Eid", "Festival", 0xFF00E676, "Sufistic qawwali beats with soulful harmonium", 112, listOf("Tabla", "Harmonium", "Claps", "Sufi Choir"), "Sufi Qawwali", "Soulful & Passionate"),
            MoodItem("christmas", "Christmas", "Festival", 0xFFE53935, "Jingle bell accents and warm orchestral strings", 118, listOf("Sleigh Bells", "Tubular Bells", "Orchestra", "Piano"), "Symphonic Holiday", "Cheerful & Festive")
        )),
        MoodCategory("Travel", 0xFF4AD8DA, "flight_takeoff", listOf(
            MoodItem("travel", "Travel", "Travel", 0xFF4AD8DA, "Breezy tempo with acoustic guitar and shakers", 115, listOf("Acoustic Guitar", "Shakers", "Bass", "Flute"), "Roadtrip Acoustic", "Breezy & Carefree"),
            MoodItem("adventure", "Adventure", "Travel", 0xFF00B894, "Fast dramatic build ups and thumping drumbeats", 126, listOf("Drums", "Electric Bass", "Synth Leads"), "Adrenaline Rock", "Aggressive & Inspiring"),
            MoodItem("road_trip", "Road Trip", "Travel", 0xFF0984E3, "Steady driving beats and beautiful guitar leads", 120, listOf("Electric Guitar", "Acoustic Guitar", "Rock Bass"), "Alternative Rock", "Youthful & Energetic"),
            MoodItem("mountain", "Mountain", "Travel", 0xFFECCC68, "Reverbed echo sounds, acoustic picking and bansuri", 92, listOf("Acoustic Guitar", "Bansuri", "Chimes", "Echo Synth"), "Mountain Indie", "Eco-friendly & Soft"),
            MoodItem("beach", "Beach", "Travel", 0xFFFF7F50, "Sunset lounge house drums with nylon guitar strokes", 110, listOf("Nylon Guitar", "Percussions", "Deep Sub Synth"), "Tropical Lounge", "Chill & Relaxed"),
            MoodItem("nature", "Nature", "Travel", 0xFF2ECC71, "Organic wooden drums and delicate bansuri notes", 85, listOf("Wooden Drums", "Bansuri Solo", "Harp", "Tanpura"), "Forest Ambient", "Soothing & Peaceful"),
            MoodItem("journey", "Journey", "Travel", 0xFF34495E, "Steady progressive rhythm depicting long travels", 100, listOf("Acoustic Guitar", "Piano", "String Pad", "Tabla"), "Progressive Folk", "Warm & Thoughtful"),
            MoodItem("freedom", "Freedom", "Travel", 0xFF1ABC9C, "Explosive epic drops, screaming violins and drums", 122, listOf("Violin Ensemble", "Drums", "Acoustic Guitar"), "Euphoric Indie", "Free & High Pitch")
        )),
        MoodCategory("National", 0xFFFF9100, "flag", listOf(
            MoodItem("patriotic", "Patriotic", "National", 0xFFFF9100, "Stately march beats with heroic shehnai and brass", 105, listOf("Marching Drums", "Brass", "Strings", "Choir", "Percussion"), "Patriotic Anthem", "Powerful, Heroic & Inspirational", "D", "Shankh / Major"),
            MoodItem("army", "Army", "National", 0xFF4B6584, "Strict snare rolls and powerful military band motifs", 120, listOf("Snare Drums", "Trumpet", "French Horn", "Cymbals"), "Military March", "Commanding & Stern"),
            MoodItem("independence", "Independence", "National", 0xFF10AC84, "Grand celebratory drums and soaring strings", 112, listOf("Dholak", "Dhol", "Grand Violin Section", "Bansuri"), "Modern Patriotic Pop", "Triumphant & Majestic"),
            MoodItem("republic_day", "Republic Day", "National", 0xFF388E3C, "Full brass orchestra playing prideful melodies", 100, listOf("Full Brass Band", "Orchestral Snares", "Flute"), "Orchestral March", "Proud & Clear"),
            MoodItem("national_pride", "National Pride", "National", 0xFFF57C00, "Soaring classical raga melodies with fast sitar jhalla", 120, listOf("Sitar", "Tabla (Fast)", "Flute", "Santoor"), "Symphonic Classical", "Grand & Majestic"),
            MoodItem("tribute", "Tribute", "National", 0xFF7F8C8D, "Somber yet heroic cellos expressing extreme gratitude", 75, listOf("Cello Solo", "Slow Strings", "Muted Timpani"), "Solemn Eulogy", "Grave & Respectful"),
            MoodItem("victory_nat", "Victory", "National", 0xFFD32F2F, "Clashing cymbals and ecstatic dhol rolls", 125, listOf("Dhol", "Cymbals", "Trumpet", "Drums"), "Symphonic Rock", "Ecstatic & High Energy"),
            MoodItem("unity", "Unity", "National", 0xFF00ACC1, "Collaborative folk instruments from all states playing in unison", 95, listOf("Sitar", "Mridangam", "Dotara", "Bansuri", "Dholak"), "National Integration Folk", "Unified & Harmonious")
        ))
    )

    // --- Search & Filters ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites

    private val _recentlyUsed = MutableStateFlow<List<String>>(emptyList())
    val recentlyUsed: StateFlow<List<String>> = _recentlyUsed

    private val _selectedMoodItem = MutableStateFlow<MoodItem?>(null)
    val selectedMoodItem: StateFlow<MoodItem?> = _selectedMoodItem

    // --- Custom Mood Builder ---
    val customRomantic = MutableStateFlow(50f)
    val customSad = MutableStateFlow(10f)
    val customEnergetic = MutableStateFlow(40f)
    val customPositive = MutableStateFlow(60f)
    val customSpiritual = MutableStateFlow(20f)
    val customIntensity = MutableStateFlow(75f)

    private val _savedCustomPresets = MutableStateFlow<List<CustomMoodPreset>>(emptyList())
    val savedCustomPresets: StateFlow<List<CustomMoodPreset>> = _savedCustomPresets

    // --- AI Mood Detector ---
    val detectorInputText = MutableStateFlow("")
    val detectorType = MutableStateFlow("Lyrics") // "Lyrics", "Prompt", "Story", "Poem", "Uploaded Audio"
    
    private val _detectionResult = MutableStateFlow<DetectionResult?>(null)
    val detectionResult: StateFlow<DetectionResult?> = _detectionResult

    private val _isDetecting = MutableStateFlow(false)
    val isDetecting: StateFlow<Boolean> = _isDetecting

    private val _detectionHistory = MutableStateFlow<List<Pair<String, DetectionResult>>>(emptyList())
    val detectionHistory: StateFlow<List<Pair<String, DetectionResult>>> = _detectionHistory

    init {
        loadSavedData()
    }

    private fun loadSavedData() {
        // Load Favorites
        val savedFavs = sharedPrefs.getStringSet("mood_favorites", emptySet()) ?: emptySet()
        _favorites.value = savedFavs

        // Load Recently Used
        val savedRecent = sharedPrefs.getString("mood_recently_used", "") ?: ""
        if (savedRecent.isNotBlank()) {
            _recentlyUsed.value = savedRecent.split(",").filter { it.isNotBlank() }
        }

        // Load Custom Presets
        val savedPresetsStr = sharedPrefs.getString("custom_presets_json", "[]") ?: "[]"
        try {
            val presets = mutableListOf<CustomMoodPreset>()
            val jsonArray = org.json.JSONArray(savedPresetsStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                presets.add(CustomMoodPreset(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    name = obj.getString("name"),
                    romanticPercent = obj.getInt("romantic"),
                    sadPercent = obj.getInt("sad"),
                    energeticPercent = obj.getInt("energetic"),
                    positivePercent = obj.getInt("positive"),
                    spiritualPercent = obj.getInt("spiritual"),
                    intensity = obj.getInt("intensity"),
                    createdTimestamp = obj.optLong("timestamp", System.currentTimeMillis())
                ))
            }
            _savedCustomPresets.value = presets
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun toggleShowOnlyFavorites() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun selectMoodItem(item: MoodItem?) {
        _selectedMoodItem.value = item
        if (item != null) {
            addToRecentlyUsed(item.id)
        }
    }

    private fun addToRecentlyUsed(id: String) {
        val currentList = _recentlyUsed.value.toMutableList()
        currentList.remove(id)
        currentList.add(0, id)
        if (currentList.size > 8) {
            currentList.removeAt(currentList.size - 1)
        }
        _recentlyUsed.value = currentList
        sharedPrefs.edit().putString("mood_recently_used", currentList.joinToString(",")).apply()
    }

    fun toggleFavorite(id: String) {
        val currentFavs = _favorites.value.toMutableSet()
        if (currentFavs.contains(id)) {
            currentFavs.remove(id)
        } else {
            currentFavs.add(id)
        }
        _favorites.value = currentFavs
        sharedPrefs.edit().putStringSet("mood_favorites", currentFavs).apply()
    }

    fun isFavorite(id: String): Boolean {
        return _favorites.value.contains(id)
    }

    // --- Custom Mood Preset Management ---
    fun saveCustomPreset(name: String) {
        if (name.isBlank()) return
        val newPreset = CustomMoodPreset(
            name = name,
            romanticPercent = customRomantic.value.toInt(),
            sadPercent = customSad.value.toInt(),
            energeticPercent = customEnergetic.value.toInt(),
            positivePercent = customPositive.value.toInt(),
            spiritualPercent = customSpiritual.value.toInt(),
            intensity = customIntensity.value.toInt()
        )
        val updatedList = _savedCustomPresets.value.toMutableList()
        updatedList.add(0, newPreset)
        _savedCustomPresets.value = updatedList
        savePresetsToPrefs(updatedList)
    }

    fun deleteCustomPreset(presetId: String) {
        val updatedList = _savedCustomPresets.value.filter { it.id != presetId }
        _savedCustomPresets.value = updatedList
        savePresetsToPrefs(updatedList)
    }

    private fun savePresetsToPrefs(list: List<CustomMoodPreset>) {
        try {
            val jsonArray = org.json.JSONArray()
            for (preset in list) {
                val obj = JSONObject()
                obj.put("id", preset.id)
                obj.put("name", preset.name)
                obj.put("romantic", preset.romanticPercent)
                obj.put("sad", preset.sadPercent)
                obj.put("energetic", preset.energeticPercent)
                obj.put("positive", preset.positivePercent)
                obj.put("spiritual", preset.spiritualPercent)
                obj.put("intensity", preset.intensity)
                obj.put("timestamp", preset.createdTimestamp)
                jsonArray.put(obj)
            }
            sharedPrefs.edit().putString("custom_presets_json", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyPresetValues(preset: CustomMoodPreset) {
        customRomantic.value = preset.romanticPercent.toFloat()
        customSad.value = preset.sadPercent.toFloat()
        customEnergetic.value = preset.energeticPercent.toFloat()
        customPositive.value = preset.positivePercent.toFloat()
        customSpiritual.value = preset.spiritualPercent.toFloat()
        customIntensity.value = preset.intensity.toFloat()
    }

    // --- AI Mood Detector ---
    fun runMoodDetection() {
        val inputText = detectorInputText.value
        if (inputText.isBlank()) return

        _isDetecting.value = true
        _detectionResult.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // local fallback simulation with random delay
                delay(3000)
                val fallbackResult = generateLocalFallBackDetection(inputText)
                withContext(Dispatchers.Main) {
                    _detectionResult.value = fallbackResult
                    _isDetecting.value = false
                    _detectionHistory.value = listOf(Pair(inputText, fallbackResult)) + _detectionHistory.value
                }
            } else {
                try {
                    val prompt = """
                        You are an expert music and emotional intelligence system for the music app SurMaya AI.
                        Analyze the following $detectorType:
                        ---
                        $inputText
                        ---
                        Provide emotional mood detection, music style recommendation, instruments and vocals in strict JSON format. 
                        Keys:
                        "primaryMood" (String - e.g. "Romantic", "Sad", "Spiritual", "Energetic", "Happy", "Patriotic")
                        "secondaryMood" (String)
                        "emotionStrength" (Integer between 0 and 100)
                        "confidenceScore" (Integer between 0 and 100)
                        "suggestedMusicStyle" (String)
                        "suggestedTempo" (String e.g. "80 BPM" or "120 BPM")
                        "suggestedInstruments" (Array of Strings)
                        "suggestedSinger" (String e.g. "Swaraj (Warm Sufi)", "Asha (Sweet Vocal)")
                        "suggestedGenre" (String)
                        "suggestedKeyScale" (String)
                        Do not include markdown or backticks, just raw JSON.
                    """.trimIndent()

                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                        generationConfig = GenerationConfig(temperature = 0.5f)
                    )

                    val response = RetrofitClient.service.generateContent(apiKey, request)
                    val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    
                    if (!jsonText.isNullOrBlank()) {
                        // Clean markdown if models mistakenly returned it
                        val cleanJson = jsonText.trim()
                            .removePrefix("```json")
                            .removePrefix("```")
                            .removeSuffix("```")
                            .trim()

                        val json = JSONObject(cleanJson)
                        val instrumentsList = mutableListOf<String>()
                        val instrumentsArray = json.optJSONArray("suggestedInstruments")
                        if (instrumentsArray != null) {
                            for (i in 0 until instrumentsArray.length()) {
                                instrumentsList.add(instrumentsArray.getString(i))
                            }
                        } else {
                            instrumentsList.addAll(listOf("Piano", "Acoustic Guitar", "Strings"))
                        }

                        val result = DetectionResult(
                            primaryMood = json.optString("primaryMood", "Romantic"),
                            secondaryMood = json.optString("secondaryMood", "Happy"),
                            emotionStrength = json.optInt("emotionStrength", 75),
                            confidenceScore = json.optInt("confidenceScore", 85),
                            suggestedMusicStyle = json.optString("suggestedMusicStyle", "Contemporary Pop"),
                            suggestedTempo = json.optString("suggestedTempo", "95 BPM"),
                            suggestedInstruments = instrumentsList,
                            suggestedSinger = json.optString("suggestedSinger", "Shrija (Sweet Pop)"),
                            suggestedGenre = json.optString("suggestedGenre", "Bollywood Pop"),
                            suggestedKeyScale = json.optString("suggestedKeyScale", "C# Yaman")
                        )

                        withContext(Dispatchers.Main) {
                            _detectionResult.value = result
                            _isDetecting.value = false
                            _detectionHistory.value = listOf(Pair(inputText, result)) + _detectionHistory.value
                        }
                    } else {
                        throw Exception("Empty response from Gemini")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback to local on error
                    delay(1500)
                    val fallbackResult = generateLocalFallBackDetection(inputText)
                    withContext(Dispatchers.Main) {
                        _detectionResult.value = fallbackResult
                        _isDetecting.value = false
                        _detectionHistory.value = listOf(Pair(inputText, fallbackResult)) + _detectionHistory.value
                    }
                }
            }
        }
    }

    private fun generateLocalFallBackDetection(text: String): DetectionResult {
        val lowercaseText = text.lowercase()
        return when {
            lowercaseText.contains("sad") || lowercaseText.contains("heart") || lowercaseText.contains("lonely") || lowercaseText.contains("missing") || lowercaseText.contains("separation") || lowercaseText.contains("rula") -> {
                DetectionResult(
                    primaryMood = "Emotional (Sad)",
                    secondaryMood = "Nostalgic",
                    emotionStrength = 88,
                    confidenceScore = 92,
                    suggestedMusicStyle = "Slow Ghazal & Ambient Acoustic",
                    suggestedTempo = "65 BPM",
                    suggestedInstruments = listOf("Sarangi", "Esraj", "Soft Piano", "Flute", "Acoustic Guitar"),
                    suggestedSinger = "Ajit (Emotional Male Voice)",
                    suggestedGenre = "Bollywood Sad Ballad",
                    suggestedKeyScale = "D# Bhairavi / Minor"
                )
            }
            lowercaseText.contains("love") || lowercaseText.contains("romantic") || lowercaseText.contains("proposal") || lowercaseText.contains("proposal") || lowercaseText.contains("pyar") || lowercaseText.contains("ishq") -> {
                DetectionResult(
                    primaryMood = "Love & Romance",
                    secondaryMood = "Positive",
                    emotionStrength = 95,
                    confidenceScore = 96,
                    suggestedMusicStyle = "Sweet Acoustic Melodies & Symphonic Strings",
                    suggestedTempo = "82 BPM",
                    suggestedInstruments = listOf("Acoustic Guitar", "Piano", "Bansuri Flute", "Warm Pads", "Violin Solo"),
                    suggestedSinger = "Shrija (Sweet Female Voice)",
                    suggestedGenre = "Bollywood Romantic Pop",
                    suggestedKeyScale = "F# Yaman / Lydian"
                )
            }
            lowercaseText.contains("krishna") || lowercaseText.contains("shiva") || lowercaseText.contains("ram") || lowercaseText.contains("god") || lowercaseText.contains("spiritual") || lowercaseText.contains("peace") || lowercaseText.contains("bhajan") || lowercaseText.contains("devotional") -> {
                DetectionResult(
                    primaryMood = "Spiritual",
                    secondaryMood = "Peaceful",
                    emotionStrength = 90,
                    confidenceScore = 94,
                    suggestedMusicStyle = "Sacred Meditative Bhakti & Classical Fusion",
                    suggestedTempo = "72 BPM",
                    suggestedInstruments = listOf("Tabla", "Harmonium", "Tanpura", "Sitar Solo", "Kartal", "Manjira"),
                    suggestedSinger = "Anup (Devotional Bhajan)",
                    suggestedGenre = "Classical Bhajan",
                    suggestedKeyScale = "A# Bhairav / Double Harmonic"
                )
            }
            lowercaseText.contains("desh") || lowercaseText.contains("india") || lowercaseText.contains("patriotic") || lowercaseText.contains("tiranga") || lowercaseText.contains("army") || lowercaseText.contains("hero") -> {
                DetectionResult(
                    primaryMood = "National (Patriotic)",
                    secondaryMood = "Heroic",
                    emotionStrength = 94,
                    confidenceScore = 95,
                    suggestedMusicStyle = "Epic Symphonic Marching & Orchestral Rock",
                    suggestedTempo = "105 BPM",
                    suggestedInstruments = listOf("Marching Snares", "French Horns", "Choir", "Cymbals", "Distorted Sitar"),
                    suggestedSinger = "Sukhwinder (Powerful High-Pitch Male)",
                    suggestedGenre = "Patriotic Symphony",
                    suggestedKeyScale = "D Major / Shankh Raga"
                )
            }
            lowercaseText.contains("dance") || lowercaseText.contains("party") || lowercaseText.contains("fun") || lowercaseText.contains("celebrate") || lowercaseText.contains("nache") || lowercaseText.contains("dhamaka") -> {
                DetectionResult(
                    primaryMood = "Energy",
                    secondaryMood = "Excited",
                    emotionStrength = 92,
                    confidenceScore = 89,
                    suggestedMusicStyle = "Fast Upbeat Desi Club Beat",
                    suggestedTempo = "128 BPM",
                    suggestedInstruments = listOf("Dholak", "Dhol", "Synthesizer Leads", "Sub-Bass", "Electronic Drums"),
                    suggestedSinger = "Badshah (Rap/Vocal Group)",
                    suggestedGenre = "Bollywood Dance Pop",
                    suggestedKeyScale = "G# Minor"
                )
            }
            else -> {
                // Default generic positive
                DetectionResult(
                    primaryMood = "Happiness (Joyful)",
                    secondaryMood = "Positive",
                    emotionStrength = 82,
                    confidenceScore = 85,
                    suggestedMusicStyle = "Modern Folk-Pop Fusion",
                    suggestedTempo = "118 BPM",
                    suggestedInstruments = listOf("Piano", "Acoustic Guitar", "Ukulele", "Flute", "Light Tabla"),
                    suggestedSinger = "Siddharth (Bright Male Pop)",
                    suggestedGenre = "Bollywood Indie Pop",
                    suggestedKeyScale = "C# Bilawal / Major"
                )
            }
        }
    }

    // --- Automatic recommendations for custom mood values ---
    fun getCustomMoodRecommendations(): Map<String, String> {
        val r = customRomantic.value
        val s = customSad.value
        val e = customEnergetic.value
        val p = customPositive.value
        val sp = customSpiritual.value
        
        val maxVal = maxOf(r, s, e, p, sp)
        
        return when (maxVal) {
            r -> mapOf(
                "Genre" to "Bollywood Romantic Ballad",
                "BPM" to "82 - 96 BPM",
                "Scale" to "Yaman / Major",
                "Key" to "F# / C#",
                "Instruments" to "Nylon Guitar, Flute, Warm Pads, Violin Solo",
                "SingerStyle" to "Warm, Soft & Romantic (Ajit / Shrija)",
                "ChordProgression" to "I - vi - IV - V (e.g. C - Am - F - G)",
                "MasteringPreset" to "Warm Acoustic Analogue"
            )
            s -> mapOf(
                "Genre" to "Bollywood Sad & Ghazal Fusion",
                "BPM" to "60 - 75 BPM",
                "Scale" to "Bhairavi / Minor",
                "Key" to "D# / A minor",
                "Instruments" to "Esraj, Sarangi, Somber Piano, Slow Sitar",
                "SingerStyle" to "Emotional, Soft & Breathless with Tremolo",
                "ChordProgression" to "i - VI - III - VII (Minor Raga Ballad)",
                "MasteringPreset" to "Deep Spatial Dynamic"
            )
            e -> mapOf(
                "Genre" to "Desi Club / Upbeat EDM Pop",
                "BPM" to "120 - 132 BPM",
                "Scale" to "Kafi / Minor",
                "Key" to "G# / E minor",
                "Instruments" to "Dhol, Dholak, Synth Leads, Bass Machine, Claps",
                "SingerStyle" to "Aggressive, Powerful, High Pitch Energetic",
                "ChordProgression" to "i - iv - VII - VI (Dance Sequence)",
                "MasteringPreset" to "Aggressive Club Limiter"
            )
            sp -> mapOf(
                "Genre" to "Meditative Sufi & Bhajan Devotional",
                "BPM" to "70 - 85 BPM",
                "Scale" to "Bhairav / Double Harmonic",
                "Key" to "A# / D#",
                "Instruments" to "Tabla, Tanpura, Veena, Santoor, Khol, Manjira",
                "SingerStyle" to "Pure, Devotional, Pitch-Perfect Alaap Voice",
                "ChordProgression" to "I - v - IV - I (Continuous Sacred Drone)",
                "MasteringPreset" to "Pure Acoustic Clarity"
            )
            else -> mapOf(
                "Genre" to "Contemporary Indian Folk Pop",
                "BPM" to "110 - 122 BPM",
                "Scale" to "Bilawal / Major",
                "Key" to "C# / G Major",
                "Instruments" to "Mandolin, Ukulele, Light Dholak, Shakers",
                "SingerStyle" to "Bright, Cheerful, Joyful & Catchy",
                "ChordProgression" to "I - V - vi - IV (Perfect Sunshine Chords)",
                "MasteringPreset" to "Bright Modern Pop Lift"
            )
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MoodViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MoodViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
