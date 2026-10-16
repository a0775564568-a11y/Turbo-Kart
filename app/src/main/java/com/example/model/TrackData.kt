package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * 12 Worlds, each with 3 track variants = 36 Tracks.
 */
enum class WorldTheme(
    val nameHe: String,
    val skyColorTop: Color,
    val skyColorBottom: Color,
    val groundColor: Color,
    val roadColor: Color,
    val roadStripeColor: Color,
    val sceneryItem: SceneryType,
    val fogColor: Color
) {
    BEACH("חוף הטורבו", Color(0xFF0288D1), Color(0xFF81D4FA), Color(0xFFFFE082), Color(0xFF455A64), Color(0xFFFFD54F), SceneryType.PALM_TREE, Color(0xFFB3E5FC)),
    DESERT("מדבר הסערות", Color(0xFFFFB300), Color(0xFFFFE082), Color(0xFFD7CCC8), Color(0xFF424242), Color(0xFFFFB300), SceneryType.CACTUS, Color(0xFFFFE082)),
    ICE("פסגת הקרח", Color(0xFF0097A7), Color(0xFFE0F7FA), Color(0xFFECEFF1), Color(0xFF78909C), Color(0xFF00E5FF), SceneryType.ICE_CRYSTAL, Color(0xFFCFD8DC)),
    VOLCANO("הר הגעש", Color(0xFFBF360C), Color(0xFFFF5722), Color(0xFF3E2723), Color(0xFF212121), Color(0xFFFF3D00), SceneryType.LAVA_ROCK, Color(0xFFD84315)),
    JUNGLE("ג'ונגל פראי", Color(0xFF2E7D32), Color(0xFFA5D6A7), Color(0xFF1B5E20), Color(0xFF37474F), Color(0xFF76FF03), SceneryType.JUNGLE_TREE, Color(0xFF388E3C)),
    NEON("עיר האורות", Color(0xFF0D47A1), Color(0xFF4A148C), Color(0xFF121212), Color(0xFF263238), Color(0xFF00E5FF), SceneryType.NEON_PILLAR, Color(0xFF311B92)),
    CASTLE("טירת המרוצים", Color(0xFF37474F), Color(0xFF78909C), Color(0xFF455A64), Color(0xFF263238), Color(0xFFFFD700), SceneryType.CASTLE_TOWER, Color(0xFF546E7A)),
    SPACE("מסלול החלל", Color(0xFF050515), Color(0xFF1A1A40), Color(0xFF0A0A20), Color(0xFF1E1B4B), Color(0xFFE040FB), SceneryType.SPACE_CRYSTAL, Color(0xFF311B92)),
    TREASURE("אי האוצרות", Color(0xFF00838F), Color(0xFF80DEEA), Color(0xFFFFCC80), Color(0xFF37474F), Color(0xFFFFD54F), SceneryType.TREASURE_CHEST, Color(0xFFB2EBF2)),
    STORM_FOREST("יער הסערות", Color(0xFF1B5E20), Color(0xFF37474F), Color(0xFF2E7D32), Color(0xFF212121), Color(0xFFFFEB3B), SceneryType.PINE_TREE, Color(0xFF2E7D32)),
    CHAMPIONS("אצטדיון האלופים", Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF283593), Color(0xFF212121), Color(0xFFFF1744), SceneryType.STADIUM_LIGHT, Color(0xFF3949AB)),
    CLOUDS("כביש העננים", Color(0xFF0277BD), Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFFECEFF1), Color(0xFF2979FF), SceneryType.CLOUD, Color(0xFFE1F5FE))
}

enum class SceneryType {
    PALM_TREE, CACTUS, ICE_CRYSTAL, LAVA_ROCK, JUNGLE_TREE,
    NEON_PILLAR, CASTLE_TOWER, SPACE_CRYSTAL, TREASURE_CHEST, PINE_TREE, STADIUM_LIGHT, CLOUD
}

enum class WeatherCondition(val nameHe: String) {
    SUNNY("שמשי ובהיר"),
    SUNSET("שקיעה"),
    RAINY("גשם שוטף"),
    STORM("סערת ברקים"),
    NIGHT("לילי מואר")
}

data class TrackSegment(
    val curve: Float = 0f,          // -1.0 (hard left) to +1.0 (hard right)
    val hill: Float = 0f,           // -1.0 (steep down) to +1.0 (steep up)
    val hasRamp: Boolean = false,
    val hasItemBox: Boolean = false,
    val hasCoins: Boolean = false,
    val hasObstacle: Boolean = false,
    val obstacleOffset: Float = 0f, // -0.8 to +0.8
    val hasIcePatch: Boolean = false,
    val hasBoostPad: Boolean = false,
    val length: Int = 10
)

data class Track(
    val id: String,
    val world: WorldTheme,
    val trackNumber: Int,           // 1, 2, or 3
    val nameHe: String,
    val descriptionHe: String,
    val defaultLaps: Int = 2,
    val difficultyHe: String = "בינוני",
    val lengthKm: Float = 1.6f,
    val weather: WeatherCondition = WeatherCondition.SUNNY,
    val segments: List<TrackSegment>
)

object TrackCatalog {
    val worlds = WorldTheme.values().toList()

    val allTracks: List<Track> by lazy {
        val list = mutableListOf<Track>()
        worlds.forEach { world ->
            list.add(createTrack(world, 1, "המסלול הקלאסי", "היכרות מהירה עם פיתולי ${world.nameHe}", 1.4f, "קל"))
            list.add(createTrack(world, 2, "ספרינט הקפיצות", "רמפות ענק, עליות חדות וקיצורי דרך", 1.8f, "בינוני"))
            list.add(createTrack(world, 3, "גרנד פרי האקסטרים", "מרוץ מאתגר עם פניות פרסה, מכשולים ומלכודות", 2.2f, "קשה"))
        }
        list
    }

    private fun createTrack(
        world: WorldTheme,
        trackNum: Int,
        subName: String,
        desc: String,
        lengthKm: Float,
        diff: String
    ): Track {
        val segs = mutableListOf<TrackSegment>()
        // Start straight
        segs.add(TrackSegment(curve = 0f, hill = 0f, length = 15))
        // Item boxes on main straight
        segs.add(TrackSegment(curve = 0f, hill = 0f, hasItemBox = true, length = 10))

        if (trackNum == 1) {
            // Gentle curves
            segs.add(TrackSegment(curve = 0.4f, hill = 0.2f, hasCoins = true, length = 20))
            segs.add(TrackSegment(curve = 0f, hill = -0.2f, hasBoostPad = true, length = 15))
            segs.add(TrackSegment(curve = -0.5f, hill = 0.1f, hasItemBox = true, length = 25))
            segs.add(TrackSegment(curve = 0f, hill = 0.4f, hasRamp = true, length = 18))
            segs.add(TrackSegment(curve = 0.3f, hill = -0.3f, hasCoins = true, length = 22))
            segs.add(TrackSegment(curve = -0.3f, hill = 0f, hasObstacle = true, obstacleOffset = 0.3f, length = 18))
            segs.add(TrackSegment(curve = 0f, hill = 0f, hasBoostPad = true, length = 15))
        } else if (trackNum == 2) {
            // Jumps and elevations
            segs.add(TrackSegment(curve = -0.6f, hill = 0.5f, hasRamp = true, length = 22))
            segs.add(TrackSegment(curve = 0.2f, hill = -0.5f, hasBoostPad = true, length = 20))
            segs.add(TrackSegment(curve = 0.7f, hill = 0.3f, hasItemBox = true, length = 26))
            segs.add(TrackSegment(curve = 0f, hill = 0.6f, hasRamp = true, length = 20))
            segs.add(TrackSegment(curve = -0.5f, hill = -0.4f, hasIcePatch = true, length = 22))
            segs.add(TrackSegment(curve = 0.4f, hill = 0.3f, hasObstacle = true, obstacleOffset = -0.4f, length = 20))
            segs.add(TrackSegment(curve = -0.3f, hill = 0f, hasBoostPad = true, length = 18))
        } else {
            // Extreme hairpins and obstacles
            segs.add(TrackSegment(curve = 0.8f, hill = 0.4f, hasObstacle = true, obstacleOffset = 0.2f, length = 25))
            segs.add(TrackSegment(curve = -0.9f, hill = -0.3f, hasIcePatch = true, length = 25))
            segs.add(TrackSegment(curve = 0f, hill = 0.7f, hasRamp = true, hasItemBox = true, length = 24))
            segs.add(TrackSegment(curve = 0.6f, hill = -0.6f, hasBoostPad = true, length = 20))
            segs.add(TrackSegment(curve = -0.7f, hill = 0.2f, hasCoins = true, length = 25))
            segs.add(TrackSegment(curve = 0.85f, hill = 0.5f, hasRamp = true, length = 22))
            segs.add(TrackSegment(curve = -0.4f, hill = -0.4f, hasObstacle = true, obstacleOffset = -0.3f, length = 20))
        }

        // Finish straight
        segs.add(TrackSegment(curve = 0f, hill = 0f, hasCoins = true, length = 20))

        val weather = when ((world.ordinal + trackNum) % 5) {
            0 -> WeatherCondition.SUNNY
            1 -> WeatherCondition.SUNSET
            2 -> WeatherCondition.RAINY
            3 -> WeatherCondition.STORM
            else -> WeatherCondition.NIGHT
        }

        return Track(
            id = "${world.name.lowercase()}_$trackNum",
            world = world,
            trackNumber = trackNum,
            nameHe = "${world.nameHe}: $subName",
            descriptionHe = desc,
            defaultLaps = 2,
            difficultyHe = diff,
            lengthKm = lengthKm,
            weather = weather,
            segments = segs
        )
    }

    fun getTrackById(id: String): Track = allTracks.firstOrNull { it.id == id } ?: allTracks[0]
}
