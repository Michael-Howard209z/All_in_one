package com.NguyenHoang.appxaml

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class PremiumActivity : AppCompatActivity() {

    private val PREFS_NAME = "FlashlightPrefs"
    private val KEY_IS_LIFETIME = "is_premium_lifetime"
    private val KEY_EXPIRY_TIME = "premium_expiry_time"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page1)
        
        supportActionBar?.title = "Nâng cấp Premium"

        setupPurchaseButtons()
    }

    private fun setupPurchaseButtons() {
        val btnBuyHour: MaterialButton = findViewById(R.id.btn_buy_hour)
        val btnBuyDay: MaterialButton = findViewById(R.id.btn_buy_day)
        val btnBuyMonth: MaterialButton = findViewById(R.id.btn_buy_month)
        val btnBuyCustom: MaterialButton = findViewById(R.id.btn_buy_custom)
        val btnBuyLifetime: MaterialButton = findViewById(R.id.btn_buy_lifetime)
        val inputCustomDays: TextInputEditText = findViewById(R.id.input_custom_days)

        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        btnBuyHour.setOnClickListener {
            // 1 Giờ = 60 * 60 * 1000 ms
            val expiry = System.currentTimeMillis() + (1 * 60 * 60 * 1000L)
            sharedPrefs.edit().putLong(KEY_EXPIRY_TIME, expiry).apply()
            Toast.makeText(this, "Đã mua gói 1 giờ!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnBuyDay.setOnClickListener {
            // 1 Ngày = 24 * 60 * 60 * 1000 ms
            val expiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
            sharedPrefs.edit().putLong(KEY_EXPIRY_TIME, expiry).apply()
            Toast.makeText(this, "Đã mua gói 1 ngày!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnBuyMonth.setOnClickListener {
            // 1 Tháng = 30 ngày
            val expiry = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L)
            sharedPrefs.edit().putLong(KEY_EXPIRY_TIME, expiry).apply()
            Toast.makeText(this, "Đã mua gói 1 tháng!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnBuyCustom.setOnClickListener {
            val daysStr = inputCustomDays.text.toString()
            if (daysStr.isNotEmpty()) {
                val days = daysStr.toLong()
                if (days > 0) {
                    val expiry = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L)
                    sharedPrefs.edit().putLong(KEY_EXPIRY_TIME, expiry).apply()
                    Toast.makeText(this, "Đã thuê $days ngày!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Vui lòng nhập số ngày hợp lệ", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập số ngày", Toast.LENGTH_SHORT).show()
            }
        }

        btnBuyLifetime.setOnClickListener {
            sharedPrefs.edit().putBoolean(KEY_IS_LIFETIME, true).apply()
            Toast.makeText(this, "Đã mua gói Vĩnh Viễn!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}