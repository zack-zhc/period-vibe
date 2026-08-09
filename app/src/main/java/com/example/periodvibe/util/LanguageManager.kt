package com.example.periodvibe.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用语言管理：跟随系统 / 简体中文 / English。
 *
 * - SharedPreferences 镜像供 attachBaseContext 同步读取（冷启动在 DB 可用前即生效）；
 * - API 33+ 用系统 LocaleManager（自动重建 Activity，所有上下文统一本地化）；
 * - API 31-32 手动更新应用 Resources 配置，由调用方触发 Activity.recreate()。
 */
@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun storedLanguage(): String =
        prefs(context).getString(KEY_LANGUAGE, LANG_SYSTEM) ?: LANG_SYSTEM

    fun apply(language: String) {
        val locale = resolveLocale(language, context)
        Locale.setDefault(locale)
        prefs(context).edit().putString(KEY_LANGUAGE, language).apply()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(if (language == LANG_SYSTEM) "" else language)
        } else {
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }

    companion object {
        const val KEY_LANGUAGE = "app_language"
        const val LANG_SYSTEM = "system"
        const val LANG_ZH = "zh"
        const val LANG_EN = "en"
        private const val PREFS_NAME = "app_language_prefs"

        fun prefs(context: Context) =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        fun readStoredLanguage(context: Context): String =
            prefs(context).getString(KEY_LANGUAGE, LANG_SYSTEM) ?: LANG_SYSTEM

        /** 解析语言为 Locale；跟随系统时取设备语言（非 Locale.getDefault()，避免被应用改动污染） */
        fun resolveLocale(language: String, context: Context): Locale = when (language) {
            LANG_ZH -> Locale.SIMPLIFIED_CHINESE
            LANG_EN -> Locale.ENGLISH
            else -> deviceLocale(context)
        }

        private fun deviceLocale(context: Context): Locale {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val systemLocales = context.getSystemService(LocaleManager::class.java).systemLocales
                if (!systemLocales.isEmpty) systemLocales[0] else Locale.getDefault()
            } else {
                val systemLocales = LocaleList.getDefault()
                if (!systemLocales.isEmpty) systemLocales[0] else Locale.getDefault()
            }
        }

        /** attachBaseContext 用：按存储语言覆写配置并返回新 Context */
        fun applyToConfiguration(language: String, base: Context): Context {
            val locale = resolveLocale(language, base)
            val config = Configuration(base.resources.configuration)
            config.setLocale(locale)
            return base.createConfigurationContext(config)
        }
    }
}
