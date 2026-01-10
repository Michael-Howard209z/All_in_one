package com.NguyenHoang.appxaml

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PremiumActivity : AppCompatActivity() {

    private val PREFS_NAME = "FlashlightPrefs"
    private val KEY_IS_PREMIUM = "is_premium"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // page1.xml is the layout for the premium purchase screen
        setContentView(R.layout.page1)
        
        // Optionally, set the title
        supportActionBar?.title = "Nâng cấp Premium"

        setupPurchaseButtons()
    }

    private fun setupPurchaseButtons() {
        val btnBuyHour: MaterialButton = findViewById(R.id.btn_buy_hour)
        val btnBuyDay: MaterialButton = findViewById(R.id.btn_buy_day)
        val btnBuyMonth: MaterialButton = findViewById(R.id.btn_buy_month)
        val btnBuyLifetime: MaterialButton = findViewById(R.id.btn_buy_lifetime)

        // Purchase logic (Simulated)
        val purchaseListener: (String) -> Unit = { packageName ->
            // MÔ PHỎNG: Cập nhật trạng thái Premium
            val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean(KEY_IS_PREMIUM, true).apply()
            
            Toast.makeText(this, "Đã mua $packageName thành công!", Toast.LENGTH_SHORT).show()
            
            // Kết thúc PremiumActivity và quay lại MainActivity để MainActivity.onResume() cập nhật trạng thái
            finish()
        }

        btnBuyHour.setOnClickListener { 
            purchaseListener("Gói Thử Nghiệm (1 Giờ)") 
        }

        btnBuyDay.setOnClickListener { 
            purchaseListener("Gói Cơ Bản (1 Ngày)") 
        }
        
        btnBuyMonth.setOnClickListener { 
            purchaseListener("Gói Tiết Kiệm (1 Tháng)") 
        }

        btnBuyLifetime.setOnClickListener { 
            purchaseListener("Gói Cao Cấp (Vĩnh Viễn)") 
        }
    }
}