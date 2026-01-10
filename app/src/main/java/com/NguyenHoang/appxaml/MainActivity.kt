package com.NguyenHoang.appxaml

import android.content.Context
import android.content.SharedPreferences
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.lang.Exception 
import android.content.Intent
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private var isFlashOn = false
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private lateinit var btnFlash: MaterialButton
    private lateinit var txtStatus: TextView
    private lateinit var btnPremium: MaterialButton

    private lateinit var btnInfor: MaterialButton
    private lateinit var infoPanel: MaterialCardView
    private lateinit var btnCloseInfo: MaterialButton
    private lateinit var viewDim: View


    // Các trường và hằng số cho logic Premium
    private lateinit var sharedPrefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    
    // Runnable để tự động tắt đèn pin sau 15 giây
    private val limitFlashlightRunnable = Runnable {
        if (isFlashOn) {
            // Tự động tắt đèn pin
            toggleFlashlight(false)
            txtStatus.text = "Hết 15 giây. Mua Premium để sử dụng không giới hạn!"
            showPremiumDialog()
        }
    }

    private val PREFS_NAME = "FlashlightPrefs"
    private val KEY_IS_PREMIUM = "is_premium"
    private val FLASH_LIMIT_MS = 15000L // 15 giây

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo views
        btnFlash = findViewById(R.id.btnFlash)
        txtStatus = findViewById(R.id.txtStatus)
        btnPremium = findViewById(R.id.btnPremium)
        btnInfor = findViewById(R.id.btnInfor)
        infoPanel = findViewById(R.id.infoPanel)
        btnCloseInfo = findViewById(R.id.btnCloseInfo)
        viewDim = findViewById(R.id.viewDim)

        // Khởi tạo Camera
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        if (cameraIds.isEmpty()) {
            txtStatus.text = "Lỗi: Không tìm thấy camera hỗ trợ đèn pin."
            btnFlash.isEnabled = false
            return
        }
        cameraId = cameraIds[0]

        // Khởi tạo SharedPreferences
        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Hiển thị hộp thoại Premium khi ứng dụng bắt đầu
        if (!isPremium()) {
            showPremiumDialog()
        }

        // Đặt trạng thái ban đầu
        updateUiForInitialState()

        btnFlash.setOnClickListener {
            toggleFlashlight(!isFlashOn)
        }
        
        // Logic cho nút Premium trên trang chính
        btnPremium.setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }

        // Logic trượt panel thông tin nâng cao (Material 3 Style)
        btnInfor.setOnClickListener {
            showInfoPanel()
        }

        btnCloseInfo.setOnClickListener {
            hideInfoPanel()
        }
        
        viewDim.setOnClickListener {
            hideInfoPanel()
        }
    }

    private fun showInfoPanel() {
        viewDim.visibility = View.VISIBLE
        viewDim.animate()
            .alpha(1f)
            .setDuration(400)
            .start()

        infoPanel.animate()
            .translationX(0f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator(0.7f)) // Tạo độ nhún (bounce)
            .start()
    }

    private fun hideInfoPanel() {
        viewDim.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction { viewDim.visibility = View.GONE }
            .start()

        infoPanel.animate()
            .translationX(-infoPanel.width.toFloat())
            .setDuration(400)
            .setInterpolator(null)
            .start()
    }

    private fun updateUiForInitialState() {
        isFlashOn = false
        btnFlash.text = "BẬT"
        btnFlash.setIconResource(R.drawable.ic_flash_off)
        
        if (isPremium()) {
            txtStatus.text = "Premium đã được kích hoạt"
        } else {
            txtStatus.text = "Đèn pin đang tắt"
        }
    }

    private fun isPremium(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_PREMIUM, false)
    }

    private fun toggleFlashlight(on: Boolean) {
        if (on == isFlashOn) return

        isFlashOn = on
        try {
            cameraManager.setTorchMode(cameraId, isFlashOn)
        } catch (e: Exception) {
            isFlashOn = !on
            txtStatus.text = "Lỗi: Không thể điều khiển đèn pin: ${e.message}"
            return
        }

        if (isFlashOn) {
            btnFlash.text = "TẮT"
            btnFlash.setIconResource(R.drawable.ic_flash_on)
            txtStatus.text = "ĐÈN PIN ĐANG BẬT"

            if (!isPremium()) {
                handler.postDelayed(limitFlashlightRunnable, FLASH_LIMIT_MS)
            }
        } else {
            handler.removeCallbacks(limitFlashlightRunnable)

            btnFlash.text = "BẬT"
            btnFlash.setIconResource(R.drawable.ic_flash_off)
            txtStatus.text = "Đèn pin đang tắt"
        }
    }

    private fun showPremiumDialog() {
        if (isFinishing || isDestroyed) return
        if (isPremium()) return

        AlertDialog.Builder(this)
            .setTitle("Nâng cấp Premium")
            .setMessage("Mở khóa Đèn Pin không giới hạn! Bạn hiện chỉ có thể dùng 15 giây mỗi lần mở ứng dụng.\n\nChọn gói:\n\n- Thuê theo Ngày: 15.000 VNĐ / ngày\n- Thuê theo Tháng: 99.000 VNĐ / tháng (Tiết kiệm)")
            .setCancelable(true)
            .setPositiveButton("Thuê 1 Tiếng") { dialog, _ ->
                sharedPrefs.edit().putBoolean(KEY_IS_PREMIUM, true).apply()
                handler.removeCallbacks(limitFlashlightRunnable) 
                txtStatus.text = "Đã mua Premium! Sử dụng trong 1 tiếng."
                dialog.dismiss()
            }
            .setNegativeButton("Thuê 1 Ngày") { dialog, _ ->
                sharedPrefs.edit().putBoolean(KEY_IS_PREMIUM, true).apply()
                handler.removeCallbacks(limitFlashlightRunnable)
                txtStatus.text = "Đã mua Premium! Sử dụng trong 1 ngày."
                dialog.dismiss()
            }
            .setNeutralButton("Dùng miễn phí (15s)") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(limitFlashlightRunnable)
        if (isFlashOn) {
            try {
                 cameraManager.setTorchMode(cameraId, false)
            } catch (e: Exception) {
            }
            isFlashOn = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (::btnFlash.isInitialized) {
            updateUiForInitialState()
        }
        if (!isPremium()) {
             showPremiumDialog()
        }
    }
}