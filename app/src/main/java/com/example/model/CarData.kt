package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Data model for the 20 customizable cartoon racing karts in Turbo Kart.
 */
data class Car(
    val id: String,
    val nameHe: String,
    val taglineHe: String,
    val baseSpeed: Float,        // 0.0 to 1.0
    val baseAccel: Float,        // 0.0 to 1.0
    val baseHandling: Float,     // 0.0 to 1.0
    val baseTurbo: Float,        // 0.0 to 1.0
    val baseStability: Float,    // 0.0 to 1.0
    val price: Int,              // In-game coins (0 for starter car)
    val bodyColor: Color,
    val secondaryColor: Color,
    val wheelColor: Color,
    val spoilerType: Int = 1,
    val bodyType: CarBodyType = CarBodyType.BUGGY,
    val wheelRadius: Float = 1.0f
)

enum class CarBodyType {
    BUGGY, SPORT, JEEP, MONSTER, FORMULA, RETRO, POLICE, FIRE, CYBER, TRUCK
}

data class CarUpgrade(
    val engineLevel: Int = 0,    // 0 to 5
    val handlingLevel: Int = 0,  // 0 to 5
    val turboLevel: Int = 0,     // 0 to 5
    val bodyLevel: Int = 0,      // 0 to 5
    val customColorHex: Long? = null,
    val customWheelType: Int = 0
) {
    val totalLevel: Int get() = engineLevel + handlingLevel + turboLevel + bodyLevel
}

object CarCatalog {
    val cars: List<Car> = listOf(
        Car(
            id = "car_01",
            nameHe = "באגי חוף",
            taglineHe = "קל, זריז ושולט בחולות",
            baseSpeed = 0.65f,
            baseAccel = 0.85f,
            baseHandling = 0.80f,
            baseTurbo = 0.75f,
            baseStability = 0.70f,
            price = 0, // Starter car
            bodyColor = Color(0xFFFF9800),
            secondaryColor = Color(0xFF03A9F4),
            wheelColor = Color(0xFF263238),
            spoilerType = 1,
            bodyType = CarBodyType.BUGGY,
            wheelRadius = 1.1f
        ),
        Car(
            id = "car_02",
            nameHe = "טורבו ספורט",
            taglineHe = "אירודינמיקה חדה ומהירות שיא",
            baseSpeed = 0.85f,
            baseAccel = 0.75f,
            baseHandling = 0.82f,
            baseTurbo = 0.80f,
            baseStability = 0.65f,
            price = 1000,
            bodyColor = Color(0xFFE53935),
            secondaryColor = Color(0xFFFFFFFF),
            wheelColor = Color(0xFFFFD700),
            spoilerType = 2,
            bodyType = CarBodyType.SPORT
        ),
        Car(
            id = "car_03",
            nameHe = "ג'יפ שטח",
            taglineHe = "עובר כל מכשול בלי למצמץ",
            baseSpeed = 0.70f,
            baseAccel = 0.70f,
            baseHandling = 0.75f,
            baseTurbo = 0.70f,
            baseStability = 0.95f,
            price = 1800,
            bodyColor = Color(0xFF4CAF50),
            secondaryColor = Color(0xFF2E7D32),
            wheelColor = Color(0xFF1B1B1B),
            spoilerType = 0,
            bodyType = CarBodyType.JEEP,
            wheelRadius = 1.25f
        ),
        Car(
            id = "car_04",
            nameHe = "מפלצת הגלגלים",
            taglineHe = "צמיגי ענק שרומסים כל תחרות",
            baseSpeed = 0.72f,
            baseAccel = 0.68f,
            baseHandling = 0.60f,
            baseTurbo = 0.85f,
            baseStability = 0.99f,
            price = 2800,
            bodyColor = Color(0xFF9C27B0),
            secondaryColor = Color(0xFF76FF03),
            wheelColor = Color(0xFF212121),
            spoilerType = 1,
            bodyType = CarBodyType.MONSTER,
            wheelRadius = 1.5f
        ),
        Car(
            id = "car_05",
            nameHe = "מכונית פורמולה",
            taglineHe = "דיוק מושלם של מסלולי אספלט",
            baseSpeed = 0.95f,
            baseAccel = 0.90f,
            baseHandling = 0.92f,
            baseTurbo = 0.85f,
            baseStability = 0.55f,
            price = 4000,
            bodyColor = Color(0xFF00E676),
            secondaryColor = Color(0xFF1E88E5),
            wheelColor = Color(0xFF212121),
            spoilerType = 3,
            bodyType = CarBodyType.FORMULA
        ),
        Car(
            id = "car_06",
            nameHe = "מכונית מדבר",
            taglineHe = "נבנתה לסופות אבק ומסלולי סלעים",
            baseSpeed = 0.78f,
            baseAccel = 0.80f,
            baseHandling = 0.78f,
            baseTurbo = 0.82f,
            baseStability = 0.80f,
            price = 5000,
            bodyColor = Color(0xFFFFB300),
            secondaryColor = Color(0xFF795548),
            wheelColor = Color(0xFF37474F),
            spoilerType = 1,
            bodyType = CarBodyType.BUGGY,
            wheelRadius = 1.2f
        ),
        Car(
            id = "car_07",
            nameHe = "מכונית שלג",
            taglineHe = "שליטה חלקה בקרח ובפיתולים קפואים",
            baseSpeed = 0.80f,
            baseAccel = 0.82f,
            baseHandling = 0.90f,
            baseTurbo = 0.78f,
            baseStability = 0.85f,
            price = 6200,
            bodyColor = Color(0xFF80D8FF),
            secondaryColor = Color(0xFFFFFFFF),
            wheelColor = Color(0xFF0288D1),
            spoilerType = 2,
            bodyType = CarBodyType.SPORT
        ),
        Car(
            id = "car_08",
            nameHe = "מכונית עתידנית",
            taglineHe = "הנעה אנרגטית עם קווי ניאון זוהרים",
            baseSpeed = 0.92f,
            baseAccel = 0.92f,
            baseHandling = 0.88f,
            baseTurbo = 0.95f,
            baseStability = 0.75f,
            price = 7800,
            bodyColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFFFF007F),
            wheelColor = Color(0xFF651FFF),
            spoilerType = 3,
            bodyType = CarBodyType.CYBER
        ),
        Car(
            id = "car_09",
            nameHe = "מכונית משטרה",
            taglineHe = "סירנות מייללות ומרדף ללא פשרות",
            baseSpeed = 0.84f,
            baseAccel = 0.85f,
            baseHandling = 0.84f,
            baseTurbo = 0.86f,
            baseStability = 0.85f,
            price = 9000,
            bodyColor = Color(0xFF212121),
            secondaryColor = Color(0xFFFAFAFA),
            wheelColor = Color(0xFF1E88E5),
            spoilerType = 2,
            bodyType = CarBodyType.POLICE
        ),
        Car(
            id = "car_10",
            nameHe = "רכב כיבוי אש",
            taglineHe = "טורבו רב-עוצמה שמכבה את התחרות",
            baseSpeed = 0.76f,
            baseAccel = 0.75f,
            baseHandling = 0.70f,
            baseTurbo = 0.92f,
            baseStability = 0.92f,
            price = 10500,
            bodyColor = Color(0xFFD50000),
            secondaryColor = Color(0xFFFFD600),
            wheelColor = Color(0xFF37474F),
            spoilerType = 1,
            bodyType = CarBodyType.FIRE,
            wheelRadius = 1.3f
        ),
        Car(
            id = "car_11",
            nameHe = "מכונית זהב",
            taglineHe = "מצופה זהב טהור וברק מנקר עיניים",
            baseSpeed = 0.90f,
            baseAccel = 0.88f,
            baseHandling = 0.86f,
            baseTurbo = 0.90f,
            baseStability = 0.82f,
            price = 12500,
            bodyColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFFFFE082),
            wheelColor = Color(0xFFFFF8E1),
            spoilerType = 3,
            bodyType = CarBodyType.SPORT
        ),
        Car(
            id = "car_12",
            nameHe = "מכונית קלאסית",
            taglineHe = "אלגנטיות נוסטלגית עם מנוע משופר",
            baseSpeed = 0.82f,
            baseAccel = 0.80f,
            baseHandling = 0.85f,
            baseTurbo = 0.82f,
            baseStability = 0.80f,
            price = 14000,
            bodyColor = Color(0xFF6D4C41),
            secondaryColor = Color(0xFFFFE0B2),
            wheelColor = Color(0xFFE0E0E0),
            spoilerType = 0,
            bodyType = CarBodyType.RETRO
        ),
        Car(
            id = "car_13",
            nameHe = "מכונית מרוץ אדומה",
            taglineHe = "כוכבת מסלול קלאסית שכולם מזהים",
            baseSpeed = 0.88f,
            baseAccel = 0.86f,
            baseHandling = 0.84f,
            baseTurbo = 0.88f,
            baseStability = 0.80f,
            price = 16000,
            bodyColor = Color(0xFFFF1744),
            secondaryColor = Color(0xFFFFFFFF),
            wheelColor = Color(0xFF263238),
            spoilerType = 2,
            bodyType = CarBodyType.SPORT
        ),
        Car(
            id = "car_14",
            nameHe = "מכונית שטח משוריינת",
            taglineHe = "בלתי ניתנת להסטה או התנגשות",
            baseSpeed = 0.75f,
            baseAccel = 0.72f,
            baseHandling = 0.72f,
            baseTurbo = 0.85f,
            baseStability = 1.0f,
            price = 18000,
            bodyColor = Color(0xFF546E7A),
            secondaryColor = Color(0xFF263238),
            wheelColor = Color(0xFF37474F),
            spoilerType = 0,
            bodyType = CarBodyType.TRUCK,
            wheelRadius = 1.35f
        ),
        Car(
            id = "car_15",
            nameHe = "מכונית סופר־טורבו",
            taglineHe = "הזרקת נייטרו כפולה בכל לחיצה",
            baseSpeed = 0.94f,
            baseAccel = 0.93f,
            baseHandling = 0.85f,
            baseTurbo = 1.0f,
            baseStability = 0.78f,
            price = 21000,
            bodyColor = Color(0xFFFF3D00),
            secondaryColor = Color(0xFFFFEA00),
            wheelColor = Color(0xFF212121),
            spoilerType = 3,
            bodyType = CarBodyType.SPORT
        ),
        Car(
            id = "car_16",
            nameHe = "מכונית חלל",
            taglineHe = "טכנולוגיית אנטי-גרביטציה ומהירות קוסמית",
            baseSpeed = 0.96f,
            baseAccel = 0.94f,
            baseHandling = 0.91f,
            baseTurbo = 0.96f,
            baseStability = 0.82f,
            price = 24000,
            bodyColor = Color(0xFF3D5AFE),
            secondaryColor = Color(0xFF00E5FF),
            wheelColor = Color(0xFF651FFF),
            spoilerType = 3,
            bodyType = CarBodyType.CYBER
        ),
        Car(
            id = "car_17",
            nameHe = "מכונית ג'ונגל",
            taglineHe = "חיית מסלול טורפת עם אחיזה מושלמת",
            baseSpeed = 0.84f,
            baseAccel = 0.88f,
            baseHandling = 0.88f,
            baseTurbo = 0.85f,
            baseStability = 0.88f,
            price = 27500,
            bodyColor = Color(0xFF388E3C),
            secondaryColor = Color(0xFFFFD600),
            wheelColor = Color(0xFF1B5E20),
            spoilerType = 2,
            bodyType = CarBodyType.JEEP,
            wheelRadius = 1.25f
        ),
        Car(
            id = "car_18",
            nameHe = "מכונית מרוצים כחולה",
            taglineHe = "ברק כחול שחוצה את קו הסיום ראשון",
            baseSpeed = 0.91f,
            baseAccel = 0.89f,
            baseHandling = 0.90f,
            baseTurbo = 0.90f,
            baseStability = 0.85f,
            price = 31000,
            bodyColor = Color(0xFF2979FF),
            secondaryColor = Color(0xFFE040FB),
            wheelColor = Color(0xFF0D47A1),
            spoilerType = 2,
            bodyType = CarBodyType.SPORT
        ),
        Car(
            id = "car_19",
            nameHe = "מכונית אליפות",
            taglineHe = "נבחרת האלופים הרשמית של הטורניר",
            baseSpeed = 0.97f,
            baseAccel = 0.96f,
            baseHandling = 0.94f,
            baseTurbo = 0.95f,
            baseStability = 0.88f,
            price = 36000,
            bodyColor = Color(0xFF7C4DFF),
            secondaryColor = Color(0xFFFFD700),
            wheelColor = Color(0xFFFFD700),
            spoilerType = 3,
            bodyType = CarBodyType.FORMULA
        ),
        Car(
            id = "car_20",
            nameHe = "מכונית טורבו אגדית",
            taglineHe = "הקארט העוצמתי ביותר ביקום",
            baseSpeed = 1.0f,
            baseAccel = 1.0f,
            baseHandling = 0.98f,
            baseTurbo = 1.0f,
            baseStability = 0.95f,
            price = 45000,
            bodyColor = Color(0xFFFF1744),
            secondaryColor = Color(0xFFFFD700),
            wheelColor = Color(0xFF00E5FF),
            spoilerType = 3,
            bodyType = CarBodyType.CYBER,
            wheelRadius = 1.2f
        )
    )

    fun getCarById(id: String): Car = cars.firstOrNull { it.id == id } ?: cars[0]
}
