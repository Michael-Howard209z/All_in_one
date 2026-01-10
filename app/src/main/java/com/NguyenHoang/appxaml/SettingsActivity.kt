package com.NguyenHoang.appxaml

import android.content.Context
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsActivity : AppCompatActivity() {

    private val PREFS_NAME = "FlashlightPrefs"
    private val KEY_DYNAMIC_COLOR = "use_dynamic_color"
    private val KEY_THEME_COLOR = "theme_color"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val switchDynamicColor: MaterialSwitch = findViewById(R.id.switchDynamicColor)
        val radioGroupColors: RadioGroup = findViewById(R.id.radioGroupColors)

        // Load settings
        switchDynamicColor.isChecked = sharedPrefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val savedColorId = sharedPrefs.getInt(KEY_THEME_COLOR, R.id.radioPurple)
        radioGroupColors.check(savedColorId)

        switchDynamicColor.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(KEY_DYNAMIC_COLOR, isChecked).apply()
            showRestartToast()
        }

        radioGroupColors.setOnCheckedChangeListener { _, checkedId ->
            // If user selects a specific color, disable dynamic color for better UX consistency
            if (switchDynamicColor.isChecked) {
                switchDynamicColor.isChecked = false
                sharedPrefs.edit().putBoolean(KEY_DYNAMIC_COLOR, false).apply()
            }
            
            sharedPrefs.edit().putInt(KEY_THEME_COLOR, checkedId).apply()
            showRestartToast()
        }
    }

    private fun showRestartToast() {
        Toast.makeText(this, "Vui lòng khởi động lại app để áp dụng giao diện mới!", Toast.LENGTH_SHORT).show()
    }
}