package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.GamePreferences
import com.example.engine.TurboAudioEngine
import com.example.model.CarCatalog
import com.example.model.GameMode
import com.example.model.Track
import com.example.model.TrackCatalog
import com.example.ui.screens.*
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TurboKartTheme

enum class AppScreen {
    MAIN_MENU,
    GARAGE,
    TRACK_SELECT,
    CHAMPIONSHIP,
    TRACK_EDITOR,
    SETTINGS,
    RECORDS,
    RACE
}

class MainActivity : ComponentActivity() {
    private val audioEngine = TurboAudioEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = GamePreferences(this)

        setContent {
            TurboKartTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = DarkBackground
                    ) {
                        var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
                        var activeTrack by remember { mutableStateOf(TrackCatalog.allTracks[0]) }
                        var activeGameMode by remember { mutableStateOf(GameMode.QUICK_RACE) }

                        // State synced with preferences
                        var coins by remember { mutableStateOf(prefs.coins) }
                        var selectedCarId by remember { mutableStateOf(prefs.selectedCarId) }
                        var unlockedCars by remember { mutableStateOf(prefs.unlockedCars) }

                        val playerCar = remember(selectedCarId) { CarCatalog.getCarById(selectedCarId) }
                        var playerUpgrade by remember(selectedCarId) { mutableStateOf(prefs.getUpgrade(selectedCarId)) }

                        // Back press navigation
                        if (currentScreen != AppScreen.MAIN_MENU) {
                            BackHandler {
                                if (currentScreen == AppScreen.RACE) {
                                    audioEngine.stop()
                                }
                                currentScreen = AppScreen.MAIN_MENU
                            }
                        }

                        when (currentScreen) {
                            AppScreen.MAIN_MENU -> {
                                MainMenuScreen(
                                    coins = coins,
                                    selectedCarId = selectedCarId,
                                    onStartRace = {
                                        activeTrack = TrackCatalog.allTracks[0]
                                        activeGameMode = GameMode.QUICK_RACE
                                        currentScreen = AppScreen.RACE
                                    },
                                    onChampionships = { currentScreen = AppScreen.CHAMPIONSHIP },
                                    onOpenGarage = { currentScreen = AppScreen.GARAGE },
                                    onSelectTrack = { currentScreen = AppScreen.TRACK_SELECT },
                                    onTrackEditor = { currentScreen = AppScreen.TRACK_EDITOR },
                                    onRecords = { currentScreen = AppScreen.RECORDS },
                                    onSettings = { currentScreen = AppScreen.SETTINGS }
                                )
                            }

                            AppScreen.GARAGE -> {
                                GarageScreen(
                                    coins = coins,
                                    selectedCarId = selectedCarId,
                                    unlockedCars = unlockedCars,
                                    getUpgrade = { prefs.getUpgrade(it) },
                                    onSelectCar = {
                                        selectedCarId = it
                                        prefs.selectedCarId = it
                                        playerUpgrade = prefs.getUpgrade(it)
                                    },
                                    onBuyCar = { car ->
                                        if (coins >= car.price) {
                                            coins -= car.price
                                            prefs.coins = coins
                                            prefs.unlockCar(car.id)
                                            unlockedCars = prefs.unlockedCars
                                            selectedCarId = car.id
                                            prefs.selectedCarId = car.id
                                            playerUpgrade = prefs.getUpgrade(car.id)
                                            audioEngine.playCoinSound()
                                        }
                                    },
                                    onUpgradeStat = { carId, stat, cost ->
                                        if (coins >= cost) {
                                            val current = prefs.getUpgrade(carId)
                                            val updated = when (stat) {
                                                "engine" -> current.copy(engineLevel = (current.engineLevel + 1).coerceAtMost(5))
                                                "handling" -> current.copy(handlingLevel = (current.handlingLevel + 1).coerceAtMost(5))
                                                "turbo" -> current.copy(turboLevel = (current.turboLevel + 1).coerceAtMost(5))
                                                "body" -> current.copy(bodyLevel = (current.bodyLevel + 1).coerceAtMost(5))
                                                else -> current
                                            }
                                            coins -= cost
                                            prefs.coins = coins
                                            prefs.saveUpgrade(carId, updated)
                                            playerUpgrade = updated
                                            audioEngine.playPowerUpCollected()
                                        }
                                    },
                                    onCustomColorChanged = { carId, colorHex ->
                                        val current = prefs.getUpgrade(carId)
                                        val updated = current.copy(customColorHex = colorHex)
                                        prefs.saveUpgrade(carId, updated)
                                        playerUpgrade = updated
                                    },
                                    onBack = { currentScreen = AppScreen.MAIN_MENU }
                                )
                            }

                            AppScreen.TRACK_SELECT -> {
                                TrackSelectScreen(
                                    onTrackSelected = { track, mode ->
                                        activeTrack = track
                                        activeGameMode = mode
                                        currentScreen = AppScreen.RACE
                                    },
                                    getBestLap = { prefs.getBestLapTime(it) },
                                    onBack = { currentScreen = AppScreen.MAIN_MENU }
                                )
                            }

                            AppScreen.CHAMPIONSHIP -> {
                                ChampionshipScreen(
                                    getCupTrophy = { prefs.getCupTrophy(it) },
                                    onStartChampionshipCup = { cup ->
                                        activeTrack = cup.tracks.first()
                                        activeGameMode = GameMode.CHAMPIONSHIP
                                        currentScreen = AppScreen.RACE
                                    },
                                    onBack = { currentScreen = AppScreen.MAIN_MENU }
                                )
                            }

                            AppScreen.TRACK_EDITOR -> {
                                TrackEditorScreen(
                                    onTestDrive = { customTrack ->
                                        activeTrack = customTrack
                                        activeGameMode = GameMode.FREE_RIDE
                                        currentScreen = AppScreen.RACE
                                    },
                                    onBack = { currentScreen = AppScreen.MAIN_MENU }
                                )
                            }

                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    prefs = prefs,
                                    onBack = { currentScreen = AppScreen.MAIN_MENU }
                                )
                            }

                            AppScreen.RECORDS -> {
                                RecordsScreen(
                                    prefs = prefs,
                                    onBack = { currentScreen = AppScreen.MAIN_MENU }
                                )
                            }

                            AppScreen.RACE -> {
                                RaceScreen(
                                    track = activeTrack,
                                    gameMode = activeGameMode,
                                    playerCar = playerCar,
                                    playerUpgrade = playerUpgrade,
                                    controlMode = prefs.controlMode,
                                    autoAccelerate = prefs.autoAccelerate,
                                    sensitivity = prefs.sensitivity,
                                    audioEngine = audioEngine,
                                    onRaceFinished = { rank, timeSeconds, earnedCoins ->
                                        coins += earnedCoins
                                        prefs.coins = coins
                                        prefs.saveBestLapTime(activeTrack.id, timeSeconds / activeTrack.defaultLaps)
                                        if (activeGameMode == GameMode.CHAMPIONSHIP && rank <= 3) {
                                            val trophy = 4 - rank
                                            prefs.saveCupTrophy(activeTrack.world.name, trophy)
                                        }
                                        currentScreen = AppScreen.MAIN_MENU
                                    },
                                    onExitToMenu = {
                                        currentScreen = AppScreen.MAIN_MENU
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.stop()
    }
}
