package com.cornellappdev.android.pollo

import android.content.Context
import android.preference.PreferenceManager

class PreferencesHelper(context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var accessToken = preferences.getString(ACCESS_TOKEN, "")
        set(value) {
            preferences.edit().putString(ACCESS_TOKEN, value).apply()
            field = preferences.getString(ACCESS_TOKEN, "")
        }

    var refreshToken = preferences.getString(REFRESH_TOKEN, "")
        set(value) {
            preferences.edit().putString(REFRESH_TOKEN, value).apply()
            field = preferences.getString(REFRESH_TOKEN, "")
        }

    var expiresAt = preferences.getLong(EXPIRES_AT, 0L)
        set(value) {
            preferences.edit().putLong(EXPIRES_AT, value).apply()
            field = preferences.getLong(EXPIRES_AT, 0L)
        }

    var displayOnboarding = preferences.getBoolean(DISPLAY_ONBOARDING, true)
        set(value) {
            preferences.edit().putBoolean(DISPLAY_ONBOARDING, value).apply()
            field = preferences.getBoolean(DISPLAY_ONBOARDING, true)
        }

    companion object {
        private const val ACCESS_TOKEN = "data.source.prefs.ACCESS_TOKEN"
        private const val REFRESH_TOKEN = "data.source.prefs.REFRESH_TOKEN"
        private const val EXPIRES_AT = "data.source.prefs.EXPIRES_AT"
        private const val DISPLAY_ONBOARDING = "data.source.prefs.DISPLAY_ONBOARDING"
    }
}