package com.NguyenHoang.appxaml

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
    }
    
    // Hàm để cập nhật theme ngay lập tức bằng cách recreate activity
    fun updateTheme() {
        recreate()
    }
}