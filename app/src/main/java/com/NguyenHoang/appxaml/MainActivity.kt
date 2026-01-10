package com.NguyenHoang.appxaml

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import java.lang.Exception

class MainActivity : BaseActivity() {

    private var isFlashOn = false
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private lateinit var btnFlash: MaterialButton
    private lateinit var txtStatus: TextView
    private lateinit var btnPremium: MaterialButton
    private lateinit var btnSettings: MaterialButton

    private lateinit var btnInfor: MaterialButton
    private lateinit var infoPanel: MaterialCardView
    private lateinit var btnCloseInfo: MaterialButton
    private lateinit var viewDim: View

    // Logic nháy đèn (Strobe)
    private var strobeHandler = Handler(Looper.getMainLooper())
    private var strobeRunnable: Runnable? = null
    private var currentMode = "Normal"
    private lateinit var toggleGroupModes: MaterialButtonToggleGroup

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
    private val KEY_IS_LIFETIME = "is_premium_lifetime"
    private val KEY_EXPIRY_TIME = "premium_expiry_time"
    private val FLASH_LIMIT_MS = 15000L // 15 giây

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo SharedPreferences
        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Khởi tạo views
        btnFlash = findViewById(R.id.btnFlash)
        txtStatus = findViewById(R.id.txtStatus)
        btnPremium = findViewById(R.id.btnPremium)
        btnInfor = findViewById(R.id.btnInfor)
        infoPanel = findViewById(R.id.infoPanel)
        btnCloseInfo = findViewById(R.id.btnCloseInfo)
        viewDim = findViewById(R.id.viewDim)
        btnSettings = findViewById(R.id.btnSettings)
        toggleGroupModes = findViewById(R.id.toggleGroupModes)

        // Khởi tạo Camera
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        if (cameraIds.isEmpty()) {
            txtStatus.text = "Lỗi: Không tìm thấy camera hỗ trợ đèn pin."
            btnFlash.isEnabled = false
            return
        }
        cameraId = cameraIds[0]

        // Hiển thị hộp thoại Premium khi ứng dụng bắt đầu
        if (!isPremium()) {
            showPremiumDialog()
        }

        // Đặt trạng thái ban đầu
        updateUiForInitialState()

        btnFlash.setOnClickListener {
            toggleFlashlight(!isFlashOn)
        }

        // Xử lý chọn chế độ nháy
        toggleGroupModes.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (!isPremium() && checkedId != R.id.btnModeNormal) {
                    // Nếu không phải Premium mà chọn chế độ nháy -> Hiện Dialog và reset về Normal
                    toggleGroupModes.check(R.id.btnModeNormal)
                    showPremiumDialog()
                } else {
                    currentMode = when (checkedId) {
                        R.id.btnModeSlow -> "Slow"
                        R.id.btnModeFast -> "Fast"
                        R.id.btnModeSOS -> "SOS"
                        R.id.btnModePolice -> "Police"
                        R.id.btnModePulse -> "Pulse"
                        R.id.btnModeDisco -> "Disco"
                        else -> "Normal"
                    }
                    // Nếu đèn đang bật, cập nhật ngay lập tức
                    if (isFlashOn) {
                        stopStrobe()
                        startStrobe(currentMode)
                    }
                }
            }
        }
        
        // Logic cho nút Premium trên trang chính
        btnPremium.setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }

        // Mở màn hình Settings
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Logic trượt panel thông tin
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

    private fun startStrobe(mode: String) {
        if (mode == "Normal") {
            try { cameraManager.setTorchMode(cameraId, true) } catch (e: Exception) {}
            return
        }

        var index = 0
        var flashState = true

        strobeRunnable = object : Runnable {
            override fun run() {
                try {
                    cameraManager.setTorchMode(cameraId, flashState)
                    
                    val delay = when (mode) {
                        "Slow" -> 500L
                        "Fast" -> 100L
                        "SOS" -> {
                            val sosPattern = longArrayOf(200, 200, 200, 200, 200, 600, 600, 600, 600, 600, 600, 200, 200, 200, 200, 200, 1000)
                            sosPattern[index % sosPattern.size]
                        }
                        "Police" -> {
                            // Nháy liên tục 3 lần rồi nghỉ ngắn
                            if (index % 8 < 6) 50L else 300L
                        }
                        "Pulse" -> {
                            // Nhịp tim (Thình thịch ... Thình thịch)
                            val pulsePattern = longArrayOf(100, 100, 100, 600)
                            pulsePattern[index % pulsePattern.size]
                        }
                        "Disco" -> {
                            // Nháy ngẫu nhiên hoặc hỗn hợp
                            (30..200).random().toLong()
                        }
                        else -> 100L
                    }
                    
                    flashState = !flashState
                    index++
                    strobeHandler.postDelayed(this, delay)
                } catch (e: Exception) {
                    stopStrobe()
                }
            }
        }
        strobeHandler.post(strobeRunnable!!)
    }

    private fun stopStrobe() {
        strobeRunnable?.let { strobeHandler.removeCallbacks(it) }
        strobeRunnable = null
        try { cameraManager.setTorchMode(cameraId, false) } catch (e: Exception) {}
    }

    private fun showInfoPanel() {
        viewDim.visibility = View.VISIBLE
        viewDim.animate().alpha(1f).setDuration(400).start()
        infoPanel.animate().translationX(0f).setDuration(500).setInterpolator(OvershootInterpolator(0.7f)).start()
    }

    private fun hideInfoPanel() {
        viewDim.animate().alpha(0f).setDuration(300).withEndAction { viewDim.visibility = View.GONE }.start()
        infoPanel.animate().translationX(-infoPanel.width.toFloat()).setDuration(400).setInterpolator(null).start()
    }

    private fun updateUiForInitialState() {
        isFlashOn = false
        btnFlash.text = "BẬT"
        btnFlash.setIconResource(R.drawable.ic_flash_off)
        toggleGroupModes.check(R.id.btnModeNormal)
        
        if (isPremium()) {
            txtStatus.text = "Premium đã được kích hoạt"
        } else {
            txtStatus.text = "Đèn pin đang tắt"
        }
    }

    private fun isPremium(): Boolean {
        if (sharedPrefs.getBoolean(KEY_IS_LIFETIME, false)) return true
        val expiryTime = sharedPrefs.getLong(KEY_EXPIRY_TIME, 0L)
        return System.currentTimeMillis() < expiryTime
    }

    private fun toggleFlashlight(on: Boolean) {
        if (on == isFlashOn) return
        isFlashOn = on
        
        if (isFlashOn) {
            btnFlash.text = "TẮT"
            btnFlash.setIconResource(R.drawable.ic_flash_on)
            txtStatus.text = "ĐÈN PIN ĐANG BẬT"
            
            // Bắt đầu nháy theo chế độ đã chọn
            startStrobe(currentMode)

            if (!isPremium()) {
                handler.postDelayed(limitFlashlightRunnable, FLASH_LIMIT_MS)
            }
        } else {
            handler.removeCallbacks(limitFlashlightRunnable)
            stopStrobe()
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
            .setMessage("Mở khóa Đèn Pin không giới hạn và các chế độ nháy (SOS, Fast, Slow, Police, Disco...)!\n\nChọn gói:\n\n- Thuê theo Ngày: 18.000 VNĐ\n- Thuê theo Tháng: 36.000 VNĐ")
            .setCancelable(true)
            .setPositiveButton("Thuê 1 Ngày") { dialog, _ ->
                val oneDayMs = 24 * 60 * 60 * 1000L
                sharedPrefs.edit().putLong(KEY_EXPIRY_TIME, System.currentTimeMillis() + oneDayMs).apply()
                handler.removeCallbacks(limitFlashlightRunnable) 
                txtStatus.text = "Đã mua Premium 1 ngày!"
                dialog.dismiss()
            }
            .setNegativeButton("Thuê 1 Tháng") { dialog, _ ->
                val oneMonthMs = 30L * 24 * 60 * 60 * 1000L
                sharedPrefs.edit().putLong(KEY_EXPIRY_TIME, System.currentTimeMillis() + oneMonthMs).apply()
                handler.removeCallbacks(limitFlashlightRunnable)
                txtStatus.text = "Đã mua Premium 1 tháng!"
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
            toggleFlashlight(false)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::btnFlash.isInitialized) updateUiForInitialState()
        if (!isPremium()) showPremiumDialog()
    }
}