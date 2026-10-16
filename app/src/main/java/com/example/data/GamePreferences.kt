package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CarUpgrade
import org.json.JSONArray
import org.json.JSONObject

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("turbo_kart_prefs", Context.MODE_PRIVATE)

    var coins: Int
        get() = prefs.getInt("coins", 1500) // generous starting amount to test upgrades right away
        set(value) = prefs.edit().putInt("coins", value).apply()

    var selectedCarId: String
        get() = prefs.getString("selected_car_id", "car_01") ?: "car_01"
        set(value) = prefs.edit().putString("selected_car_id", value).apply()

    var unlockedCars: Set<String>
        get() = prefs.getStringSet("unlocked_cars", setOf("car_01")) ?: setOf("car_01")
        set(value) = prefs.edit().putStringSet("unlocked_cars", value).apply()

    fun unlockCar(carId: String) {
        val set = unlockedCars.toMutableSet()
        set.add(carId)
        unlockedCars = set
    }

    // Car Upgrades
    fun getUpgrade(carId: String): CarUpgrade {
        val json = prefs.getString("upgrade_$carId", null) ?: return CarUpgrade()
        return try {
            val obj = JSONObject(json)
            CarUpgrade(
                engineLevel = obj.optInt("engine", 0),
                handlingLevel = obj.optInt("handling", 0),
                turboLevel = obj.optInt("turbo", 0),
                bodyLevel = obj.optInt("body", 0),
                customColorHex = if (obj.has("color")) obj.getLong("color") else null,
                customWheelType = obj.optInt("wheel", 0)
            )
        } catch (e: Exception) {
            CarUpgrade()
        }
    }

    fun saveUpgrade(carId: String, upgrade: CarUpgrade) {
        val obj = JSONObject().apply {
            put("engine", upgrade.engineLevel)
            put("handling", upgrade.handlingLevel)
            put("turbo", upgrade.turboLevel)
            put("body", upgrade.bodyLevel)
            upgrade.customColorHex?.let { put("color", it) }
            put("wheel", upgrade.customWheelType)
        }
        prefs.edit().putString("upgrade_$carId", obj.toString()).apply()
    }

    // Settings
    var controlMode: Int // 0: Touch buttons, 1: Virtual wheel, 2: Tilt sensors
        get() = prefs.getInt("control_mode", 0)
        set(value) = prefs.edit().putInt("control_mode", value).apply()

    var autoAccelerate: Boolean
        get() = prefs.getBoolean("auto_accelerate", false)
        set(value) = prefs.edit().putBoolean("auto_accelerate", value).apply()

    var sensitivity: Float
        get() = prefs.getFloat("sensitivity", 1.0f)
        set(value) = prefs.edit().putFloat("sensitivity", value).apply()

    var musicVolume: Float
        get() = prefs.getFloat("music_volume", 0.7f)
        set(value) = prefs.edit().putFloat("music_volume", value).apply()

    var sfxVolume: Float
        get() = prefs.getFloat("sfx_volume", 0.85f)
        set(value) = prefs.edit().putFloat("sfx_volume", value).apply()

    var targetFps: Int
        get() = prefs.getInt("target_fps", 60)
        set(value) = prefs.edit().putInt("target_fps", value).apply()

    var graphicsQuality: String // "LOW", "MEDIUM", "HIGH"
        get() = prefs.getString("graphics_quality", "HIGH") ?: "HIGH"
        set(value) = prefs.edit().putString("graphics_quality", value).apply()

    // Records
    fun getBestLapTime(trackId: String): Float = prefs.getFloat("best_lap_$trackId", 99.99f)
    fun saveBestLapTime(trackId: String, timeSeconds: Float) {
        val current = getBestLapTime(trackId)
        if (timeSeconds < current) {
            prefs.edit().putFloat("best_lap_$trackId", timeSeconds).apply()
        }
    }

    // Trophies won per world cup (0: None, 1: Bronze, 2: Silver, 3: Gold)
    fun getCupTrophy(worldName: String): Int = prefs.getInt("cup_trophy_$worldName", 0)
    fun saveCupTrophy(worldName: String, trophyRank: Int) {
        val current = getCupTrophy(worldName)
        if (trophyRank > current) {
            prefs.edit().putInt("cup_trophy_$worldName", trophyRank).apply()
        }
    }

    // Custom Tracks
    fun getCustomTracksJson(): String = prefs.getString("custom_tracks_data", "[]") ?: "[]"
    fun saveCustomTracksJson(json: String) {
        prefs.edit().putString("custom_tracks_data", json).apply()
    }
}
