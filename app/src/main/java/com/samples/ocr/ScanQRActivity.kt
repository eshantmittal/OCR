package com.samples.ocr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_scan_qr.*
import java.io.IOException
import java.util.regex.Pattern



class ScanQRActivity : AppCompatActivity() {

    private lateinit var cameraView: SurfaceView

    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr)

        cameraView = findViewById(R.id.cameraView)

        iv_close.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onStart() {
        super.onStart()
        startScanning()
    }

    private fun startScanning() {
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(displayMetrics.heightPixels, displayMetrics.widthPixels)
            .setAutoFocusEnabled(true)
            .build()

        cameraView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                checkForPermissionsCamera()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode?> {
            override fun release() {}
            override fun receiveDetections(detections: Detections<Barcode?>) {
                val barcodes: SparseArray<Barcode?>? = detections.detectedItems

                if (barcodes?.size() != 0) {
                    val scannedValue = barcodes?.valueAt(0)?.displayValue ?: return
                    Log.d("qr", scannedValue)
                    checkForAadhaar(scannedValue)
                }
            }
        })
    }

    private fun checkForPermissionsCamera() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {

                override fun onPermissionGranted(response: PermissionGrantedResponse) { /* ... */
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                this@ScanQRActivity,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }
                        cameraSource.start(cameraView.holder)
                    } catch (ie: IOException) {
                        ie.message?.let { Log.e("CAMERA SOURCE", it) }
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) { /* ... */
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }
            }).check()
    }

    /*
     Function to check Aadhaar number in the scanned qr code.
     Check for XML,UID to validate if this is a aadhaar xml. After reading xml check for 12 digit
     number which would be sent as result to calling activity with extra 'uid'
    */
    private fun checkForAadhaar(scannedValue: String) {

        val hasAadhaarInfo = scannedValue.contains("xml", true) ||
                scannedValue.contains("uid", true) ||
                scannedValue.contains("PrintLetterBarcodeData", true)
        if (!hasAadhaarInfo) {
            runOnUiThread(Runnable {
                Toast.makeText(this, "Please scan a valid Aadhaar document", Toast.LENGTH_LONG)
                    .show()
            })
        }

        val uid = extractDigits(scannedValue)

        if (TextUtils.isEmpty(uid) || uid.length != 12) {
            runOnUiThread(Runnable {
                Toast.makeText(this, "Aadhaar UID not found", Toast.LENGTH_LONG)
                    .show()
            })
        }

        runOnUiThread(Runnable {
            Toast.makeText(this@ScanQRActivity, uid, Toast.LENGTH_SHORT).show()
        })

        val intent = Intent()
        intent.putExtra("UID", uid)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        cameraSource.stop()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    fun extractDigits(`in`: String?): String {
        val p = Pattern.compile("(\\d{12,12})")
        val m = p.matcher(`in`)
        return if (m.find()) {
            m.group(0)
        } else ""
    }
}