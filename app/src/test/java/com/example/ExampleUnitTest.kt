package com.example

import com.example.engine.RacerPhysics
import com.example.model.CarCatalog
import com.example.model.PowerUpType
import com.example.model.TrackCatalog
import com.example.model.WorldTheme
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCarCatalogContains20Cars() {
        assertEquals(20, CarCatalog.cars.size)
        // Check first and last car
        assertEquals("באגי חוף", CarCatalog.cars.first().nameHe)
        assertEquals("מכונית טורבו אגדית", CarCatalog.cars.last().nameHe)
        // Starter car is free
        assertEquals(0, CarCatalog.cars.first().price)
    }

    @Test
    fun testTrackCatalogContains36TracksAcross12Worlds() {
        assertEquals(12, WorldTheme.values().size)
        assertEquals(36, TrackCatalog.allTracks.size)
        // Check world themes
        val beachTracks = TrackCatalog.allTracks.filter { it.world == WorldTheme.BEACH }
        assertEquals(3, beachTracks.size)
    }

    @Test
    fun testPowerUpCountAndTypes() {
        assertEquals(10, PowerUpType.values().size)
        assertTrue(PowerUpType.values().any { it.nameHe == "טורבו" })
        assertTrue(PowerUpType.values().any { it.nameHe == "מגן" })
        assertTrue(PowerUpType.values().any { it.nameHe == "קרח" })
    }

    @Test
    fun testRacerPhysicsAndDrift() {
        val car = CarCatalog.cars[0]
        val track = TrackCatalog.allTracks[0]
        val racer = RacerPhysics("player", "אתה", isPlayer = true, car = car)

        // Initial state
        assertEquals(0f, racer.forwardSpeed, 0.01f)
        assertEquals(0f, racer.distanceAlongTrack, 0.01f)

        // Accelerate
        racer.update(dt = 0.5f, throttleInput = 1.0f, steerInput = 0f, driftButton = false, track = track)
        assertTrue(racer.forwardSpeed > 0f)
        assertTrue(racer.distanceAlongTrack > 0f)

        // Drift charge
        racer.forwardSpeed = 30f
        racer.update(dt = 0.5f, throttleInput = 1.0f, steerInput = 0.8f, driftButton = true, track = track)
        assertTrue(racer.isDrifting)
    }
}
