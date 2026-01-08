package com.NguyenHoang.appxaml

import android.hardware.camera2.CameraManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var isFlashOn = false
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnFlash = findViewById<MaterialButton>(R.id.btnFlash)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]

        btnFlash.setOnClickListener {
            isFlashOn = !isFlashOn
            cameraManager.setTorchMode(cameraId, isFlashOn)

            if (isFlashOn) {
                btnFlash.text = "TẮT"
                btnFlash.setIconResource(R.drawable.ic_flash_on)
                txtStatus.text = "ĐÈN PIN ĐANG BẬT"
            } else {
                btnFlash.text = "BẬT"
                btnFlash.setIconResource(R.drawable.ic_flash_off)
                txtStatus.text = "ĐÈN PIN ĐANG TẮT"
            }
        }
    }
}
