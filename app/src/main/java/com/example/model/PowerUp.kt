package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * 10 Special Power-Ups in Turbo Kart.
 */
enum class PowerUpType(
    val nameHe: String,
    val descriptionHe: String,
    val iconSymbol: String,
    val badgeColor: Color,
    val durationSeconds: Float = 3.0f
) {
    TURBO("טורבו", "האצת נייטרו מתפרצת", "🚀", Color(0xFFFF5722), 3.0f),
    SHIELD("מגן", "הגנה מוחלטת מפני פגיעות", "🛡️", Color(0xFF00E5FF), 5.0f),
    ICE("קרח", "משטח חלקלק שמעיף יריבים", "❄️", Color(0xFF80D8FF), 0.0f),
    TORNADO("טורנדו", "מערבולת רוח שפוגעת במובילים", "🌪️", Color(0xFF7E57C2), 4.0f),
    LIGHTNING("ברק", "מכת ברק המאטה את כל המתחרים", "⚡", Color(0xFFFFD600), 2.5f),
    MAGNET("מגנט", "שאיבת מטבעות אוטומטית סביבך", "🧲", Color(0xFFE91E63), 6.0f),
    GHOST("רוח רפאים", "מעבר דרך מכשולים ומכוניות", "👻", Color(0xFFB39DDB), 4.5f),
    PAINT_MISSILE("טיל צבע", "טיל מונחה שמכתים ומסחרר יריב", "🎯", Color(0xFFFF1744), 0.0f),
    MEGA_JUMP("קפיצת ענק", "זינוק אדיר לגובה מעל כולם", "🦘", Color(0xFF00E676), 1.5f),
    DOUBLE_NITRO("טורבו כפול", "הצתת מבערים כפולה ועוצמתית", "🔥", Color(0xFFFF9100), 4.5f);

    companion object {
        fun getRandom(): PowerUpType = values().random()
    }
}

data class ActivePowerUp(
    val type: PowerUpType,
    var remainingTime: Float
)
