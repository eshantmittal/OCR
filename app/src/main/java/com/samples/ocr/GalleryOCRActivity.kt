package com.samples.ocr

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.model.Image
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.activity_gallery_ocr.*
import java.io.File
import java.io.FileInputStream


class GalleryOCRActivity : AppCompatActivity() {

    private val images = arrayListOf<Image>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_ocr)

        button_pick_image.setOnClickListener { imagePicker.single().start() }
    }

    private val imagePicker: ImagePicker
        get() {
            val returnAfterCapture = ef_switch_return_after_capture.isChecked
            val isSingleMode = ef_switch_single.isChecked
            //val useCustomImageLoader = ef_switch_imageloader.isChecked
            val folderMode = ef_switch_folder_mode.isChecked
            //val includeVideo = ef_switch_include_video.isChecked
            //val onlyVideo = ef_switch_only_video.isChecked
            val isExclude = ef_switch_include_exclude.isChecked
            val imagePicker = ImagePicker.create(this)
                //.language("in") // Set image picker language
                .theme(R.style.NoActionBar)
                .single()
                .returnMode(if (returnAfterCapture) ReturnMode.ALL else ReturnMode.NONE) // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                .folderMode(folderMode) // set folder mode (false by default)
                //.includeVideo(includeVideo) // include video (false by default)
                //.onlyVideo(onlyVideo) // include video (false by default)
                .toolbarArrowColor(Color.RED) // set toolbar arrow up color
                .toolbarFolderTitle("Folder") // folder selection title
                .toolbarImageTitle("Tap to select") // image selection title
                .toolbarDoneButtonText("DONE") // done button text

/*
            ImagePickerComponentHolder.getInstance().imageLoader = if (useCustomImageLoader) {
                GrayscaleImageLoader()
            } else {
                DefaultImageLoader()
            }
*/

            if (isSingleMode) {
                imagePicker.single()
            } else {
                imagePicker.multi() // multi mode (default mode)
            }
            if (isExclude) {
                imagePicker.exclude(images) // don't show anything on this selected images
            } else {
                imagePicker.origin(images) // original selected images, used in multi mode
            }
            return imagePicker.limit(10) // max images can be selected (99 by default)
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera") // captured image directory name ("Camera" folder by default)
                .imageFullDirectory(Environment.getExternalStorageDirectory().path) // can be full path
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            var images = ImagePicker.getImages(data)
            // or get a single image only
            var image = ImagePicker.getFirstImageOrNull(data)
            Log.d("path", image.path)
            readTextFromImage(image)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    /*
    * Function which takes an image for OCR and checks for PAN or Aadhaar in text detected from image
    */
    private fun readTextFromImage(image: Image) {
        var bitmap: Bitmap? = null
        val sdcardPath =
            Environment.getExternalStorageDirectory().toString()
        val imgFile = File(image.path)
        if (imgFile.exists()) {
            bitmap = BitmapFactory.decodeFile(imgFile.path)
        }

        // Starting Text Recognizer
        val txtRecognizer = TextRecognizer.Builder(applicationContext).build()
        if (!txtRecognizer.isOperational) {
            // Shows if your Google Play services is not up to date or OCR is not supported for the device
            text_view.text = "Detector dependencies are not yet available"
        } else {
            // Set the bitmap taken to the frame to perform OCR Operations.
            val frame: Frame = Frame.Builder().setBitmap(bitmap).build()
            val items: SparseArray<*> = txtRecognizer.detect(frame)
            val strBuilder = StringBuilder()


            //*Adarsh - Items are text which is detected from the frame. Once items are received from the frame
            // check for PAN or Aadhaar using regex
            for (i in 0 until items.size()) {
                val item: TextBlock? = items.valueAt(i) as TextBlock
                if (item != null && item.value != null) {
                    strBuilder.append(item.value)
                    strBuilder.append("\n")
                    if (RegexChecker().checkForRegex(item.value, RegexChecker.PAN_NUMBER)!!) {
                        Toast.makeText(this, "Pan detected" + item.value, Toast.LENGTH_LONG).show()
                    }
                    if (RegexChecker().checkForRegex(item.value, RegexChecker.AADHAR_NUMBER_PATTERN)!!) {
                        Toast.makeText(this, "Aadhaar detected" + item.value, Toast.LENGTH_LONG).show()
                    }
                }
            }
            text_view.text = strBuilder.toString()
        }
    }

    private fun getBitmapFromImage(path: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val f = File(path)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
            Log.d("bitmap", bitmap?.byteCount.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }
}