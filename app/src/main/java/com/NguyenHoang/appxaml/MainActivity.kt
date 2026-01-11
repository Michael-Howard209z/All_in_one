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
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import java.lang.Exception
import java.util.*

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

    private lateinit var txtUsageCount: TextView
    private lateinit var txtTotalTime: TextView
    private lateinit var txtRating: TextView

    // Logic nháy đèn (Strobe)
    private var strobeHandler = Handler(Looper.getMainLooper())
    private var strobeRunnable: Runnable? = null
    private var currentMode = "Normal"
    private lateinit var toggleGroupModes: MaterialButtonToggleGroup

    // Thống kê
    private var flashStartTime = 0L

    // Các trường và hằng số cho logic Premium
    private lateinit var sharedPrefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    
    private val limitFlashlightRunnable = Runnable {
        if (isFlashOn) {
            toggleFlashlight(false)
            txtStatus.text = "Hết 15 giây. Mua Premium để sử dụng không giới hạn!"
            showPremiumDialog()
        }
    }

    private val PREFS_NAME = "FlashlightPrefs"
    private val KEY_IS_LIFETIME = "is_premium_lifetime"
    private val KEY_EXPIRY_TIME = "premium_expiry_time"
    private val KEY_FIRST_OPEN = "first_open"
    private val KEY_USAGE_COUNT = "usage_count"
    private val KEY_TOTAL_TIME = "total_time_ms"
    private val FLASH_LIMIT_MS = 15000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        btnFlash = findViewById(R.id.btnFlash)
        txtStatus = findViewById(R.id.txtStatus)
        btnPremium = findViewById(R.id.btnPremium)
        btnInfor = findViewById(R.id.btnInfor)
        infoPanel = findViewById(R.id.infoPanel)
        btnCloseInfo = findViewById(R.id.btnCloseInfo)
        viewDim = findViewById(R.id.viewDim)
        btnSettings = findViewById(R.id.btnSettings)
        toggleGroupModes = findViewById(R.id.toggleGroupModes)

        txtUsageCount = findViewById(R.id.txtUsageCount)
        txtTotalTime = findViewById(R.id.txtTotalTime)
        txtRating = findViewById(R.id.txtRating)

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        if (cameraIds.isEmpty()) {
            txtStatus.text = "Lỗi: Không tìm thấy camera hỗ trợ đèn pin."
            btnFlash.isEnabled = false
            return
        }
        cameraId = cameraIds[0]

        if (sharedPrefs.getBoolean(KEY_FIRST_OPEN, true)) {
            showTermsDialog()
        } else if (!isPremium()) {
            showPremiumDialog()
        }

        updateUiForInitialState()
        updateStatistics()

        btnFlash.setOnClickListener {
            toggleFlashlight(!isFlashOn)
        }

        toggleGroupModes.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (!isPremium() && checkedId != R.id.btnModeNormal) {
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
                        R.id.btnModeCandle -> "Candle"
                        R.id.btnModeSuperFlash -> "SuperFlash"
                        else -> "Normal"
                    }
                    if (isFlashOn) {
                        stopStrobe()
                        startStrobe(currentMode)
                    }
                }
            }
        }
        
        btnPremium.setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnInfor.setOnClickListener {
            updateStatistics()
            showInfoPanel()
        }

        btnCloseInfo.setOnClickListener {
            hideInfoPanel()
        }
        
        viewDim.setOnClickListener {
            hideInfoPanel()
        }
    }

    private fun showTermsDialog() {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_multiple_choice, null)
        val builder = AlertDialog.Builder(this)
            .setTitle("Điều khoản sử dụng")
            .setMessage("App này chỉ có nhiệm vụ bật đèn.\nNếu bạn kỳ vọng nhiều hơn, lỗi ở bạn.\n\nBạn có chấp nhận không?")
            .setCancelable(false)
        
        val checkBox = CheckBox(this)
        checkBox.text = "Tôi chấp nhận sự thật phũ phàng"
        builder.setView(checkBox)

        builder.setPositiveButton("Vào app") { dialog, _ ->
            if (checkBox.isChecked) {
                sharedPrefs.edit().putBoolean(KEY_FIRST_OPEN, false).apply()
                dialog.dismiss()
                if (!isPremium()) showPremiumDialog()
            } else {
                Toast.makeText(this, "Bạn phải chấp nhận sự thật để tiếp tục!", Toast.LENGTH_SHORT).show()
                showTermsDialog()
            }
        }
        builder.setNegativeButton("Thoát") { _, _ -> finish() }
        builder.show()
    }

    private fun updateStatistics() {
        val count = sharedPrefs.getInt(KEY_USAGE_COUNT, 0)
        val totalMs = sharedPrefs.getLong(KEY_TOTAL_TIME, 0L)
        val totalSeconds = totalMs / 1000
        
        txtUsageCount.text = "Số lần bật đèn: $count"
        txtTotalTime.text = "Thời gian soi đèn: $totalSeconds giây cuộc đời"
        
        if (totalSeconds > 10) {
            txtRating.text = "Đánh giá: Bạn quá rảnh"
        } else {
            txtRating.text = "Đánh giá: Người dùng bình thường"
        }
    }

    private fun startStrobe(mode: String) {
        if (mode == "Normal") {
            try { cameraManager.setTorchMode(cameraId, true) } catch (e: Exception) {}
            txtStatus.text = "ĐÈN PIN ĐANG BẬT"
            return
        }

        var index = 0
        var flashState = true
        if (mode == "Candle") txtStatus.text = "Thắp sáng niềm tin"

        strobeRunnable = object : Runnable {
            override fun run() {
                try {
                    cameraManager.setTorchMode(cameraId, flashState)
                    val delay = when (mode) {
                        "Slow" -> 500L
                        "Fast" -> 100L
                        "SOS" -> longArrayOf(200, 200, 200, 200, 200, 600, 600, 600, 600, 600, 600, 200, 200, 200, 200, 200, 1000)[index % 17]
                        "Police" -> if (index % 8 < 6) 50L else 300L
                        "Pulse" -> longArrayOf(100, 100, 100, 600)[index % 4]
                        "Disco" -> (30..200).random().toLong()
                        "Candle" -> (50..400).random().toLong() // Nháy nhẹ như nến
                        "SuperFlash" -> longArrayOf(35, 35, 30, 30)[index % 4]
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
        if (isPremium()) txtStatus.text = "Premium đã kích hoạt" else txtStatus.text = "Đèn pin đang tắt"
    }

    private fun isPremium(): Boolean {
        if (sharedPrefs.getBoolean(KEY_IS_LIFETIME, false)) return true
        return System.currentTimeMillis() < sharedPrefs.getLong(KEY_EXPIRY_TIME, 0L)
    }

    private fun toggleFlashlight(on: Boolean) {
        if (on == isFlashOn) return
        isFlashOn = on
        
        if (isFlashOn) {
            flashStartTime = System.currentTimeMillis()
            val count = sharedPrefs.getInt(KEY_USAGE_COUNT, 0)
            sharedPrefs.edit().putInt(KEY_USAGE_COUNT, count + 1).apply()

            btnFlash.text = "TẮT"
            btnFlash.setIconResource(R.drawable.ic_flash_on)
            txtStatus.text = "ĐÈN PIN ĐANG BẬT"
            startStrobe(currentMode)
            if (!isPremium()) handler.postDelayed(limitFlashlightRunnable, FLASH_LIMIT_MS)
        } else {
            val duration = System.currentTimeMillis() - flashStartTime
            val totalTime = sharedPrefs.getLong(KEY_TOTAL_TIME, 0L)
            sharedPrefs.edit().putLong(KEY_TOTAL_TIME, totalTime + duration).apply()

            handler.removeCallbacks(limitFlashlightRunnable)
            stopStrobe()
            btnFlash.text = "BẬT"
            btnFlash.setIconResource(R.drawable.ic_flash_off)
            txtStatus.text = "Đèn pin đang tắt"
        }
    }

    private fun showPremiumDialog() {
        if (isFinishing || isDestroyed || isPremium()) return
        AlertDialog.Builder(this)
            .setTitle("Nâng cấp Premium")
            .setMessage("Mở khóa Đèn Pin không giới hạn và các chế độ nháy (SOS, Nến, Disco...)!\n\nChọn gói:\n- Thuê theo Ngày: 18.000 VNĐ\n- Thuê theo Tháng: 36.000 VNĐ")
            .setCancelable(true)
            .setPositiveButton("Thuê 1 Ngày") { _, _ ->
                sharedPrefs.edit().putLong(KEY_EXPIRY_TIME, System.currentTimeMillis() + 24*3600*1000L).apply()
                txtStatus.text = "Đã mua Premium 1 ngày!"
            }
            .setNegativeButton("Thuê 1 Tháng") { _, _ ->
                sharedPrefs.edit().putLong(KEY_EXPIRY_TIME, System.currentTimeMillis() + 30L*24*3600*1000L).apply()
                txtStatus.text = "Đã mua Premium 1 tháng!"
            }
            .setNeutralButton("Dùng miễn phí (15s)") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onPause() {
        super.onPause()
        if (isFlashOn) toggleFlashlight(false)
    }

    override fun onResume() {
        super.onResume()
        if (::btnFlash.isInitialized) updateUiForInitialState()
        if (!isPremium() && !sharedPrefs.getBoolean(KEY_FIRST_OPEN, true)) showPremiumDialog()
    }
}