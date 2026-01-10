package com.NguyenHoang.appxaml

import android.app.Application
import com.google.android.material.color.DynamicColors

class AppXaml : Application() {
    override fun onCreate() {
        super.onCreate()
        // Kiểm tra cài đặt trong SharedPreferences để áp dụng Dynamic Color
        val sharedPrefs = getSharedPreferences("FlashlightPrefs", MODE_PRIVATE)
        val useDynamicColor = sharedPrefs.getBoolean("use_dynamic_color", true)
        
        if (useDynamicColor) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}