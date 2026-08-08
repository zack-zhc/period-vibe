package com.example.periodvibe.util

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppLockGuardTest {

    @Before
    fun setUp() {
        AppLockGuard.isSystemPickerActive = false
    }

    @After
    fun tearDown() {
        AppLockGuard.isSystemPickerActive = false
    }

    @Test
    fun `locks when unlocked and app lock enabled and no picker`() {
        assertTrue(AppLockGuard.shouldLockOnStop(isUnlocked = true, appLockEnabled = true))
    }

    @Test
    fun `does not lock when system picker is active`() {
        AppLockGuard.isSystemPickerActive = true

        assertFalse(AppLockGuard.shouldLockOnStop(isUnlocked = true, appLockEnabled = true))
    }

    @Test
    fun `does not lock when app lock disabled`() {
        assertFalse(AppLockGuard.shouldLockOnStop(isUnlocked = true, appLockEnabled = false))
    }

    @Test
    fun `does not lock when already locked`() {
        assertFalse(AppLockGuard.shouldLockOnStop(isUnlocked = false, appLockEnabled = true))
    }

    @Test
    fun `does not lock when picker active and app lock disabled`() {
        AppLockGuard.isSystemPickerActive = true

        assertFalse(AppLockGuard.shouldLockOnStop(isUnlocked = true, appLockEnabled = false))
    }

    @Test
    fun `locks again after picker flag is cleared`() {
        AppLockGuard.isSystemPickerActive = true
        assertFalse(AppLockGuard.shouldLockOnStop(isUnlocked = true, appLockEnabled = true))

        AppLockGuard.isSystemPickerActive = false

        assertTrue(AppLockGuard.shouldLockOnStop(isUnlocked = true, appLockEnabled = true))
    }
}
