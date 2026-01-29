package com.example.periodvibe.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePin(pin: String) {
        with(sharedPreferences.edit()) {
            putString("app_pin", pin)
            apply()
        }
    }

    fun getPin(): String? {
        return sharedPreferences.getString("app_pin", null)
    }

    fun hasPin(): Boolean {
        return sharedPreferences.contains("app_pin")
    }

    fun deletePin() {
        with(sharedPreferences.edit()) {
            remove("app_pin")
            apply()
        }
    }
}