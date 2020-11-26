package com.samples.ocr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_gallery_ocr.button_camera
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_camera.setOnClickListener {
            startActivity(Intent(this, OcrCaptureActivity::class.java))
        }

        button_gallery.setOnClickListener {
            startActivity(Intent(this, GalleryOCRActivity::class.java))
        }

        button_scan_qr.setOnClickListener {
            startActivity(Intent(this, ScanQRActivity::class.java))
        }
    }

}