package com.example.model

enum class GameMode(
    val nameHe: String,
    val subtitleHe: String,
    val descriptionHe: String,
    val hasOpponents: Boolean,
    val hasLaps: Boolean,
    val powerupSpawnRateMultiplier: Float = 1.0f,
    val aiAggressiveness: Float = 1.0f
) {
    CHAMPIONSHIP(
        nameHe = "אליפות הטורבו",
        subtitleHe = "סדרת מרוצים לגביעים וניקוד",
        descriptionHe = "התחרו ב-4 מרוצים עוקבים, צברו נקודות וזכו בגביע האליפות המוזהב!",
        hasOpponents = true,
        hasLaps = true,
        aiAggressiveness = 1.0f
    ),
    TIME_TRIAL(
        nameHe = "מרוץ נגד השעון",
        subtitleHe = "שבירת שיאי הקפה אישיים",
        descriptionHe = "רק אתם, המכונית והמסלול. שברו את שיא ההקפה שלכם ללא הפרעות!",
        hasOpponents = false,
        hasLaps = true,
        powerupSpawnRateMultiplier = 0.5f
    ),
    QUICK_RACE(
        nameHe = "מרוץ חופשי",
        subtitleHe = "מרוץ מותאם אישית",
        descriptionHe = "בחרו מכונית, מסלול, מספר יריבים וצאו להתחרות במהירות ובכיף.",
        hasOpponents = true,
        hasLaps = true
    ),
    POWERUP_FRENZY(
        nameHe = "מרוץ הכוחות",
        subtitleHe = "עומס כוחות מיוחדים ובלגן טהור",
        descriptionHe = "קופסאות הפתעה בכל פינה, טילים, סופות טורנדו והמון אקשן בלתי פוסק!",
        hasOpponents = true,
        hasLaps = true,
        powerupSpawnRateMultiplier = 2.5f,
        aiAggressiveness = 1.2f
    ),
    CHAMPIONS_TOURNAMENT(
        nameHe = "טורניר האלופים",
        subtitleHe = "מרוץ אקסטרים מול הנהגים הטובים ביותר",
        descriptionHe = "יריבי AI קשוחים עם מכוניות משודרגות למקסימום. למקצוענים בלבד!",
        hasOpponents = true,
        hasLaps = true,
        aiAggressiveness = 1.4f
    ),
    FREE_RIDE(
        nameHe = "נהיגה חופשית",
        subtitleHe = "חופש מוחלט ללא מגבלת זמן",
        descriptionHe = "תרגלו קפיצות, בצעו פעלולים באוויר וגלו את כל סודות המסלול בכיף.",
        hasOpponents = false,
        hasLaps = false
    )
}
