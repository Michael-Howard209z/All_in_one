package com.NguyenHoang.appxaml

import android.app.Activity
import android.content.Context
import com.google.android.material.color.DynamicColors

object ThemeUtils {
    private val PREFS_NAME = "FlashlightPrefs"
    private val KEY_DYNAMIC_COLOR = "use_dynamic_color"
    private val KEY_THEME_COLOR = "theme_color"

    fun applyTheme(activity: Activity) {
        val sharedPrefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        val useDynamicColor = sharedPrefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        if (useDynamicColor && DynamicColors.isDynamicColorAvailable()) {
            // Dynamic color is handled by the Application class usually, 
            // but we can force it here if needed or let it be.
            // However, to change immediately without restart, we need to apply a base theme first.
            activity.setTheme(R.style.Theme_AppXamL)
        } else {
            val themeId = sharedPrefs.getInt(KEY_THEME_COLOR, R.id.radioPurple)
            when (themeId) {
                R.id.radioPurple -> activity.setTheme(R.style.Theme_AppXamL_Purple)
                R.id.radioBlue -> activity.setTheme(R.style.Theme_AppXamL_Blue)
                R.id.radioGreen -> activity.setTheme(R.style.Theme_AppXamL_Green)
                R.id.radioOrange -> activity.setTheme(R.style.Theme_AppXamL_Orange)
                R.id.radioRed -> activity.setTheme(R.style.Theme_AppXamL_Red)
                else -> activity.setTheme(R.style.Theme_AppXamL_Purple)
            }
        }
    }
}