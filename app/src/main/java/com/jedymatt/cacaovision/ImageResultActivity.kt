package com.jedymatt.cacaovision

import android.graphics.*
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.gms.vision.TfLiteVision
import org.tensorflow.lite.task.gms.vision.detector.Detection
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector

class ImageResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_result)

        val imgUri = intent.getParcelableExtra<Uri>("image_uri") ?: return


        val imagePreview = findViewById<ImageView>(R.id.imageView)
        imagePreview.setImageURI(imgUri)

        var bitmap = convertUriToBitmap(imgUri)

        runObjectDetection(bitmap)
    }


    private fun convertUriToBitmap(imgUri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    this.contentResolver, imgUri
                )
            ).copy(Bitmap.Config.ARGB_8888, true)
        } else {
            MediaStore.Images.Media.getBitmap(this.contentResolver, imgUri)
        }
    }

    private fun runObjectDetection(bitmap: Bitmap) {
        TfLiteGpu.isGpuDelegateAvailable(this).onSuccessTask { gpuAvailable: Boolean ->
            val optionsBuilder = TfLiteInitializationOptions.builder()
            if (gpuAvailable) {
                optionsBuilder.setEnableGpuDelegateSupport(true)
            }
            TfLiteVision.initialize(this, optionsBuilder.build())
        }.addOnSuccessListener {
            val image = TensorImage.fromBitmap(bitmap)

            val options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(3)
                .setScoreThreshold(0.5f).build()
            val detector = ObjectDetector.createFromFileAndOptions(
                this, "mobilenetv1.tflite", options
            )

            Log.i(TAG, detector.detect(image).joinToString(System.lineSeparator()))

            val resultImg = drawDetectionResult(bitmap, detector.detect(image))

            val imagePreview = findViewById<ImageView>(R.id.imageView)
            imagePreview.setImageBitmap(resultImg)
        }.addOnFailureListener {
            Log.e(TAG, "Error initializing TFLite", it)
        }

    }

    private fun drawDetectionResult(
        bitmap: Bitmap,
        detectionResults: List<Detection>
    ): Bitmap {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT

        detectionResults.forEach {
            // draw bounding box
            pen.color = Color.RED
            pen.strokeWidth = 8F
            pen.style = Paint.Style.STROKE
            val box = it.boundingBox
            canvas.drawRect(box, pen)


            val tagSize = Rect(0, 0, 0, 0)

            // calculate the right font size
            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW
            pen.strokeWidth = 2f

            pen.textSize = 96f

            val firstCategory = it.categories.first()
            val text = firstCategory.label + " " + String.format("%.2f", firstCategory.score * 100.0) + "%"
            pen.getTextBounds(text, 0, text.length, tagSize)
            val fontSize: Float = pen.textSize * box.width() / tagSize.width()

            // adjust the font size so texts are inside the bounding box
            if (fontSize < pen.textSize) pen.textSize = fontSize

            var margin = (box.width() - tagSize.width()) / 2.0F
            if (margin < 0F) margin = 0F
            canvas.drawText(
                text, box.left + margin,
                box.top + tagSize.height().times(1F), pen
            )
        }
        return outputBitmap
    }

    companion object {
        private const val TAG = "ImageResultActivity"
    }
}