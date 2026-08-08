// 仅 migrateLegacyPin() 使用已弃用的 EncryptedSharedPreferences 读取旧数据
@file:Suppress("DEPRECATION")

package com.example.periodvibe.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PIN 码的安全存储。
 * 使用 Android Keystore 中的 AES/GCM 密钥加密后存入普通 SharedPreferences，
 * 替代已弃用的 EncryptedSharedPreferences。
 */
@Singleton
class SecurityRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val lockPrefs = context.getSharedPreferences(LOCK_PREFS_NAME, Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
    private val keyAlias = "period_vibe_master_key"

    init {
        migrateLegacyPin()
    }

    fun savePin(pin: String) {
        prefs.edit().putString(KEY_PIN, encrypt(pin)).apply()
    }

    fun getPin(): String? {
        val encrypted = prefs.getString(KEY_PIN, null) ?: return null
        return try {
            decrypt(encrypted)
        } catch (e: Exception) {
            // 密钥不可用（Keystore 异常/备份恢复等），按无 PIN 处理，避免锁死用户
            e.printStackTrace()
            null
        }
    }

    fun hasPin(): Boolean = getPin() != null

    fun deletePin() {
        prefs.edit().remove(KEY_PIN).apply()
    }

    // ==================== PIN 失败计数与递增锁定 ====================

    /**
     * 记录一次 PIN 验证失败。
     * 连续失败达到阈值后进入锁定，锁定时间随失败次数递增（60s → 120s → 240s…，封顶 30 分钟）。
     * @return 当前剩余锁定毫秒数（0 表示未在锁定中）
     */
    fun recordFailedAttempt(): Long {
        val attempts = lockPrefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        lockPrefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts).apply()
        if (attempts >= MAX_ATTEMPTS_BEFORE_LOCKOUT) {
            val power = attempts - MAX_ATTEMPTS_BEFORE_LOCKOUT + 1
            val lockSeconds = minOf(
                BASE_LOCKOUT_SECONDS * (1L shl (power - 1)),
                MAX_LOCKOUT_SECONDS
            )
            val until = System.currentTimeMillis() + lockSeconds * 1000L
            lockPrefs.edit().putLong(KEY_LOCKOUT_UNTIL, until).apply()
        }
        return getLockoutRemainingMillis()
    }

    /**
     * @return 当前剩余锁定毫秒数；锁定已过期时自动清理并返回 0
     */
    fun getLockoutRemainingMillis(): Long {
        val until = lockPrefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        if (until <= 0L) return 0L
        val remaining = until - System.currentTimeMillis()
        if (remaining <= 0L) {
            resetFailedAttempts()
            return 0L
        }
        return remaining
    }

    fun resetFailedAttempts() {
        lockPrefs.edit()
            .remove(KEY_FAILED_ATTEMPTS)
            .remove(KEY_LOCKOUT_UNTIL)
            .apply()
    }

    // ==================== Keystore AES/GCM 加解密 ====================

    private fun getOrCreateKey(): SecretKey {
        (keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry)?.let {
            return it.secretKey
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val data = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = data.copyOfRange(0, IV_SIZE)
        val ciphertext = data.copyOfRange(IV_SIZE, data.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    // ==================== 旧 EncryptedSharedPreferences 数据迁移 ====================

    private fun migrateLegacyPin() {
        if (prefs.contains(KEY_PIN)) return
        // 旧文件不存在或为空则跳过（避免每次启动都创建旧存储）
        if (context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE).all.isEmpty()) return

        val legacy = EncryptedSharedPreferences.create(
            context,
            LEGACY_PREFS_NAME,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val legacyPin = legacy.getString("app_pin", null) ?: return
        savePin(legacyPin)
        legacy.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "secure_prefs"
        const val LEGACY_PREFS_NAME = "secret_shared_prefs"
        const val KEY_PIN = "app_pin_enc"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12

        const val LOCK_PREFS_NAME = "lock_state"
        const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        const val KEY_LOCKOUT_UNTIL = "lockout_until"
        const val MAX_ATTEMPTS_BEFORE_LOCKOUT = 5
        const val BASE_LOCKOUT_SECONDS = 60L
        const val MAX_LOCKOUT_SECONDS = 1800L
    }
}
